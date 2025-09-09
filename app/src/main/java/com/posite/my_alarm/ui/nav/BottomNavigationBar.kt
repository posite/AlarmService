package com.posite.my_alarm.ui.nav

import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.posite.my_alarm.util.BottomNavItem

@Composable
fun BottomNavigationBar(navHostController: NavHostController) {

    val navigationTabList = listOf(BottomNavItem.Alarm, BottomNavItem.Timer)
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar {
        navigationTabList.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = item.drawable
                        ), contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (selectedItem == index) Color.Black else Color.Gray
                    )
                },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navHostController.navigate(item.screen.route) {
                        popUpTo(navHostController.graph.findStartDestination().id) {
                            saveState = true //  Pop할 때 상태를 저장합니다.
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = {
                    Text(
                        item.screen.route,
                        color = if (selectedItem == index) Color.Black else Color.Gray
                    )
                }
            )
        }
    }
}