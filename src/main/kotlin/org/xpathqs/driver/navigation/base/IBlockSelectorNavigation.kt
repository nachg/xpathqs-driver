package org.xpathqs.driver.navigation.base

import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.driver.model.IBaseModel

interface IBlockSelectorNavigation {
    fun navigate(
        elem: ISelector,
        navigator: INavigator,
        model: IBaseModel
    ) : Boolean

    fun isSelfApply(
        elem: ISelector,
        navigator: INavigator,
        model: IBaseModel
    ) : Boolean = false
}