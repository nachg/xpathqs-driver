package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.base.hasAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.block.allInnerSelectors
import org.xpathqs.core.selector.extensions.isChildOf
import org.xpathqs.core.selector.extensions.parents
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.extensions.isHidden
import org.xpathqs.driver.extensions.isVisible
import org.xpathqs.driver.extensions.waitForVisible
import org.xpathqs.driver.model.IBaseModel
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.base.IBlockSelectorNavigation
import org.xpathqs.driver.navigation.base.INavigator
import org.xpathqs.driver.navigation.impl.PageState.Companion.isSelfStaticSelector
import org.xpathqs.driver.page.Page
import org.xpathqs.driver.widgets.CheckBox
import java.time.Duration

class CheckBoxNavigation : IBlockSelectorNavigation {

    override fun isSelfApply(elem: ISelector, navigator: INavigator, model: IBaseModel): Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }

            (elem.rootParent as? Block)?.allInnerSelectors?.filter {
                it.hasAnnotation(UI.Widgets.Checkbox::class)
            }?.forEach { cb ->
                if (cb != null && cb is CheckBox) {
                    val ann = cb.findAnnotation<UI.Widgets.Checkbox>()
                    if (ann != null) {
                        if (ann.visibilityOf != BaseSelector::class) {
                            val block = ann.visibilityOf.objectInstance
                            if (block != null && (elem.isChildOf(block) || elem === block)) {
                                return true
                            }

                        } else {
                            val checkedBlock = ann.onChecked.objectInstance
                            val uncheckedBlock = ann.onUnchecked.objectInstance

                            if (checkedBlock != null && uncheckedBlock != null) {
                                if (elem === checkedBlock) {
                                    return true
                                } else if (elem === uncheckedBlock) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    override fun navigate(elem: ISelector, navigator: INavigator, model: IBaseModel) : Boolean {
        if(elem is BaseSelector) {
            if(elem.isVisible) {
                return true
            }
            val annotations = (elem.rootParent as? Block)?.allInnerSelectors?.filter {
                it.hasAnnotation(UI.Widgets.Checkbox::class)
            }

            val selfAnnotations = annotations?.filter { cb ->
                if (cb != null && cb is CheckBox) {
                    val ann = cb.findAnnotation<UI.Widgets.Checkbox>()
                    if (ann != null) {
                        if (ann.visibilityOf != BaseSelector::class) {
                            val block = ann.visibilityOf.objectInstance
                            if (block != null && (elem.isChildOf(block) || elem === block)) {
                                return@filter true
                            }

                        } else {
                            val checkedBlock = ann.onChecked.objectInstance
                            val uncheckedBlock = ann.onUnchecked.objectInstance

                            if (checkedBlock != null && uncheckedBlock != null) {
                                if (elem === checkedBlock) {
                                    return@filter true
                                } else if (elem === uncheckedBlock) {
                                    return@filter true
                                }
                            }
                        }
                    }
                }
                false
            }

            val resAnnotations = if(selfAnnotations?.isNotEmpty() == true) selfAnnotations else annotations
            resAnnotations?.forEach { cb ->
                if (cb != null && cb is CheckBox) {
                    val ann = cb.findAnnotation<UI.Widgets.Checkbox>()
                    if (ann != null) {
                        var wasFound = false
                        if (ann.visibilityOf != BaseSelector::class) {
                            val block = ann.visibilityOf.objectInstance
                            if (block != null && (elem.isChildOf(block) || elem === block)) {
                                Log.action("Apply CheckBoxNavigation") {
                                    cb.check()
                                    wasFound = true
                                }
                            }

                        } else {
                            val checkedBlock = ann.onChecked.objectInstance
                            val uncheckedBlock = ann.onUnchecked.objectInstance

                            if (checkedBlock != null && uncheckedBlock != null) {
                                val isChildForBlock = elem.isChildOf(checkedBlock) && isSelfStaticSelector(elem)
                                if (isChildForBlock || elem === checkedBlock) {
                                    Log.action("Apply CheckBoxNavigation") {
                                        cb.check()
                                        wasFound = true
                                    }
                                } else if (elem.isChildOf(uncheckedBlock) || elem === uncheckedBlock) {
                                    Log.action("Apply CheckBoxNavigation") {
                                        cb.uncheck()
                                        wasFound = true
                                    }
                                }
                            }
                        }
                        if (wasFound) {
                            Log.info("Selector was found")
                            elem.waitForVisible(Duration.ofSeconds(1))
                            if(elem.isHidden) {
                                if(elem.parents.flatMap {
                                    it.annotations
                                }.find {
                                    if(it is UI.Visibility.Dynamic) {
                                        it.ajax
                                    } else false
                                } != null) {
                                    (elem.rootParent as? Page)?.addError(elem)
                                }

                                Log.error("Selector is still hidden even after checkbox navigation")
                            }
                        }

                        if (elem.isVisible) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }
}