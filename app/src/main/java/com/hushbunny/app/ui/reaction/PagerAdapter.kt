package com.hushbunny.app.ui.reaction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hushbunny.app.ui.enumclass.ReactionPageName

class PagerAdapter(fragment: Fragment, val momentId: String) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return TAB_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            ReactionPageName.HEART.pageIndex -> ReactionListingFragment.getInstance(ReactionPageName.HEART.name, momentId)
            ReactionPageName.LAUGH.pageIndex -> ReactionListingFragment.getInstance(ReactionPageName.LAUGH.name, momentId)
            ReactionPageName.SAD.pageIndex -> ReactionListingFragment.getInstance(ReactionPageName.SAD.name, momentId)
            else -> ReactionListingFragment.getInstance(ReactionPageName.ALL.name, momentId)
        }
        return fragment
    }

    companion object {
        const val TAB_COUNT = 4
    }
}
