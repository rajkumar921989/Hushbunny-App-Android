package com.hushbunny.app.uitls

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base for all ViewModels
 * Common ViewModel related implementations will be available here
 */
open class BaseViewModel : ViewModel() {
    private val apiJobTag: String = "CancellableAPI"
    lateinit var cancellableJob: CompletableJob
    init {
        if (!::cancellableJob.isInitialized) {
            initJob()
        }
    }
    private fun initJob() {
        cancellableJob = SupervisorJob()
        cancellableJob.invokeOnCompletion {
            it?.message.let {
            }
        }
    }
    fun resetJob() {
        if (cancellableJob.isActive || cancellableJob.complete()) {
            cancellableJob.cancel("resetting job")
        }
        initJob()
    }

    public override fun onCleared() {
        super.onCleared()
        cancellableJob.cancel()
    }
}
