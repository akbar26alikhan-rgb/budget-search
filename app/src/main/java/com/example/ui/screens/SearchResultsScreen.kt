package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.engine.BudgetMatchResult
import com.example.engine.SmartBudgetEngine
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.SortOption
import com.example.ui.components.QuickFilterChip
import com.example.ui.components.RatingPill
import com.example.ui.components.RestaurantImageBanner
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenLight
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    viewModel: MainViewModel,
    restaurants: List<RestaurantEntity>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val context = LocalContext.current
    val budget by viewModel.totalBudget.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val dietaryPref by viewModel.dietaryPreference.collectAsState()
    val diningMode by viewModel.diningMode.collectAsState()
    val radiusKm by viewModel.searchRadiusKm.collectAsState()
    val selectedLocality by viewModel.selectedLocality.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCuisine by viewModel.selectedCuisine.collectAsState()
    val outletFilter by viewModel.outletTypeFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val compareIds by viewModel.compareRestaurantIds.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    // Store calculated budget results per restaurant
    val resultsMap = remember { mutableStateMapOf<String, BudgetMatchResult>() }

    // Recalculate when budget, group, dietary, dining mode, or restaurants change
    LaunchedEffect(restaurants, budget, groupSize, dietaryPref, diningMode) {
        restaurants.forEach { r ->
            val menuItems = viewModel.repository.getMenuItemsDirect(r.id)
            val match = SmartBudgetEngine.calculateMatchesForRestaurant(
                restaurant = r,
                menuItems = menuItems,
                groupSize = groupSize,
                totalBudget = budget,
                dietaryPreference = dietaryPref,
                diningMode = diningMode
            )
            resultsMap[r.id] = match
        }
    }

    // Filter list
    val filteredList = restaurants.filter { r ->
        val distance = viewModel.repository.calculateDistanceKm(
            viewModel.userLatitude.value,
            viewModel.userLongitude.value,
            r.latitude,
            r.longitude
        )
        val matchesRadius = distance <= radiusKm
        val matchesLocality = selectedLocality == "All Vasai" || r.locality.contains(selectedLocality, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                r.name.contains(searchQuery, ignoreCase = true) ||
                r.cuisineTypes.contains(searchQuery, ignoreCase = true)
        val matchesCuisine = selectedCuisine == null || r.cuisineTypes.contains(selectedCuisine!!, ignoreCase = true)
        val matchesOutlet = outletFilter == null || r.outletType == outletFilter

        matchesRadius && matchesLocality && matchesQuery && matchesCuisine && matchesOutlet
    }

    // Sort list
    val sortedList = filteredList.sortedWith { r1, r2 ->
        val res1 = resultsMap[r1.id]
        val res2 = resultsMap[r2.id]
        val d1 = viewModel.repository.calculateDistanceKm(viewModel.userLatitude.value, viewModel.userLongitude.value, r1.latitude, r1.longitude)
        val d2 = viewModel.repository.calculateDistanceKm(viewModel.userLatitude.value, viewModel.userLongitude.value, r2.latitude, r2.longitude)

        when (sortOption) {
            SortOption.BEST_BUDGET_FIT -> {
                // Prioritize within budget success, then closest total to budget without exceeding
                val cost1 = if (res1 is BudgetMatchResult.Success) res1.bestFit.totalEstimatedBill else 99999.0
                val cost2 = if (res2 is BudgetMatchResult.Success) res2.bestFit.totalEstimatedBill else 99999.0
                cost1.compareTo(cost2)
            }
            SortOption.LOWEST_PRICE -> {
                val cost1 = when (res1) {
                    is BudgetMatchResult.Success -> res1.bestFit.totalEstimatedBill
                    is BudgetMatchResult.BudgetInsufficient -> res1.minimumTotal
                    else -> 99999.0
                }
                val cost2 = when (res2) {
                    is BudgetMatchResult.Success -> res2.bestFit.totalEstimatedBill
                    is BudgetMatchResult.BudgetInsufficient -> res2.minimumTotal
                    else -> 99999.0
                }
                cost1.compareTo(cost2)
            }
            SortOption.SHORTEST_DISTANCE -> d1.compareTo(d2)
            SortOption.HIGHEST_RATING -> r2.verifiedRating.compareTo(r1.verifiedRating)
        }
    }

    Column(modifier = modifier.fillMaxSize().testTag("search_results_screen")) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Meals for $groupSize under ₹${budget.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vasai • ${sortedList.size} Outlets Discovered",
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
                IconButton(onClick = { viewModel.showGoogleApiDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Google Places API Key",
                        tint = ForestGreenPrimary
                    )
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Best Budget Fit") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.BEST_BUDGET_FIT
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lowest Total Cost") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.LOWEST_PRICE
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shortest Distance") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.SHORTEST_DISTANCE
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Verified Rating") },
                            onClick = {
                                viewModel.sortOption.value = SortOption.HIGHEST_RATING
                                showSortMenu = false
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Summary & Parameter Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GreenContainer
            ) {
                Text(
                    text = "Budget: ₹${budget.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OrangeContainer
            ) {
                Text(
                    text = "$groupSize People",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmOrange,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Diet: $dietaryPref",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Mode: $diningMode",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Radius: ${radiusKm.toInt()}km",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            QuickFilterChip(
                text = "Dhabas Only 🏕️",
                isSelected = outletFilter == "Dhaba",
                onClick = {
                    viewModel.outletTypeFilter.value = if (outletFilter == "Dhaba") null else "Dhaba"
                }
            )

            QuickFilterChip(
                text = "Hotels & Dining 🏨",
                isSelected = outletFilter == "Family Restaurant",
                onClick = {
                    viewModel.outletTypeFilter.value = if (outletFilter == "Family Restaurant") null else "Family Restaurant"
                }
            )
        }

        // Compare Bar Indicator if items are selected
        if (compareIds.isNotEmpty()) {
            Surface(
                color = ForestGreenDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${compareIds.size} restaurants selected to compare",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { viewModel.navigateTo(Screen.COMPARE) },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compare Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Restaurant Results List
        if (sortedList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No restaurants match your filters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try increasing your travel radius, expanding budget, or clearing cuisine filters.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.selectedCuisine.value = null
                            viewModel.searchRadiusKm.value = 15.0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Expand Search Radius to 15km")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(sortedList) { restaurant ->
                    val distance = viewModel.repository.calculateDistanceKm(
                        viewModel.userLatitude.value,
                        viewModel.userLongitude.value,
                        restaurant.latitude,
                        restaurant.longitude
                    )
                    val matchResult = resultsMap[restaurant.id]
                    val isCompared = compareIds.contains(restaurant.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("result_card_${restaurant.id}"),
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
                                heightDp = 140
                            )

                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = restaurant.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${restaurant.cuisineTypes} • ${restaurant.locality}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "$distance km away",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                            // Smart Budget Meal Result Box
                            when (matchResult) {
                                is BudgetMatchResult.Success -> {
                                    val bestPlan = matchResult.bestFit
                                    Surface(
                                        color = GreenContainer.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "✓ QUALIFYING FAMILY MEAL",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = ForestGreenPrimary
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ForestGreenPrimary
                                                ) {
                                                    Text(
                                                        text = "+₹${bestPlan.remainingBudget.toInt()} Left In Budget",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = bestPlan.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = ForestGreenDark
                                            )

                                            // Dish preview items
                                            bestPlan.items.forEach { item ->
                                                Text(
                                                    text = "• ${item.quantity}x ${item.menuItem.name} (₹${item.lineTotal.toInt()})",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Bill Breakdown summary
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Estimated Total (incl. 5% GST):",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "₹${bestPlan.totalEstimatedBill.toInt()} for $groupSize people",
                                                        fontSize = 17.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = ForestGreenPrimary
                                                    )
                                                }
                                                Text(
                                                    text = "₹${bestPlan.perPersonCost.toInt()} / person",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                is BudgetMatchResult.BudgetInsufficient -> {
                                    Surface(
                                        color = OrangeContainer.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = WarmOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "BUDGET OVERRUN NOTICE",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = WarmOrange
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = matchResult.explanation,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                is BudgetMatchResult.NoMatchingDishes -> {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = matchResult.reason,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                null -> {
                                    // Calculating...
                                    Text(
                                        text = "Calculating best combo...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Compare Checkbox
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.toggleCompare(restaurant.id) }
                                ) {
                                    Checkbox(
                                        checked = isCompared,
                                        onCheckedChange = { viewModel.toggleCompare(restaurant.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Compare",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.selectRestaurant(restaurant.id, openBuilder = false) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("View Menu", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.selectRestaurant(restaurant.id, openBuilder = true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp).testTag("card_build_meal_${restaurant.id}")
                                    ) {
                                        Text("Build Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = {
                                            val gmmIntentUri = Uri.parse("geo:${restaurant.latitude},${restaurant.longitude}?q=${Uri.encode(restaurant.name + " " + restaurant.address)}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                viewModel.showToast("Opening map for ${restaurant.name}")
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Directions, contentDescription = "Directions", tint = ForestGreenPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
