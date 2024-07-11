package org.xpathqs.driver.navigation.base

import org.xpathqs.driver.navigation.util.Loading
import java.time.Duration

interface ILoadableDelegate : ILoadable {
    val loadable: ILoadable

    override val loading: Loading
        get() = loadable.loading

    override fun waitForLoad(duration: Duration) {
        loadable.waitForLoad(duration)
    }

    override fun waitForDisappear(duration: Duration) {
        loadable.waitForDisappear(duration)
    }
}