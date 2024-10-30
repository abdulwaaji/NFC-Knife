package com.waajid.nfcknife.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


enum class NdefType(val displayName: String) {
    TEXT("Text"),
    URL("URL"),
    PHONE_NUMBER("Phone Number"),
    EMAIL("Email");

    override fun toString(): String {
        return displayName
    }
}
data class WriteType(val route: String, val label: NdefType)

@Composable
fun WriteTagScreen(navController: NavController, nfcViewModel: NfcViewModel) {
    val writeTypes = listOf(
        WriteType("writeType/${NdefType.TEXT.name}", NdefType.TEXT),
        WriteType("writeType/${NdefType.URL.name}", NdefType.URL),
        WriteType("writeType/${NdefType.PHONE_NUMBER.name}", NdefType.PHONE_NUMBER),
        WriteType("writeType/${NdefType.EMAIL.name}", NdefType.EMAIL)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        items(writeTypes) { type ->
            Button(
                onClick = { navController.navigate(type.route)
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .heightIn(min = 100.dp) // Set minimum height
            ) {
                Text(text = "Write ${type.label.displayName}")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WriteTagScreenPreview() {
    WriteTagScreen(navController = rememberNavController(), NfcViewModel())
}
