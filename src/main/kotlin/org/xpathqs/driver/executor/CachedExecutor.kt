package org.xpathqs.driver.executor

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.selector.Selector
import org.xpathqs.driver.actions.*
import org.xpathqs.driver.cache.ICache
import org.xpathqs.driver.constants.Global
import org.xpathqs.driver.extensions.count
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.log.Log
import org.xpathqs.driver.log.action
import org.xpathqs.driver.log.xpath
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.Navigator
import org.xpathqs.driver.navigation.base.INavigator
import java.time.Duration

open class CachedExecutor(
    origin: IExecutor,
    val cache: ICache,
    var nav: INavigator? = null
) : Decorator(origin) {

    private var needRefreshCache = true

    val actual: Boolean
        get() = !needRefreshCache

    private val actionHandlerCache = ActionExecMap().apply {
        set(WaitForSelectorAction(Selector()).name) {
            executeAction(it as WaitForSelectorAction)
        }
        set(WaitForFirstSelectorAction(listOf(Selector())).name) {
            executeAction(it as WaitForFirstSelectorAction)
        }
        set(WaitForAllSelectorAction(listOf(Selector())).name) {
            executeAction(it as WaitForAllSelectorAction)
        }
        set(WaitForAllSelectorDisappearAction(listOf(Selector())).name) {
            executeAction(it as WaitForAllSelectorDisappearAction)
        }
        set(WaitForSelectorDisappearAction(Selector()).name) {
            executeAction(it as WaitForSelectorDisappearAction)
        }
        set(WaitForSelectorCountAction(Selector(), 0).name) {
            executeAction(it as WaitForSelectorCountAction)
        }
    }

    override fun isPresent(selector: ISelector): Boolean {
        checkCache()
        return cache.isPresent(selector.toXpath())
    }

    open fun refreshCache() {
        Log.action("Trigger Cache refresh") {
            cache.update(driver.pageSource)
            if(nav is Navigator && nav != null) {
                (nav as Navigator).prevCurrentPage = null
            }
            needRefreshCache = false
        }
    }

    protected open fun executeAction(action: WaitForSelectorAction) {
        waitHelper({ action.selector.isHidden }, action.timeout)
    }

    protected open fun executeAction(action: WaitForFirstSelectorAction) {
        Log.action("WaitForFirstSelectorAction") {
            waitHelper(
                {
                    action.selectors.firstOrNull {
                        Log.info("${it.name} isVisible: ${it.isVisible}")
                        it.isVisible
                    } == null
                },
                action.timeout
            )
        }
    }

    protected open fun executeAction(action: WaitForAllSelectorAction) {
        Log.action("WaitForAllSelectorAction") {
            waitHelper(
                {
                    action.selectors.firstOrNull {
                        Log.info("${it.name} isVisible: ${it.isVisible}")
                        it.isHidden
                    } != null
                },
                action.timeout
            )
        }
    }

    protected open fun executeAction(action: WaitForAllSelectorDisappearAction) {
        Log.action("WaitForAllSelectorDisappearAction") {
            waitHelper(
                {
                    action.selectors.firstOrNull {
                        Log.info("${it.name} isHidden: ${it.isHidden}")
                        it.isVisible
                    } != null
                },
                action.timeout
            )
        }
    }

    protected open fun executeAction(action: WaitForSelectorDisappearAction) {
        waitHelper({ action.selector.isVisible }, action.timeout)
    }

    protected open fun executeAction(action: WaitForSelectorCountAction) {
        waitHelper({
            val c = action.selector.count
            Log.trace("${action.selector.name} count: $c")
            !action.isWaitCompleted(c)
        }, action.timeout)
    }

    private fun waitHelper(func: () -> Boolean, duration: Duration): Boolean {
        val t1 = System.currentTimeMillis()
        fun timeoutNotExpired() = !isTimeoutExpired(t1, duration)

        while (func() && timeoutNotExpired()) {
            execute(
                WaitAction(
                    timeout =  Global.REFRESH_CACHE_TIMEOUT,
                    logMessage = "waitHelper wait"
                )
            )
            refreshCache()
        }
        Log.info("Completed waitHelper")
        return timeoutNotExpired()
    }

    private fun isTimeoutExpired(startTime: Long, duration: Duration): Boolean {
        return System.currentTimeMillis() - startTime > duration.toMillis()
    }

    override fun hasActionHandler(action: IAction): Boolean {
        if (!actionHandlerCache.containsKey(action.name)) {
            return origin.hasActionHandler(action)
        }
        return true
    }

    override fun getActionHandler(action: IAction): ActionExecLambda {
        return actionHandlerCache[action.name]
            ?: origin.getActionHandler(action)
    }

    override fun getElementsCount(selector: ISelector): Int {
        return cache.getElementsCount(selector.toXpath())
    }

    override fun getAttr(selector: BaseSelector, attr: String, model: IBaseModel?): String {
        return Log.action("Get '$attr' of '${selector}'") {
            Log.xpath(selector)
            checkCache()
            cache.getAttribute(selector.toXpath(), attr)
        }
    }

    override fun getAllAttrs(selector: BaseSelector, model: IBaseModel?): Collection<Pair<String, String>> {
        return Log.action("Get all attributes of '${selector}'") {
            Log.xpath(selector)
            checkCache()
            cache.getAttributes(selector.toXpath())
        }
    }

    override fun getAttrs(selector: BaseSelector, attr: String, model: IBaseModel?): Collection<String> {
        return Log.action("Get all '$attr' of '${selector}'") {
            Log.xpath(selector)
            checkCache()
            cache.getAttributes(selector.toXpath(), attr)
        }
    }

    open fun invalidateCache() {
        Log.trace("Cache marked as invalidated")
        needRefreshCache = true
        (nav as Navigator).prevCurrentPage = null
    }

    protected open fun checkCache() {
        if(needRefreshCache) {
            Log.trace("Cache is invalid, updating...")
            refreshCache()
        }
    }

    override fun afterAction(action: IAction) {
        super.afterAction(action)
        if(action is SelectorInteractionAction && action !is MakeVisibleAction) {
            invalidateCache()
        } else {
            needRefreshCache = false
        }
    }
}