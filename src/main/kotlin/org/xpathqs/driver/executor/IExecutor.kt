package org.xpathqs.driver.executor

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.driver.core.IDriver
import org.xpathqs.driver.actions.IAction
import org.xpathqs.driver.actions.MakeVisibleAction
import org.xpathqs.driver.actions.SelectorInteractionAction
import org.xpathqs.driver.actions.WaitAction
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.wait
import org.xpathqs.driver.extensions.waitForFirstVisibleOf
import org.xpathqs.driver.widgets.IFormInput
import java.time.Duration

typealias ActionExecLambda = (IAction) -> Unit
typealias ActionExecMap = HashMap<String, ActionExecLambda>

interface IExecutor {
    fun execute(action: IAction)
    fun getElementsCount(selector: ISelector): Int
    fun isPresent(selector: ISelector): Boolean
    fun isAllPresent(selectors: Collection<BaseSelector>)
        = selectors.find { it.isHidden } == null

    fun getAttr(selector: BaseSelector, attr: String): String
    fun getAllAttrs(selector: BaseSelector): Collection<Pair<String,String>>
    fun getAttrs(selector: BaseSelector, attr: String): Collection<String>

    fun beforeAction(action: IAction) {}
    fun afterAction(action: IAction) {}

    fun hasActionHandler(action: IAction): Boolean
    fun getActionHandler(action: IAction): ActionExecLambda

    fun onPostCreate() {}

    val driver: IDriver
    val actions: ActionExecMap

    fun processBeforeActionExtensions(action: IAction) {
        if(action is SelectorInteractionAction && action !is WaitAction && action !is MakeVisibleAction) {
            if(action.beforeActionDelay != Duration.ZERO) {
                wait(action.beforeActionDelay, "beforeAction delay")
                Thread.sleep(action.beforeActionDelay.toMillis())
            }

            getKeyFromPropsMap(action, SelectorInteractionAction.BEFORE_ACTION_DELAY)?.let {
                it as Duration
                execute(WaitAction(it))
            }

            getKeyFromPropsMap(action, SelectorInteractionAction.BEFORE_ACTION_LAMBDA)?.let { lambda ->
                lambda as ()->Unit
                lambda.invoke()
            }
        }
    }

    private fun getKeyFromPropsMap(action: IAction, key: String): Any? {
        return if(action is SelectorInteractionAction) {
            action.on.customPropsMap[key] ?:
            (action.on.parents.filterIsInstance<IFormInput>()?.firstOrNull() as? BaseSelector)?.customPropsMap?.get(key)
        } else null
    }

    fun processAfterActionExtensions(action: IAction) {
        if(action is SelectorInteractionAction && action !is WaitAction && action !is MakeVisibleAction) {
            getKeyFromPropsMap(action, SelectorInteractionAction.AFTER_ACTION_DELAY)?.let {
                it as Duration
                execute(WaitAction(it))
            }

            getKeyFromPropsMap(action, SelectorInteractionAction.AFTER_ACTION_LAMBDA)?.let { lambda ->
                lambda as ()->Unit
                lambda.invoke()
            }

            getKeyFromPropsMap(action, SelectorInteractionAction.AFTER_ACTION_WAIT)?.let { waitConfig ->
                waitConfig as Pair<Collection<BaseSelector>, Duration>
                waitConfig.first.waitForFirstVisibleOf(waitConfig.second)
            }
        }
    }
}