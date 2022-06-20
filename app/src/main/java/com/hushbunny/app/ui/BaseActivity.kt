package com.hushbunny.app.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Base for all Activities
 * Common Activity related implementations will be available here
 */
abstract class BaseActivity : AppCompatActivity() {


    open fun setBottomNavigationVisibility(visibility: Int) {
        // making bottom nav visibility
    }


    open fun makeToolbarAsSticky(toolBar: View) {}

    /**
     * This Method used to remove the Toolbar
     * It can be override from child activity and the fragment can make call to remove toolbar
     */
    open fun removeCustomToolbar() {}

    /**
     * This Method used to animate the Bottom navigation
     * Using param [hide] true/false
     * It can be override from child activity and the fragment can make call to remove toolbar
     */
    open fun animateBottomNavigation(hide: Boolean) {}


}
