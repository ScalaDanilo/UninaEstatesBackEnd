package com.dieti.backend.repository

import com.dieti.backend.dto.ImmobileSearchFilters
import com.dieti.backend.entity.ImmobileEntity
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class ImmobileSpecification(private val filters: ImmobileSearchFilters) : Specification<ImmobileEntity> {
    override fun toPredicate(
        root: Root<ImmobileEntity>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        val predicates = mutableListOf<Predicate>()

        // Filtro per testo (Indirizzo o Località)
        if (!filters.query.isNullOrBlank()) {
            val searchLike = "%${filters.query.lowercase()}%"
            val predicateIndirizzo = criteriaBuilder.like(criteriaBuilder.lower(root.get("indirizzo")), searchLike)
            val predicateLocalita = criteriaBuilder.like(criteriaBuilder.lower(root.get("localita")), searchLike)
            predicates.add(criteriaBuilder.or(predicateIndirizzo, predicateLocalita))
        }

        // Tipo Vendita
        filters.tipoVendita?.let {
            predicates.add(criteriaBuilder.equal(root.get<Boolean>("tipoVendita"), it))
        }

        // Prezzo
        filters.minPrezzo?.let {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prezzo"), it))
        }
        filters.maxPrezzo?.let {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("prezzo"), it))
        }

        // Metri Quadri
        filters.minMq?.let {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mq"), it))
        }
        filters.maxMq?.let {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("mq"), it))
        }

        // Condizione
        filters.condizione?.let {
            predicates.add(criteriaBuilder.equal(root.get<String>("statoProprieta"), it))
        }

        return if (predicates.isEmpty()) null else criteriaBuilder.and(*predicates.toTypedArray())
    }
}