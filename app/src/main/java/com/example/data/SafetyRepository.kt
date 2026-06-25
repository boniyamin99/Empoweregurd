package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.*

class SafetyRepository(private val db: AppDatabase) {

    val allNotes: Flow<List<Note>> = db.noteDao().getAllNotes()
    val allStations: Flow<List<PoliceStation>> = db.policeStationDao().getAllStations()
    val allIncidents: Flow<List<Incident>> = db.incidentDao().getAllIncidents()
    val allPeers: Flow<List<Peer>> = db.peerDao().getAllPeers()
    val allSosLogs: Flow<List<SosLog>> = db.sosLogDao().getAllSosLogs()

    // Seeds the database with mock Bangladesh-based data if empty
    suspend fun seedDatabaseIfEmpty() {
        if (db.policeStationDao().getCount() == 0) {
            val mockStations = listOf(
                PoliceStation(
                    name = "Khulna Sadar Police Station",
                    area = "Khulna",
                    phone = "+8801713373286",
                    latitude = 22.8122,
                    longitude = 89.5644
                ),
                PoliceStation(
                    name = "Daulatpur Thana Police Station",
                    area = "Khulna",
                    phone = "+8801713373289",
                    latitude = 22.8808,
                    longitude = 89.5194
                ),
                PoliceStation(
                    name = "Dhanmondi Thana (DMP)",
                    area = "Dhaka",
                    phone = "+8801713373155",
                    latitude = 23.7462,
                    longitude = 90.3742
                ),
                PoliceStation(
                    name = "Gulshan Thana (DMP)",
                    area = "Dhaka",
                    phone = "+8801713373166",
                    latitude = 23.7925,
                    longitude = 90.4178
                ),
                PoliceStation(
                    name = "Uttara Thana (DMP)",
                    area = "Dhaka",
                    phone = "+8801713373156",
                    latitude = 23.8729,
                    longitude = 90.4031
                ),
                PoliceStation(
                    name = "Shahbagh Thana (DMP)",
                    area = "Dhaka",
                    phone = "+8801713373157",
                    latitude = 23.7383,
                    longitude = 90.3957
                )
            )
            db.policeStationDao().insertStations(mockStations)
        }

        if (db.incidentDao().getCount() == 0) {
            val mockIncidents = listOf(
                Incident(
                    title = "Rupsha Bridge Footpath (Unlit)",
                    description = "Reports of harassment and lack of streetlights after 9:00 PM.",
                    dangerLevel = "High",
                    latitude = 22.7989,
                    longitude = 89.5855
                ),
                Incident(
                    title = "Daulatpur Rail Gate Alleyway",
                    description = "Recent local snatching incident reported near the dark alley next to the rail tracks.",
                    dangerLevel = "High",
                    latitude = 22.8821,
                    longitude = 89.5180
                ),
                Incident(
                    title = "Dhanmondi Lake - South End Row",
                    description = "Poorly patrolled walkways near the lakeside thickets during late evening hours.",
                    dangerLevel = "Medium",
                    latitude = 23.7420,
                    longitude = 90.3750
                ),
                Incident(
                    title = "Gulshan-2 Circle Underpass Area",
                    description = "Crowded area with frequent complaints of verbal catcalling near exit lanes.",
                    dangerLevel = "Medium",
                    latitude = 23.7920,
                    longitude = 90.4160
                )
            )
            db.incidentDao().insertIncidents(mockIncidents)
        }

        if (db.peerDao().getCount() == 0) {
            val mockPeers = listOf(
                Peer(
                    name = "Nusrat Jahan",
                    phone = "+8801723456789",
                    latitude = 22.8150,
                    longitude = 89.5600,
                    isActive = true
                ),
                Peer(
                    name = "Fahmida Rahman",
                    phone = "+8801834567890",
                    latitude = 23.7480,
                    longitude = 90.3725,
                    isActive = true
                ),
                Peer(
                    name = "Sadia Islam",
                    phone = "+8801945678901",
                    latitude = 23.7940,
                    longitude = 90.4190,
                    isActive = true
                ),
                Peer(
                    name = "Tasnim Ahmed",
                    phone = "+8801556789012",
                    latitude = 22.8100,
                    longitude = 89.5700,
                    isActive = true
                ),
                Peer(
                    name = "Ayesha Siddiqua",
                    phone = "+8801667890123",
                    latitude = 23.7360,
                    longitude = 90.3990,
                    isActive = true
                )
            )
            db.peerDao().insertPeers(mockPeers)
        }
    }

    // Notes operations
    suspend fun addNote(title: String, content: String) {
        db.noteDao().insertNote(Note(title = title, content = content))
    }

    suspend fun deleteNote(note: Note) {
        db.noteDao().deleteNote(note)
    }

    suspend fun deleteNoteById(id: Int) {
        db.noteDao().deleteNoteById(id)
    }

    // SOS Logging
    suspend fun logSos(type: String, lat: Double, lng: Double, nearestStation: String, wasOffline: Boolean) {
        db.sosLogDao().insertSosLog(
            SosLog(
                type = type,
                latitude = lat,
                longitude = lng,
                nearestStation = nearestStation,
                wasOffline = wasOffline
            )
        )
    }

    // Nearest Police Station Search (using Haversine)
    suspend fun findNearestStation(userLat: Double, userLng: Double): PoliceStation? {
        val stations = db.policeStationDao().getAllStations().first()
        if (stations.isEmpty()) return null
        
        return stations.minByOrNull { calculateDistance(userLat, userLng, it.latitude, it.longitude) }
    }

    // Find nearby active female speeders/helpers (filtered within radius e.g. 5km)
    suspend fun findNearbyHelpers(userLat: Double, userLng: Double): List<Peer> {
        val peers = db.peerDao().getAllPeers().first()
        return peers.filter { it.isActive }.map { peer ->
            val distKm = calculateDistance(userLat, userLng, peer.latitude, peer.longitude)
            peer.copy(distanceText = String.format("%.2f km away", distKm))
        }.sortedBy { it.distanceText }
    }

    // Distance calculation utility (Haversine Formula)
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
