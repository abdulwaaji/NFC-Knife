package com.waajid.nfcknife.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController, nfcViewModel: NfcViewModel) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, nfcViewModel) }
        composable("writeTag") { WriteTagScreen(navController, nfcViewModel) }
        composable("readTag") { ReadTagScreen(navController, nfcViewModel) }

        composable("writeType/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            WriteTypeScreen(navController, nfcViewModel, type)
        }

        composable("tagInfoScreen?tagId={tagId}&tagMessage={tagMessage}") { backStackEntry ->
            val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
            val tagMessage = backStackEntry.arguments?.getString("tagMessage") ?: ""
            TagInfoScreen(tagId = tagId, tagMessages = tagMessage, navController = navController)
        }
    }
}
