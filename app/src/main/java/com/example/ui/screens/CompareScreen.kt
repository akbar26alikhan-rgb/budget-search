package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Stars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RestaurantEntity
import com.example.engine.BudgetMatchResult
import com.example.engine.SmartBudgetEngine
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.RatingPill
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: MainViewModel,
    allRestaurants: List<RestaurantEntity>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val compareIds by viewModel.compareRestaurantIds.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val budget by viewModel.totalBudget.collectAsState()
    val dietaryPref by viewModel.dietaryPreference.collectAsState()
    val diningMode by viewModel.diningMode.collectAsState()

    val comparedRestaurants = allRestaurants.filter { compareIds.contains(it.id) }

    val comparisonResults = remember { mutableStateMapOf<String, BudgetMatchResult>() }

    LaunchedEffect(comparedRestaurants, budget, groupSize, dietaryPref, diningMode) {
        comparedRestaurants.forEach { r ->
            val menu = viewModel.repository.getMenuItemsDirect(r.id)
            val res = SmartBudgetEngine.calculateMatchesForRestaurant(
                restaurant = r,
                menuItems = menu,
                groupSize = groupSize,
                totalBudget = budget,
                dietaryPreference = dietaryPref,
                diningMode = diningMode
            )
            comparisonResults[r.id] = res
        }
    }

    Column(modifier = modifier.fillMaxSize().testTag("compare_screen")) {
        TopAppBar(
            title = {
                Text(
                    text = "Compare Vasai Outlets (${comparedRestaurants.size}/5)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.handleBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (comparedRestaurants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(54.dp), tint = ForestGreenPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No restaurants selected to compare", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select up to 5 restaurants from the search results to compare prices, portions, and meal combos side-by-side.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.SEARCH_RESULTS) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Go to Search Results")
                    }
                }
            }
        } else {
            // Find highlights: Cheapest, Closest, Highest rated
            val distances = comparedRestaurants.associate { r ->
                r.id to viewModel.repository.calculateDistanceKm(viewModel.userLatitude.value, viewModel.userLongitude.value, r.latitude, r.longitude)
            }
            val closestId = distances.minByOrNull { it.value }?.key
            val highestRatedId = comparedRestaurants.maxByOrNull { it.verifiedRating }?.id
            val cheapestId = comparisonResults.entries
                .filter { it.value is BudgetMatchResult.Success }
                .minByOrNull { (it.value as BudgetMatchResult.Success).bestFit.totalEstimatedBill }
                ?.key

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Highlights banner
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("QUICK HIGHLIGHTS FOR $groupSize PEOPLE:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ForestGreenPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (cheapestId != null) {
                                val cheapRest = comparedRestaurants.firstOrNull { it.id == cheapestId }
                                Text("• 💰 Cheapest Option: ${cheapRest?.name}", fontSize = 12.sp, color = ForestGreenDark)
                            }
                            if (closestId != null) {
                                val closeRest = comparedRestaurants.firstOrNull { it.id == closestId }
                                Text("• 📍 Closest to Vasai Stn: ${closeRest?.name} (${distances[closestId]} km)", fontSize = 12.sp, color = ForestGreenDark)
                            }
                            if (highestRatedId != null) {
                                val topRest = comparedRestaurants.firstOrNull { it.id == highestRatedId }
                                Text("• ⭐ Highest Verified Rating: ${topRest?.name} (${topRest?.verifiedRating} ★)", fontSize = 12.sp, color = ForestGreenDark)
                            }
                        }
                    }
                }

                // Horizontal scrollable side-by-side comparison cards
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        comparedRestaurants.forEach { r ->
                            val match = comparisonResults[r.id]
                            val dist = distances[r.id] ?: 0.0

                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .testTag("compare_col_${r.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = r.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.toggleCompare(r.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${r.locality} • $dist km away",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        RatingPill(rating = r.verifiedRating, reviewCount = r.ratingCount)
                                        VerifiedBadge(status = r.verificationStatus)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Metrics
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            when (match) {
                                                is BudgetMatchResult.Success -> {
                                                    val plan = match.bestFit
                                                    Text(
                                                        text = "Total: ₹${plan.totalEstimatedBill.toInt()}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 16.sp,
                                                        color = ForestGreenPrimary
                                                    )
                                                    Text(
                                                        text = "₹${plan.perPersonCost.toInt()} / person",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "+₹${plan.remainingBudget.toInt()} Left in Budget",
                                                        fontSize = 11.sp,
                                                        color = ForestGreenPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Dishes: ${plan.items.size} varieties (~${plan.totalServings} servings)",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                is BudgetMatchResult.BudgetInsufficient -> {
                                                    Text(
                                                        text = "Min Cost: ₹${match.minimumTotal.toInt()}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = WarmOrange
                                                    )
                                                    Text(
                                                        text = "Exceeds by ₹${match.shortfall.toInt()}",
                                                        fontSize = 11.sp,
                                                        color = WarmOrange
                                                    )
                                                }
                                                else -> {
                                                    Text("Analyzing...", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Taxes & Extra Charges:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "GST: ${r.gstPercentage.toInt()}% • Packaging: ₹${r.packagingFee.toInt()}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.selectRestaurant(r.id, openBuilder = true)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("Build Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
