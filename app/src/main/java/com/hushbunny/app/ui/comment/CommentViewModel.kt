package com.hushbunny.app.ui.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.MomentType
import com.hushbunny.app.ui.enumclass.ReportType
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
import com.hushbunny.app.uitls.EventWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class CommentViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val momentRepository: MomentRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _commentListResponse: MutableLiveData<EventWrapper<CommentResponseInfo>> = MutableLiveData()
    val commentListObserver: LiveData<EventWrapper<CommentResponseInfo>> = _commentListResponse

    private val _deleteCommentResponse: MutableLiveData<CommentDeletedResponseInfo> = MutableLiveData()
    val deleteCommentObserver: LiveData<CommentDeletedResponseInfo> = _deleteCommentResponse

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _reportResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val reportObserver: LiveData<BaseResponse> = _reportResponse

    fun getCommentList(currentPage: Int, momentId: String) {
        val queryParams = hashMapOf<String, Any>()
        queryParams[APIConstants.QUERY_PARAMS_PAGE] = currentPage
        queryParams[APIConstants.QUERY_PARAMS_PER_PAGE] = APIConstants.QUERY_PARAMS_PER_PAGE_VALUE
        queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = momentId
        ioScope.launch {
            _commentListResponse.postValue(EventWrapper(momentRepository.getCommentList(queryParams = queryParams)))
        }
    }

    fun addNewComment(comment: String, momentId: String) {
        if (comment.isEmpty()) {
            _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_comment))
        } else {
            _errorValidation.postValue(APIConstants.SUCCESS)
            ioScope.launch {
                _commentListResponse.postValue(
                    EventWrapper(
                    momentRepository.postComment(
                        addNewCommentRequest = AddNewCommentRequest(
                            comment = comment,
                            momentId = momentId
                        )
                    ))
                )
            }
        }
    }


    fun deleteComment(position: Int, commentId: String) {
        ioScope.launch {
            _deleteCommentResponse.postValue(momentRepository.deleteComment(position = position, commentId = commentId))
        }
    }

}