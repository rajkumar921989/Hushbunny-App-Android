package com.hushbunny.app.application

import kotlinx.coroutines.CoroutineScope

class ApplicationScopeProviderImp(override val scope: CoroutineScope) : ApplicationScopeProvider
