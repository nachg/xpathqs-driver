package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.block.findAllWithAnnotation
import org.xpathqs.core.selector.extensions.isParentOf
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.seconds
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.log.Log
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.driver.model.IModelStates
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberFunctions

class AutocloseNavigation : IBlockSelectorNavigation {

    override fun isSelfApply(elem: ISelector, navigator: INavigator, model: IBaseModel): Boolean {
        (elem as? BaseSelector)?.let {
            return getModals(elem).isNotEmpty()
        }
        return false
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            getModals(elem).forEach {
                Log.action("Applying AutocloseNavigation") {
                    it.close()
                }
            }
        }

        return false
    }

    fun getModals(elem: BaseSelector, checkToClose: Boolean = true) : Collection<IModalDialog> {
        val result = ArrayList<IModalDialog>()
        (elem.rootParent as? Block)?.let { root ->
            val overlapped = root.findAllWithAnnotation(UI.Visibility.Dynamic::class).filter { sel ->
                sel.findAnnotation<UI.Visibility.Dynamic>()!!.overlapped
            }

            var foundParent = false
            overlapped.forEach {
                it.parents.forEach {
                    if(it is IModalDialog && it.isParentOf(elem)) {
                        foundParent = true
                    }
                }
            }

            overlapped.forEach {
                it.parents.forEach {
                    if(it is IModalDialog && (!checkToClose || !foundParent)) {
                        result.add(it)
                    }
                }
            }
        }
        return result
    }
}