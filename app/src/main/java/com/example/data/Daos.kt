package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface PoliceStationDao {
    @Query("SELECT * FROM police_stations ORDER BY name ASC")
    fun getAllStations(): Flow<List<PoliceStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<PoliceStation>)

    @Query("SELECT COUNT(*) FROM police_stations")
    suspend fun getCount(): Int
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<Incident>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<Incident>)

    @Query("SELECT COUNT(*) FROM incidents")
    suspend fun getCount(): Int
}

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY name ASC")
    fun getAllPeers(): Flow<List<Peer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeers(peers: List<Peer>)

    @Query("SELECT COUNT(*) FROM peers")
    suspend fun getCount(): Int
}

@Dao
interface SosLogDao {
    @Query("SELECT * FROM sos_logs ORDER BY timestamp DESC")
    fun getAllSosLogs(): Flow<List<SosLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosLog(log: SosLog)
}
