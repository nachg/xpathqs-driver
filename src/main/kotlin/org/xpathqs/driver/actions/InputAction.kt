package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.constants.Messages
import org.xpathqs.driver.extensions.isSecret
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.style.StyleFactory
import org.xpathqs.log.style.StyledString
import java.time.Duration

open class InputAction(
    var text: String,
    val to: BaseSelector,
    val clearBeforeInput: Boolean = true,
    val validateInput: Boolean = true,
    val clickSelector: BaseSelector = to,
    model: IBaseModel? = null,
    beforeActionDelay: Duration = Duration.ZERO,
    afterActionDelay: Duration = Duration.ZERO,
) : SelectorInteractionAction(
    on = to,
    beforeActionDelay = beforeActionDelay,
    afterActionDelay = afterActionDelay,
    model = model
) {
    override fun toStyledString(): StyledString {
        val value = if (to.isSecret() && text.isNotEmpty()) "'******'" else "'$text'"
        return StyledString.fromDefaultFormatString(
            Messages.Actions.Input.toString,
            value, to.name
        )
    }
}