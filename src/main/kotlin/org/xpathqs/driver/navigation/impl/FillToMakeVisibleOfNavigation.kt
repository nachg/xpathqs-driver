package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.extensions.isChildOf
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.getDefaultModel
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator

private const val FILL_TO_MAKE_VISIBLE_OF = "LINKED_VISIBILITY_FILLED_KEY"

class FillToMakeVisibleOfNavigation : IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val elems = (elem.rootParent as? Block)?.allInnerSelectors?.filter {
                it.customPropsMap.containsKey(FILL_TO_MAKE_VISIBLE_OF)
            }
            elems?.forEach { annotatedSelector ->
                val linked = annotatedSelector.customPropsMap[FILL_TO_MAKE_VISIBLE_OF] as LinkedVisibilityWhenFilled<BaseSelector>
                if(linked.sel == elem || elem.isChildOf(linked.sel)) {
                    Log.action("Apply FillToMakeVisibleOfNavigation") {
                        linked.sel.getDefaultModel()?.let { model ->
                            model.findPropBySel(annotatedSelector)?.let {p ->
                                model.fill(p)
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

class LinkedVisibilityWhenFilled<T: BaseSelector>(
    val sel: BaseSelector
)

fun <T: BaseSelector> T.fillToMakeVisibleOf(
    sel: BaseSelector
) : T {
    this.customPropsMap[FILL_TO_MAKE_VISIBLE_OF] = LinkedVisibilityWhenFilled<T>(
        sel
    )
    return this
}