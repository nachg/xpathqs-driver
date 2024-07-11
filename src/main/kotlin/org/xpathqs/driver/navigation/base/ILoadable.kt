package org.xpathqs.driver.navigation.base

import org.xpathqs.driver.navigation.util.Loading
import java.time.Duration

interface ILoadable {
    val loading: Loading
    fun waitForLoad(duration: Duration)
    fun waitForDisappear(duration: Duration)
}