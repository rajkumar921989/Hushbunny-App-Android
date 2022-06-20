package com.hushbunny.app.ui.blockeduser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.ui.model.BlockUnBlockUserRequest
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.uitls.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BlockedUserViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val homeRepository: HomeRepository
) : BaseViewModel() {
    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _blockedUserListResponse: MutableLiveData<BlockedUserList> = MutableLiveData()
    val blockedUserListObserver: LiveData<BlockedUserList> = _blockedUserListResponse


    fun getBlockedUserList() {
        ioScope.launch {
            _blockedUserListResponse.postValue(homeRepository.getBlockedUserList())
        }
    }

    fun blockAndUnblockUser(userId: String, action: String) {
        ioScope.launch {
            _blockedUserListResponse.postValue(
                homeRepository.blockUnBlockUser(
                    blockUnBlockUserRequest = BlockUnBlockUserRequest(
                        userId = userId,
                        action = action
                    )
                )
            )
        }
    }


}