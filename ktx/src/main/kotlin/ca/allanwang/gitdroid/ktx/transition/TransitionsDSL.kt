package ca.allanwang.gitdroid.ktx.transition

import androidx.transition.Transition
import androidx.transition.TransitionSet

inline fun transitionSet(action: TransitionSet.() -> Unit = {}) = TransitionSet().apply(action)

inline fun <T : Transition> TransitionSet.add(t: T, vararg targetIds: Int, action: T.() -> Unit = {}) =
    addTransition(t.addTargets(targetIds).apply(action))

fun <T : Transition> T.addTargets(targetIds: IntArray) = apply {
    targetIds.forEach {
        addTarget(it)
    }
}
