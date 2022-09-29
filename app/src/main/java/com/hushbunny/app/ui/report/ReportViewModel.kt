package com.hushbunny.app.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.ui.enumclass.ReportType
import com.hushbunny.app.ui.model.AddReportRequest
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReportViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val momentRepository: MomentRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _reportResponse: MutableLiveData<ReportReasonInfo> = MutableLiveData()
    val reportObserver: LiveData<ReportReasonInfo> = _reportResponse

    private val _createReportResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val createReportObserver: LiveData<BaseResponse> = _createReportResponse

    fun getReportReason(type: String, commentId: String = "", momentId: String = "", userId: String = "") {
        ioScope.launch {
            val queryParams = hashMapOf<String, Any>()
            queryParams[APIConstants.QUERY_PARAMS_TYPE] = type
            if (commentId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_COMMENT_ID] = commentId
            if (momentId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = momentId
            if (userId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_USER_ID] = userId
            _reportResponse.postValue(momentRepository.getReportReasonList(queryParams))
        }
    }

    fun postReport(
        type: String,
        reasonId: String,
        commentId: String? = null,
        reason: String? = null,
        momentId: String? = null,
        userId: String? = null
    ) {
        ioScope.launch {
            val reportRequest =
                AddReportRequest(type = type, reasonId = reasonId, commentId = commentId?.ifEmpty { null }, reason = reason, momentId = momentId?.ifEmpty { null }, userId = userId?.ifEmpty { null })
            _createReportResponse.postValue(momentRepository.postReport(request = reportRequest))
        }
    }

}