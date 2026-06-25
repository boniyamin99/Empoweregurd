package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Incident
import com.example.data.Peer
import com.example.data.PoliceStation
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SafetyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State bindings
    val currentCity by viewModel.currentCity.collectAsState()
    val isSosTriggered by viewModel.isSosTriggered.collectAsState()
    val sosAlertMessage by viewModel.sosAlertMessage.collectAsState()
    val nearestStation by viewModel.nearestStation.collectAsState()

    // Modules activation states
    val isSirenActive by viewModel.isSirenActive.collectAsState()
    val isFakeCallRinging by viewModel.isFakeCallRinging.collectAsState()
    val isFakeCallActive by viewModel.isFakeCallActive.collectAsState()
    val isStealthRecordActive by viewModel.isStealthRecordActive.collectAsState()

    // Active expanded sub-module container index (to keep screen tidy)
    var expandedModuleIndex by remember { mutableStateOf<Int?>(null) }

    // Ambient glow animation for background
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF130A2D), DarkBackground),
                    center = Offset(500f + gradientShift / 2f, 500f),
                    radius = 1200f
                )
            )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "EMPOWERGUARD",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = PureWhite
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("back_to_notes_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Camouflage Mode",
                                tint = PureWhite
                            )
                        }
                    },
                    actions = {
                        // Location Picker Pill
                        LocationTogglePill(viewModel)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    StatusBarWidget(viewModel)
                }

                item {
                    Text(
                        text = "SAFETY NET MODULES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Module Grid / Expansion Cards
                // We present all 8 modules neatly in standard expandable glassmorphic cards for ultimate density.
                item {
                    ModuleItemCard(
                        index = 1,
                        title = "Smart SOS Activator",
                        icon = Icons.Default.Emergency,
                        tagline = "Broadcast panic alarm instantly",
                        accentColor = EmergencyRed,
                        isExpanded = expandedModuleIndex == 1,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 1) null else 1 }
                    ) {
                        SosModuleLayout(viewModel, context)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 2,
                        title = "Journey Watch Live",
                        icon = Icons.Default.Timeline,
                        tagline = "Failsafe timer route support",
                        accentColor = AccentPurpleGlow,
                        isExpanded = expandedModuleIndex == 2,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 2) null else 2 }
                    ) {
                        JourneyModuleLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 3,
                        title = "Danger Heatmap",
                        icon = Icons.Default.Map,
                        tagline = "Visual hotspots & warnings",
                        accentColor = Color(0xFFFF9500),
                        isExpanded = expandedModuleIndex == 3,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 3) null else 3 }
                    ) {
                        HeatmapModuleLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 4,
                        title = "Fake Call Simulator",
                        icon = Icons.Default.PhoneCallback,
                        tagline = "Simulate mock incoming call",
                        accentColor = Color(0xFF34C759),
                        isExpanded = expandedModuleIndex == 4,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 4) null else 4 }
                    ) {
                        FakeCallPanelLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 5,
                        title = "Aesthetic Siren Loop",
                        icon = Icons.Default.Campaign,
                        tagline = "Strobe flashes & acoustics",
                        accentColor = Color(0xFFFF2D55),
                        isExpanded = expandedModuleIndex == 5,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 5) null else 5 }
                    ) {
                        SirenModuleLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 6,
                        title = "Stealth Recording Dot",
                        icon = Icons.Default.SettingsVoice,
                        tagline = "Discreet background capture",
                        accentColor = Color(0xFFA28BFF),
                        isExpanded = expandedModuleIndex == 6,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 6) null else 6 }
                    ) {
                        StealthRecordModuleLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 7,
                        title = "Peer Guardian Radar",
                        icon = Icons.Default.Radar,
                        tagline = "Verify nearby helper network",
                        accentColor = Color(0xFF5AC8FA),
                        isExpanded = expandedModuleIndex == 7,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 7) null else 7 }
                    ) {
                        GuardianRadarLayout(viewModel)
                    }
                }

                item {
                    ModuleItemCard(
                        index = 8,
                        title = "Bangladesh Directory",
                        icon = Icons.Default.Contacts,
                        tagline = "Local quick-dial emergency direct",
                        accentColor = AccentPurpleGlow,
                        isExpanded = expandedModuleIndex == 8,
                        onToggle = { expandedModuleIndex = if (expandedModuleIndex == 8) null else 8 }
                    ) {
                        LocalDirectoryLayout(viewModel, context)
                    }
                }
            }
        }

        // Global Overlays: Fake Call & Siren Takeover UI
        AnimatedVisibility(
            visible = isFakeCallRinging,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            FakeCallRingingOverlay(viewModel)
        }

        AnimatedVisibility(
            visible = isFakeCallActive,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            FakeCallActiveOverlay(viewModel)
        }

        AnimatedVisibility(
            visible = isSirenActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SirenTakeoverOverlay(viewModel)
        }

        // Small Blinking recording dot in top corner if Stealth Record is Active
        if (isStealthRecordActive) {
            BlinkingStealthIndicator()
        }
    }
}

