package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "police_stations")
data class PoliceStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val area: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dangerLevel: String, // "High", "Medium", "Safe"
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "peers")
data class Peer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean,
    val distanceText: String = ""
)

@Entity(tableName = "sos_logs")
data class SosLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "SOS BUTTON", "JOURNEY WATCH"
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val nearestStation: String,
    val wasOffline: Boolean
)
