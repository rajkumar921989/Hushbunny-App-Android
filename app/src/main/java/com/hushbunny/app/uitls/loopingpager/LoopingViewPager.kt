package com.hushbunny.app.uitls.loopingpager

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hushbunny.app.uitls.loopingpager.LoopingUtil.getInternalPosition
import com.hushbunny.app.uitls.loopingpager.LoopingUtil.getPagerPosition

/**
 * LoopingViewPager used as infinite ViewPager
 */
class LoopingViewPager : ViewPager {

    private var originalAdapter: PagerAdapter? = null
    private var internalAdapter: PagerAdapter? = null
    private val pageChangeListenerMap = HashMap<OnPageChangeListener, InternalOnPageChangeListener>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        super.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_IDLE) {
                    handleSetCurrentItem(getSuperCurrentItem())
                }
            }
        })
    }

    override fun getAdapter(): PagerAdapter? {
        return originalAdapter
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        if (adapter == null) {
            super.setAdapter(null)
            return
        }
        val internalAdapter = InternalLoopingAdapter(adapter)
        handleSetAdapter(internalAdapter, adapter)
    }

    private fun handleSetAdapter(internalAdapter: PagerAdapter, originalAdapter: PagerAdapter) {
        this.internalAdapter = internalAdapter
        this.originalAdapter = originalAdapter
        super.setAdapter(internalAdapter)
        originalAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                internalAdapter.notifyDataSetChanged()
            }

            override fun onInvalidated() {
                internalAdapter.notifyDataSetChanged()
            }
        })
        super.setCurrentItem(1, false)
    }

    override fun getCurrentItem(): Int {
        return getPagerPosition(originalAdapter, super.getCurrentItem())
    }

    fun getSuperCurrentItem(): Int {
        return super.getCurrentItem()
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(getInternalPosition(originalAdapter, item))
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        val count = adapter?.count ?: 0
        if (item == 0 && currentItem == count - 1) {
            super.setCurrentItem(count + 2, smoothScroll)
        } else {
            super.setCurrentItem(getInternalPosition(originalAdapter, item), smoothScroll)
        }
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        val internalListener =
            InternalOnPageChangeListener(
                listener
            ) { originalAdapter }
        pageChangeListenerMap[listener] = internalListener
        super.addOnPageChangeListener(internalListener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        pageChangeListenerMap.remove(listener)?.let {
            super.removeOnPageChangeListener(it)
        }
    }

    private fun handleSetCurrentItem(position: Int) {
        val itemCount = internalAdapter?.count ?: 0
        if (itemCount <= 1) return

        val lastPosition = itemCount - 1
        if (position == 0) {
            super.setCurrentItem(lastPosition - 1, false)
        } else if (position == lastPosition) {
            super.setCurrentItem(1, false)
        }
    }
}
