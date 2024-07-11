package org.xpathqs.driver.widgets

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.extensions.parentCount
import org.xpathqs.driver.extensions.*
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.navigation.util.IBlockNavigation

open class CheckBox(
    base: BaseSelector,
    val input: BaseSelector,
    val label: BaseSelector,
    @UI.Visibility.Dynamic
    private val checkActiveSelector: BaseSelector,
): Block(base), IFormRead, IBlockSelectorNavigation {

    open val isChecked: Boolean
        get() {
            if(input.makeVisible().isHidden) {
                throw Exception("Unable to set visible of Checkbox: $xpath")
            }
            return checkActiveSelector.isVisible
        }

    fun check() {
        if(!isChecked) {
            click()
        }
    }

    fun uncheck() {
        if(isChecked) {
            click()
        }
    }

    override fun readBool(model: IBaseModel?): Boolean {
        return isChecked
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean{
        if(elem == checkActiveSelector) {
            check()
        }
        return true
    }
}