package com.waajid.nfcknife.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ReadTagScreen(navController: NavController, nfcViewModel: NfcViewModel) {

    nfcViewModel.performOperation(NfcOperationType.READ)
    navController.popBackStack()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagInfoScreen(tagId: String, tagMessages: String, navController: NavController) {

    var tagIdValue by remember { mutableStateOf(TextFieldValue(tagId)) }
    var tagMessagesValue by remember { mutableStateOf(TextFieldValue(tagMessages)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card view for Tag ID
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tag ID",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                TextField(
                    value = tagIdValue,
                    onValueChange = { tagIdValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        disabledTextColor = Color.Black
                    ),
                    enabled = false // Tag ID is usually non-editable
                )
            }
        }

        // Card view for Tag Messages
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // This makes the card take up the remaining space
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fill the card space
                    .padding(16.dp)
            ) {
                Text(
                    text = "Message",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                TextField(
                    value = tagMessagesValue,
                    onValueChange = { tagMessagesValue = it },
                    modifier = Modifier
                        .fillMaxSize(), // Fill the remaining space in the card
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        disabledTextColor = Color.Black
                    ),
                    enabled = false // Tag message is also non-editable
                )
            }
        }
    }

    BackHandler {
        navController.navigate("main") {
            popUpTo("main") { inclusive = true }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagInfoScreenPreview() {
    TagInfoScreen(
        tagId = "4892389349",
        tagMessages = "This is the message from the tag",
        navController = rememberNavController()
    )
}
