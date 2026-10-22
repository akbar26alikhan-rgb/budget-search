package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RestaurantEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.QuickFilterChip
import com.example.ui.components.RatingPill
import com.example.ui.components.RestaurantImageBanner
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.SurfaceVariantCream
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    restaurants: List<RestaurantEntity>,
    modifier: Modifier = Modifier
) {
    val budget by viewModel.totalBudget.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val mealTiming by viewModel.mealTiming.collectAsState()
    val dietaryPref by viewModel.dietaryPreference.collectAsState()
    val diningMode by viewModel.diningMode.collectAsState()
    val radiusKm by viewModel.searchRadiusKm.collectAsState()
    val selectedLocality by viewModel.selectedLocality.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val budgetPresets = listOf(500, 1000, 1500, 2000, 3000)
    val localities = listOf("All Vasai", "Vasai West", "Vasai East", "Manickpur", "Babhola", "Stella", "Papdy")
    val cuisines = listOf("All", "Dhabas Only 🏕️", "Hotels & Dining 🏨", "Thalis", "Maharashtrian", "North Indian", "Malvani Seafood", "Pav Bhaji")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Header Location & App Title
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ForestGreenPrimary, ForestGreenDark)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = WarmOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Vasai, Maharashtra",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Palghar District • Near Vasai Road Stn",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ForestGreenLight,
                                modifier = Modifier.clickable {
                                    viewModel.showGoogleApiDialog.value = true
                                }.testTag("btn_open_google_api")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Google API",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WarmOrange,
                                modifier = Modifier.clickable {
                                    viewModel.navigateTo(Screen.ADMIN_PORTAL)
                                }
                            ) {
                                Text(
                                    text = "Admin",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "BudgetBites Vasai",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Never overspend when eating out with family",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("What would you like to eat? (e.g. Thali, Biryani)") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = ForestGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.navigateTo(Screen.SEARCH_RESULTS) }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = ForestGreenPrimary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = WarmOrange,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }
        }

        // 2. Localities Filter Row
        item {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    text = "Vasai Neighborhoods",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localities.forEach { loc ->
                        QuickFilterChip(
                            text = loc,
                            isSelected = selectedLocality == loc,
                            onClick = { viewModel.selectedLocality.value = loc }
                        )
                    }
                }
            }
        }

        // 3. THE PROMINENT BUDGET & GROUP OPTIMIZER HERO CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("budget_optimizer_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FIND FOOD WITHIN BUDGET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmOrange,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Smart Family Meal Optimizer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = GreenContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("₹", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Budget Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Total Food Budget",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₹${budget.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = ForestGreenPrimary
                        )
                    }

                    Slider(
                        value = budget.toFloat(),
                        onValueChange = { viewModel.totalBudget.value = it.toDouble() },
                        valueRange = 300f..5000f,
                        steps = 46, // increments of ~100
                        colors = SliderDefaults.colors(
                            thumbColor = WarmOrange,
                            activeTrackColor = ForestGreenPrimary
                        ),
                        modifier = Modifier.testTag("budget_slider")
                    )

                    // Quick Budget Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        budgetPresets.forEach { preset ->
                            val isSelected = budget.toInt() == preset
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) WarmOrange else MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.totalBudget.value = preset.toDouble() }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "₹$preset",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Per-Person breakdown badge
                    Surface(
                        color = OrangeContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(WarmOrange, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${(budget / maxOf(1, groupSize)).toInt()} / person",
                                    color = WarmOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "for family of $groupSize",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Group Size & Meal Timing Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Group Size
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "People in Group",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (groupSize > 1) viewModel.groupSize.value = groupSize - 1
                                        },
                                        modifier = Modifier.size(32.dp).testTag("group_decrement")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "$groupSize persons",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )

                                    IconButton(
                                        onClick = {
                                            if (groupSize < 20) viewModel.groupSize.value = groupSize + 1
                                        },
                                        modifier = Modifier.size(32.dp).testTag("group_increment")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Lunch vs Dinner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Meal Time",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    val isLunch = mealTiming == "LUNCH"
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLunch) ForestGreenPrimary else Color.Transparent,
                                        modifier = Modifier.clickable { viewModel.mealTiming.value = "LUNCH" }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LunchDining,
                                                contentDescription = null,
                                                tint = if (isLunch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Lunch",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLunch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!isLunch) ForestGreenPrimary else Color.Transparent,
                                        modifier = Modifier.clickable { viewModel.mealTiming.value = "DINNER" }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DinnerDining,
                                                contentDescription = null,
                                                tint = if (!isLunch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Dinner",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!isLunch) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dietary Preference Selector (All, Pure Veg, Non-Veg)
                    Text(
                        text = "Dietary Preference",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All Food",
                            "VEG" to "Pure Veg 🌱",
                            "NON_VEG" to "Non-Veg 🍗"
                        ).forEach { (code, label) ->
                            val isSel = dietaryPref == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.dietaryPreference.value = code }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dining Mode (Dine-in, Takeaway, Delivery)
                    Text(
                        text = "Dining Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Dine-in", "Takeaway", "Delivery").forEach { mode ->
                            val isSel = diningMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) WarmOrange else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.diningMode.value = mode }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Radius Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Travel Radius: ${radiusKm.toInt()} km from Vasai",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(2.0, 5.0, 10.0, 15.0).forEach { r ->
                                val isSel = radiusKm == r
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { viewModel.searchRadiusKm.value = r }
                                ) {
                                    Text(
                                        text = "${r.toInt()}km",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Main Search CTA Button
                    Button(
                        onClick = {
                            viewModel.navigateTo(Screen.SEARCH_RESULTS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("find_food_button")
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find Meals for $groupSize under ₹${budget.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // 4. Quick Cuisines
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Popular Vasai Cuisines",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cuisines.forEach { cuisine ->
                        val selectedCuisineVal by viewModel.selectedCuisine.collectAsState()
                        val selectedOutletVal by viewModel.outletTypeFilter.collectAsState()
                        val isSel = when (cuisine) {
                            "All" -> selectedCuisineVal == null && selectedOutletVal == null
                            "Dhabas Only 🏕️" -> selectedOutletVal == "Dhaba"
                            "Hotels & Dining 🏨" -> selectedOutletVal == "Family Restaurant"
                            else -> selectedCuisineVal == cuisine
                        }

                        QuickFilterChip(
                            text = cuisine,
                            isSelected = isSel,
                            onClick = {
                                when (cuisine) {
                                    "All" -> {
                                        viewModel.selectedCuisine.value = null
                                        viewModel.outletTypeFilter.value = null
                                    }
                                    "Dhabas Only 🏕️" -> {
                                        viewModel.outletTypeFilter.value = "Dhaba"
                                        viewModel.selectedCuisine.value = null
                                    }
                                    "Hotels & Dining 🏨" -> {
                                        viewModel.outletTypeFilter.value = "Family Restaurant"
                                        viewModel.selectedCuisine.value = null
                                    }
                                    else -> {
                                        viewModel.selectedCuisine.value = cuisine
                                        viewModel.outletTypeFilter.value = null
                                    }
                                }
                                viewModel.navigateTo(Screen.SEARCH_RESULTS)
                            }
                        )
                    }
                }
            }
        }

        // 5. Affordable Family Meal Recommendations Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WarmOrange,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Family of 6 for ₹1,500?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ForestGreenDark
                        )
                        Text(
                            text = "Hotel Rajeshwar offers 6 Special Thalis + Buttermilk for ₹945 total (leaves ₹555 in your pocket!)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.selectRestaurant("vasai_rajeshwar_veg", openBuilder = true)
                        }
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Open", tint = WarmOrange)
                    }
                }
            }
        }

        // 6. Nearby Vasai Restaurants List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Food Outlets in Vasai",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All (${restaurants.size})",
                    fontSize = 13.sp,
                    color = ForestGreenPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.SEARCH_RESULTS) }
                )
            }
        }

        // 7. Nearby Restaurants Carousel / Cards
        items(restaurants.take(6)) { restaurant ->
            val distance = viewModel.repository.calculateDistanceKm(
                viewModel.userLatitude.value,
                viewModel.userLongitude.value,
                restaurant.latitude,
                restaurant.longitude
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .clickable { viewModel.selectRestaurant(restaurant.id) }
                    .testTag("restaurant_card_${restaurant.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column {
                    RestaurantImageBanner(
                        imageUrl = restaurant.imageUrl,
                        name = restaurant.name,
                        outletType = restaurant.outletType,
                        rating = restaurant.verifiedRating,
                        ratingCount = restaurant.ratingCount,
                        verificationStatus = restaurant.verificationStatus,
                        heightDp = 135
                    )

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = restaurant.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${restaurant.cuisineTypes} • ${restaurant.locality}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$distance km",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = restaurant.address,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenContainer
                            ) {
                                Text(
                                    text = "GST 5% • Family Seating",
                                    fontSize = 10.sp,
                                    color = OnGreenContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.selectRestaurant(restaurant.id, openBuilder = false) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Menu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { viewModel.selectRestaurant(restaurant.id, openBuilder = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("build_meal_btn_${restaurant.id}")
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
}
