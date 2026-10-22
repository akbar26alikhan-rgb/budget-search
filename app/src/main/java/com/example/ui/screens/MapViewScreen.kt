package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RestaurantEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.QuickFilterChip
import com.example.ui.components.RatingPill
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.WarmOrange
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen(
    viewModel: MainViewModel,
    restaurants: List<RestaurantEntity>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val context = LocalContext.current
    val radiusKm by viewModel.searchRadiusKm.collectAsState()
    val centerLat = viewModel.userLatitude.value
    val centerLon = viewModel.userLongitude.value

    var selectedRestaurant by remember { mutableStateOf<RestaurantEntity?>(restaurants.firstOrNull()) }

    Column(modifier = modifier.fillMaxSize().testTag("map_view_screen")) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Vasai Food Radar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visual Proximity & Outlets in Vasai",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.handleBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.navigateTo(Screen.SEARCH_RESULTS) }) {
                    Icon(Icons.Default.List, contentDescription = "List View")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Radius Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search Radius:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2.0, 5.0, 10.0, 15.0).forEach { r ->
                    QuickFilterChip(
                        text = "${r.toInt()} km",
                        isSelected = radiusKm == r,
                        onClick = { viewModel.searchRadiusKm.value = r }
                    )
                }
            }
        }

        // Radar Canvas Area
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF1F8E9))
                .border(1.5.dp, ForestGreenLight.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val maxRadiusPx = minOf(centerX, centerY) * 0.88f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(restaurants, radiusKm) {
                        detectTapGestures { tapOffset ->
                            // Hit test for nearest pin
                            val tapped = restaurants.minByOrNull { r ->
                                val dLat = r.latitude - centerLat
                                val dLon = r.longitude - centerLon
                                // approximate 1 deg lat ~= 111 km, 1 deg lon ~= 105 km
                                val xKm = dLon * 105.0
                                val yKm = -dLat * 111.0

                                val px = centerX + (xKm / radiusKm * maxRadiusPx).toFloat()
                                val py = centerY + (yKm / radiusKm * maxRadiusPx).toFloat()

                                val dx = tapOffset.x - px
                                val dy = tapOffset.y - py
                                dx * dx + dy * dy
                            }
                            if (tapped != null) {
                                selectedRestaurant = tapped
                            }
                        }
                    }
            ) {
                // Concentric circles
                val ringCount = 4
                for (i in 1..ringCount) {
                    val radius = maxRadiusPx * (i.toFloat() / ringCount)
                    drawCircle(
                        color = ForestGreenPrimary.copy(alpha = 0.15f),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Center crosshairs
                drawLine(
                    color = ForestGreenPrimary.copy(alpha = 0.2f),
                    start = Offset(centerX - maxRadiusPx, centerY),
                    end = Offset(centerX + maxRadiusPx, centerY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = ForestGreenPrimary.copy(alpha = 0.2f),
                    start = Offset(centerX, centerY - maxRadiusPx),
                    end = Offset(centerX, centerY + maxRadiusPx),
                    strokeWidth = 1f
                )

                // Center Marker (Vasai Road Station)
                drawCircle(
                    color = WarmOrange,
                    radius = 8f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(centerX, centerY)
                )

                // Draw Restaurant Pins
                restaurants.forEach { r ->
                    val dLat = r.latitude - centerLat
                    val dLon = r.longitude - centerLon
                    val xKm = dLon * 105.0
                    val yKm = -dLat * 111.0

                    val px = centerX + (xKm / radiusKm * maxRadiusPx).toFloat()
                    val py = centerY + (yKm / radiusKm * maxRadiusPx).toFloat()

                    val isSelected = selectedRestaurant?.id == r.id

                    // Glow circle if selected
                    if (isSelected) {
                        drawCircle(
                            color = WarmOrange.copy(alpha = 0.4f),
                            radius = 20f,
                            center = Offset(px, py)
                        )
                    }

                    // Pin circle
                    drawCircle(
                        color = if (isSelected) WarmOrange else ForestGreenPrimary,
                        radius = if (isSelected) 12f else 9f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 5f else 3.5f,
                        center = Offset(px, py)
                    )
                }
            }

            // Legend / Scale overlay
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text("📍 Center: Vasai West Stn", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("⭕ Outer ring: ${radiusKm.toInt()} km", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }

        // Selected Restaurant Bottom Card
        selectedRestaurant?.let { r ->
            val dist = viewModel.repository.calculateDistanceKm(centerLat, centerLon, r.latitude, r.longitude)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("map_preview_sheet"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = r.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${r.locality} • $dist km away",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RatingPill(rating = r.verifiedRating, reviewCount = r.ratingCount)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifiedBadge(status = r.verificationStatus)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:${r.latitude},${r.longitude}?q=${Uri.encode(r.name + " " + r.address)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    try { context.startActivity(mapIntent) } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Directions", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.selectRestaurant(r.id, openBuilder = true)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Build Meal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
