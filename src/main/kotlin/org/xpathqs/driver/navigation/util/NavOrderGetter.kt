package org.xpathqs.driver.navigation.util

import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.UI.Nav.Order.Companion.DEFAULT

class NavOrderGetter(
    private val source: Block
) {
    val navOrder: Int
        get() {
            return source.findAnnotation<UI.Nav.Order>()?.let {
                if(it.order != -1) it.order else it.type.value
            } ?: DEFAULT
        }
}