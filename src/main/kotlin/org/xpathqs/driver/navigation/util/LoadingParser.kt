package org.xpathqs.driver.navigation.util

import org.xpathqs.core.selector.NullSelector
import org.xpathqs.core.selector.base.*
import org.xpathqs.core.selector.block.*
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.actions.WaitAction
import org.xpathqs.driver.constants.Global
import org.xpathqs.driver.executor.CachedExecutor
import org.xpathqs.driver.executor.Decorator
import org.xpathqs.driver.extensions.ms
import org.xpathqs.driver.extensions.wait
import org.xpathqs.driver.navigation.NavExecutor
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.WaitForLoadEnum
import org.xpathqs.driver.navigation.base.INavigableDetermination
import org.xpathqs.driver.navigation.base.IPageState
import org.xpathqs.driver.navigation.impl.PageState

class LoadingParser(
    private val block: Block
) {
    fun parse(): Loading {
        var selectors = block.allInnerSelectors + block.allInnerSelectorBlocks.filter { it.base !is NullSelector }
        if(block is IPageState) {
            var state = (block as IPageState).pageState

            if(state == UI.Visibility.UNDEF_STATE) {
                var repeatCount = 0
                while (repeatCount < 5 && state == UI.Visibility.UNDEF_STATE) {
                    wait(500.ms, "delay in LoadingParser before refreshing the page")
                    (Global.executor as Decorator).findOriginInstance<CachedExecutor>()?.refreshCache()
                    repeatCount++
                    state = (block as IPageState).pageState
                }
            }

            selectors = selectors.filter {
                val ann = it.findAnyParentAnnotation<UI.Visibility.State>()
                ann == null || state == ann.value
            }
        }

        val anyColl = ArrayList<BaseSelector>()
        val allColl = ArrayList<BaseSelector>()
        val noneColl = ArrayList<BaseSelector>()
        val oneOfSelectors = block.getOneOfSelectors()

        block.findAnnotation<UI.Nav.WaitForLoad>()?.let {
            if(it.type == WaitForLoadEnum.LOAD_SELF) {
                return Loading(loadSelector = block)
            }
        }

        if(oneOfSelectors.isNotEmpty()) {
            return Loading(loadAnySelectors = oneOfSelectors)
        }

        selectors.forEach { sel ->
            val ann = sel.findAnyParentAnnotation<UI.Nav.WaitForLoad>()
                ?: sel.findAnnotation<UI.Nav.WaitForLoad>()
            if(ann != null) {
                when(ann.type) {
                    WaitForLoadEnum.LOAD_ANY -> anyColl.add(sel)
                    WaitForLoadEnum.LOAD_ALL -> allColl.add(sel)
                    WaitForLoadEnum.LOAD_NONE -> {noneColl.add(sel)}
                    WaitForLoadEnum.LOAD_SELF -> {}
                    WaitForLoadEnum.LOAD_ERROR -> {}
                }
            }
        }

        if(anyColl.isEmpty() && allColl.isEmpty()) {
            (block as? INavigableDetermination)?.determination?.let {
                if(it.exist.isNotEmpty()) {
                    allColl.addAll(it.exist)
                }
            }
        }

        val filteredAll = allColl.filter {
            !it.hasAnnotation(UI.Visibility.Dynamic::class)
        }

        return if(anyColl.isNotEmpty()) {
            Loading(
                loadAnySelectors = anyColl,
                loadNoneSelectors = noneColl
            )
        } else if(filteredAll.isNotEmpty()) {
            Loading(
                loadAllSelectors = filteredAll,
                loadNoneSelectors = noneColl
            )

        } else {
            //BackendGroup ...
            Loading(
                loadAllSelectors = if(block is IPageState) {
                        selectors
                    } else {
                        (block as INavigableDetermination).determination.exist.ifEmpty { selectors }
                    },
                loadNoneSelectors = noneColl
            )
        }
    }
}

fun Block.getRootWithOnOfSelectors(): Block? {
    var res: Block? = this

    while (res?.getOneOfSelectors()?.isEmpty() == true) {
        res = res.base as? Block
    }

    return res
}

fun Block.getOneOfSelectors() : Collection<BaseSelector> {
    val oneOfSelectors = ArrayList<BaseSelector>()

    selectors.forEach {
        if(it.hasAnnotation(UI.Visibility.OneOf::class)) {
            oneOfSelectors.add(it)
        }
    }

    selectorBlocks.forEach {
        if(it.hasAnnotation(UI.Visibility.OneOf::class)) {
            oneOfSelectors.addAll(it.allInnerSelectors)
        }
    }

    if(selectors.isEmpty() && selectorBlocks.size == 1 && oneOfSelectors.isEmpty()) {
        return selectorBlocks.first().getOneOfSelectors()
    }

    return oneOfSelectors
}