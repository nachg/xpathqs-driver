package org.xpathqs.driver.navigation.util

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.isVisible

class Loading(
    val loadSelector: BaseSelector? = null,
    val loadAllSelectors: Collection<BaseSelector> = emptyList(),
    val loadAnySelectors: Collection<BaseSelector> = emptyList(),
    val loadNoneSelectors: Collection<BaseSelector> = emptyList()
  //  var loadOneOfSelectors: Collection<BaseSelector> = emptyList()
) {
    val isLoaded: Boolean
        get() {
            val r1 = if(loadSelector != null) {
                loadSelector!!.isVisible
            } else {
                if(loadAllSelectors.isNotEmpty()) {
                    loadAllSelectors.none { it.isHidden }
                } else if(loadAnySelectors.isNotEmpty()) {
                    loadAnySelectors.any { it.isVisible }
                } else {
                    false
                }
            }

            return if(loadNoneSelectors.isEmpty()) {
                r1
            } else {
                r1 && loadNoneSelectors.none{ it.isVisible }
            }
        }
}