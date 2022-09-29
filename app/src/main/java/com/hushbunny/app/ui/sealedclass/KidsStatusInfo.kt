package com.hushbunny.app.ui.sealedclass

import com.hushbunny.app.ui.model.KidsResponseModel

sealed class KidsStatusInfo {
    object ApiError : KidsStatusInfo()
    object NoKids : KidsStatusInfo()
    data class UserList(val userList: List<KidsResponseModel>) : KidsStatusInfo()
}
