package org.xpathqs.driver.moke

import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.driver.core.IDriver

open class MkDriver : IDriver {

    override fun click(x: Int, y: Int) {

    }

    override fun click(selector: ISelector) {

    }

    override fun clear(selector: ISelector, clickSelector: ISelector) {

    }

    override fun input(selector: ISelector, value: String) {

    }

    override val pageSource: String
        get() = ""
}