package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.findAllWithAnnotation
import org.xpathqs.core.selector.block.findWithAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.exceptions.XPathQsException
import org.xpathqs.driver.extensions.*
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.Edge
import org.xpathqs.driver.navigation.NavWrapper
import org.xpathqs.driver.navigation.Navigator
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import org.xpathqs.driver.navigation.base.*
import org.xpathqs.driver.navigation.util.IBlockNavigation
import org.xpathqs.driver.page.Page
import java.time.Duration

open class Navigable(
    protected val navigator: Navigator,
    protected val block: INavigable,
    protected val selectorNavigators: List<IBlockSelectorNavigation> = listOf(
        DirectSelectorNavigation(),
       // AutocloseNavigation(),
        ModelStateSelectorNavigation(),
        FormSelectorSelectOptionNavigation(),
        ModelStateParentNavigation(),
        CheckBoxNavigation(),
        CheckBoxLinkedNavigation(),
        SelectableNavigation(),
        ClickToBackNavigation(),
        InternalPageStateNavigation(),
        VisibilityMapInputNavigation(),
        VisibleWhenNavigation(),
        LinkedVisibilityNavigation(),
        FillToMakeVisibleOfNavigation(),
    //    TriggerModelNavigation(),
        BlockSelectorNavigationImpl()
    )
) : INavigable, IBlockSelectorNavigation {
    override fun addNavigation(
        to: INavigable,
        order: Int,
        selfState: Int,
        state: Int,
        globalState: Int,
        action: (() -> Unit)?
    ) {
        navigator.addEdge(
            Edge(
                from = NavWrapper.get(block, selfState, globalState),
                to = NavWrapper.get(to, state),
                _weight = order.toDouble(),
                action = action
            )
        )

        /*val targetPage = to as? Block
        targetPage?.let { page ->
            page.allInnerSelectors.firstOrNull {
                it.hasAnnotation(UI.Widgets.Back::class)
            }?.let { back ->
                navigator.addEdge(
                    Edge(
                        from = NavWrapper.get(to, state),
                        to = NavWrapper.get(block),
                        _weight = order.toDouble(),
                        action = {
                            back.click()
                        }
                    )
                )
            }
        }*/
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(((elem as? BaseSelector)?.rootParent as? Page)?.isLoadingError(elem) == true) {
            Log.error("Selector was not loaded")
            throw Exception("Selector was not loaded")
        }
        (elem as? BaseSelector)?.parents?.let { parents ->
            parents.reversed().forEach { parent ->
                if(parent.name.isNotEmpty()
                    && parent.xpath.isNotEmpty()
                    && parent.isHidden
                ) {
                    if((elem.rootParent as? Page)?.isLoadingError(elem) == true) {
                        Log.error("Selector was not loaded")
                        throw Exception("Selector was not loaded")
                    }

                    selectorNavigators.filter {
                        it.isSelfApply(parent, navigator, model)
                    }.forEach {
                        if(it.navigate(parent, navigator, model)) {
                            return@forEach
                        }
                    }
                    if(parent.isHidden) {
                        selectorNavigators.forEach {
                            if (it.navigate(parent, navigator, model)) {
                                return@forEach
                            }
                        }
                    }

                    if(parent.isHidden) {
                        Log.error("Navigation to the $parent was not completed")
                    }
                }
            }
        }

        if((elem as? BaseSelector)?.isHidden == true) {
            (elem as? BaseSelector)?.parents?.filterIsInstance<ISelectorNav>()?.forEach {
                it.navigateDirectly(elem)
                if(elem.isVisible) {
                    return true
                }
            }
        }

       return false
    }

    override fun navigate(state: Int) {
        if(this.block is IBlockNavigation) {
            if((this.block as? INavigableDetermination)?.isVisible == false) {
                if(this.block.selfNavigation.byCheckbox is BaseSelector) {
                    (this.block.selfNavigation.byCheckbox as BaseSelector).click()
                    (this.block as ILoadable).waitForLoad(Duration.ofSeconds(30))
                    return
                }
            }
        }

        if(this.block is Page) {
            try {
                val currentPage = navigator.currentPage
                val currentState = if(currentPage is IPageState) {
                    currentPage.pageState
                } else {
                    (this.block.findAnnotation<UI.Nav.Config>())?.defaultState ?: UNDEF_STATE
                }
                val navState = if(currentPage === this.block && state == UNDEF_STATE) {
                    currentState
                } else state

                if((currentPage as Block).hasAnnotation(UI.Nav.Autoclose::class)) {
                    currentPage.findAllWithAnnotation(UI.Widgets.ClickToFocusLost::class).forEach {
                        if(it.isVisible) {
                            it.click()
                        }
                    }
                    currentPage.waitForDisappear(
                        Duration.ofSeconds(2)
                    )
                    if(currentPage.isVisible) {
                        throw Exception("Unable to close Auto-Closable page $currentPage")
                    }
                    navigate(state)
                } else {
                    var from = NavWrapper.get(currentPage, currentState)
                    var to = NavWrapper.get(this.block, navState)
                    try {
                        navigator.navigate(
                            from,
                            to
                        )
                    } catch (e: XPathQsException.NoNavigation) {
                        currentPage.findWithAnnotation(UI.Widgets.Back::class)?.let { backButton ->
                            Log.info("Trying to navigate via back button")

                            var cp = currentPage
                            var bb = backButton
                            var cs = currentState

                            while(navigator.findPath(from, to) == null) {
                                cp as Block
                                bb.click()
                                cp.waitForDisappear()

                                cp = navigator.waitForCurrentPage()
                                val cs = if(cp is IPageState) {
                                    cp.pageState
                                } else {
                                    (this.block.findAnnotation<UI.Nav.Config>())?.defaultState ?: UNDEF_STATE
                                }
                                from = NavWrapper.get(cp, cs)
                                bb = (cp as Block).findWithAnnotation(UI.Widgets.Back::class) ?: break
                            }

                            navigator.navigate(
                                from,
                                to
                            )
                        }
                    }
                }
            } catch (e : XPathQsException.NoNavigation) {
                this.block.findWithAnnotation(UI.Nav.DeterminateBy::class)?.makeVisible()
            }
        }
    }
}

