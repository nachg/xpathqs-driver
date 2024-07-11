package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.constants.Global
import org.xpathqs.driver.extensions.click
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.makeVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.NavExecutor
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator

class ClickToBackNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val cp = navigator.currentPage
            if(cp !== elem.rootParent) {
                (cp as? Block)?.allInnerSelectors?.firstOrNull {
                    it.hasAnnotation(UI.Widgets.Back::class)
                }?.let {
                    Log.action("Apply ClickToBackNavigation") {
                        if(it.isVisible) {
                            it.click()

                            Log.action("Trying to make $elem visible, after closing") {
                                elem.makeVisible()
                            }

                            if(elem.isVisible) {
                                Log.info("$elem became visible")
                                return@action
                            }
                        }
                    }
                }
            }
        }

        return false
    }
}