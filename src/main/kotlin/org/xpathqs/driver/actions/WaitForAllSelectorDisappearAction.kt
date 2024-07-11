package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.constants.Global
import java.time.Duration

open class WaitForAllSelectorDisappearAction(
    val selectors: Collection<BaseSelector>,
    timeout: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT
) : WaitAction(timeout) {

    override val name: String
        get() = "WaitForAllSelectorDisappearAction"
}