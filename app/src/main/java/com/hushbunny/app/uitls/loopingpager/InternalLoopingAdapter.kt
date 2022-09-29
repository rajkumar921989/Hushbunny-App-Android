package com.hushbunny.app.uitls.loopingpager

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.hushbunny.app.uitls.loopingpager.LoopingUtil

internal class InternalLoopingAdapter(private val pagerAdapter: PagerAdapter) : PagerAdapter() {

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return pagerAdapter.isViewFromObject(view, obj)
    }

    override fun getCount(): Int {
        val itemsSize = pagerAdapter.count
        return if (itemsSize > 1) itemsSize + 2 else itemsSize
    }

    override fun startUpdate(container: ViewGroup) {
        pagerAdapter.startUpdate(container)
    }

    override fun getItemPosition(obj: Any): Int {
        return pagerAdapter.getItemPosition(obj)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return pagerAdapter.instantiateItem(
            container,
            LoopingUtil.getPagerPosition(
                pagerAdapter,
                position
            )
        )
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        pagerAdapter.destroyItem(
            container,
            LoopingUtil.getPagerPosition(
                pagerAdapter,
                position
            ),
            obj
        )
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        pagerAdapter.setPrimaryItem(container, position, obj)
    }

    override fun finishUpdate(container: ViewGroup) {
        pagerAdapter.finishUpdate(container)
    }

    override fun saveState(): Parcelable? {
        return pagerAdapter.saveState()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        pagerAdapter.restoreState(state, loader)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pagerAdapter.getPageTitle(position)
    }

    override fun getPageWidth(position: Int): Float {
        return pagerAdapter.getPageWidth(position)
    }
}
