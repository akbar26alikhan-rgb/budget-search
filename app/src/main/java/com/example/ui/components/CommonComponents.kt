package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.OnOrangeContainer
import com.example.ui.theme.OrangeContainer
import com.example.ui.theme.StarGold
import com.example.ui.theme.VegGreen
import com.example.ui.theme.WarmOrange

@Composable
fun DietaryBadge(dietaryType: String, modifier: Modifier = Modifier) {
    val isVeg = dietaryType == "VEG"
    val color = if (isVeg) VegGreen else NonVegRed
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .border(1.5.dp, color, RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isVeg) "VEG" else "NON-VEG",
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun VerifiedBadge(status: String, modifier: Modifier = Modifier) {
    val isVerified = status == "VERIFIED"
    Surface(
        color = if (isVerified) GreenContainer else OrangeContainer,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Status",
                tint = if (isVerified) OnGreenContainer else OnOrangeContainer,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isVerified) "VERIFIED" else "DEMO",
                color = if (isVerified) OnGreenContainer else OnOrangeContainer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun RatingPill(rating: Double, reviewCount: Int, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFFFF9E6),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = StarGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = String.format("%.1f", rating),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF5D4037)
            )
            if (reviewCount > 0) {
                Text(
                    text = " ($reviewCount)",
                    fontSize = 11.sp,
                    color = Color(0xFF8D6E63)
                )
            }
        }
    }
}

@Composable
fun QuickFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("filter_chip_$text")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

/**
 * Beautiful image banner for restaurants with gradient scrim, rating pill, and tags.
 */
@Composable
fun RestaurantImageBanner(
    imageUrl: String?,
    name: String,
    outletType: String,
    rating: Double,
    ratingCount: Int,
    verificationStatus: String,
    heightDp: Int = 140,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(ForestGreenDark, ForestGreenPrimary)
                )
            )
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant placeholder food pattern
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ForestGreenDark, WarmOrange.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Top gradient scrim for status badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        // Bottom gradient scrim for high contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // Top Badges (Outlet type & Verification)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = outletType,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            VerifiedBadge(status = verificationStatus)
        }

        // Bottom Rating Pill floating on image
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 8.dp)
        ) {
            RatingPill(rating = rating, reviewCount = ratingCount)
        }
    }
}

/**
 * Dashed or receipt style divider for meal breakdowns.
 */
@Composable
fun ReceiptDashedDivider(modifier: Modifier = Modifier, color: Color = Color(0xFFD7CCC8)) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/**
 * Budget Progress Visualizer showing amount used vs limit.
 */
@Composable
fun BudgetVisualizerBar(
    currentAmount: Double,
    maxBudget: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (maxBudget > 0) (currentAmount / maxBudget).coerceIn(0.0, 1.2).toFloat() else 0f
    val barColor = when {
        percentage > 1.0f -> AlertRed
        percentage > 0.85f -> WarmOrange
        else -> ForestGreenPrimary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Utilization: ${(percentage * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val diff = maxBudget - currentAmount
            Text(
                text = if (diff >= 0) "₹${diff.toInt()} remaining" else "₹${(-diff).toInt()} over budget",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (diff >= 0) ForestGreenPrimary else AlertRed
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage).coerceAtMost(1.0f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}
