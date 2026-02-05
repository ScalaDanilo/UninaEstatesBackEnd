package com.dieti.backend.service

import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisGeoService(
    private val redisTemplate: StringRedisTemplate
) {
    private val KEY_IMMOBILI_GEO = "immobili:geo"
    private val KEY_CITIES = "immobili:cities" // Set per i nomi dei comuni

    // --- GEOSPAZIALE (Mappa) ---

    fun addLocation(immobileId: String, lat: Double, lon: Double) {
        try {
            redisTemplate.opsForGeo().add(KEY_IMMOBILI_GEO, Point(lon, lat), immobileId)
        } catch (e: Exception) {
            println("Errore Redis Geo Add: ${e.message}")
        }
    }

    fun findNearbyImmobiliIds(lat: Double, lon: Double, radiusKm: Double): List<String> {
        return try {
            val circle = Circle(Point(lon, lat), Distance(radiusKm, Metrics.KILOMETERS))
            val args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().sortAscending()

            val results = redisTemplate.opsForGeo().radius(KEY_IMMOBILI_GEO, circle, args)

            results?.content?.map { it.content.name } ?: emptyList()
        } catch (e: Exception) {
            println("Errore Redis Geo Search: ${e.message}")
            emptyList()
        }
    }

    // --- AUTOCOMPLETAMENTO COMUNI ---

    fun addCity(city: String) {
        try {
            // Aggiungiamo il comune al Set (gestisce automaticamente i duplicati essendo un SET)
            redisTemplate.opsForSet().add(KEY_CITIES, city)
        } catch (e: Exception) {
            println("Errore Redis Add City: ${e.message}")
        }
    }

    fun searchCities(query: String): List<String> {
        return try {
            // Recuperiamo tutti i comuni (il dataset è piccolo, max 8k comuni in Italia, Redis è istantaneo)
            val allCities = redisTemplate.opsForSet().members(KEY_CITIES) ?: emptySet()

            // Filtriamo in memoria (molto veloce per liste di stringhe semplici)
            allCities.filter { it.contains(query, ignoreCase = true) }
                .sorted()
                .take(10) // Limitiamo a 10 suggerimenti per la UI
        } catch (e: Exception) {
            println("Errore Redis Search City: ${e.message}")
            emptyList()
        }
    }
}