package com.hushbunny.app.ui.moment

import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.FilterType
import com.hushbunny.app.ui.enumclass.MediaType
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
import com.hushbunny.app.uitls.DateFormatUtils.convertFilterDateIntoISODateFormat
import com.hushbunny.app.uitls.EventWrapper
import com.hushbunny.app.uitls.OgTagParser
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AddMomentViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val momentRepository: MomentRepository,
    private val fileUploadRepository: FileUploadRepository? = null
) : BaseViewModel() {
    var momentImageList = mutableListOf<MomentMediaModel>()
    var selectedKidList = ArrayList<String>()

    private var momentRefreshJob = SupervisorJob()
    private var momentScope = CoroutineScope(ioDispatcher + momentRefreshJob)
    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _kidsListResponse: MutableLiveData<KidsStatusInfo> = MutableLiveData()
    val kidsListObserver: LiveData<KidsStatusInfo> = _kidsListResponse

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _addOrEditMomentResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val addOrEditMomentObserver: LiveData<BaseResponse> = _addOrEditMomentResponse

    private val _momentResponse: MutableLiveData<EventWrapper<MomentResponseInfo>> = MutableLiveData()
    val momentObserver: LiveData<EventWrapper<MomentResponseInfo>> = _momentResponse

    private val _bookMarkResponse: MutableLiveData<EventWrapper<BookMarkResponseInfo>> = MutableLiveData()
    val bookMarkObserver: LiveData<EventWrapper<BookMarkResponseInfo>> = _bookMarkResponse

    private val _reactionResponse: MutableLiveData<EventWrapper<BookMarkResponseInfo>> = MutableLiveData()
    val reactionObserver: LiveData<EventWrapper<BookMarkResponseInfo>> = _reactionResponse

    private val _userDetailResponse: MutableLiveData<UserDetailResponse> = MutableLiveData()
    val userDetailResponseObserver: LiveData<UserDetailResponse> = _userDetailResponse

    private val _reportResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val reportObserver: LiveData<BaseResponse> = _reportResponse

    private val _markAsImportantMomentResponse: MutableLiveData<EventWrapper<BookMarkResponseInfo>> = MutableLiveData()
    val markAsImportantMomentObserver: LiveData<EventWrapper<BookMarkResponseInfo>> = _markAsImportantMomentResponse

    private val _deleteCommentResponse: MutableLiveData<CommentDeletedResponseInfo> = MutableLiveData()
    val deleteCommentObserver: LiveData<CommentDeletedResponseInfo> = _deleteCommentResponse

    private val _kidDetailResponse: MutableLiveData<KidByIdResponseModel> = MutableLiveData()
    val kidDetailResponseObserver: LiveData<KidByIdResponseModel> = _kidDetailResponse

    private val _loadImageResourceResponse: MutableLiveData<FileDownLoadState> = MutableLiveData()
    val loadImageResourceResponseObserver: LiveData<FileDownLoadState> = _loadImageResourceResponse

    fun getKidsList(isOtherUser: Boolean = false, userId: String = "") {
        ioScope.launch {
            val queryParams = hashMapOf<String, Any>()
            if (userId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_USER_ID] = userId
            _kidsListResponse.postValue(momentRepository.getKidsList(isOtherUser, queryParams))
        }
    }

    fun getUserDetail(userId: String) {
        ioScope.launch {
            val queryParams = hashMapOf<String, Any>()
            if (userId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_USER_ID] = userId
            _userDetailResponse.postValue(momentRepository.getUserDetail(queryParams))
        }
    }

    fun addOREditMoment(
        isEdit: Boolean,
        momentId: String,
        momentDate: String,
        description: String,
        isImportant: Boolean,
    ) {
        when {
            description.isEmpty() && momentImageList.isEmpty() -> {
                _errorValidation.postValue(resourceProvider.getString(R.string.media_or_description_error))
            }
            selectedKidList.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_select_kid))
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                val updateFileList = momentImageList.filter {
                    !it.isUploaded
                }
                val alreadyUpdatedFileList = mutableListOf<AddMomentMediaRequest>()
                momentImageList.filter {
                    it.isUploaded
                }.forEach { mediaFile ->
                    alreadyUpdatedFileList.add(
                        AddMomentMediaRequest(
                            type = mediaFile.type,
                            original = mediaFile.original,
                            thumbnail = mediaFile.thumbnail
                        )
                    )
                }
                val fileList = mutableListOf<File>()
                updateFileList.forEach {
                    if (it.type == MediaType.VIDEO.name)
                        fileList.add(File(it.thumbnail.orEmpty()))
                    else fileList.add(File(it.original.orEmpty()))
                }
                ioScope.launch {
                    if (fileList.isNotEmpty()) {
                        val uploadedFileResponse = fileUploadRepository?.uploadFileForMoment(fileList)
                        if (uploadedFileResponse?.statusCode == APIConstants.API_RESPONSE_200) {
                            fileList.forEach { fileName ->
                                momentImageList.find { media ->
                                    fileName.toString() == media.original || fileName.toString() == media.thumbnail
                                }?.isUploaded = true
                            }
                            for (data in uploadedFileResponse.data.orEmpty()) {
                                alreadyUpdatedFileList.add(
                                    AddMomentMediaRequest(
                                        type = data.url.type,
                                        original = data.url.original,
                                        thumbnail = data.url.thumbnail
                                    )
                                )
                            }
                        }
                    }
                    _addOrEditMomentResponse.postValue(
                        momentRepository.addOREditMoment(
                            isEdit,
                            addMomentRequest = AddMomentRequest(
                                kidId = selectedKidList,
                                momentId = momentId.ifEmpty { null },
                                momentDate = momentDate.convertDateToISOFormat().orEmpty(),
                                isImportant = isImportant,
                                mediaContent = alreadyUpdatedFileList,
                                description = description.ifEmpty { null }
                            )
                        )
                    )
                }
            }
        }
    }

    fun cancelCurrentJob() {
        if (momentRefreshJob.isActive) {
            momentRefreshJob.cancel("resetting job")
            momentRefreshJob = SupervisorJob()
            momentScope = ioScope + momentRefreshJob
        }
    }

    fun getMomentList(
        currentPage: Int,
        type: String,
        kidID: String = "",
        momentID: String = "",
        date: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        filterType: String? = null,
        userId: String? = null,
        sortBy: String? = null
    ) {
        val queryParams = hashMapOf<String, Any>()
        queryParams[APIConstants.QUERY_PARAMS_PAGE] = currentPage
        queryParams[APIConstants.QUERY_PARAMS_PER_PAGE] = APIConstants.QUERY_PARAMS_PER_PAGE_VALUE
        queryParams[APIConstants.QUERY_PARAMS_TYPE] = type
        if (!sortBy.isNullOrEmpty())
            queryParams[APIConstants.QUERY_PARAMS_SORT] = sortBy.orEmpty()
        when (filterType) {
            FilterType.DATE.name -> {
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = date.convertFilterDateIntoISODateFormat(filterType)
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = date.convertFilterDateIntoISODateFormat(filterType)
            }
            FilterType.MONTH.name -> {
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = date.convertFilterDateIntoISODateFormat(filterType)
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = date.convertFilterDateIntoISODateFormat(filterType, isEndDate = true)
            }
            FilterType.YEAR.name -> {
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = date.convertFilterDateIntoISODateFormat(filterType)
                if (!date.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = date.convertFilterDateIntoISODateFormat(filterType, isEndYear = true)
            }
            FilterType.DATE_RANGE.name -> {
                if (!startDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = startDate.convertFilterDateIntoISODateFormat(filterType)
                if (!endDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = endDate.convertFilterDateIntoISODateFormat(filterType)
            }
            FilterType.MONTH_RANGE.name -> {
                if (!startDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = startDate.convertFilterDateIntoISODateFormat(filterType)
                if (!endDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = endDate.convertFilterDateIntoISODateFormat(filterType, isEndDate = true)
            }
            FilterType.YEAR_RANGE.name -> {
                if (!startDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_START_DATE] = startDate.convertFilterDateIntoISODateFormat(filterType)
                if (!endDate.isNullOrEmpty())
                    queryParams[APIConstants.QUERY_PARAMS_END_DATE] = endDate.convertFilterDateIntoISODateFormat(filterType, isEndYear = true)
            }
        }
        if (kidID.isNotEmpty())
            queryParams[APIConstants.QUERY_PARAMS_KID_ID] = kidID
        if (momentID.isNotEmpty())
            queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = momentID
        if (!userId.isNullOrEmpty())
            queryParams[APIConstants.QUERY_PARAMS_USER_ID] = userId
        momentScope.launch {
            _momentResponse.postValue(EventWrapper(momentRepository.getMomentList(queryParams)))
        }
    }

    fun bookMarkMoment(momentId: String, position: Int) {
        ioScope.launch {
            _bookMarkResponse.postValue(
                EventWrapper(
                    momentRepository.postBookmark(
                        position = position,
                        addBookmarkRequest = AddBookmarkRequest(momentId)
                    )
                )
            )
        }
    }

    fun addReaction(position: Int, emojiType: String, momentId: String) {
        ioScope.launch {
            _reactionResponse.postValue(
                EventWrapper(
                    momentRepository.postReaction(
                        position = position,
                        addReactionRequest = AddReactionRequest(emojiType = emojiType, momentId = momentId)
                    )
                )
            )
        }
    }

    fun postReport(
        position: Int,
        type: String,
        reasonId: String? = null,
        commentId: String? = null,
        reason: String? = null,
        userId: String? = null,
        momentId: String? = null
    ) {
        ioScope.launch {
            val reportRequest = AddReportRequest(
                type = type, reasonId = reasonId, commentId = commentId,
                reason = reason, momentId = momentId, userId = userId
            )
            _reportResponse.postValue(momentRepository.postReport(reportRequest))
        }
    }

    fun markMomentAsImportant(position: Int, momentId: String) {
        ioScope.launch {
            _markAsImportantMomentResponse.postValue(EventWrapper(momentRepository.markMomentAsImportant(position = position, momentId = momentId)))
        }
    }

    fun deleteComment(position: Int, commentId: String) {
        ioScope.launch {
            _deleteCommentResponse.postValue(momentRepository.deleteComment(position = position, commentId = commentId))
        }
    }

    fun getKidDetailById(kidId: String) {
        ioScope.launch {
            val queryParams = hashMapOf<String, Any>()
            if (kidId.isNotEmpty())
                queryParams[APIConstants.QUERY_PARAMS_KID_ID] = kidId
            _kidDetailResponse.postValue(momentRepository.getKidDetailByKidId(queryParams))
        }
    }

    fun shareMoment(momentId: String) {
        ioScope.launch {
            momentRepository.shareMoment(ShareMomentRequest(momentId = momentId))
        }
    }

    fun loadImageLink(urlToParse: String) {
        ioScope.launch {
            val isAlreadyNotAdded = momentImageList.firstOrNull { it.text.equals(urlToParse, true) } == null
            if(isAlreadyNotAdded) {
                val content = OgTagParser().getContents(urlToParse)
                content?.let { linkSource ->
                    if (linkSource.image.isNotEmpty()) {
                        _loadImageResourceResponse.postValue(FileDownLoadState.Loading)
                        val url = URL(linkSource.image)
                        val connection: HttpURLConnection?
                        try {
                            connection = url.openConnection() as HttpURLConnection
                            connection.connect()
                            val inputStream: InputStream = connection.inputStream
                            val bufferedInputStream = BufferedInputStream(inputStream)
                            _loadImageResourceResponse.postValue(FileDownLoadState.Success(
                                bitmap = BitmapFactory.decodeStream(bufferedInputStream),
                                imageText = urlToParse
                            ))
                        } catch (e: IOException) {
                            _loadImageResourceResponse.postValue(FileDownLoadState.Error)
                        }
                    }
                } ?: run {
                    _loadImageResourceResponse.postValue(FileDownLoadState.Error)
                }
            }
        }
    }
}
