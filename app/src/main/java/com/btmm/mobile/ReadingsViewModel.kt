package com.btmm.mobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btmm.mobile.btmm.Assessment
import com.btmm.mobile.btmm.FormState
import com.btmm.mobile.btmm.Verdict
import com.btmm.mobile.btmm.assess
import com.btmm.mobile.btmm.parse
import com.btmm.mobile.btmm.storage.ReadingRepository
import com.btmm.mobile.btmm.storage.SavedReading
import com.btmm.mobile.btmm.toForm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingsViewModel(private val repository: ReadingRepository) : ViewModel() {

    var form by mutableStateOf(FormState())
        private set

    /** Id of the saved reading being edited, or null when the form is a new reading. */
    var editingId by mutableStateOf<Long?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    val readings: StateFlow<List<SavedReading>> =
        repository.readings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateForm(newForm: FormState) {
        form = newForm
        message = null
    }

    fun clearForm() {
        form = FormState()
        editingId = null
        message = null
    }

    fun startEditing(saved: SavedReading) {
        form = saved.reading.toForm()
        editingId = saved.id
        message = null
    }

    /** Saving is refused for an empty or invalid form; an incomplete one is allowed so it can be finished later. */
    fun canSave(): Boolean = form.assess().verdict.let { it != Verdict.NO_DATA && it != Verdict.INVALID }

    fun save() {
        if (!canSave()) return
        val reading = form.parse().reading
        val id = editingId
        viewModelScope.launch {
            if (id == null) {
                repository.add(reading)
                message = "Reading saved."
            } else if (repository.edit(id, reading)) {
                message = "Reading updated."
            } else {
                repository.add(reading)
                message = "The original reading was deleted, so this was saved as a new one."
            }
            form = FormState()
            editingId = null
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.remove(id)
            if (editingId == id) {
                form = FormState()
                editingId = null
            }
            message = null
        }
    }

    fun assessmentOf(saved: SavedReading): Assessment = saved.reading.toForm().assess()
}
