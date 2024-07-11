package org.xpathqs.driver.extensions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.findAnyParentAnnotation
import org.xpathqs.core.selector.block.*
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import org.xpathqs.driver.navigation.impl.PageState
import org.xpathqs.driver.navigation.impl.PageState.Companion.isSelfStaticSelector
import org.xpathqs.driver.navigation.impl.PageState.Companion.isStaticSelector

val Block.staticBlockSelectors: Collection<BaseSelector>
    get() = getStaticSelectorsWithState(
        state = UI.Visibility.UNDEF_STATE,
        onlyCurrentBlock = true
    )

val Block.staticSelectors: Collection<BaseSelector>
    get() = getStaticSelectorsWithState(state = UI.Visibility.UNDEF_STATE)

fun Block.getStaticSelectorsWithState(
    state: Int,
    includeContains: Boolean = true,
    onlyCurrentBlock: Boolean = false
): Collection<BaseSelector> {
    val res = ArrayList<BaseSelector>()
    if(includeContains) {
        this.annotations.filterIsInstance<UI.Nav.PathTo>().forEach {
            if(it.selfPageState == state) {
                // it.contains.forEach {
                if(it.contain != Block::class) {
                    res.addAll(
                        it.contain.objectInstance?.allInnerSelectors ?: emptyList()
                    )
                }

                //  }
            }
        }
    }

    val filteredRes = if(onlyCurrentBlock) {
        res + this.selectorBlocks.filter {
            isSelfStaticSelector(it)
        }.flatMap {
            it.allInnerSelectors
        } + this.selectors
    } else {
        res + this.allInnerSelectors
    }

    return filteredRes.filter {
            if(state == UNDEF_STATE) {
                true
            } else  {
                it.findAnnotation<UI.Visibility.State>()?.value == state
                        || it.findAnyParentAnnotation<UI.Visibility.State>()?.value == state
            }
        }.filter {
            if(onlyCurrentBlock) {
                val parentCheck = if(it.base != this && it.base is BaseSelector) {
                    isSelfStaticSelector(it.base as BaseSelector)
                } else true

                isSelfStaticSelector(it) && parentCheck
            } else {
                isStaticSelector(it)
            }
        }
}