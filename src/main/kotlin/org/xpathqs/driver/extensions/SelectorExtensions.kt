package org.xpathqs.driver.extensions

import org.xpathqs.core.reflection.freeze
import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectorBlocks
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.extensions.core.clone
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.selector.Selector
import org.xpathqs.driver.actions.*
import org.xpathqs.driver.actions.SelectorInteractionAction.Companion.AFTER_ACTION_DELAY
import org.xpathqs.driver.actions.SelectorInteractionAction.Companion.AFTER_ACTION_LAMBDA
import org.xpathqs.driver.actions.SelectorInteractionAction.Companion.BEFORE_ACTION_DELAY
import org.xpathqs.driver.actions.SelectorInteractionAction.Companion.BEFORE_ACTION_LAMBDA
import org.xpathqs.driver.constants.Global
import org.xpathqs.driver.executor.CachedExecutor
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.model.default
import org.xpathqs.driver.navigation.base.IModelBlock
import org.xpathqs.driver.navigation.impl.PageState.Companion.isSelfStaticSelector
import org.xpathqs.driver.selector.NearSelector
import org.xpathqs.driver.selector.SecretInput
import org.xpathqs.driver.widgets.IFormInput
import org.xpathqs.driver.widgets.IFormRead
import org.xpathqs.log.Log
import java.time.Duration

val <T : BaseSelector> T.isVisible: Boolean
    get() {
        if(this is Block && this.isBlank) {
            if(this.staticBlockSelectors.isEmpty()) {
                Log.error("Impossible to determinate the visibility of block")
                this.allInnerSelectors.firstOrNull {
                    it.isVisible
                } != null
            }
            return this.staticBlockSelectors.firstOrNull { it.isHidden } == null
        }
        return Global.executor.isPresent(this)
    }

val <T : BaseSelector> T.isHidden: Boolean
    get() = !isVisible

val <T : BaseSelector> T.isDisabled: Boolean
    get() {
        return this.getAllAttrs().find { it.first == "disabled" } != null
    }

fun <T : BaseSelector> T.waitForVisible(duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT): T {
    Global.executor.execute(
        WaitForSelectorAction(this, duration)
    )
    return this
}

fun <T : BaseSelector> Collection<T>.waitForFirstVisibleOf(duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT) {
    Global.executor.execute(
        WaitForFirstSelectorAction(this, duration)
    )
}

fun <T : BaseSelector> Collection<T>.waitForAllVisible(duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT) {
    Global.executor.execute(
        WaitForAllSelectorAction(this, duration)
    )
}

fun <T : BaseSelector> Collection<T>.waitForAllDisappear(duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT) {
    Global.executor.execute(
        WaitForAllSelectorDisappearAction(this, duration)
    )
}

fun wait(duration: Duration, logMsg: String) {
    Global.executor.execute(
        WaitAction(
            timeout = duration,
            logMessage = logMsg
        )
    )
}

fun <T : BaseSelector> T.waitForElementsCount(
    count: Int = -1,
    moreThen: Int = -1,
    lessThen: Int = -1,
    duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT
): T {
    Global.executor.execute(
        WaitForSelectorCountAction(
            this,
            expected = count,
            moreThen = moreThen,
            lessThen = lessThen,
            timeout = duration
        )
    )
    return this
}

fun <T : BaseSelector> T.waitForDisappear(duration: Duration = Global.WAIT_FOR_ELEMENT_TIMEOUT): T {
    Global.executor.execute(
        WaitForSelectorDisappearAction(this, duration)
    )
    return this
}

fun <T : BaseSelector> T.click(
    moveMouse: Boolean = false,
    model: IBaseModel? = null,
    checkBeforeAction: Boolean = true
): T {
    Global.executor.execute(
        ClickAction(
            on = this,
            moveMouse = moveMouse,
            model = model,
            checkBeforeAction = checkBeforeAction
        )
    )
    return this
}

fun <T : BaseSelector> T.input(value: String, clear: Boolean = true, model: IBaseModel? = null): T {
    val beforeDelay = this.customPropsMap[BEFORE_ACTION_DELAY] as? Duration
        ?: (this.parents.filterIsInstance<IFormInput>().firstOrNull() as? BaseSelector)?.customPropsMap?.get(BEFORE_ACTION_DELAY) as? Duration
        ?: Duration.ZERO

    val afterDelay = this.customPropsMap[AFTER_ACTION_DELAY] as? Duration
        ?: (this.parents.filterIsInstance<IFormInput>().firstOrNull() as? BaseSelector)?.customPropsMap?.get(AFTER_ACTION_DELAY) as? Duration
        ?: Duration.ZERO

    Global.executor.execute(
        InputAction(
            text = value,
            to = this,
            model = model,
            clearBeforeInput = clear,
            beforeActionDelay = beforeDelay,
            afterActionDelay = afterDelay
        )
    )
    return this
}

