package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiKeyManager
import com.example.data.api.GooglePlacesService
import com.example.data.api.PlacesApiResult
import com.example.ui.MainViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.WarmOrange
import kotlinx.coroutines.launch

@Composable
fun GooglePlacesConfigDialog(
    apiKeyManager: ApiKeyManager,
    placesService: GooglePlacesService,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var inputKey by remember { mutableStateOf(apiKeyManager.getApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<PlacesApiResult?>(null) }
    var showInstructions by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = OrangeContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Google Places API", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Live Hotel & Dhaba Sync", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Insert your Google Places API Key to fetch live nearby hotels, dhabas, opening hours, and ratings directly from Google Maps.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input Field for API Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Google Places API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide" else "Show"
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let { text ->
                                    inputKey = text.trim()
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("google_api_key_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons for Key: Save & Remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            apiKeyManager.saveApiKey(inputKey)
                            viewModel.showToast("Google Places API Key saved")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("save_api_key_btn")
                    ) {
                        Text("Save Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (apiKeyManager.hasApiKey()) {
                        OutlinedButton(
                            onClick = {
                                apiKeyManager.clearApiKey()
                                inputKey = ""
                                syncResult = null
                                viewModel.showToast("API Key removed. Using offline Vasai catalog.")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Key", fontSize = 12.sp, color = AlertRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test & Live Sync Button
                Button(
                    onClick = {
                        if (inputKey.isBlank()) {
                            viewModel.showToast("Please enter an API Key first")
                            return@Button
                        }
                        isLoading = true
                        syncResult = null
                        apiKeyManager.saveApiKey(inputKey)

                        coroutineScope.launch {
                            val result = placesService.searchAndSyncVasaiPlaces(
                                apiKey = inputKey,
                                latitude = viewModel.userLatitude.value,
                                longitude = viewModel.userLongitude.value,
                                radiusMeters = viewModel.searchRadiusKm.value * 1000.0,
                                keyword = "dhaba hotel restaurant"
                            )
                            isLoading = false
                            syncResult = result
                            if (result is PlacesApiResult.Success) {
                                viewModel.showToast("Success! Imported ${result.importedCount} live outlets from Google Places")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("sync_places_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting to Google...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Key & Fetch Live Places", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Result feedback card
                syncResult?.let { res ->
                    when (res) {
                        is PlacesApiResult.Success -> {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = GreenContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("SUCCESSFULLY SYNCED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreenPrimary)
                                        Text(
                                            text = "Fetched and added ${res.importedCount} live hotels & dhabas into BudgetBites catalog. You can now build family meals for them!",
                                            fontSize = 11.sp,
                                            color = ForestGreenDark
                                        )
                                    }
                                }
                            }
                        }
                        is PlacesApiResult.Error -> {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("CONNECTION FAILED", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AlertRed)
                                        Text(text = res.message, fontSize = 11.sp, color = Color(0xFFB71C1C))
                                        if (res.details.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = res.details, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Expandable Instructions
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInstructions = !showInstructions }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "How to get a Google API Key",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ForestGreenPrimary
                                )
                            }
                            Text(
                                text = if (showInstructions) "Hide" else "Show Steps",
                                fontSize = 11.sp,
                                color = WarmOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showInstructions) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Visit Google Cloud Console: console.cloud.google.com\n" +
                                        "2. Create a project and enable 'Places API (New)' and 'Geocoding API'.\n" +
                                        "3. Navigate to Credentials ➔ Create Credentials ➔ API Key.\n" +
                                        "4. (Optional) Restrict key to Android app package: com.aistudio.budgetbites.vasai\n" +
                                        "5. Free Tier: Google provides $200 free credit monthly (up to 10,000 queries free).\n" +
                                        "6. Paste your key in the box above and tap 'Test Key & Fetch Live Places'.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.cloud.google.com/apis/library/places-backend.googleapis.com"))
                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(34.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Google Cloud Console", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Text("Done")
            }
        }
    )
}
