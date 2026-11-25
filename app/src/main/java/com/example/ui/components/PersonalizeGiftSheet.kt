package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CelebrationProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizeGiftSheet(
    profile: CelebrationProfileEntity,
    onDismiss: () -> Unit,
    onSave: (friendName: String, senderName: String, letterTitle: String, closingLine: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var friendName by remember(profile) { mutableStateOf(profile.friendName) }
    var senderName by remember(profile) { mutableStateOf(profile.senderName) }
    var letterTitle by remember(profile) { mutableStateOf(profile.letterTitle) }
    var closingLine by remember(profile) { mutableStateOf(profile.closingLine) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Personalize This Birthday Gift",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Customize her name and your signature before sharing the surprise.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = friendName,
                onValueChange = { friendName = it },
                label = { Text("Friend's Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friend_name_input")
            )

            OutlinedTextField(
                value = letterTitle,
                onValueChange = { letterTitle = it },
                label = { Text("Letter Heading") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("letter_title_input")
            )

            OutlinedTextField(
                value = closingLine,
                onValueChange = { closingLine = it },
                label = { Text("Closing Line") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("closing_line_input")
            )

            OutlinedTextField(
                value = senderName,
                onValueChange = { senderName = it },
                label = { Text("Your Name (Signature)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sender_name_input")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(friendName, senderName, letterTitle, closingLine)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_personalize_button")
                ) {
                    Text("Save Gift")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
