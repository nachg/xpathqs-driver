package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.seconds
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.log.Log
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.model.IModelStates
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.ILoadableDelegate
import org.xpathqs.driver.navigation.base.IModelBlock
import org.xpathqs.driver.navigation.base.INavigator
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberFunctions

class DirectSelectorNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            if(elem is IBlockSelectorNavigation) {
                elem::class.declaredMemberFunctions.firstOrNull {
                    it.name == IBlockSelectorNavigation::navigate.name
                }?.let {
                    elem.navigate(elem, navigator, model)
                    return elem.isVisible
                }
            }
        }

        return false
    }
}