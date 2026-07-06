package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AlAtheerViewModel
import com.example.ui.AlAtheerViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.AlAtheerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AlAtheerViewModel by viewModels {
        AlAtheerViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            AlAtheerTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // 1. Dashboard (Home)
                        composable("home") {
                            MainDashboardScreen(
                                viewModel = viewModel,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        // 2. Groups List
                        composable("groups") {
                            GroupsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditGroup = { groupId -> navController.navigate("edit_group/$groupId") }
                            )
                        }

                        // 3. Edit Group details
                        composable(
                            route = "edit_group/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
                            EditGroupScreen(
                                groupId = groupId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 4. Students List
                        composable("students") {
                            StudentsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onViewProfile = { studentId -> navController.navigate("student_profile/$studentId") }
                            )
                        }

                        // 5. Student Profile
                        composable(
                            route = "student_profile/{studentId}",
                            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
                            StudentProfileScreen(
                                studentId = studentId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Attendance marking
                        composable("attendance") {
                            AttendanceScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Payments tracking
                        composable("payments") {
                            PaymentsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Reports Screen
                        composable("reports") {
                            ReportsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 9. Settings Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
