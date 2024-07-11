package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.driver.extensions.getDefaultModel
import org.xpathqs.driver.extensions.input
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.widgets.IFormInput
import org.xpathqs.log.Log
import kotlin.reflect.KMutableProperty

private const val VISIBILITY_WHEN = "VISIBILITY_WHEN"

class VisibleWhenNavigation : IBlockSelectorNavigation {
    override fun isSelfApply(elem: ISelector, navigator: INavigator, model: IBaseModel): Boolean {
        return (elem as? BaseSelector)?.customPropsMap?.get(VISIBILITY_WHEN) != null
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if (elem is BaseSelector) {
            if (elem.isVisible) {
                return true
            }
            elem.customPropsMap[VISIBILITY_WHEN]?.let { visibleWhen ->
                val (sel, values) = visibleWhen as Pair<BaseSelector, Any>
                (values as? Array<Object>)?.let { v ->
                    Log.action("Apply VisibleWhenNavigation") {
                        sel.smartInput(v.first().toString(), model = model)
                        sel.waitForVisible()
                    }
                    if (elem.isVisible) {
                        return true
                    }
                }

            }
        }
        return false
    }
}

fun <T : BaseSelector> T.visibleWhen(
    selector: BaseSelector,
    vararg values: Any
): T {
    this.customPropsMap[VISIBILITY_WHEN] = selector to values
    return this
}

fun BaseSelector.smartInput(
    value: String,
    default: Boolean = false,
    processInputValue: ((Any) -> Any)? = null,
    model: IBaseModel? = null
) {
    val processedValue = processInputValue?.invoke(value) ?: value
    IBaseModel.enableUiUpdate()

    this.getDefaultModel()?.let { m ->
        m.findPropBySel(this)?.let { p ->
            (p as? KMutableProperty<*>)?.let {
                val parent = m.findParent(m, p)
                if(default) {
                    val value = it.getter.call(parent) ?: value
                    val processedValue = processInputValue?.invoke(value) ?: value
                    it.setter.call(
                        parent,
                        processedValue
                    )
                } else {
                    it.setter.call(
                        parent,
                        processedValue
                    )
                }
                return
            }
        }
    }

    if(this is IFormInput) {
        this.input(processedValue.toString(), model)
        return
    }

    this.input(processedValue.toString())
}