fun <T : BaseSelector> T.file(value: String): T {
    Global.executor.execute(
        InputFileAction(value, this)
    )
    return this
}

fun <T : BaseSelector> T.clear(clickSelector: BaseSelector? = null, model: IBaseModel? = null): T {
    Global.executor.execute(
        ClearAction(this, clickSelector ?: this, model = model)
    )
    return this
}

val <T : BaseSelector> T.text: String
    get() = getAttr(Global.TEXT_ARG)

val <T : BaseSelector> T.int: Int?
    get() = text.filter { it.isDigit() }.toIntOrNull()

val <T : BaseSelector> T.textItems: Collection<String>
    get() = getAttrs(Global.TEXT_ARG)

val <T : BaseSelector> T.value: String
    get() = getAttr("value")

val <T : BaseSelector> T.cls: String
    get() = getAttr("class")

val <T : BaseSelector> T.stringValue: String
    get() {
        return if(this is IFormRead) {
            this.readString()
        } else {
            this.text.ifEmpty { this.value }
        }
    }

val <T : BaseSelector> T.isChecked: Boolean
    get() {
    return return try {
        getAttr("checked")
        true
    } catch (e: Exception) {
        false
    }
}


fun <T : BaseSelector> T.getAttr(name: String, model: IBaseModel? = null) =
    Global.executor.getAttr(this, name, model)

fun <T : BaseSelector> T.getAllAttrs() =
    Global.executor.getAllAttrs(this)

fun <T : BaseSelector> T.getAttrs(name: String) =
    Global.executor.getAttrs(this, name)

val <T : BaseSelector> T.count: Int
    get() = Global.executor.getElementsCount(this)

fun <T : BaseSelector> T.isSecret(): Boolean {
    return this is SecretInput
}

fun <T : BaseSelector> T.makeVisible(model: IBaseModel? = null): T {
    Global.executor.execute(
        MakeVisibleAction(
            to = this,
            model = model
        )
    )
    return this
}

fun <T : BaseSelector> T.screenshot(boundRect: Boolean=true): T {
    Global.executor.execute(
        ScreenShotAction(this, boundRect)
    )
    return this
}

infix fun <T : BaseSelector> T.near(sel: Selector): NearSelector {
    //if(Global.executor is CachedExecutor) {
        return NearSelector((Global.executor as CachedExecutor).cache, this.clone().freeze(), sel.clone().freeze())
    //}
   // throw Exception("Near works only for CachedExecutors")
}

fun <T : BaseSelector> T.afterActionDelay(duration: Duration): T {
    this.customPropsMap[AFTER_ACTION_DELAY] = duration
    return this
}

fun <T : BaseSelector> T.beforeActionDelay(duration: Duration): T {
    this.customPropsMap[BEFORE_ACTION_DELAY] = duration
    return this
}

fun <T : BaseSelector> T.beforeAction(lambda: ()->Unit): T {
    this.customPropsMap[BEFORE_ACTION_LAMBDA] = lambda
    return this
}

fun <T : BaseSelector> T.afterAction(lambda: ()->Unit): T {
    this.customPropsMap[AFTER_ACTION_LAMBDA] = lambda
    return this
}

fun <T : BaseSelector> T.ajaxInput(
    onSuccess: BaseSelector,
    onError: BaseSelector
): T {
    this.customPropsMap[SelectorInteractionAction.AFTER_AJAX_INPUT] = onSuccess to onError
    return this
}

fun <T : BaseSelector> T.afterActionWait(sel: BaseSelector, duration: Duration = Duration.ofSeconds(10)): T {
    return afterActionWait(listOf(sel), duration)
}

fun <T : BaseSelector> T.afterActionWait(selectors: List<BaseSelector>, duration: Duration = Duration.ofSeconds(10)): T {
    return afterAction {
        selectors.waitForAllVisible(duration)
    }
}

fun <T : BaseSelector> T.getDefaultModel(): IBaseModel? {
    parents.filterIsInstance<IModelBlock<*>>().firstOrNull()?.apply {
        var m = invoke()
        return m?.default()
    }
    return null
}