package com.dieti.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class GeoapifyService {

    private val client = OkHttpClient()
    private val mapper = ObjectMapper()

    // TODO: Assicurati che la chiave sia corretta!
    private val apiKey = "4fe4eefe40284c7ebeda92bd33614495"

    data class GeoResult(
        val lat: Double,
        val lon: Double,
        val city: String?
    )

    data class AmenitiesResult(
        val hasParks: Boolean,
        val hasSchools: Boolean,
        val hasPublicTransport: Boolean
    )

    fun getCoordinates(indirizzo: String, localitaInput: String?): GeoResult? {
        try {
            // Costruiamo la query di ricerca
            val searchText = if (!localitaInput.isNullOrBlank()) {
                "$indirizzo, $localitaInput"
            } else {
                indirizzo
            }

            val encodedAddress = URLEncoder.encode(searchText, StandardCharsets.UTF_8.toString())
            // IMPORTANTE: Aggiunto &lang=it per avere i nomi in Italiano
            val url = "https://api.geoapify.com/v1/geocode/search?text=$encodedAddress&apiKey=$apiKey&limit=1&lang=it"

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()

                // DEBUG LOG: Vediamo cosa risponde esattamente Geoapify
                println("DEBUG GEOAPIFY JSON: $bodyString")

                if (!response.isSuccessful || bodyString == null) return null

                val json = mapper.readTree(bodyString)
                val features = json.get("features")

                if (features != null && features.isArray && features.size() > 0) {
                    val properties = features[0].get("properties")
                    val lat = properties.get("lat").asDouble()
                    val lon = properties.get("lon").asDouble()

                    // Logica di estrazione "a cascata" per trovare il nome del posto più preciso
                    val city = when {
                        properties.has("city") && !properties.get("city").isNull -> properties.get("city").asText()
                        properties.has("town") && !properties.get("town").isNull -> properties.get("town").asText()
                        properties.has("municipality") && !properties.get("municipality").isNull -> properties.get("municipality").asText()
                        properties.has("village") && !properties.get("village").isNull -> properties.get("village").asText()
                        properties.has("hamlet") && !properties.get("hamlet").isNull -> properties.get("hamlet").asText() // Frazioni
                        properties.has("suburb") && !properties.get("suburb").isNull -> properties.get("suburb").asText() // Quartieri
                        properties.has("county") && !properties.get("county").isNull -> properties.get("county").asText() // Provincia (Fallback)
                        else -> null
                    }

                    return GeoResult(lat, lon, city)
                }
            }
        } catch (e: Exception) {
            println("Errore Geoapify Geocoding: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    fun checkAmenities(lat: Double, lon: Double): AmenitiesResult {
        var hasParks = false
        var hasSchools = false
        var hasTransport = false

        try {
            val categories = "leisure.park,education.school,public_transport"
            val radius = 1000
            val url = "https://api.geoapify.com/v2/places?categories=$categories&filter=circle:$lon,$lat,$radius&limit=20&apiKey=$apiKey"

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = mapper.readTree(response.body?.string())
                    val features = json.get("features")
                    if (features != null && features.isArray) {
                        for (feature in features) {
                            val props = feature.get("properties")
                            val cats = props.get("categories").toString()

                            if (cats.contains("leisure.park")) hasParks = true
                            if (cats.contains("education.school")) hasSchools = true
                            if (cats.contains("public_transport") || cats.contains("public_transport.subway") || cats.contains("public_transport.train")) hasTransport = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Errore Geoapify Places: ${e.message}")
        }

        return AmenitiesResult(hasParks, hasSchools, hasTransport)
    }
}