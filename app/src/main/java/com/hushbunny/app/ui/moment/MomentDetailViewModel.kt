package com.hushbunny.app.ui.moment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.MomentDetailResponseModel
import com.hushbunny.app.ui.model.ShareMomentRequest
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import kotlinx.coroutines.*

class MomentDetailViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val momentRepository: MomentRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)


    private val _momentDetailResponse: MutableLiveData<MomentDetailResponseModel> = MutableLiveData()
    val momentDetailObserver: LiveData<MomentDetailResponseModel> = _momentDetailResponse


    fun getMomentID(momentId: String) {
        ioScope.launch {
            val queryParams = hashMapOf<String, Any>()
            queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = momentId
            _momentDetailResponse.postValue(momentRepository.getMomentDetail(queryParams))
        }
    }
    fun shareMoment(momentId: String) {
        ioScope.launch {
            momentRepository.shareMoment(ShareMomentRequest(momentId = momentId))
        }
    }
}