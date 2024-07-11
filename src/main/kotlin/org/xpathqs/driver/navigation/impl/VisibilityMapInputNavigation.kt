package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.input
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.ILoadableDelegate
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.widgets.IFormInput
import org.xpathqs.log.Log
import java.time.Duration

private const val VISIBILITY_MAP_KEY = "VISIBILITY_MAP"

class VisibilityMapInputNavigation : IBlockSelectorNavigation {

    override fun isSelfApply(elem: ISelector, navigator: INavigator, model: IBaseModel): Boolean {
        if(elem is BaseSelector) {
            if (elem.isVisible) {
                return true
            }
            val elems = (elem.rootParent as? Block)?.allInnerSelectors?.filter {
                it.customPropsMap.containsKey(VISIBILITY_MAP_KEY)
            }
            elems?.forEach { inputSelector ->
                val map = inputSelector.customPropsMap[VISIBILITY_MAP_KEY] as Map<String, Any>
                map.entries.forEach { (k, v) ->

                    if (v is BaseSelector) {
                        if(elem.name.startsWith(v.name)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val elems = (elem.rootParent as? Block)?.allInnerSelectors?.filter {
                it.customPropsMap.containsKey(VISIBILITY_MAP_KEY)
            }
            elems?.forEach { inputSelector ->
                val map = inputSelector.customPropsMap[VISIBILITY_MAP_KEY] as Map<String, Any>
                map.entries.forEach { (k,v) ->

                        if(v is BaseSelector) {
                            if(elem.name.startsWith(v.name) && v.isHidden) {
                                Log.action("Apply VisibilityMapInputNavigation") {
                                    if (inputSelector is IFormInput) {
                                        inputSelector.input(k, model = model)
                                    } else {
                                        inputSelector.input(k, model = model)
                                    }
                                    if(elem is ILoadableDelegate) {
                                        elem.waitForLoad(Duration.ofSeconds(20))
                                    } else {
                                        elem.waitForVisible(Duration.ofSeconds(20))
                                    }
                                }
                            }
                        } else if(v is Collection<*>) {
                            v as Collection<BaseSelector>
                            v.forEach { sel ->
                                if(elem.name.startsWith(sel.name) && sel.isHidden) {
                                    Log.action("Apply VisibilityMapInputNavigation") {
                                        if (inputSelector is IFormInput) {
                                            inputSelector.input(k)
                                        } else {
                                            inputSelector.input(k)
                                        }
                                        if(elem is ILoadableDelegate) {
                                            elem.waitForLoad(Duration.ofSeconds(20))
                                        } else {
                                            elem.waitForVisible(Duration.ofSeconds(20))
                                        }
                                    }
                                }
                            }
                        }


                    if(elem.isVisible) {
                        return true
                    }
                }
            }
        }
        return false
    }
}

fun <T: BaseSelector> T.visibilityMap(map: Map<Any?, Any>) : T {
    this.customPropsMap[VISIBILITY_MAP_KEY] = map
    return this
}