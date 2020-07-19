package com.geospock

import org.mockito.AdditionalMatchers
import org.mockito.Mockito

object MockitoExtensions {

    fun <T> eq(obj: T): T {
        return Mockito.eq(obj)
    }


    fun <T> not(obj: T): T {
        return AdditionalMatchers.not(obj)
    }

    fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}