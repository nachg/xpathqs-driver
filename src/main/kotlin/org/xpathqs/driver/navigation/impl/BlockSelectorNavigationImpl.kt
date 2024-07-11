package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.exceptions.XPathQsException
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.page.Page

class BlockSelectorNavigationImpl: IBlockSelectorNavigation {
    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) return true
        }
        ((elem as? BaseSelector)?.rootParent as? Page)?.addError(elem)

        Log.info("There is no valid navigation callback for the $elem")
        throw XPathQsException.UnableToNavigate(elem)
    }
}