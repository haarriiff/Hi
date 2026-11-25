package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.CelebrationProfileEntity
import com.example.data.LetterSectionEntity
import com.example.data.LetterSectionType
import com.example.ui.components.PolaroidPhotoCard
import com.example.ui.theme.BerryRose
import com.example.ui.theme.CandleGold
import com.example.ui.theme.CaveatFamily
import com.example.ui.theme.CocoaMuted
import com.example.ui.theme.EspressoInk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayLetterScreen(
    profile: CelebrationProfileEntity,
    sections: List<LetterSectionEntity>,
    onBack: () -> Unit,
    onReplaySurprise: () -> Unit,
    onOpenPersonalize: () -> Unit,
    onAddPhoto: (uri: Uri, caption: String, insertAtOrder: Int?) -> Unit,
    onAddParagraph: (text: String) -> Unit,
    onUpdateSection: (LetterSectionEntity) -> Unit,
    onDeleteSection: (LetterSectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var isEditMode by remember { mutableStateOf(false) }
    var isAutoReading by remember { mutableStateOf(false) }
    var pendingInsertOrder by remember { mutableStateOf<Int?>(null) }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pickedPhotoCaption by remember { mutableStateOf("") }

    var editingSection by remember { mutableStateOf<LetterSectionEntity?>(null) }
    var editingTextDraft by remember { mutableStateOf("") }
    var showAddParagraphDialog by remember { mutableStateOf(false) }
    var newParagraphDraft by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Pause auto-reading gracefully if the user touches/drags the letter manually
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start && isAutoReading) {
                isAutoReading = false
            }
        }
    }

    // Smooth, relaxed frame-synchronized slow scroll through the birthday letter
    LaunchedEffect(isAutoReading) {
        if (isAutoReading) {
            if (!listState.canScrollForward) {
                listState.animateScrollToItem(0)
            }
            val speedPxPerSecond = with(density) { 34.dp.toPx() }
            var lastFrameNanos = 0L
            while (isActive && isAutoReading) {
                val frameNanos = withFrameNanos { it }
                if (lastFrameNanos != 0L) {
                    val dtSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f)
                        .coerceIn(0f, 0.05f)
                    val deltaPx = speedPxPerSecond * dtSeconds
                    if (deltaPx > 0f) {
                        val consumed = listState.scrollBy(deltaPx)
                        if (consumed <= 0.01f && !listState.canScrollForward) {
                            isAutoReading = false
                        }
                    }
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    fun toggleAutoReading() {
        if (isAutoReading) {
            isAutoReading = false
        } else {
            coroutineScope.launch {
                if (!listState.canScrollForward) {
                    listState.animateScrollToItem(0)
                }
                isAutoReading = true
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedPhotoUri = uri
            pickedPhotoCaption = ""
        } else {
            pendingInsertOrder = null
        }
    }

    fun launchPhotoPickerAt(order: Int?) {
        isAutoReading = false
        pendingInsertOrder = order
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Birthday Letter",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("letter_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isEditMode = !isEditMode },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_edit_letter_button")
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Filled.Check else Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.cd_edit_item),
                            tint = if (isEditMode) BerryRose else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onOpenPersonalize,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("letter_personalize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = stringResource(R.string.cd_personalize)
                        )
                    }
                    IconButton(
                        onClick = onReplaySurprise,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("replay_surprise_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Replay,
                            contentDescription = stringResource(R.string.cd_replay)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExtendedFloatingActionButton(
                    onClick = { toggleAutoReading() },
                    containerColor = if (isAutoReading) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = EspressoInk,
                    modifier = Modifier.testTag("fab_read_letter"),
                    icon = {
                        Icon(
                            imageVector = if (isAutoReading) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.cd_read_letter)
                        )
                    },
                    text = {
                        Text(
                            text = if (isAutoReading) {
                                "Pause"
                            } else {
                                stringResource(R.string.stage_letter_read)
                            }
                        )
                    }
                )

                ExtendedFloatingActionButton(
                    onClick = { launchPhotoPickerAt(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_photo"),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.cd_add_photo)
                        )
                    },
                    text = {
                        Text(stringResource(R.string.stage_letter_add_photo))
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 620.dp)
                    .testTag("letter_scroll_list"),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 104.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stationery Letter Header Card
                item(key = "letter_header") {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Wax seal crest
                            Surface(
                                shape = CircleShape,
                                color = BerryRose,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = profile.letterTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            HorizontalDivider(
                                modifier = Modifier.width(80.dp),
                                thickness = 2.dp,
                                color = CandleGold.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Dearest ${profile.friendName},",
                                fontFamily = CaveatFamily,
                                fontSize = 32.sp,
                                color = BerryRose,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (profile.savedWish.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint = CandleGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Wish sealed tonight: \"${profile.savedWish}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EspressoInk
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            FilledTonalButton(
                                onClick = { toggleAutoReading() },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isAutoReading) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                    contentColor = if (isAutoReading) {
                                        EspressoInk
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("read_letter_button")
                            ) {
                                Icon(
                                    imageVector = if (isAutoReading) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAutoReading) {
                                        stringResource(R.string.stage_letter_pause_read)
                                    } else {
                                        stringResource(R.string.stage_letter_read)
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                // Letter Paragraphs & Interleaved Polaroid Photos
                itemsIndexed(
                    items = sections,
                    key = { _, item -> item.id }
                ) { index, section ->
                    if (section.type == LetterSectionType.PHOTO.name) {
                        PolaroidPhotoCard(
                            section = section,
                            isEditMode = isEditMode,
                            onEditCaption = {
                                editingSection = section
                                editingTextDraft = section.photoCaption
                            },
                            onDelete = { onDeleteSection(section) }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Text(
                                    text = section.textContent,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                AnimatedVisibility(visible = isEditMode) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingSection = section
                                                editingTextDraft = section.textContent
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = stringResource(R.string.cd_edit_item),
                                                tint = CocoaMuted
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteSection(section) },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = stringResource(R.string.cd_delete_item),
                                                tint = BerryRose
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Inline "Add Photo Here" prompt between paragraphs while scrolling
                        val nextIsPhoto = sections.getOrNull(index + 1)?.type == LetterSectionType.PHOTO.name
                        if (!nextIsPhoto) {
                            InlineInsertPhotoStrip(
                                onClick = { launchPhotoPickerAt(section.sortOrder + 1) },
                                testTag = "insert_photo_after_${section.id}"
                            )
                        }
                    }
                }

                // Bottom Letter Actions & Handwritten Signature
                item(key = "letter_footer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { launchPhotoPickerAt(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("footer_add_photo_button"),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Photo")
                            }
                            OutlinedButton(
                                onClick = {
                                    newParagraphDraft = ""
                                    showAddParagraphDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("footer_add_paragraph_button"),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.stage_letter_add_note))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = profile.closingLine,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = profile.senderName,
                                    fontFamily = CaveatFamily,
                                    fontSize = 34.sp,
                                    color = BerryRose,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog after selecting a photo via Android Photo Picker to add a handwritten caption
    if (pickedPhotoUri != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = {
                pickedPhotoUri = null
                pendingInsertOrder = null
            },
            title = {
                Text(
                    text = "Add Memory Caption",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(pickedPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.cd_memory_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    OutlinedTextField(
                        value = pickedPhotoCaption,
                        onValueChange = { pickedPhotoCaption = it },
                        label = { Text("Caption for this photo") },
                        placeholder = { Text("One of my favorite memories with you") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_caption_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pickedPhotoUri
                        if (uri != null) {
                            onAddPhoto(uri, pickedPhotoCaption, pendingInsertOrder)
                        }
                        pickedPhotoUri = null
                        pendingInsertOrder = null
                    },
                    modifier = Modifier.testTag("confirm_add_photo_button")
                ) {
                    Text("Add to Letter")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickedPhotoUri = null
                        pendingInsertOrder = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to edit an existing paragraph or photo caption
    val currentEdit = editingSection
    if (currentEdit != null) {
        val isPhoto = currentEdit.type == LetterSectionType.PHOTO.name
        AlertDialog(
            onDismissRequest = { editingSection = null },
            title = {
                Text(
                    text = if (isPhoto) "Edit Photo Caption" else "Edit Paragraph",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = editingTextDraft,
                    onValueChange = { editingTextDraft = it },
                    minLines = if (isPhoto) 2 else 4,
                    maxLines = if (isPhoto) 3 else 8,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = if (isPhoto) {
                            currentEdit.copy(photoCaption = editingTextDraft.trim())
                        } else {
                            currentEdit.copy(textContent = editingTextDraft.trim())
                        }
                        onUpdateSection(updated)
                        editingSection = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSection = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to append a new letter paragraph
    if (showAddParagraphDialog) {
        AlertDialog(
            onDismissRequest = { showAddParagraphDialog = false },
            title = {
                Text(
                    text = "Add a New Note",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = newParagraphDraft,
                    onValueChange = { newParagraphDraft = it },
                    placeholder = { Text("Write another memory or birthday wish…") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_paragraph_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newParagraphDraft.isNotBlank()) {
                            onAddParagraph(newParagraphDraft)
                        }
                        showAddParagraphDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_paragraph_button")
                ) {
                    Text("Add to Letter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddParagraphDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InlineInsertPhotoStrip(
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier
                .height(48.dp)
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = BerryRose,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.stage_letter_add_memory_here),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
