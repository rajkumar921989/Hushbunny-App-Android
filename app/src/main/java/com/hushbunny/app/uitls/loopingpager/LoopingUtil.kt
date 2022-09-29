package com.hushbunny.app.uitls.loopingpager

import androidx.viewpager.widget.PagerAdapter

object LoopingUtil {
    /**
     * controlling onPageSelected of OnPageChangeListener event fire based on flag
     * if position is duplicated or if position is not 0 and not last then need to reset flag
     */
    const val RESET_LAST_OUTER_INDEX = -1
    /**
     * Convert pager position to internal position
     */
    fun getInternalPosition(originalAdapter: PagerAdapter?, pagerPosition: Int): Int {
        val count = originalAdapter?.count ?: 0
        return if (count > 1) pagerPosition + 1 else pagerPosition
    }

    /**
     * Convert internal position to pager position
     */
    fun getPagerPosition(originalAdapter: PagerAdapter?, internalPosition: Int): Int {
        val count = originalAdapter?.count ?: 0
        return when (internalPosition) {
            0 -> count - 1
            count + 1 -> 0
            else -> internalPosition - 1
        }
    }
}