// -------------------------------------------------------------
// REUSABLE GLASSMORPHIC CARD WITH GLOW ACCENT
// -------------------------------------------------------------
@Composable
fun ModuleItemCard(
    index: Int,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tagline: String,
    accentColor: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorderGlass, RoundedCornerShape(20.dp))
            .clickable { onToggle() }
            .testTag("module_card_$index"),
        colors = CardDefaults.cardColors(
            containerColor = CardBgGlass
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tagline,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Divider(color = CardBorderGlass, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0E000000))
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LOCATION PILL TOGGLE (For easy Dhaka/Khulna testing!)
// -------------------------------------------------------------
@Composable
fun LocationTogglePill(viewModel: SafetyViewModel) {
    val lat by viewModel.userLat.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    Button(
        onClick = {
            if (lat > 23.0) {
                // Currently Dhaka -> Switch to Khulna Thana Center
                viewModel.setLocation(22.8122, 89.5644)
            } else {
                // Currently Khulna -> Switch back to Dhanmondi Center
                viewModel.setLocation(23.7462, 90.3742)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = CardBgGlass,
            contentColor = PureWhite
        ),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, CardBorderGlass),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
            .height(34.dp)
            .testTag("location_toggle_pill")
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Change Location",
            tint = AccentPurpleGlow,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (lat > 23.0) "Dhaka" else "Khulna",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -------------------------------------------------------------
// TOP STATUS BAR WIDGET
// -------------------------------------------------------------
@Composable
fun StatusBarWidget(viewModel: SafetyViewModel) {
    val currentCity by viewModel.currentCity.collectAsState()
    val isSosTriggered by viewModel.isSosTriggered.collectAsState()
    val nearestStation by viewModel.nearestStation.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorderGlass, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CardBgGlass
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isSosTriggered) EmergencyRed else StatusOkGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSosTriggered) "SOS ACTIVE BROADCAST" else "GUARD ON STANDBY",
                        color = if (isSosTriggered) EmergencyRed else statusActiveColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = currentCity,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(CardBgGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Nearest Station Point Detected:",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = nearestStation?.name ?: "Finding nearest coordinates...",
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentPurpleGlow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Encrypted direct peer networks active. System ready.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

val statusActiveColor = Color(0xFF30D158)

// -------------------------------------------------------------
// MODULE 1 LAYOUT: SMART SOS
// -------------------------------------------------------------
@Composable
fun SosModuleLayout(viewModel: SafetyViewModel, context: Context) {
    val isSosTriggered by viewModel.isSosTriggered.collectAsState()
    val sosAlertMessage by viewModel.sosAlertMessage.collectAsState()
    val nearestStation by viewModel.nearestStation.collectAsState()

    // Pulsing circle scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Circular panic button container
        Box(
            modifier = Modifier
                .size(180.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isSosTriggered) {
                                viewModel.triggerSmartSos()
                            } else {
                                viewModel.clearSos()
                            }
                        }
                    )
                }
                .testTag("sos_pulse_button"),
            contentAlignment = Alignment.Center
        ) {
            // Ripple glow behind circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isSosTriggered) pulseScale else 1f)
                    .background(
                        color = (if (isSosTriggered) EmergencyRed else DeepPurple).copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .border(1.5.dp, (if (isSosTriggered) EmergencyRed else DeepPurple).copy(alpha = 0.25f), CircleShape)
            )

            // Actual core button
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isSosTriggered) {
                                listOf(EmergencyRed, Color(0xFF9E0B0B))
                            } else {
                                listOf(DeepPurple, Color(0xFF3314B8))
                            }
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, if (isSosTriggered) PureWhite.copy(alpha = 0.6f) else CardBorderGlass, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSosTriggered) "RESET" else "SOS PANIC",
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        sosAlertMessage?.let { msg ->
            Text(
                text = msg,
                color = EmergencyRed,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier
                    .background(Color(0x3BFF3B30), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tap-to-Call closest station
        nearestStation?.let { station ->
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${station.phone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("call_nearest_station_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurpleGlow
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Dial")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Direct Call: ${station.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } ?: run {
            // Offline fallback dial emergency button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_fallback_call_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmergencyRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Dial Toll-Free")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OFFLINE DIAL TOLL-FREE: 999",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 2 LAYOUT: JOURNEY WATCH
// -------------------------------------------------------------
@Composable
fun JourneyModuleLayout(viewModel: SafetyViewModel) {
    val vehicleNo by viewModel.journeyVehicleNo.collectAsState()
    val timerDurationSec by viewModel.journeyTimerDurationSec.collectAsState()
    val timeLeftSec by viewModel.journeyTimeLeftSec.collectAsState()
    val isJourneyActive by viewModel.isJourneyWatchActive.collectAsState()
    val showPinDialog by viewModel.showJourneyPinDialog.collectAsState()

    var customPinEntry by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isJourneyActive) {
            Text(
                text = "Track Vehicle Journey safely. Auto SOS is raised if timer details elapsed without secret cancel PIN codes.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = vehicleNo,
                onValueChange = { viewModel.journeyVehicleNo.value = it },
                label = { Text("Vehicle License plate details", color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurpleGlow,
                    unfocusedBorderColor = CardBorderGlass,
                    focusedLabelColor = AccentPurpleGlow,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journey_vehicle_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Safety Countdown Buffer: $timerDurationSec seconds",
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            
            Slider(
                value = timerDurationSec.toFloat(),
                onValueChange = { viewModel.journeyTimerDurationSec.value = it.toInt() },
                valueRange = 10f..300f,
                steps = 29,
                colors = SliderDefaults.colors(
                    thumbColor = AccentPurpleGlow,
                    activeTrackColor = AccentPurpleGlow,
                    inactiveTrackColor = CardBorderGlass
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.startJourneyWatch() },
                enabled = vehicleNo.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journey_start_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepPurple,
                    disabledContainerColor = CardBgGlass
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Active")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Engage Journey Watch")
            }
        } else {
            // Active Watch Details
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x24000000)),
                border = BorderStroke(0.5.dp, CardBorderGlass)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "JOURNEY ACTIVE",
                            color = statusActiveColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "No: $vehicleNo",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val min = timeLeftSec / 60
                    val sec = timeLeftSec % 60
                    val timerString = String.format("%02d:%02d", min, sec)

                    Text(
                        text = timerString,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (timeLeftSec < 15) EmergencyRed else PureWhite,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("journey_timer")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Auto broadcasting SOS details if zero hit",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.requestCancelJourneyWatch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmergencyRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journey_cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Cancel")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AUTHENTICATE CANCEL (PIN)")
                    }
                }
            }
        }

        // Pin cancellation dialog
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeJourneyPinDialog() },
                title = { Text("Confirm Identity PIN", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter security bypass code (Preset PIN: 2580) to de-escalate watch mode.", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customPinEntry,
                            onValueChange = { customPinEntry = it },
                            placeholder = { Text("Enter PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                focusedBorderColor = AccentPurpleGlow
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journey_pin_field")
                        )
                        pinErrorText?.let { err ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(err, color = EmergencyRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (viewModel.verifyAndCancelJourney(customPinEntry)) {
                                customPinEntry = ""
                                pinErrorText = null
                            } else {
                                pinErrorText = "Invalid bypass signature. Try again!"
                            }
                        },
                        modifier = Modifier.testTag("journey_pin_submit")
                    ) {
                        Text("DE-ESCALATE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeJourneyPinDialog() }) {
                        Text("BACK")
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

// -------------------------------------------------------------
// MODULE 3 LAYOUT: DANGER HEATMAP
// -------------------------------------------------------------
@Composable
fun HeatmapModuleLayout(viewModel: SafetyViewModel) {
    val incidents by viewModel.incidents.collectAsState()
    val city by viewModel.currentCity.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Active Hazard Indices reported coordinates filter ($city):",
            color = TextSecondary,
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Custom stylized graphic map drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0B1E))
                .border(0.5.dp, CardBorderGlass, RoundedCornerShape(16.dp))
                .testTag("heatmap_container")
        ) {
            // Draw schematic radar meshes and colored markers representing hot threat levels
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0x1A8B63FF)
                // Grid coordinates draw
                for (i in 1..8) {
                    val x = (size.width / 8f) * i
                    val y = (size.height / 8f) * i
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.5f)
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.5f)
                }

                // Draw high-threat circles
                drawCircle(
                    color = EmergencyRed.copy(alpha = 0.08f),
                    radius = 45.dp.toPx(),
                    center = Offset(size.width * 0.35f, size.height * 0.45f)
                )
                drawCircle(
                    color = EmergencyRed,
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.35f, size.height * 0.45f)
                )

                drawCircle(
                    color = Color(0xFFFF9500).copy(alpha = 0.08f),
                    radius = 35.dp.toPx(),
                    center = Offset(size.width * 0.72f, size.height * 0.65f)
                )
                drawCircle(
                    color = Color(0xFFFF9500),
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * 0.72f, size.height * 0.65f)
                )
            }

            // Legend indicators overlay index
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xD917122E), RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(EmergencyRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("High threat zones", fontSize = 9.sp, color = PureWhite)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9500), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unlit / reported alleys", fontSize = 9.sp, color = PureWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List active warnings
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val relevantThreats = incidents.filter {
                if (city.contains("Dhaka")) it.title.contains("Dhanmondi") || it.title.contains("Gulshan")
                else it.title.contains("Rupsha") || it.title.contains("Daulatpur")
            }

            relevantThreats.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBgGlass, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (item.dangerLevel == "High") EmergencyRed else Color(0xFFFF9500), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 4 LAYOUT: FAKE CALL PANEL
