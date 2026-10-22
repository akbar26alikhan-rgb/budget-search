package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.ui.MainViewModel
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.WarmOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    restaurants: List<RestaurantEntity>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.handleBack()
    }

    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Owner Claim state
    var claimOwnerName by remember { mutableStateOf("") }
    var claimPhone by remember { mutableStateOf("") }
    var selectedRestaurantForClaim by remember { mutableStateOf(restaurants.firstOrNull()) }
    var claimDropdownExpanded by remember { mutableStateOf(false) }

    // Price Update State
    var selectedRestaurantForPrice by remember { mutableStateOf(restaurants.firstOrNull()) }
    var priceDropdownExpanded by remember { mutableStateOf(false) }
    var menuItemsForSelectedRest by remember { mutableStateOf<List<MenuItemEntity>>(emptyList()) }
    var selectedItemForPrice by remember { mutableStateOf<MenuItemEntity?>(null) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var updatedPriceInput by remember { mutableStateOf("") }

    val claims by viewModel.repository.getAllClaims().collectAsState(initial = emptyList())
    val priceHistory by viewModel.repository.getAllPriceHistory().collectAsState(initial = emptyList())

    // Fetch items when selected restaurant changes
    remember(selectedRestaurantForPrice) {
        selectedRestaurantForPrice?.let { r ->
            coroutineScope.launch {
                val items = viewModel.repository.getMenuItemsDirect(r.id)
                menuItemsForSelectedRest = items
                selectedItemForPrice = items.firstOrNull()
                updatedPriceInput = items.firstOrNull()?.price?.toInt()?.toString() ?: ""
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().testTag("admin_screen")) {
        TopAppBar(
            title = {
                Text(text = "Admin & Restaurant Portal", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.handleBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ForestGreenPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Price Update", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Claim Outlet", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Audit & Setup", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Menu Price Management & In-Stock Updates
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Update Menu Item Price",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Maintain price accuracy for Vasai diners. Updates are recorded in price history.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Select Restaurant Dropdown
                                Text("Select Restaurant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = priceDropdownExpanded,
                                    onExpandedChange = { priceDropdownExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedRestaurantForPrice?.name ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priceDropdownExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = priceDropdownExpanded,
                                        onDismissRequest = { priceDropdownExpanded = false }
                                    ) {
                                        restaurants.forEach { r ->
                                            DropdownMenuItem(
                                                text = { Text(r.name) },
                                                onClick = {
                                                    selectedRestaurantForPrice = r
                                                    priceDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Select Dish Dropdown
                                if (menuItemsForSelectedRest.isNotEmpty()) {
                                    Text("Select Dish to Update", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = itemDropdownExpanded,
                                        onExpandedChange = { itemDropdownExpanded = it }
                                    ) {
                                        OutlinedTextField(
                                            value = "${selectedItemForPrice?.name} (Current: ₹${selectedItemForPrice?.price?.toInt()})",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = itemDropdownExpanded,
                                            onDismissRequest = { itemDropdownExpanded = false }
                                        ) {
                                            menuItemsForSelectedRest.forEach { item ->
                                                DropdownMenuItem(
                                                    text = { Text("${item.name} - ₹${item.price.toInt()}") },
                                                    onClick = {
                                                        selectedItemForPrice = item
                                                        updatedPriceInput = item.price.toInt().toString()
                                                        itemDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // New Price Input
                                    Text("New Verified Price (₹)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = updatedPriceInput,
                                        onValueChange = { updatedPriceInput = it.filter { char -> char.isDigit() } },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("admin_new_price_input")
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val newPrice = updatedPriceInput.toDoubleOrNull()
                                            val dish = selectedItemForPrice
                                            if (newPrice != null && dish != null) {
                                                coroutineScope.launch {
                                                    viewModel.repository.updateMenuItemPrice(
                                                        itemId = dish.id,
                                                        dishName = dish.name,
                                                        previousPrice = dish.price,
                                                        newPrice = newPrice,
                                                        source = "Vasai Restaurant Owner / Admin Verified"
                                                    )
                                                    viewModel.showToast("Updated ${dish.name} price to ₹${newPrice.toInt()}")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("admin_save_price_btn")
                                    ) {
                                        Text("Save Verified Price", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Price History Log
                    item {
                        Text(
                            text = "Recent Price Audit Trail (${priceHistory.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (priceHistory.isEmpty()) {
                        item {
                            Text("No price changes logged yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(priceHistory.take(5)) { log ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = log.dishName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "Updated: ₹${log.previousPrice.toInt()} ➔ ₹${log.newPrice.toInt()} (${log.source})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = log.verifiedAt, fontSize = 11.sp, color = ForestGreenPrimary)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Claim Restaurant Outlet
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Vasai Restaurant Owner Onboarding",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Are you the proprietor of a food outlet in Vasai? Claim your listing to verify menus directly.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Select Restaurant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = claimDropdownExpanded,
                                    onExpandedChange = { claimDropdownExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedRestaurantForClaim?.name ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = claimDropdownExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = claimDropdownExpanded,
                                        onDismissRequest = { claimDropdownExpanded = false }
                                    ) {
                                        restaurants.forEach { r ->
                                            DropdownMenuItem(
                                                text = { Text(r.name) },
                                                onClick = {
                                                    selectedRestaurantForClaim = r
                                                    claimDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Owner / Manager Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = claimOwnerName,
                                    onValueChange = { claimOwnerName = it },
                                    placeholder = { Text("e.g. Ramesh Patel") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Contact Phone (WhatsApp / SMS)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = claimPhone,
                                    onValueChange = { claimPhone = it },
                                    placeholder = { Text("+91 98200 12345") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val rest = selectedRestaurantForClaim
                                        if (rest != null && claimOwnerName.isNotBlank() && claimPhone.isNotBlank()) {
                                            coroutineScope.launch {
                                                viewModel.repository.submitRestaurantClaim(
                                                    restaurantId = rest.id,
                                                    restaurantName = rest.name,
                                                    ownerName = claimOwnerName,
                                                    phone = claimPhone
                                                )
                                                viewModel.showToast("Claim submitted for review! We will verify via SMS/WhatsApp.")
                                                claimOwnerName = ""
                                                claimPhone = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text("Submit Ownership Claim", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Existing claims
                    item {
                        Text("Submitted Claims (${claims.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (claims.isEmpty()) {
                        item {
                            Text("No pending claims yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(claims) { claim ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(claim.restaurantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Claimant: ${claim.ownerName} (${claim.ownerPhone})", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GreenContainer
                                    ) {
                                        Text(
                                            text = claim.claimStatus,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Audit, API Integrations & Demo Mode Disclosures
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = OrangeContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = WarmOrange)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("DATA VERIFICATION & DEMO MODE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenDark)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All restaurants in this app correspond to authentic localities in Vasai (Vasai West, Station Road, Ambadi Rd, Babhola, Evershine, Papdy). Listings marked 'VERIFIED MENU' have had their 2026 pricing audited. Listings marked 'DEMO DATA' have estimated menus pending physical audit.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("External API Integration Status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Local Room Database: ACTIVE (Offline Ready)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Smart Knapsack Budget Solver: ACTIVE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google Places API / Supabase: Standby (Keys optional in .env)", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.showGoogleApiDialog.value = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Configure Google Places API Key", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "To connect live Supabase or Google Places credentials, tap the button above or provide GOOGLE_PLACES_API_KEY. The application runs autonomously with verified offline Vasai listings if no key is entered.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
