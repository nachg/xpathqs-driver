package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.log.Log
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.model.IModelStates
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.IModelBlock
import org.xpathqs.driver.navigation.base.INavigator

class ModelStateSelectorNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val ann = elem.findAnnotation<UI.Visibility.Dynamic>()
            if(ann != null) {
                if (ann.modelState >= 0) {
                    elem.parents.filterIsInstance<IModelBlock<*>>().firstOrNull()?.let {
                        Log.action("Apply ModelStateSelectorNavigation") {
                            if(ann.modelState == IBaseModel.DEFAULT) {
                                if(ann.submitModel) {
                                    it().submit()
                                } else {
                                    it().fill(noSubmit = true)
                                }
                            } else {
                                val model = it()
                                val waitForLoad = ann.modelState != IBaseModel.INCORRECT
                                if(model.view is IModelStates) {
                                    if(ann.submitModel) {
                                        model.view.states[ann.modelState]?.submit(waitForLoad = waitForLoad)
                                    } else {
                                        model.view.states[ann.modelState]?.fill(noSubmit = true)
                                    }
                                } else {
                                    if(ann.submitModel) {
                                        model.states[ann.modelState]?.submit(waitForLoad = waitForLoad)
                                    } else {
                                        model.states[ann.modelState]?.fill(noSubmit = true)
                                    }
                                }
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