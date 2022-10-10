package com.hushbunny.app.ui.enumclass

import com.hushbunny.app.R

enum class ReactionPageName(val pageIndex: Int) {
    ALL(0),
    HEART(1),
    LAUGH(2),
    SAD(3)
}

fun String?.getImage(): Int? {
    return when (this) {
        ReactionPageName.HEART.name -> R.drawable.ic_heart_reaction
        ReactionPageName.LAUGH.name -> R.drawable.ic_laugh_reaction
        ReactionPageName.SAD.name -> R.drawable.ic_sad_reaction
        else -> null
    }
}

fun String?.reactionText(): String {
    return when (this) {
        ReactionPageName.HEART.name -> " loved this"
        ReactionPageName.LAUGH.name -> " laughed seeing this"
        ReactionPageName.SAD.name -> " cried seeing this"
        else -> ""
    }
}