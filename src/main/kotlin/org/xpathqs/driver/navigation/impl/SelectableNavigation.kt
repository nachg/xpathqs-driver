package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.block.findWithAnnotation
import org.xpathqs.core.selector.extensions.isChildOf
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.click
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.widgets.IFormSelect

class SelectableNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val selectable = (elem.rootParent as? Block)?.allInnerSelectors?.firstOrNull {
                it.hasAnnotation(UI.Widgets.Selectable::class)
            }
            if(selectable != null && selectable is IFormSelect) {
                val ann = selectable.findAnnotation<UI.Widgets.Selectable>()
                ann?.onSelected?.forEach {
                    val obj = it.objectInstance
                    if(obj != null && elem.isChildOf(obj)) {
                        Log.action("Apply SelectableNavigation") {
                            selectable.selectAny()

                            if(elem.hasAnnotation(UI.Widgets.ValidationError::class)) {
                                (elem.rootParent as? Block)?.findWithAnnotation(UI.Widgets.Submit::class)?.click()
                            }
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