package com.parkpulse.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.parkpulse.app.presentation.theme.ParksColors

enum class BottomTab(val label: String, val icon: ImageVector) {
    LIST("List", Icons.AutoMirrored.Filled.List),
    MAP("Map", Icons.Filled.Place)
}

@Composable
fun ParkPulseBottomBar(
    selected: BottomTab,
    onListClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier, containerColor = ParksColors.White, tonalElevation = 0.dp) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = {
                    if (tab != selected) {
                        if (tab == BottomTab.LIST) onListClick() else onMapClick()
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ParksColors.DarkGreen,
                    selectedTextColor = ParksColors.DarkGreen,
                    indicatorColor = ParksColors.PrimaryGreen,
                    unselectedIconColor = ParksColors.SecondaryGreen,
                    unselectedTextColor = ParksColors.SecondaryGreen
                )
            )
        }
    }
}