// -------------------------------------------------------------
@Composable
fun FakeCallPanelLayout(viewModel: SafetyViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Generate a high-fidelity incoming voice call. Use this overlay structure to excuse yourself or ward off suspicious advances cleanly.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.triggerFakeCall() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trigger_fake_call_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = "Active simulation")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Queue Calling Scenario Now", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// FAKE CALL OVERLAYS (Ringing & Call Screens)
// -------------------------------------------------------------
@Composable
fun FakeCallRingingOverlay(viewModel: SafetyViewModel) {
    val context = LocalContext.current
    BackHandler {
        viewModel.hangUpFakeCall()
    }

    // Glowing animation loop
    val transition = rememberInfiniteTransition(label = "ringer_vibe")
    val sizePulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutCirc), RepeatMode.Reverse),
        label = "size_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B13))
            .padding(16.dp)
            .testTag("fake_call_ringing_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home (Dad)",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mobile Incoming Call...",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        // Center Pulsing Call Symbol
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.Center)
                .scale(sizePulse),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF34C759).copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF34C759), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Action Sliding Anchors bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Decline Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(EmergencyRed, CircleShape)
                        .clickable { viewModel.hangUpFakeCall() }
                        .testTag("fake_call_decline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline call",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Decline", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Accept Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFF34C759), CircleShape)
                        .clickable { viewModel.answerFakeCall() }
                        .testTag("fake_call_accept_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept call",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Accept", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FakeCallActiveOverlay(viewModel: SafetyViewModel) {
    var timerCount by remember { mutableStateOf(0) }
    
    // Increment active call seconds timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timerCount += 1
        }
    }

    val min = timerCount / 60
    val sec = timerCount % 60
    val formattedTime = String.format("%02d:%02d", min, sec)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B13))
            .padding(16.dp)
            .testTag("fake_call_active_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home (Dad)",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedTime,
                fontSize = 16.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Simulated Soundwave / Waveform visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            val waveTransition = rememberInfiniteTransition(label = "soundwave")
            val waveHeightY by waveTransition.animateFloat(
                initialValue = 10f,
                targetValue = 90f,
                animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
                label = "wave"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = size.width / 15f
                for (i in 0..14) {
                    val scaleOffset = if (i % 2 == 0) waveHeightY else (waveHeightY * 0.5f)
                    val x = step * i + step / 2f
                    val top = (size.height - scaleOffset) / 2f
                    val bottom = (size.height + scaleOffset) / 2f
                    drawLine(
                        color = Color(0xFF34C759).copy(alpha = 0.8f),
                        start = Offset(x, top),
                        end = Offset(x, bottom),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
        }

        // Call functional buttons grid
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 180.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CallFeatureIcon(Icons.Default.MicOff, "Mute")
                CallFeatureIcon(Icons.Default.Dialpad, "Keypad")
                CallFeatureIcon(Icons.Default.VolumeUp, "Speaker")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CallFeatureIcon(Icons.Default.Add, "Add Call")
                CallFeatureIcon(Icons.Default.VideoCall, "FaceTime")
                CallFeatureIcon(Icons.Default.Contacts, "Contacts")
            }
        }

        // Hang Up Button at precise lower edge
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(EmergencyRed, CircleShape)
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .clickable { viewModel.hangUpFakeCall() }
                .testTag("fake_call_hangup_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "Hang up call",
                tint = PureWhite,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun CallFeatureIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = PureWhite, fontSize = 11.sp)
    }
}

