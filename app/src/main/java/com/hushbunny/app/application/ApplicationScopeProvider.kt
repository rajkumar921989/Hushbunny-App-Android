package com.hushbunny.app.application

import kotlinx.coroutines.CoroutineScope

/**
 * A provider for application level scopes, this should only
 * be used for coroutines in the data layers.  Ideal scenario is
 * the coroutines are using ViewModel/Fragment/Activity level scopes instead of this
 * application level scope.
 */
interface ApplicationScopeProvider {
    val scope: CoroutineScope
}
