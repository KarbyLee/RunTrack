package com.runtrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runtrack.data.local.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class TrackState(
    val data: TrackSnapshot = TrackSnapshot(), val loading: Boolean = true,
    val saving: Boolean = false, val error: String? = null, val loadFailed: Boolean = false
)

class TrackViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalTrackStore(application)
    private val mutation = Mutex()
    private val mutable = MutableStateFlow(TrackState())
    val state = mutable.asStateFlow()
    init { reload() }

    fun reload() = viewModelScope.launch {
        mutable.value = mutable.value.copy(loading = true, error = null)
        try { mutable.value = TrackState(data = store.load(), loading = false) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { mutable.value = mutable.value.copy(loading = false, loadFailed = true, error = error.message) }
    }

    fun clearError() { mutable.value = mutable.value.copy(error = null) }
    fun saveShoe(draft: ShoeDraft, id: String?, done: () -> Unit) = change(done) { data ->
        data.saveShoe(if (id == null) draft.toShoe() else draft.toShoe(id))
    }
    fun saveRun(draft: RunDraft, id: String?, done: () -> Unit) = change(done) { data ->
        data.saveRun(if (id == null) draft.toRun(data.shoes) else draft.toRun(data.shoes, id))
    }
    fun deleteShoe(id: String) = change { it.deleteShoe(id) }
    fun deleteRun(id: String) = change { it.deleteRun(id) }

    private fun change(done: () -> Unit = {}, transform: (TrackSnapshot) -> TrackSnapshot) {
        if (mutable.value.loading || mutable.value.loadFailed || mutable.value.saving) return
        mutable.value = mutable.value.copy(saving = true, error = null)
        viewModelScope.launch {
            mutation.withLock {
                try {
                    val next = transform(mutable.value.data)
                    store.save(next)
                    mutable.value = mutable.value.copy(data = next, saving = false)
                    done()
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { mutable.value = mutable.value.copy(saving = false, error = error.message ?: "저장하지 못했어요. 다시 시도해 주세요.") }
            }
        }
    }
}
