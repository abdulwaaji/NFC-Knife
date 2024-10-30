package com.waajid.nfcknife

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.waajid.nfcknife.ui.AppNavGraph
import com.waajid.nfcknife.ui.ImageTextDialog
import com.waajid.nfcknife.ui.NdefType
import com.waajid.nfcknife.ui.NfcOperationType
import com.waajid.nfcknife.ui.NfcViewModel
import com.waajid.nfcknife.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private val nfcViewModel: NfcViewModel by viewModels()
    private lateinit var operationType: NfcOperationType

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            val isLoading by nfcViewModel.isLoading.observeAsState(false)
            val navigateToTagInfo by nfcViewModel.navigateToTagInfo.observeAsState()
            val status by nfcViewModel.status
            val message by nfcViewModel.message

            MaterialTheme {
                MyAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController, nfcViewModel)

                        if (isLoading) {
                            LoadingDialog(isLoading = isLoading)
                        }

                        LaunchedEffect(navigateToTagInfo) {
                            navigateToTagInfo?.let { (tagId, tagMessage) ->
                                navController.navigate("tagInfoScreen?tagId=$tagId&tagMessage=$tagMessage")
                                nfcViewModel.clearNavigationEvent() // Clear the event after navigation
                            }
                        }

                        status?.let {
                            ShowStatus(
                                status = it,
                                message = message ?: "",
                                onDismiss = {
                                    nfcViewModel.resetStatus()
                                }
                            )
                        }
                    }
                }
            }


        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if the device supports NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
        } else {
            if (nfcAdapter?.isEnabled == false) {
                Toast.makeText(this, "NFC is disabled", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                startActivity(intent)
            }
        }

        nfcViewModel.operation.observe(this) { operation ->
            operation.let {
                if (it != null) {
                    operationType = it
                }

                nfcViewModel.setLoading(true)
            }
        }

        operationType = NfcOperationType.READ // setting read as default operation mode

    }

    private fun handleReadOperation(tag: Tag?) {
        // Handle the NFC read operation
        nfcViewModel.setLoading(true)
        if (tag != null) {
            readNdefData(tag)
        }
        nfcViewModel.setLoading(false)
    }

    private fun handleWriteOperation(writeDataType: NdefType?, userInput: String?, tag: Tag?) {
        nfcViewModel.setLoading(true)
        var status = false
        if (userInput != null && tag != null && writeDataType != null) {
            status = writeNdefData(userInput, writeDataType, tag)
        }
        nfcViewModel.setLoading(false)
        nfcViewModel.updateStatus(status, "Data Write " + if (status) "Success" else "Failed")
        nfcViewModel.clearNavigationEvent()
    }

    private fun readNdefData(tag: Tag) {
        val ndef = Ndef.get(tag)
        var tagId = ""
        var tagMessage = ""

        Log.e("NFC", "ndef is null: ${ndef == null}")

        ndef?.let {
            try {
                it.connect()

                // Convert tag id to hex string
                tagId = tag.id.toHexString()

                // Process NDEF message if available
                it.ndefMessage?.records?.forEach { record ->
                    val text = getTextFromNdefRecord(record)
                    tagMessage += text
                    Log.e("NFC", "Read content: $text")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Ensure NDEF tag is closed after use
                try {
                    it.close()
                } catch (closeException: Exception) {
                    closeException.printStackTrace()
                }
            }
        } ?: run {
            tagMessage = "No Data found"
        }

        // Get NavController from LocalNavController and navigate
        nfcViewModel.onTagRead(tagId, tagMessage)
        nfcViewModel.setLoading(false)
    }

    private fun handleEraseOperation(tag: Tag?) {
        // Handle the NFC erase operation
        var status = false
        nfcViewModel.setLoading(true)
        if (tag != null) {
            status = eraseNdefMessages(tag)
        }
        nfcViewModel.setLoading(false)

        nfcViewModel.updateStatus(status, if (status) "Data Erase Success" else "Data Erase Failed")

    }

    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    private fun enableForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            if (it.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                val tag: Tag? = it.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.let {
                    Toast.makeText(
                        this, "NFC Tag Detected!\nTag ID: ${tag.id}", Toast.LENGTH_SHORT
                    ).show()
                }

                when (operationType) {
                    NfcOperationType.READ -> handleReadOperation(tag)
                    NfcOperationType.WRITE -> handleWriteOperation(nfcViewModel.writeDataType.value, nfcViewModel.userInput.value, tag)
                    NfcOperationType.ERASE -> handleEraseOperation(tag)
                }
            }


        }

    }


    @Composable
    fun LoadingDialog(isLoading: Boolean) {
        if (isLoading) {
            ImageTextDialog(
                onDismiss = { nfcViewModel.setLoading(false) },
                imageResId = R.drawable.tap_pay,
                message = "Please Tap the NFC card to the back side of your phone"
            )
        }
    }

    @Composable
    fun ShowStatus(status: Boolean, message: String, onDismiss: () -> Unit) {
        if (status) {
            ImageTextDialog(
                onDismiss = onDismiss,
                imageResId = R.drawable.success,
                message = message
            )
        } else {
            ImageTextDialog(
                onDismiss = onDismiss,
                imageResId = R.drawable.error,
                message = message
            )
        }
    }


    fun writeNdefData(userInput: String, writeDataType: NdefType, tag: Tag): Boolean {
        try {
            // Get an instance of Ndef from the tag
            val ndef = Ndef.get(tag)

            // Create an NdefRecord for text
            val ndefRecord = createNdefRecord(userInput, writeDataType)


            // Create an NdefMessage containing the NdefRecord
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))

            // Check if there's enough space on the tag
            if (ndef.maxSize < ndefMessage.toByteArray().size) {
                ndef.close()
                return false // Not enough space on the tag
            }

            if (ndef != null) {
                // Connect to the NFC tag
                ndef.connect()

                if (!ndef.isWritable) {
                    // If the tag is not writable, return false
                    ndef.close()
                    return false
                }

                // Write an empty NDEF message to erase the data
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                return true // Successfully erased
            } else {

                // If the tag is not NDEF formatted, try formatting it
                val ndefFormatable = NdefFormatable.get(tag)

                if (ndefFormatable != null) {
                    // Connect and format the tag
                    ndefFormatable.connect()
                    ndefFormatable.format(ndefMessage) // Write an empty message during format
                    ndefFormatable.close()
                    return true // Successfully erased
                }
            }

            return true // Write successful
        } catch (e: Exception) {
            e.printStackTrace()
            return false // Write failed
        }
    }


}