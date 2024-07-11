package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnyParentAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.IModelBlock
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.log.Log

class ModelStateParentNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            elem.findAnyParentAnnotation<UI.Visibility.Dynamic>()?.let { p ->
                if(p.modelState >= 0) {
                    elem.parents.filterIsInstance<IModelBlock<*>>().firstOrNull()?.let {
                        Log.action("Apply ModelStateParentNavigation") {
                            if(p.submitModel) {
                                it().submit(p.modelState)
                            } else {
                                it().states[p.modelState]?.fill(noSubmit = true)
                            }

                            elem.waitForVisible()
                        }

                        if(elem.isVisible) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }
}