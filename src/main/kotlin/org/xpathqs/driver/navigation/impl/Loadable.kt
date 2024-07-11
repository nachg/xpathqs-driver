package org.xpathqs.driver.navigation.impl

import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.compose.ComposeSelector
import org.xpathqs.core.util.SelectorFactory.compose
import org.xpathqs.driver.constants.Global
import org.xpathqs.driver.executor.Decorator
import org.xpathqs.driver.extensions.*
import org.xpathqs.driver.navigation.NavExecutor
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.base.ILoadable
import org.xpathqs.driver.navigation.util.Loading
import org.xpathqs.driver.navigation.util.LoadingParser
import org.xpathqs.driver.navigation.util.getOneOfSelectors
import org.xpathqs.driver.navigation.util.getRootWithOnOfSelectors
import org.xpathqs.driver.page.Page
import java.time.Duration

open class Loadable(private val block: Block) : ILoadable {
    private var isLoading = false

    override val loading: Loading //by lazy {
        get() {
            return if (!isLoading) {
                isLoading = true
                (block as? ILoadable)?.loading
            } else {
                LoadingParser(block).parse()
            } ?: LoadingParser(block).parse()
    }
  //  }

    override fun waitForLoad(duration: Duration) {
        val text = if(block is Page) "страницы" else "блока"
        Log.action("Ожидаем Загрузки $text: ${block.name}") {
            val oneOf = block.getRootWithOnOfSelectors()
            val loading = if(oneOf != null) {
                Loadable(oneOf).loading
            } else loading

            if(loading.loadSelector != null) {
                loading.loadSelector!!.waitForVisible(duration)
                if(loading.loadSelector!!.isHidden) {
                    Log.error("Страница не была загружена")
                    throw Exception("Страница не была загружена")
                }
            } else if(loading.loadAllSelectors.isNotEmpty()) {
                loading.loadAllSelectors.waitForAllVisible(duration)
            } else if(loading.loadAnySelectors.isNotEmpty()) {
                loading.loadAnySelectors.waitForFirstVisibleOf(duration)
            } else {
                Log.warning("No Load Selector defined")
            }

            if(loading.loadNoneSelectors.isNotEmpty()) {
                Log.action("Waiting for none selectors to be visible and disappear") {
                    loading.loadNoneSelectors.waitForAllVisible(2.seconds)
                    loading.loadNoneSelectors.waitForAllDisappear(duration)
                }
            }

            if(block is Page) {
                navExecutor?.navigator?.prevCurrentPage = null
            }

            if(!loading.isLoaded) {
                loading.isLoaded
                Log.error("Страница не была загружена")
                throw Exception("Страница не была загружена")
            }
        }
    }

    override fun waitForDisappear(duration: Duration) {
        val text = if(block is Page) "страницы" else "блока"
        Log.action("Ожидаем исчезновения $text: ${block.name}") {
            val oneOf = block.getRootWithOnOfSelectors()
            val loading = if(oneOf != null) {
                Loadable(oneOf).loading
            } else loading

            if(loading.loadSelector != null) {
                loading.loadSelector!!.waitForDisappear(duration)
                if(loading.loadSelector!!.isVisible) {
                    Log.error("Страница не исчезла")
                }
            } else if(loading.loadAllSelectors.isNotEmpty()) {
                loading.loadAllSelectors.forEach {
                    if (it.isVisible) {
                        it.waitForDisappear(duration)
                    }
                }
            } else if(loading.loadAnySelectors.isNotEmpty()) {
                loading.loadAnySelectors.waitForFirstVisibleOf(duration)
            } else {
                Log.warning("No Load Selector defined")
            }

            if(!loading.isLoaded) {
                Log.error("Страница не была загружена")
            }
        }
    }

    private val navExecutor: NavExecutor
        get() {
            if(Global.executor is NavExecutor) return Global.executor as NavExecutor
            return (Global.executor as Decorator).origin as NavExecutor
        }
}