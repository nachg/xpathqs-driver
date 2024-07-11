package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.widgets.CheckBox
import org.xpathqs.log.Log
import java.time.Duration

private const val CHECKBOX_LINKED_KEY = "CHECKBOX_LINKED_KEY"
private class LinkWithCheckbox(
    val checkbox: CheckBox,
    val reverted: Boolean
)

class CheckBoxLinkedNavigation : IBlockSelectorNavigation {
    override fun isSelfApply(elem: ISelector, navigator: INavigator, model: IBaseModel): Boolean {
        if(elem is BaseSelector) {
            return elem.customPropsMap[CHECKBOX_LINKED_KEY] ?: elem.parents.firstOrNull {
                it.customPropsMap.containsKey(CHECKBOX_LINKED_KEY)
            }?.customPropsMap?.get(CHECKBOX_LINKED_KEY) != null
        }
        return false
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val linkedCheckbox = elem.customPropsMap[CHECKBOX_LINKED_KEY] ?:
                elem.parents.firstOrNull {
                    it.customPropsMap.containsKey(CHECKBOX_LINKED_KEY)
                }?.customPropsMap?.get(CHECKBOX_LINKED_KEY)

            (linkedCheckbox as? LinkWithCheckbox)?.let {
                Log.action("Trying to apply CheckBoxLinkedNavigation") {
                    if(it.reverted) it.checkbox.uncheck() else it.checkbox.check()
                    elem.waitForVisible(Duration.ofSeconds(2))
                }
                if(elem.isVisible) {
                    return true
                }
            }
        }

        return false
    }
}

fun <T: BaseSelector> T.linkWithCheckbox(checkbox: CheckBox, reverted: Boolean = false) : T {
    customPropsMap[CHECKBOX_LINKED_KEY] = LinkWithCheckbox(checkbox, reverted)
    return this
}