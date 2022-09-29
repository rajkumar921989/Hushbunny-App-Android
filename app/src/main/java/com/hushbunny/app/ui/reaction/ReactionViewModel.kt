package com.hushbunny.app.ui.reaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.model.AddReactionRequest
import com.hushbunny.app.ui.model.ReactionCountModel
import com.hushbunny.app.ui.model.ReactionModel
import com.hushbunny.app.ui.model.ReactionResponseModel
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.ReactionResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.EventWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReactionViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val momentRepository: MomentRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _reactionListResponse: MutableLiveData<EventWrapper<ReactionResponseInfo>> = MutableLiveData()
    val reactionListObserver: LiveData<EventWrapper<ReactionResponseInfo>> = _reactionListResponse

    private val _reactionType: MutableLiveData<String> = MutableLiveData()
    val reactionTypeObserver: LiveData<String> = _reactionType

    private val _reactionCountResponse: MutableLiveData<EventWrapper<ReactionCountModel>> = MutableLiveData()
    val reactionCountObserver: LiveData<EventWrapper<ReactionCountModel>> = _reactionCountResponse

    var reactionList = ArrayList<ReactionModel>()
    var currentPage = 1

    fun getReactionList(currentPage: Int, momentId: String) {
        val queryParams = hashMapOf<String, Any>()
        queryParams[APIConstants.QUERY_PARAMS_PAGE] = currentPage
        queryParams[APIConstants.QUERY_PARAMS_PER_PAGE] = APIConstants.QUERY_PARAMS_PER_PAGE_VALUE
        queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = momentId
        ioScope.launch {
            _reactionListResponse.postValue(EventWrapper(momentRepository.getReactionList(queryParams)))
        }
    }



    fun getReactionList(emojiType: String): List<ReactionModel> {
        return if (emojiType == ReactionPageName.ALL.name)
            reactionList.toList()
        else reactionList.filter {
            it.emojiType == emojiType
        }
    }

    fun setReactionType(emojiType: String) {
        _reactionType.postValue(emojiType)
    }

    fun setReactionCount(reactionCountModel: ReactionCountModel) {
        _reactionCountResponse.postValue(EventWrapper(reactionCountModel))
    }

    fun getReactionMessage(): String {
        return when (reactionTypeObserver.value) {
            ReactionPageName.HEART.name -> resourceProvider.getString(R.string.no_heart_reaction_found)
            ReactionPageName.LAUGH.name -> resourceProvider.getString(R.string.no_laugh_reaction_found)
            ReactionPageName.SAD.name -> resourceProvider.getString(R.string.no_sad_reaction_found)
            else -> resourceProvider.getString(R.string.no_reaction_found)
        }
    }
}