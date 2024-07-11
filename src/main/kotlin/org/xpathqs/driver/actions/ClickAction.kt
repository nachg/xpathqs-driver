package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.constants.Messages
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.style.StyledString

open class ClickAction(
    on: BaseSelector,
    val moveMouse: Boolean = true,
    model: IBaseModel? = null,
    checkBeforeAction: Boolean = true
) : SelectorInteractionAction(
    on = on,
    model = model,
    checkBeforeAction = checkBeforeAction
) {
    override fun toStyledString() =
        StyledString.fromDefaultFormatString(
            Messages.Actions.Click.toString,
            on.name
        )
}