package com.waajid.nfcknife

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.waajid.nfcknife.ui.NdefType
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale

fun eraseNdefMessages(tag: Tag): Boolean {
    try {
        val ndef = Ndef.get(tag)

        if (ndef != null) {
            // Connect to the tag
            ndef.connect()
            if (!ndef.isWritable) {
                // If the tag is not writable, return false
                ndef.close()
                return false
            }

            // Create a truly empty NDEF message (no records)
            val emptyMessage = NdefMessage(
                arrayOf(
                    NdefRecord(
                        NdefRecord.TNF_EMPTY,
                        byteArrayOf(),
                        byteArrayOf(),
                        byteArrayOf()
                    )
                )
            )

            // Write the empty message to the tag
            ndef.writeNdefMessage(emptyMessage)

            // Ensure the erase is complete by filling the tag with an empty message
            ndef.close()
            return true
        } else {
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                // If the tag is not NDEF formatted, try formatting it
                ndefFormatable.connect()
                // Write an empty NDEF message during formatting
                ndefFormatable.format(
                    NdefMessage(
                        arrayOf(
                            NdefRecord(
                                NdefRecord.TNF_EMPTY,
                                byteArrayOf(),
                                byteArrayOf(),
                                byteArrayOf()
                            )
                        )
                    )
                )
                ndefFormatable.close()
                return true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun ByteArray.toHexString(): String {
    return joinToString(separator = ":") { byte -> "%02X".format(byte) }
}


fun createNdefRecord(data: String, type: NdefType): NdefRecord {
    return when (type) {
        NdefType.TEXT -> createTextRecord(data)
        NdefType.URL -> createUrlRecord(data)
        NdefType.EMAIL -> createEmailRecord(data)
        NdefType.PHONE_NUMBER -> createPhoneRecord(data)
        else -> throw IllegalArgumentException("Unsupported NDEF type")
    }
}


fun createTextRecord(text: String): NdefRecord {
    // Create a new NdefRecord for a text message
    val languageCode = Locale.getDefault().language // Get the language code (e.g., "en")
    val textBytes = text.toByteArray(Charset.forName("UTF-8")) // Convert text to bytes
    val languageCodeBytes = languageCode.toByteArray(Charset.forName("US-ASCII"))

    // Payload is language code length, language code, and text
    val payload = ByteArray(1 + languageCodeBytes.size + textBytes.size)

    payload[0] = languageCodeBytes.size.toByte() // Set the language code length
    System.arraycopy(languageCodeBytes, 0, payload, 1, languageCodeBytes.size) // Copy language code
    System.arraycopy(textBytes, 0, payload, 1 + languageCodeBytes.size, textBytes.size) // Copy text

    // Create the NdefRecord for a well-known text type (TNF_WELL_KNOWN and RTD_TEXT)
    return NdefRecord(
        NdefRecord.TNF_WELL_KNOWN,  // Type Name Format: Well-known
        NdefRecord.RTD_TEXT,        // Record Type Definition for text
        ByteArray(0),               // No ID for this record
        payload                     // The actual payload (language code + text)
    )
}

// Helper function to create a URL record
private fun createUrlRecord(url: String): NdefRecord {
    val urlBytes = url.toByteArray(Charset.forName("UTF-8"))

    return NdefRecord(
        NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_URI,
        ByteArray(0),
        urlBytes
    )
}

// Helper function to create an email record
private fun createEmailRecord(email: String): NdefRecord {
    val emailUri = "mailto:$email"
    val emailBytes = emailUri.toByteArray(Charset.forName("UTF-8"))

    return NdefRecord(
        NdefRecord.TNF_ABSOLUTE_URI,
        emailBytes,
        ByteArray(0),
        ByteArray(0)
    )
}

// Helper function to create a phone number record
private fun createPhoneRecord(phoneNumber: String): NdefRecord {
    val phoneUri = "tel:$phoneNumber"
    val phoneBytes = phoneUri.toByteArray(Charset.forName("UTF-8"))

    return NdefRecord(
        NdefRecord.TNF_ABSOLUTE_URI,
        phoneBytes,
        ByteArray(0),
        ByteArray(0)
    )
}

fun getTextFromNdefRecord(ndefRecord: NdefRecord): String {
    // Get the payload from the NdefRecord
    val payload = ndefRecord.payload

    // Check if the payload is empty
    if (payload.isEmpty()) {
        return "No data found" // Return an appropriate message if there's no data
    }

    // Check the type of NdefRecord (TNF and type) to determine the format
    return when {
        // Well-known text record (TNF_WELL_KNOWN and RTD_TEXT)
        ndefRecord.tnf == NdefRecord.TNF_WELL_KNOWN && ndefRecord.type.contentEquals(NdefRecord.RTD_TEXT) -> {
            extractTextFromPayload(payload)
        }
        // Well-known URI record (TNF_WELL_KNOWN and RTD_URI)
        ndefRecord.tnf == NdefRecord.TNF_WELL_KNOWN && ndefRecord.type.contentEquals(NdefRecord.RTD_URI) -> {
            extractUriAsString(payload)
        }
        // Absolute URI record for email and phone (TNF_ABSOLUTE_URI)
        ndefRecord.tnf == NdefRecord.TNF_ABSOLUTE_URI -> {
            extractUriAsString(payload)
        }

        else -> "Unknown NDEF record type" // Handle unsupported record types
    }
}

// Helper function to extract text from a text NDEF record
private fun extractTextFromPayload(payload: ByteArray): String {
    // The first byte of the payload contains the length of the language code
    val languageCodeLength = payload[0].toInt() and 0x3F

    // Make sure the payload has enough data to read the text
    return if (payload.size > 1 + languageCodeLength) {
        String(
            payload,
            1 + languageCodeLength, // Offset to start reading the text (skip the language code)
            payload.size - 1 - languageCodeLength, // Length of the text part
            Charset.forName("UTF-8") // Encoding is UTF-8
        )
    } else {
        "Invalid NDEF text data"
    }
}

// Helper function to extract a URI (URL) from a URI NDEF record
private fun extractUriFromPayload(payload: ByteArray): String {
    // The first byte of the payload contains the URI prefix (scheme)
    val uriPrefixCode = payload[0].toInt()
    val uriPrefix =
        uriPrefixMap[uriPrefixCode] ?: "" // Use a map of URI prefix codes (e.g., "http://www.")

    // Remaining bytes are the URI string
    return uriPrefix + String(payload, 1, payload.size - 1, Charset.forName("UTF-8"))
}

// Helper function to extract a URI (for email and phone) from an absolute URI NDEF record
private fun extractUriAsString(type: ByteArray): String {
    return String(
        type,
        Charset.forName("UTF-8")
    ) // Simply convert the type to string (URI for email and phone)
}

// Example map for URI prefixes (scheme shortcuts)
val uriPrefixMap = mapOf(
    0x00 to "", // No prefix
    0x01 to "http://www.",
    0x02 to "https://www.",
    0x03 to "http://",
    0x04 to "https://"
    // Add more URI prefixes as needed
)
