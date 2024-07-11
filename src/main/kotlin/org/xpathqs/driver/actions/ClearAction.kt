package org.xpathqs.driver.actions

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.driver.model.IBaseModel

open class ClearAction(
    selector: BaseSelector,
    val clickSelector: BaseSelector,
    model: IBaseModel? = null,
) : SelectorInteractionAction(
    on = selector,
    model = model
)