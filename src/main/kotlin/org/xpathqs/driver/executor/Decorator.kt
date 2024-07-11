package org.xpathqs.driver.executor

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.driver.core.IDriver
import org.xpathqs.driver.actions.IAction
import org.xpathqs.driver.actions.SelectorInteractionAction
import org.xpathqs.driver.exceptions.XPathQsException
import org.xpathqs.log.Log
import org.xpathqs.driver.log.action
import org.xpathqs.driver.log.xpath
import org.xpathqs.driver.model.IBaseModel

open class Decorator(
    val origin: IExecutor
) : IExecutor {

    override fun execute(action: IAction) {
        Log.action(action) {
            if (hasActionHandler(action)) {
                beforeAction(action)
                executeConcreteAction(action)
                afterAction(action)
            } else {
                origin.execute(action)
            }
        }
    }

    override fun getElementsCount(selector: ISelector): Int {
        return origin.getElementsCount(selector)
    }

    override fun isPresent(selector: ISelector): Boolean {
        return origin.isPresent(selector)
    }

    override fun getAttr(selector: BaseSelector, attr: String, model: IBaseModel?): String {
        return origin.getAttr(selector, attr, model)
    }

    override fun getAllAttrs(selector: BaseSelector, model: IBaseModel?): Collection<Pair<String, String>> {
        return origin.getAllAttrs(selector)
    }

    override fun getAttrs(selector: BaseSelector, attr: String, model: IBaseModel?): Collection<String> {
        return origin.getAttrs(selector, attr)
    }

    override fun hasActionHandler(action: IAction): Boolean {
        if (actions.containsKey(action.name)) {
            return true
        }
        return origin.hasActionHandler(action)
    }

    override fun beforeAction(action: IAction) {
        origin.beforeAction(action)
    }

    override fun afterAction(action: IAction) {
        origin.afterAction(action)
    }

    override fun onPostCreate() {
        origin.onPostCreate()
    }

    override fun getActionHandler(action: IAction): ActionExecLambda {
        return actions[action.name] ?: origin.getActionHandler(action)
    }

    override val driver: IDriver
        get() = origin.driver

    override val actions: ActionExecMap
        get() = origin.actions

    protected open fun executeConcreteAction(action: IAction) {
        if (hasActionHandler(action)) {
            getActionHandler(action).invoke(action)
        } else {
            throw XPathQsException.ActionNotFound(action, this)
        }
    }

    inline fun<reified T> findOriginInstance(): T? {
        var check: Decorator? = origin as? Decorator
        while (check !is T && check != null) {
            check = (check as? Decorator)?.origin as? Decorator
        }
        return check as? T
    }
}