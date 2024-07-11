package org.xpathqs.driver.constants

import org.xpathqs.core.constants.CoreGlobalProps
import org.xpathqs.driver.executor.IExecutor
import java.time.Duration

open class DriverGlobalProps : CoreGlobalProps() {
    private val WAIT_FOR_ELEMENT = 5000
    private val REFRESH_CACHE = 100

    val WAIT_FOR_ELEMENT_TIMEOUT: Duration
        get() = Duration.ofMillis(
            (props["constants.timeouts.wait_for_element"] as? String
                ?: "$WAIT_FOR_ELEMENT").toLong()
        )

    val REFRESH_CACHE_TIMEOUT: Duration
        get() = Duration.ofMillis(
            (props["constants.timeouts.refresh_cache"] as? String
                ?: "$REFRESH_CACHE").toLong()
        )

    val executor: IExecutor
        get() = localExecutor.get()

    val localExecutor = ThreadLocal<IExecutor>()
}

object Global : DriverGlobalProps()