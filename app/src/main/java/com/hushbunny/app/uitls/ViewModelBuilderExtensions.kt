package com.hushbunny.app.uitls

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlin.reflect.KClass

/**
 * Get a [ViewModel] in a [Fragment] scoped to the lifecycle of it's host [Activity].
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModelBuilderActivityScope(
    noinline viewModelInitializer: () -> VM
): Lazy<VM> {
    return ViewModelLazy(
        viewModelClass = VM::class,
        storeProducer = { requireActivity().viewModelStore },
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST") // Casting T as ViewModel
                    return viewModelInitializer.invoke() as T
                }
            }
        }
    )
}

/**
 * Get a [ViewModel] in a [Fragment] scoped to the lifecycle of that [Fragment].
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModelBuilderFragmentScope(
    noinline viewModelInitializer: () -> VM
): Lazy<VM> {
    return ViewModelLazy(
        viewModelClass = VM::class,
        storeProducer = { viewModelStore },
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST") // Casting T as ViewModel
                    return viewModelInitializer.invoke() as T
                }
            }
        }
    )
}

/**
 * Get a [ViewModel] in an [ComponentActivity].
 */
@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModelBuilder(
    noinline viewModelInitializer: () -> VM
): Lazy<VM> {
    return ViewModelLazy(
        viewModelClass = VM::class,
        storeProducer = { viewModelStore },
        factoryProducer = {
            return@ViewModelLazy object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST") // Casting T as ViewModel
                    return viewModelInitializer.invoke() as T
                }
            }
        }
    )
}
/**
 * Get a [ViewModel] in an [NavGraphViewModelLazyKt].
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModelBuilderNavGraphScope(
    @NavigationRes navGraphId: Int,
    noinline viewModelInitializer: () -> VM
): Lazy<VM> {
    return lazy {
        val storeOwner = findNavController().getViewModelStoreOwner(navGraphId)
        val viewModelProvider = ViewModelProvider(
            storeOwner,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST") // Casting T as ViewModel
                    return viewModelInitializer.invoke() as T
                }
            }
        )
        val clazz: KClass<VM> = VM::class
        viewModelProvider.get(clazz.java)
    }
}
