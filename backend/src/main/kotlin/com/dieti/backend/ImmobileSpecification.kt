package com.dieti.backend.repository

import com.dieti.backend.dto.ImmobileSearchFilters
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.entity.AmbienteEntity
import com.dieti.backend.entity.ImmobileEntity
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

class ImmobileSpecification(
    private val filters: ImmobileSearchFilters
) : Specification<ImmobileEntity> {

    override fun toPredicate(
        root: Root<ImmobileEntity>,
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder
    ): Predicate? {
        val predicates = mutableListOf<Predicate>()

        // --- FILTRO FONDAMENTALE: Solo immobili gestiti da un agente ---
        // Se agente è NULL, significa che è in attesa di approvazione
        predicates.add(cb.isNotNull(root.get<AgenteEntity>("agente")))

        // 1. Filtro Tipo Vendita
        filters.tipoVendita?.let {
            predicates.add(cb.equal(root.get<Boolean>("tipoVendita"), it))
        }

        // 2. Filtro Prezzo
        filters.minPrezzo?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("prezzo"), it))
        }
        filters.maxPrezzo?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("prezzo"), it))
        }

        // 3. Filtro Superficie (MQ)
        filters.minMq?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("mq"), it))
        }
        filters.maxMq?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("mq"), it))
        }

        // 4. Filtro Condizione/Stato
        if (!filters.condizione.isNullOrBlank()) {
            predicates.add(cb.equal(root.get<String>("statoProprieta"), filters.condizione))
        }

        // 5. Filtro Stanze
        if (filters.minStanze != null || filters.maxStanze != null) {
            val subquery = query.subquery(Long::class.java)
            val ambienteRoot = subquery.from(AmbienteEntity::class.java)
            subquery.select(cb.count(ambienteRoot))
            subquery.where(cb.equal(ambienteRoot.get<ImmobileEntity>("immobile"), root))

            filters.minStanze?.let {
                predicates.add(cb.greaterThanOrEqualTo(subquery, it.toLong()))
            }
            filters.maxStanze?.let {
                predicates.add(cb.lessThanOrEqualTo(subquery, it.toLong()))
            }
        }

        // 6. Filtro Bagni
        filters.bagni?.let { minBagni ->
            val subquery = query.subquery(Long::class.java)
            val ambienteRoot = subquery.from(AmbienteEntity::class.java)
            subquery.select(cb.count(ambienteRoot))
            subquery.where(
                cb.equal(ambienteRoot.get<ImmobileEntity>("immobile"), root),
                cb.like(cb.lower(ambienteRoot.get("tipologia")), "%bagno%")
            )
            predicates.add(cb.greaterThanOrEqualTo(subquery, minBagni.toLong()))
        }

        // 7. Filtro Query Testuale
        if (!filters.query.isNullOrBlank()) {
            val q = "%${filters.query!!.lowercase()}%"
            val predicateLocalita = cb.like(cb.lower(root.get("localita")), q)
            val predicateIndirizzo = cb.like(cb.lower(root.get("indirizzo")), q)
            val predicateCategoria = cb.like(cb.lower(root.get("categoria")), q)
            val predicateDesc = cb.like(cb.lower(root.get("descrizione")), q)

            predicates.add(cb.or(predicateLocalita, predicateIndirizzo, predicateCategoria, predicateDesc))
        }

        // 8. Filtro Geografico (Opzionale nel caso venga passato)
        if (filters.lat != null && filters.lon != null && filters.radiusKm != null) {
            // Nota: Per le query geospaziali precise in Specification serve Hibernate Spatial o formule native complesse.
            // Se usi PostGIS è meglio usare una Native Query nel repository.
            // Qui lo ometto per brevità per non rompere la compilazione se non hai le estensioni,
            // ma la logica "nearest agency" è gestita nel createImmobile.
        }

        if (predicates.isEmpty()) {
            return cb.conjunction()
        }

        return cb.and(*predicates.toTypedArray())
    }
}