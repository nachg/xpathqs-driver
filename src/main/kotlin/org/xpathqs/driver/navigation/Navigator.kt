package org.xpathqs.driver.navigation

import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.core.selector.base.findAnnotation
import org.xpathqs.core.selector.block.Block
import org.xpathqs.core.selector.extensions.rootParent
import org.xpathqs.driver.constants.Messages
import org.xpathqs.driver.exceptions.XPathQsException
import org.xpathqs.driver.executor.CachedExecutor
import org.xpathqs.driver.executor.IExecutor
import org.xpathqs.driver.extensions.ms
import org.xpathqs.driver.extensions.wait
import org.xpathqs.driver.navigation.annotations.NavOrderType
import org.xpathqs.log.Log
import org.xpathqs.driver.navigation.annotations.UI
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import org.xpathqs.driver.navigation.base.*
import org.xpathqs.driver.navigation.util.NavigationParser
import org.xpathqs.driver.page.*
import java.time.Duration
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations

open class Navigator : INavigator {
    private lateinit var executor: IExecutor
    private val pages = ArrayList<INavigableDetermination>()
    private val blocks = ArrayList<INavigable>()
    private val edges = ArrayList<Edge>()
    private val graph = SimpleDirectedWeightedGraph<NavWrapper, Edge>(
        Edge::class.java
    )
    private val shortestPath = DijkstraShortestPath(graph)

    private val firstTimeDetection = HashSet<String>()
    //private val prevPage: INavigableDetermination? = null
    
    fun register(page: INavigable) {
        page as ISelector
        if(!pages.contains(page)) {
            //Log.info(selectorName(page.name) + " was added to the " + keyword(this::class.simpleName!!))
            if(page is Page && page is INavigableDetermination) {
                pages.add(page)

                val ann = page::class.findAnnotations<UI.Nav.PathTo>().filter {
                    it.bySubmit != Block::class
                }
                if(ann.isNotEmpty()) {
                    ann.forEach {
                        val state = page::class.findAnnotation<UI.Nav.Config>()?.defaultState ?: it.pageState
                        graph.addVertex(
                            NavWrapper.get(it.bySubmit.objectInstance as INavigableDetermination, state, it.globalState)
                        )
                    }
                }
            } else if(page is Block) {
                if(page.rootParent !is Page) {
                    blocks.add(page)
                }
            }

            val state = page::class.findAnnotation<UI.Nav.Config>()?.defaultState ?: UNDEF_STATE
            graph.addVertex(
                NavWrapper.get(nav = page, state = state, globalState = UNDEF_STATE)
            )
        } else {
            Log.error("Page ${page.fullName} already registered")
        }
    }

    fun getByName(pageName: String): Page? {
        return pages.firstOrNull {
            (it as? Page)?.name == pageName
        } as? Page
    }

    fun addEdge(edge: Edge) {
        //Log.info("Call addEdge to the: " + this)
        if(!edges.contains(edge)) {
            graph.addVertex(edge.from)
            graph.addVertex(edge.to)

            if(edge.from.state == UNDEF_STATE) {
                graph.vertexSet().filter {
                    it.nav == edge.from.nav
                }.forEach {
                    val newEdge = graph.addEdge(it, edge.to)
                    if(newEdge != null) {
                        newEdge.from =it
                        newEdge.to = edge.to
                        newEdge.action = edge.action

                        edges.add(newEdge)
                        graph.setEdgeWeight(newEdge, edge._weight)
                    } else {
                    //    println("newEdge is null")
                    }

                }
            } else {
                val newEdge = graph.addEdge(edge.from, edge.to)
                if(newEdge != null) {
                    newEdge.from = edge.from
                    newEdge.to = edge.to
                    newEdge.action = edge.action

                    edges.add(newEdge)
                    graph.setEdgeWeight(newEdge, edge._weight)
                } else {
                    println("New edge is null 2")
                }
            }
        }
    }

    fun init(executor: IExecutor) {
        this.executor = executor
    }

    fun initNavigations() {
        pages.sortByDescending { it.navOrder }
        pages.forEach {
            it.initNavigation()
            NavigationParser(it).parse()
        }
        blocks.forEach {
            it.initNavigation()
            NavigationParser(it).parse()
        }
    }

   private val sortedPages: Collection<INavigableDetermination> by lazy {
       pages.sortByDescending { p ->
           (p as Block).findAnnotation<UI.Nav.Order>()?.let { ann ->
               if(ann.order == -1) {
                   ann.type.value
               } else {
                   ann.order
               }
           } ?: UI.Nav.Order.DEFAULT
       }
       pages
   }

    var prevCurrentPage: INavigableDetermination? = null

    override val currentPage: INavigableDetermination
        get() {
            if(prevCurrentPage != null && executor is NavExecutor) {
                if(((executor as NavExecutor).cachedExecutor).actual) {
                    Log.info("Page was returned from cache: $prevCurrentPage")
                    return prevCurrentPage ?: throw Exception("Expected page to be initialized")
                }
            }
            prevCurrentPage = Log.action(Messages.Navigator.curPage) {
                val res = sortedPages.find {
                    if(it is IUrlDetermination) {
                        it.isOpen
                    } else {
                        Log.action(Messages.Navigator.checkPageIteration(it as Page)) {
                            executor.isAllPresent(it.determination.exist)
                                    && (it.determination.notExist.isEmpty() ||
                                    !executor.isAllPresent(it.determination.notExist))
                        }
                    }
                }
                if(res != null) {
                    Log.result(Messages.Navigator.pageFound)
                    if(res is IPageCallback) {
                        if(res is Page) {
                            if(!firstTimeDetection.contains(res.name)) {
                                firstTimeDetection.add(res.name)
                                res.afterPageDetectedFirstTime()
                            } else {
                                res.afterPageDetected()
                            }
                        }

                    }
                    res
                } else {
                    Log.warning(Messages.Navigator.pageNotFound)
                    throw XPathQsException.CurrentPageNotFound()
                }
            }
            return prevCurrentPage ?: throw XPathQsException.CurrentPageNotFound()
        }

    fun waitForCurrentPage(duration: Duration = Duration.ofSeconds(60)): INavigableDetermination {
        val tsStart = System.currentTimeMillis()
        while ((System.currentTimeMillis() - tsStart) < duration.toMillis()) {
            try {
                return currentPage
            } catch (e: XPathQsException.CurrentPageNotFound) {
                wait(500.ms, "short delay in waitForCurrentPage when it is not found")
                (executor as? NavExecutor)?.cachedExecutor?.refreshCache()
            }
        }

        throw XPathQsException.CurrentPageNotFound()
    }

    fun findPath(from: NavWrapper?, to: NavWrapper?): GraphPath<NavWrapper, Edge>? {
        if(to == null) return null
        return try {
            shortestPath.getPath(from, to)
        } catch (e : Exception) {
            Log.error(e.message ?: "findPath error")
            null
        }
    }

    override fun navigate(from: NavWrapper, to: NavWrapper) {
        if(from === to) return

        val navigations = findPath(from, to)
            ?: throw XPathQsException.NoNavigation()

        navigations.edgeList.forEach {
            if(it.action != null) {
                it.action!!()
                wait(500.ms, "short delay after navigation action in Navigator")
                (executor as? NavExecutor)?.refreshCache()
            }
            (it.to.nav as? ILoadable)?.waitForLoad(Duration.ofSeconds(30))
            val cp = currentPage
            if((it.to.nav is INavigableDetermination && it.to.nav is Page) && it.to.nav != cp) {

                currentPage
                throw Exception("Wrong page")
            }
        }
    }

}