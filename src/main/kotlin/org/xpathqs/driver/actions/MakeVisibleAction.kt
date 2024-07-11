package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.model.IBaseModel

class MakeVisibleAction (
    val to: BaseSelector,
    model: IBaseModel? = null,
) : SelectorInteractionAction(
    on = to,
    model = model
)