package org.xpathqs.driver.widgets

import org.xpathqs.driver.model.IBaseModel

interface IFormRead {
    fun readInt(model: IBaseModel? = null) = 0
    fun readString(model: IBaseModel? = null) = ""
    fun readBool(model: IBaseModel? = null) = false
    fun isReady(model: IBaseModel? = null) = true
    fun isDisabled(model: IBaseModel? = null) = false
}