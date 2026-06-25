package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = SafetyRepository(db)

    // Current mocked User Location (Dhaka default, toggleable to Khulna)
    private val _userLat = MutableStateFlow(23.7462)
    val userLat: StateFlow<Double> = _userLat

    private val _userLng = MutableStateFlow(90.3742)
    val userLng: StateFlow<Double> = _userLng

    val currentCity = combine(_userLat, _userLng) { lat, _ ->
        if (lat > 23.0) "Dhaka (Capital)" else "Khulna City"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dhaka (Capital)")

    // Observables from Database
    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stations: StateFlow<List<PoliceStation>> = repository.allStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents: StateFlow<List<Incident>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peerList = MutableStateFlow<List<Peer>>(emptyList())
    val nearestStation = MutableStateFlow<PoliceStation?>(null)

    // Dynamic Module States
    // SOS Module
    val isSosTriggered = MutableStateFlow(false)
    val sosAlertMessage = MutableStateFlow<String?>(null)

    // Journey Watch Module
    val journeyVehicleNo = MutableStateFlow("")
    val journeyTimerDurationSec = MutableStateFlow(60) // defaults to 60 sec
    val journeyTimeLeftSec = MutableStateFlow(0)
    val isJourneyWatchActive = MutableStateFlow(false)
    val showJourneyPinDialog = MutableStateFlow(false)
    val journeyWatchPin = "2580" // Security PIN for cancellation
    private var journeyTimerJob: Job? = null

    // Fake Call Module
    val isFakeCallRinging = MutableStateFlow(false)
    val isFakeCallActive = MutableStateFlow(false)

    // Siren Module
    val isSirenActive = MutableStateFlow(false)

    // Stealth Record Module
    val isStealthRecordActive = MutableStateFlow(false)

    // Search query for directory
    val directorySearchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Seed database immediately
            repository.seedDatabaseIfEmpty()
            
            // Initial calculations
            updateLocationDependentStates()
        }
    }

    fun setLocation(lat: Double, lng: Double) {
        _userLat.value = lat
        _userLng.value = lng
        viewModelScope.launch {
            updateLocationDependentStates()
        }
    }

    private suspend fun updateLocationDependentStates() {
        nearestStation.value = repository.findNearestStation(_userLat.value, _userLng.value)
        peerList.value = repository.findNearbyHelpers(_userLat.value, _userLng.value)
    }

    // Camouflage Notes Actions
    fun insertNote(title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(title, content)
        }
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    // Smart SOS Activator
    fun triggerSmartSos() {
        val lat = _userLat.value
        val lng = _userLng.value
        val station = nearestStation.value
        
        isSosTriggered.value = true
        viewModelScope.launch {
            repository.logSos(
                type = "SOS BUTTON",
                lat = lat,
                lng = lng,
                nearestStation = station?.name ?: "Unknown - 999 Fallback",
                wasOffline = false
            )
            sosAlertMessage.value = "Emergency broadcast dispatched! Auto-dialing closest station..."
        }
    }

    fun clearSos() {
        isSosTriggered.value = false
        sosAlertMessage.value = null
    }

    // Journey Watch Activator
    fun startJourneyWatch() {
        if (journeyVehicleNo.value.isBlank()) return
        
        journeyTimeLeftSec.value = journeyTimerDurationSec.value
        isJourneyWatchActive.value = true
        
        journeyTimerJob?.cancel()
        journeyTimerJob = viewModelScope.launch {
            while (journeyTimeLeftSec.value > 0 && isJourneyWatchActive.value) {
                delay(1000)
                journeyTimeLeftSec.value -= 1
            }
            if (journeyTimeLeftSec.value == 0 && isJourneyWatchActive.value) {
                // Timer expired! Trigger SOS automatic logs and activation.
                triggerSmartSos()
                isJourneyWatchActive.value = false
            }
        }
    }

    fun requestCancelJourneyWatch() {
        showJourneyPinDialog.value = true
    }

    fun verifyAndCancelJourney(enteredPin: String): Boolean {
        return if (enteredPin == journeyWatchPin) {
            isJourneyWatchActive.value = false
            journeyTimerJob?.cancel()
            showJourneyPinDialog.value = false
            true
        } else {
            false
        }
    }

    fun closeJourneyPinDialog() {
        showJourneyPinDialog.value = false
    }

    // Fake Call Activator
    fun triggerFakeCall() {
        isFakeCallRinging.value = true
        isFakeCallActive.value = false
    }

    fun answerFakeCall() {
        isFakeCallRinging.value = false
        isFakeCallActive.value = true
    }

    fun hangUpFakeCall() {
        isFakeCallRinging.value = false
        isFakeCallActive.value = false
    }

    // Siren
    fun toggleSiren() {
        isSirenActive.value = !isSirenActive.value
    }

    // Stealth Record Tracker
    fun toggleStealthRecord() {
        isStealthRecordActive.value = !isStealthRecordActive.value
    }
}
