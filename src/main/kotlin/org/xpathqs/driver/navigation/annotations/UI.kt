package org.xpathqs.driver.navigation.annotations

import org.xpathqs.core.selector.base.BaseSelector
import org.xpathqs.core.selector.block.Block
import org.xpathqs.driver.navigation.annotations.UI.Nav.PathTo.Companion.UNDEF
import org.xpathqs.driver.navigation.annotations.UI.Visibility.Companion.UNDEF_STATE
import kotlin.reflect.KClass

class UI {
    class Widgets {
        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Form(
            val cleanByDefault: Boolean = true
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Submit

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Input(
            val type: String = "",
            val afterInputDelayMs: Long = 0
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Select

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class OptionItem

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class DropdownItem

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class ValidationError

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class ResetForm

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class ClickToFocusLost

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Back

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Checkbox(
            val onChecked: KClass<out BaseSelector> = BaseSelector::class,
            val onUnchecked: KClass<out BaseSelector> = BaseSelector::class,
            val default: Boolean = true,
            val visibilityOf: KClass<out BaseSelector> = BaseSelector::class,
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Selectable(
            val onSelected: Array<KClass<out BaseSelector>> = [],
        )
    }

    class Visibility {
        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Always

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Dynamic(
            val modelState: Int = UNDEF_STATE,
            val modelClass: KClass<*> = Any::class,
            val modelSubmitFirst: Boolean = false,
            val modelDepends: Int = UNDEF_STATE,
            val submitModel: Boolean = false,
            val overlapped: Boolean = false,
            val internalState: Int = UNDEF_STATE,

            //Annotation may be applied for specific page states
            val canApplyForGlobalState: Int = UNDEF_STATE,
            val canApplyForGlobalStateGroup: Int = UNDEF_STATE,
            val ajax: Boolean = false
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Backend

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class OneOf

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class State(
            val value: Int
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class GlobalState(
            val value: Int,
            val stateGroup: Int = UNDEF_STATE
        )

        companion object {
            const val UNDEF_STATE = -1
            const val DEFAULT_STATE = 0
        }
    }

    class Enable {
        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class GlobalState(
            val value: Int,
            val group: Int = UNDEF_STATE
        )
    }

    class Nav {
        @Target(
            AnnotationTarget.CLASS,
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Config(
            val defaultState: Int
        )

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY,
            AnnotationTarget.FUNCTION
        )
        @Retention(AnnotationRetention.RUNTIME)
        @Repeatable
        annotation class PathTo(
            val byClick:  KClass<out Block> = Block::class,
            val byInvoke: KClass<out Block> = Block::class,
            val bySubmit: KClass<out Block> = Block::class,
            val weight: Int = UNDEF,
        //    val contains: Array<KClass<out Block>> = [],
            val contain: KClass<out Block> = Block::class,
            val switchTab: Boolean = false,
            val pageState: Int = UNDEF,
            val selfPageState: Int = UNDEF,
            val modelState: Int = UNDEF,
            val globalState: Int = UNDEF,
            val loadSeconds: Int = UNDEF
        ) {
            companion object {
                const val UNDEF = -1
                const val ALREADY_PRESENT_WEIGHT = 10
                const val DEFAULT_WEIGHT = 100
            }
        }

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class DeterminateBy(
            val determination: DeterminationType = DeterminationType.EXIST,
            val stateDetermination: Boolean = false
        )

        @Target(
            AnnotationTarget.CLASS
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Order(
            val order: Int = -1,
            val type: NavOrderType = NavOrderType.DEFAULT
        ) {
            companion object {
                const val DEFAULT = 100
            }
        }

        @Target(
            AnnotationTarget.CLASS,
            AnnotationTarget.PROPERTY
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class WaitForLoad(
            val type: WaitForLoadEnum = WaitForLoadEnum.LOAD_ALL,
            val durationMs: Long = -1
        )

        @Target(
            AnnotationTarget.CLASS
        )
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Autoclose
    }

    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY
    )
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Animated(
        val timeToCompleteMs: Int = 500,
        val autoCloseMs: Int = 0
    )
}
