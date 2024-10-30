package com.waajid.nfcknife.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WriteTypeScreen(navController: NavController, nfcViewModel: NfcViewModel, type: String?) {
    var inputText by remember { mutableStateOf("") }
    val writeType = type?.let { NdefType.valueOf(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(text = "Enter $type") },
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = {
                // Implement writing logic here
                nfcViewModel.performOperation(NfcOperationType.WRITE, inputText, writeType)
                navController.popBackStack() // Navigate back to the main screen or the appropriate screen
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Write $type")
        }
    }
}
