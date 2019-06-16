package ca.allanwang.gitdroid.ktx.transition

import androidx.transition.*

fun transitionSet(action: TransitionSet.() -> Unit = {}) = TransitionSet().apply(action)

fun TransitionSet.fade(vararg targetIds: Int, action: Fade.() -> Unit = {}) =
    addTransition(Fade().addTargets(targetIds).apply(action))

fun TransitionSet.auto(vararg targetIds: Int, action: AutoTransition.() -> Unit = {}) =
    addTransition(AutoTransition().addTargets(targetIds).apply(action))

fun TransitionSet.changeBounds(vararg targetIds: Int, action: ChangeBounds.() -> Unit = {}) =
    addTransition(ChangeBounds().addTargets(targetIds).apply(action))

private fun <T : Transition> T.addTargets(targetIds: IntArray) = apply {
    targetIds.forEach {
        addTarget(it)
    }
}