// -------------------------------------------------------------
// MODULE 5: SIREN SCREEN strobe and animation
// -------------------------------------------------------------
@Composable
fun SirenModuleLayout(viewModel: SafetyViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Generate rapid visual strobes and flashing overlays to signal active danger alerts in public areas or dark space walks.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.toggleSiren() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("toggle_siren_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = "Active strobe")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Engage High Intensity Siren", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SirenTakeoverOverlay(viewModel: SafetyViewModel) {
    var flashFlag by remember { mutableStateOf(false) }

    // Frequency strobe state loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(120) // rapidly strobe colors
            flashFlag = !flashFlag
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (flashFlag) EmergencyRed else PureWhite)
            .clickable { viewModel.toggleSiren() }
            .testTag("siren_takeover_overlay")
    ) {
        // Massive cancel prompt
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = if (flashFlag) PureWhite else EmergencyRed,
                modifier = Modifier
                    .size(160.dp)
                    .scale(if (flashFlag) 1f else 1.25f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SIREN OSCILLATING ACTIVE",
                color = if (flashFlag) PureWhite else Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap anywhere on screen to silence alarm mode",
                color = if (flashFlag) PureWhite.copy(alpha = 0.8f) else Color.DarkGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------------------------------------------------
// MODULE 6: STEALTH AUDIO/VIDEO PRESERVATION DOT
// -------------------------------------------------------------
@Composable
fun StealthRecordModuleLayout(viewModel: SafetyViewModel) {
    val isRecordingActive by viewModel.isStealthRecordActive.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Simulates back-channel verification telemetry recording. Activates discrete ambient monitoring feeds and saves logs to internal sandbox storage.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x2E000000), RoundedCornerShape(12.dp))
                .border(0.5.dp, CardBorderGlass, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isRecordingActive) EmergencyRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecordingActive) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = if (isRecordingActive) EmergencyRed else PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isRecordingActive) "Stealth Capture Engaged" else "Stealth Capture Inactive",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("Blinking indicator active", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Switch(
                checked = isRecordingActive,
                onCheckedChange = { viewModel.toggleStealthRecord() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EmergencyRed,
                    checkedTrackColor = EmergencyRed.copy(alpha = 0.4f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardBgGlass
                ),
                modifier = Modifier.testTag("stealth_record_toggle")
            )
        }
    }
}

