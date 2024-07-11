package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.*
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.findWithAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.click
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.IModelBlock
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.model.IModelStates
import org.xpathqs.driver.model.default
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import org.xpathqs.driver.util.getConstructor
import org.xpathqs.driver.util.newInstance
import kotlin.reflect.KMutableProperty

class TriggerModelNavigation : IBlockSelectorNavigation {
    override fun navigate(sel: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(sel is BaseSelector) {
            if(sel.isVisible) {
                return true
            }
            //val isValidationError = sel.hasAnnotation(UI.Widgets.ValidationError::class)

            sel.parents.filterIsInstance<IModelBlock<*>>().firstOrNull()?.let {
                val ann = sel.findAnnotation<UI.Visibility.Dynamic>()
                    ?: sel.findAnyParentAnnotation<UI.Visibility.Dynamic>()

                if(ann != null && (ann.modelDepends != UNDEF_STATE || ann.submitModel)) {
                    Log.action("Apply TriggerModelNavigation") {
                        var m = it()
                        m.default()

                        var useSubmit = ann.submitModel
                        var m2: IBaseModel? = if(ann.modelDepends != UNDEF_STATE) {
                            if(m.view is IModelStates) {
                                (m.view as IModelStates).states[ann.modelDepends]
                            } else {
                                m.states[ann.modelDepends]
                            }
                        } else if(ann.modelClass != Any::class) {
                            (ann.modelClass.getConstructor(0).newInstance() as IBaseModel).apply {
                                default()
                            }
                        } else null

                        val model = m2 ?: m
                        var prop = model.findPropBySel(sel)

                        if(prop != null && model.triggerModelNavigationByProp) {
                            if(!useSubmit) {
                                model.fill(prop as KMutableProperty<*>)
                            } else {
                                model.submit()
                            }
                        } else {
                            if(sel is Block) {
                                if(!useSubmit) {
                                    model.fill(checkLambda = {sel.isVisible})
                                } else {
                                    model.submit()
                                }
                            }
                        }
                    }

                    if(sel.isVisible) {
                        return true
                    }
                }

            }
        }

        return false
    }
}