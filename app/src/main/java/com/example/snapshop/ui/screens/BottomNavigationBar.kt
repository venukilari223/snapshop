package com.example.snapshop.ui.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.snapshop.ui.navigation.Destinations
import com.example.snapshop.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    // Elevation transition effect for a modern sleek look
    val elevation by animateDpAsState(
        targetValue = if (currentDestination == Destinations.HOME) 10.dp else 4.dp,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    val gradientColors = listOf(Color(0xFFE35B30), Color(0xFFFC825A)) // Customizable gradient
    val backgroundBrush = Brush.horizontalGradient(colors = gradientColors)

    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = elevation,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(brush = backgroundBrush)
    ) {


        NavigationItem(
            iconRes = R.drawable.home,
            label = "Home",
            isSelected = currentDestination == Destinations.HOME,
            onClick = { navController.navigateIfNotCurrent(Destinations.HOME) }
        )
        NavigationItem(
            iconRes = R.drawable.compare,
            label = "Compare",
            isSelected = currentDestination == Destinations.COMPARE,
            onClick = { navController.navigateIfNotCurrent(Destinations.COMPARE) }
        )

        NavigationItem(
            iconRes = R.drawable.shoppingcart1,
            label = "Cart",
            isSelected = currentDestination == Destinations.CART,
            onClick = { navController.navigateIfNotCurrent(Destinations.CART) }
        )


        NavigationItem(
            iconRes = R.drawable.person,
            label = "Profile",
            isSelected = currentDestination == Destinations.PROFILE,
            onClick = { navController.navigateIfNotCurrent(Destinations.PROFILE) }
        )
    }
}

@Composable
fun RowScope.NavigationItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = Color.White
    val unselectedColor = Color(0xFFFCDEDA)
    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 28.dp,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    NavigationBarItem(
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) selectedColor else unselectedColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.height(4.dp))  // Space between icon and text
                Text(
                    text = label,
                    color = if (isSelected) selectedColor else unselectedColor,
                    style = TextStyle(
                        fontSize = 7.sp,  // Further reduced font size to avoid wrapping
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Light,
                        letterSpacing = 0.7.sp // Adjusted letter-spacing
                    )
                )
            }
        },
        selected = isSelected,
        onClick = { onClick() },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            unselectedIconColor = unselectedColor,
            selectedTextColor = selectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = Color.Transparent
        )
    )
}

fun NavController.navigateIfNotCurrent(destination: String) {
    if (currentBackStackEntry?.destination?.route != destination) {
        navigate(destination)
    }
}