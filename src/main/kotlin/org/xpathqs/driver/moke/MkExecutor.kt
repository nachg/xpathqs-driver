package org.xpathqs.driver.moke

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.base.ISelector
import org.xpathqs.driver.executor.Executor
import org.xpathqs.driver.model.IBaseModel

open class MkExecutor : Executor(MkDriver()) {
    override fun isPresent(selector: ISelector): Boolean {
        return false
    }

    override fun getAttr(selector: BaseSelector, attr: String, model: IBaseModel?): String {
        return ""
    }

    override fun getAttrs(selector: BaseSelector, attr: String, model: IBaseModel?): Collection<String> {
        return emptyList()
    }
}