@Composable
fun BlinkingStealthIndicator() {
    var visibleDot by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            visibleDot = !visibleDot
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xE617122E), RoundedCornerShape(50))
                .border(1.dp, CardBorderGlass, RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(if (visibleDot) 1f else 0f)
                    .background(EmergencyRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("REC_STLTH", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

// -------------------------------------------------------------
// MODULE 7: PEER RADAR SCREEN
// -------------------------------------------------------------
@Composable
fun GuardianRadarLayout(viewModel: SafetyViewModel) {
    val peerList by viewModel.peerList.collectAsState()
    val city by viewModel.currentCity.collectAsState()

    // Infinite radar angle sweep
    val transition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "radar_angle"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Geospatial Peer Verification Radar ($city):",
            color = TextSecondary,
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(10.dp))

        // Large futuristic Circular Radar sweep
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0B1E))
                .border(0.5.dp, CardBorderGlass, RoundedCornerShape(16.dp))
                .testTag("radar_scan_container"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val gridStroke = Color(0x2B8B63FF)

                // Concentric active radar bounds circles
                drawCircle(gridStroke, radius = 25.dp.toPx(), center = centerOffset, style = Stroke(1.5f))
                drawCircle(gridStroke, radius = 55.dp.toPx(), center = centerOffset, style = Stroke(1.5f))
                drawCircle(gridStroke, radius = 85.dp.toPx(), center = centerOffset, style = Stroke(1.5f))

                // Crossing axis
                drawLine(gridStroke, Offset(0f, centerOffset.y), Offset(size.width, centerOffset.y), 1.5f)
                drawLine(gridStroke, Offset(centerOffset.x, 0f), Offset(centerOffset.x, size.height), 1.5f)

                // Sweep radar sweep line
                val angleRad = Math.toRadians(sweepAngle.toDouble())
                val endX = centerOffset.x + 120.dp.toPx() * kotlin.math.cos(angleRad).toFloat()
                val endY = centerOffset.y + 120.dp.toPx() * kotlin.math.sin(angleRad).toFloat()
                drawLine(
                    color = Color(0xFF5AC8FA).copy(alpha = 0.6f),
                    start = centerOffset,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Drawn simulated targets blinking
            Text(
                text = "${peerList.size} GUARDIANS NEARBY",
                color = Color(0xFF5AC8FA),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Peer contact panels
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            peerList.forEach { peer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBgGlass, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF5AC8FA).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF5AC8FA))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(peer.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(peer.distanceText, color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Direct Speed-Dial Anchor
                    val contextLocal = LocalContext.current
                    IconButton(
                        onClick = {
                            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${peer.phone}"))
                            contextLocal.startActivity(i)
                        },
                        modifier = Modifier.testTag("peer_dial_${peer.id}")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = statusActiveColor)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 8: LOCAL REGIONAL DIRECTORY
// -------------------------------------------------------------
@Composable
fun LocalDirectoryLayout(viewModel: SafetyViewModel, context: Context) {
    val stations by viewModel.stations.collectAsState()
    var searchVal by remember { mutableStateOf("") }
    var regionFilter by remember { mutableStateOf("All") } // "All", "Dhaka", "Khulna"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Official verify-certified Bangladesh Law Enforcement stations in active database ranges:",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchVal,
            onValueChange = { searchVal = it },
            placeholder = { Text("Search police stations...", color = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = AccentPurpleGlow,
                unfocusedBorderColor = CardBorderGlass
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("directory_search_bar")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Dhaka", "Khulna").forEach { tabName ->
                val selected = regionFilter == tabName
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) AccentPurpleGlow else CardBgGlass)
                        .border(1.dp, if (selected) AccentPurpleGlow else CardBorderGlass, RoundedCornerShape(8.dp))
                        .clickable { regionFilter = tabName }
                        .padding(vertical = 6.dp)
                        .testTag("directory_tab_$tabName"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Station Lists scrollable up to a max standard heights box to keep nesting clean
        val filteredList = stations.filter {
            val matchesRegion = regionFilter == "All" || it.area.equals(regionFilter, ignoreCase = true)
            val matchesSearch = it.name.contains(searchVal, ignoreCase = true) || it.area.contains(searchVal, ignoreCase = true)
            matchesRegion && matchesSearch
        }

        Column(
            modifier = Modifier
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local authority matching record.", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                filteredList.forEach { station ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBgGlass, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(station.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Region: ${station.area} | Code: ${station.phone}", color = TextSecondary, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${station.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("dial_station_${station.id}")
                        ) {
                            Icon(Icons.Default.LocalPhone, contentDescription = "Call", tint = AccentPurpleGlow)
                        }
                    }
                }
            }
        }
    }
}

// System Back press utility
@Composable
fun BackHandler(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
}
