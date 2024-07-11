package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.*
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectorBlocks
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.value
import org.xpathqs.driver.navigation.annotations.DeterminationType
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import org.xpathqs.driver.navigation.base.IPageState
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

class PageState(
    val base: Block
) : IPageState {
    private val stateSelectorsMap: Map<Int, Collection<BaseSelector>> by lazy {
        val res = HashMap<Int, HashSet<BaseSelector>>()
        base.allInnerSelectorBlocks.filter {
            isStaticSelector(it) && it.hasAnnotation(UI.Visibility.State::class)
        }.forEach { block ->
            val stateBlockAnn = block.findAnnotation<UI.Visibility.State>()!!
            val determinationAnn = block.findAnnotation<UI.Nav.DeterminateBy>()
            if(determinationAnn != null && determinationAnn.stateDetermination) {
                val selectors = res.getOrPut(stateBlockAnn.value) {
                    HashSet()
                }
                when(determinationAnn.determination) {
                    DeterminationType.EXIST -> selectors.add(block)
                    DeterminationType.EXIST_ALL -> selectors.addAll(block.allInnerSelectors)
                    else -> {}
                }
            } else {
                (base.allInnerSelectorBlocks + block.allInnerSelectors).filter {
                    isStaticSelector(it) && it.findAnyParentAnnotation<UI.Visibility.State>()?.value == stateBlockAnn.value
                }.forEach {
                    val ann = it.findAnnotation<UI.Nav.DeterminateBy>()
                    if(ann != null && ann.stateDetermination) {
                        val selectors = res.getOrPut(stateBlockAnn.value) {
                            HashSet()
                        }
                        when(ann.determination) {
                            DeterminationType.EXIST, DeterminationType.EXIST_ALL -> selectors.add(it)
                            else -> {}
                        }
                    }
                }
                block.allInnerSelectors.filter {
                    isStaticSelector(it) && it.findAnyParentAnnotation<UI.Visibility.State>()?.value == stateBlockAnn.value
                } .forEach {
                    val ann = it.findAnyParentAnnotation<UI.Nav.DeterminateBy>()
                    if(ann != null && ann.stateDetermination) {
                        val selectors = res.getOrPut(stateBlockAnn.value) {
                            HashSet()
                        }
                        when(ann.determination) {
                            DeterminationType.EXIST_ALL -> selectors.add(it)
                            else -> {}
                        }
                    }
                }
            }
        }
        res
    }

    override val pageState: Int
        get() {
            stateSelectorsMap.entries.forEach { (k, v) ->
                if(v.none { it.isHidden }) {
                    return k
                }
            }
            return UNDEF_STATE
        }

    companion object {
        fun isStaticSelector(it: BaseSelector): Boolean {
            return !it.hasAnnotation(UI.Visibility.Dynamic::class)
                    && !it.hasAnnotation(UI.Visibility.Backend::class)
                    && !it.hasAnyParentAnnotation(UI.Visibility.Backend::class)
                    && !it.hasAnyParentAnnotation(UI.Visibility.Dynamic::class)
                    && !it.hasAnyParentAnnotation(UI.Widgets.ValidationError::class)
                    && !it.hasAnnotation(UI.Widgets.ValidationError::class)
                    && !it.hasAnnotation(UI.Widgets.OptionItem::class)
                    && (it.property?.visibility == KVisibility.PUBLIC
                            || it.property == null
                        )
        }
        fun isSelfStaticSelector(it: BaseSelector): Boolean {
            return !it.hasAnnotation(UI.Visibility.Dynamic::class)
                    && !it.hasAnnotation(UI.Visibility.Backend::class)
                    && !it.hasAnnotation(UI.Widgets.ValidationError::class)
                    && !it.hasAnnotation(UI.Widgets.OptionItem::class)

                    && (it.property?.visibility == KVisibility.PUBLIC
                            || it.property == null
                        )
        }
    }
}