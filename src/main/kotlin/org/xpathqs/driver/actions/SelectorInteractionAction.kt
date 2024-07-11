package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.style.StyleFactory
import org.xpathqs.log.style.StyledString
import java.time.Duration

abstract class SelectorInteractionAction(
    val on: BaseSelector,
    val beforeActionDelay: Duration = Duration.ZERO,
    val afterActionDelay: Duration = Duration.ZERO,
    val model: IBaseModel? = null,
    val checkBeforeAction: Boolean = true
) : IAction {

    override fun toStyledString(): StyledString {
        return StyleFactory.text("Interaction $name, with: ") + StyleFactory.selectorName(on.name)
    }

    companion object {
        const val BEFORE_ACTION_DELAY = "BEFORE_ACTION_DELAY"
        const val AFTER_ACTION_DELAY = "AFTER_ACTION_DELAY"

        const val BEFORE_ACTION_LAMBDA = "BEFORE_ACTION_LAMBDA"
        const val AFTER_ACTION_LAMBDA = "AFTER_ACTION_LAMBDA"

        const val AFTER_ACTION_WAIT = "AFTER_ACTION_WAIT"

        const val AFTER_AJAX_INPUT = "AFTER_AJAX_INPUT"
    }
}