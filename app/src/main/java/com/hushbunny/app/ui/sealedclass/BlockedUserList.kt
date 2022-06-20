package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.BlockedUserResponseModel

sealed class BlockedUserList {
    data class Error(val message: String) : BlockedUserList()
    object NoList : BlockedUserList()
    data class UserList(val userList: List<BlockedUserResponseModel>) : BlockedUserList()
}
