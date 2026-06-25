package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamouflageNotesScreen(
    viewModel: SafetyViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val scope = rememberCoroutineScope()

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var isCreatingNote by remember { mutableStateOf(false) }
    var secretProgress by remember { mutableStateOf(0f) } // Progress bar during hold
    var isHoldingSecret by remember { mutableStateOf(false) }

    // Subtle/professional colors for a standard gray-themed modern notes app
    val notepadBodyColor = Color(0xFFF9F9FA)
    val notepadTitleColor = Color(0xFF1C1C1E)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHoldingSecret = true
                                        secretProgress = 0f
                                        val job = scope.launch {
                                            for (i in 1..30) {
                                                delay(100)
                                                secretProgress = i / 30f
                                            }
                                            isHoldingSecret = false
                                            secretProgress = 0f
                                            onNavigateToDashboard()
                                        }
                                        try {
                                            awaitRelease()
                                        } finally {
                                            job.cancel()
                                            isHoldingSecret = false
                                            secretProgress = 0f
                                        }
                                    }
                                )
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text(
                                text = "Notes",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = notepadTitleColor,
                                modifier = Modifier.testTag("notes_title")
                            )
                            if (isHoldingSecret) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { secretProgress },
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(3.dp),
                                    color = Color(0xFF5E2BFF),
                                    trackColor = Color(0xFFE5E5EA)
                                )
                            } else {
                                Text(
                                    text = "Tap & hold title to sync folders",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = notepadBodyColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNote = !isCreatingNote },
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(
                    imageVector = if (isCreatingNote) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Create new note"
                )
            }
        },
        containerColor = notepadBodyColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            AnimatedVisibility(
                visible = isCreatingNote,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "New Work Memo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = notepadTitleColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            placeholder = { Text("Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color(0xFFE5E5EA)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            placeholder = { Text("Write content here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("note_content_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color(0xFFE5E5EA)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank()) {
                                    viewModel.insertNote(noteTitle, noteContent)
                                    noteTitle = ""
                                    noteContent = ""
                                    isCreatingNote = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1C1C1E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("note_save_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Note")
                        }
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Empty notes",
                            tint = Color.LightGray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No notes recorded yet",
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use notes to organize standard tasks offline.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_item_${note.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = note.title,
                                        fontWeight = FontWeight.Bold,
                                        color = notepadTitleColor,
                                        fontSize = 17.sp
                                    )
                                    if (note.content.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = note.content,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteNoteById(note.id) },
                                    modifier = Modifier.testTag("delete_note_button_${note.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Note",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
