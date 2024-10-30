package com.waajid.nfcknife.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class NfcOperationType {
    READ, WRITE, ERASE
}

class NfcViewModel : ViewModel() {

    private val _operation = MutableLiveData<NfcOperationType?>()
    val operation: LiveData<NfcOperationType?> = _operation

    private val _writeDataType = MutableLiveData<NdefType?>()
    val writeDataType: LiveData<NdefType?> = _writeDataType

    private val _userInput = MutableLiveData<String?>()
    val userInput: LiveData<String?> = _userInput

    private val _writeType = MutableLiveData<WriteType?>()
    val writeType: LiveData<WriteType?> = _writeType

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _navigateToTagInfo = MutableLiveData<Pair<String, String>?>()
    val navigateToTagInfo: LiveData<Pair<String, String>?> = _navigateToTagInfo

    private val _status = mutableStateOf<Boolean?>(null)
    val status: State<Boolean?> = _status

    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    fun performOperation(type: NfcOperationType, input: String? = null, writeDataType: NdefType? = null) {
        _operation.value = type
        _userInput.value = input
        _writeDataType.value = writeDataType
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun onTagRead(tagId: String, tagMessage: String) {
        _navigateToTagInfo.value = Pair(tagId, tagMessage)
    }

    fun clearNavigationEvent() {
        _navigateToTagInfo.value = null
    }

    fun updateStatus(status: Boolean, message: String) {
        _status.value = status
        _message.value = message
    }

    fun resetStatus() {
        _status.value = null
        _message.value = null
    }
}

