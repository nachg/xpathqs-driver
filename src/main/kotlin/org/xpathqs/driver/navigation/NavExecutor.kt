package org.xpathqs.driver.navigation

import org.jgrapht.GraphPath
import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.findAnyParentAnnotation
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.findWithAnnotation
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.core.selector.selector.Selector
import org.xpathqs.driver.actions.IAction
import org.xpathqs.driver.actions.MakeVisibleAction
import org.xpathqs.driver.actions.SelectorInteractionAction
import org.xpathqs.driver.constants.Messages
import org.xpathqs.driver.exceptions.XPathQsException
import org.xpathqs.driver.executor.ActionExecMap
import org.xpathqs.driver.executor.CachedExecutor
import org.xpathqs.driver.executor.Decorator
import org.xpathqs.driver.executor.IExecutor
import org.xpathqs.driver.extensions.*
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.*
import org.xpathqs.driver.navigation.impl.AutocloseNavigation
import org.xpathqs.driver.navigation.impl.NavigableDetermination
import org.xpathqs.driver.page.Page
import java.time.Duration

open class NavExecutor(
    origin: IExecutor,
    val navigator: Navigator,
    val globalState: IGlobalState = NoGlobalState
) : Decorator(origin) {
    init {
        navigator.init(this)
    }

    fun refreshCache() {
        cachedExecutor.refreshCache()
    }

    val cachedExecutor: CachedExecutor by lazy {
        var e: IExecutor? = origin
        while(e !is CachedExecutor && e != null) {
            e = (e as? Decorator)?.origin
        }
        e as CachedExecutor
    }

    override val actions: ActionExecMap = ActionExecMap().apply {
        set(MakeVisibleAction(Selector()).name) {
            executeAction(it as MakeVisibleAction)
        }
    }

    override fun beforeAction(action: IAction) {
       // super.beforeAction(action)
        if(action is SelectorInteractionAction && action.checkBeforeAction) {
            Log.action(Messages.NavExecutor.beforeAction(action)) {
            //    val ann = (action.on.rootParent as? BaseSelector)?.findAnnotation<NavOrder>()
                AutocloseNavigation().getModals(action.on)?.forEach {
                    it.close()
                }

                var curPage = action.currentPage
                val sourcePage = action.on.rootParent as? INavigable

                val blockIsVisible = (sourcePage !is Page
                    && (sourcePage as? INavigableDetermination)?.isVisible == true)

                if(sourcePage != null && curPage != sourcePage && !blockIsVisible && sourcePage is Page) {
                    Log.action("Navigation required") {
                        Log.action("trying to autoclose current block") {

                            if(sourcePage.isLoadingError(action.on)) {
                                throw Exception("beforeAction LoadingError")
                            }

                            if((curPage as? Block)?.hasAnnotation(UI.Nav.Autoclose::class) == true) {
                                val closeBtn = (curPage as Block).findWithAnnotation(UI.Widgets.Back::class) ?:
                                (curPage as Block).findWithAnnotation(UI.Widgets.ClickToFocusLost::class)

                                closeBtn?.click()
                                if(curPage is Page) {
                                    (curPage as Page).waitForDisappear(Duration.ofSeconds(2))
                                    repeat(10) {
                                        val cp = try {
                                            action.currentPage
                                        } catch (e: Exception) {
                                            null
                                        }
                                        if(cp != null && cp != curPage) {
                                            return@repeat
                                        }
                                        wait(500.ms, "Waiting for 500ms for navigator to change the page")
                                        refreshCache()
                                    }
                                }
                                curPage = action.currentPage
                                (curPage as? ILoadable)?.waitForLoad(Duration.ofSeconds(10))
                            }
                        }


                        val ann = action.on.findAnnotation<UI.Visibility.State>() ?: action.on.findAnyParentAnnotation<UI.Visibility.State>()
                        val state = ann?.value ?: UI.Visibility.UNDEF_STATE
                        navigator.navigate(NavWrapper(curPage as INavigable), NavWrapper.get(sourcePage, state))
                    }

                    val endPage = action.currentPage as Page
                    Log.info("Текущая страница: " + endPage.name)
                }

                (action.on as? Block)?.let {
                    if(it.isBlank) {
                        it.staticBlockSelectors.firstOrNull {
                            it.isHidden
                        }?.makeVisible()
                    }
                }

                if(action.on.isHidden) {
                    action.on.parents.reversed().forEach {
                        (it as? INavigable)?.navigate()
                    }
                }

                if(action.on.isHidden) {
                    var navigations: GraphPath<NavWrapper, Edge>? =
                        if(action.on.base is INavigable) {
                            navigator.findPath(NavWrapper(curPage as INavigable), NavWrapper.get(action.on.base as INavigable))
                        } else null

                    if(navigations != null) {
                        navigations.edgeList.forEach {
                            if(it.action != null) {
                                it.action!!()
                                wait(500.ms, "short delay after navigation action")
                                refreshCache()
                            }
                            (it.to.nav as? ILoadable)?.waitForLoad(Duration.ofSeconds(30))
                        }
                    } else {
                        action.on.parents.filterIsInstance<IBlockSelectorNavigation>().reversed().forEach {
                            try {
                                it.navigate(
                                    action.on,
                                    navigator,
                                    IBaseModel()
                                )
                            } catch (e: XPathQsException.UnableToNavigate) {
                               Log.error(e.message!!)
                            }
                        }
                    }
                }

                processBeforeActionExtensions(action)

                if(action.on.isHidden && action is MakeVisibleAction) {
                    (action.currentPage as? NavigableDetermination)?.navigate(
                        elem = action.on,
                        navigator = navigator,
                        model = IBaseModel()
                    )
                }
            }
        }
    }

    override fun afterAction(action: IAction) {
        super.afterAction(action)
        processAfterActionExtensions(action)
    }

    protected fun executeAction(action: MakeVisibleAction) {
        //the processing of this action now in "before" interaction
    }

    override fun getAttr(selector: BaseSelector, attr: String, model: IBaseModel?): String {
        selector.makeVisible(model)
        return super.getAttr(selector, attr, model)
    }

    private val SelectorInteractionAction.currentPage: Page?
        get() = this.model?.currentPage ?: navigator.currentPage as? Page
}