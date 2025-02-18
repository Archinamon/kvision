/*
 * Copyright (c) 2019. Robert Jaros
 */
package io.kvision.state

import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.ChangeDelta
import io.github.petertrr.diffutils.patch.DeleteDelta
import io.github.petertrr.diffutils.patch.EqualDelta
import io.github.petertrr.diffutils.patch.InsertDelta
import io.kvision.core.Component
import io.kvision.core.Container
import io.kvision.core.Display
import io.kvision.form.GenericFormComponent
import io.kvision.panel.SimplePanel
import kotlin.js.Date

/**
 * An extension function which binds the widget to the observable state.
 *
 * @param S the state type
 * @param W the widget type
 * @param observableState the state
 * @param removeChildren remove all children of the component
 * @param factory a function which re-creates the view based on the given state
 */
fun <S, W : Component> W.bind(
    observableState: ObservableState<S>,
    removeChildren: Boolean = true,
    factory: (W.(S) -> Unit)
): W {
    this.addBeforeDisposeHook(observableState.subscribe {
        this.singleRenderAsync {
            if (removeChildren) (this as? Container)?.disposeAll()
            factory(it)
        }
    })
    return this
}

/**
 * An extension function which binds the container to the observable state with a list of items.
 *
 * @param S the state type
 * @param W the container type
 * @param observableState the state
 * @param equalizer optional custom equalizer function
 * @param factory a function which re-creates the view based on the given state
 */
fun <S, W : SimplePanel> W.bindEach(
    observableState: ObservableState<List<S>>,
    equalizer: ((S, S) -> Boolean)? = null,
    factory: (SimplePanel.(S) -> Unit)
): W {
    fun getSingleComponent(state: S): Component {
        val tempContainer = SimplePanel {
            display = Display.CONTENTS
            factory(state)
        }
        return if (tempContainer.getChildren().size == 1) {
            val child = tempContainer.getChildren().first()
            tempContainer.removeAll()
            tempContainer.dispose()
            child
        } else {
            tempContainer
        }
    }

    this.archivedState = null
    val unsubscribe = observableState.subscribe {
        this.singleRender {
            val previousState = archivedState?.unsafeCast<List<S>>() ?: emptyList()
            val patch = diff(previousState, it, equalizer)
            val deltas = patch.deltas
            val iterator = deltas.listIterator(deltas.size)
            while (iterator.hasPrevious()) {
                when (val delta = iterator.previous()) {
                    is ChangeDelta -> {
                        val position: Int = delta.source.position
                        val size: Int = delta.source.size()
                        for (i in 0 until size) {
                            val component = this.getChildren()[position]
                            this.removeAt(position)
                            component.dispose()
                        }
                        delta.target.lines.forEachIndexed { i, line ->
                            this.add(position + i, getSingleComponent(line))
                        }
                    }
                    is DeleteDelta -> {
                        val position = delta.source.position
                        for (i in 0 until delta.source.size()) {
                            val component = this.getChildren()[position]
                            this.removeAt(position)
                            component.dispose()
                        }
                    }
                    is InsertDelta -> {
                        val position = delta.source.position
                        delta.target.lines.forEachIndexed { i, line ->
                            this.add(position + i, getSingleComponent(line))
                        }
                    }
                    is EqualDelta -> {
                    }
                }
            }
            archivedState = it.toList()
        }
    }
    this.addBeforeDisposeHook {
        this.archivedState = null
        unsubscribe()
    }
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <S, T : GenericFormComponent<S>> T.bindTo(state: MutableState<S>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it)
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<String?>> T.bindTo(state: MutableState<String>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it ?: "")
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<Number?>> T.bindTo(state: MutableState<Int?>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it?.toInt())
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<Number?>> T.bindTo(state: MutableState<Int>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it?.toInt() ?: 0)
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<Number?>> T.bindTo(state: MutableState<Double?>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it?.toDouble())
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<Number?>> T.bindTo(state: MutableState<Double>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it?.toDouble() ?: 0.0)
    })
    return this
}

/**
 * Bidirectional data binding to the MutableState instance.
 * @param state the MutableState instance
 * @return current component
 */
fun <T : GenericFormComponent<Date?>> T.bindTo(state: MutableState<Date>): T {
    bind(state, false) {
        if (value != it) value = it
    }
    addBeforeDisposeHook(subscribe {
        state.setState(it ?: Date())
    })
    return this
}
