package ca.allanwang.gitdroid.ktx.transition

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues
import ca.allanwang.kau.ui.ProgressAnimator

private const val TAG = "gitdroid"

//class CircularRevealTransition : Visibility() {
//    override fun onAppear(
//        sceneRoot: ViewGroup,
//        startValues: TransitionValues,
//        startVisibility: Int,
//        endValues: TransitionValues,
//        endVisibility: Int
//    ): Animator {
//        return super.onAppear(sceneRoot, startValues, startVisibility, endValues, endVisibility)
//    }
//
//    override fun onAppear(
//        sceneRoot: ViewGroup?,
//        view: View?,
//        startValues: TransitionValues,
//        endValues: TransitionValues
//    ): Animator {
//        view?.transitionAlpha
//        return super.onAppear(sceneRoot, view, startValues, endValues)
//    }
//}

class ProgressTransition(
    private val start: Float?, private val end: Float?, private val getter: ((view: View) -> Float?)? = null,
    private val setter: (view: View, progress: Float) -> Unit
) : Transition() {
    override fun captureStartValues(transitionValues: TransitionValues) {
        capture(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        capture(transitionValues)
    }

    private fun capture(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_VALUE] = getter?.invoke(transitionValues.view)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val start = startValue(startValues) ?: return null
        val end = endValue(endValues) ?: return null
        val view = startValues?.view ?: endValues?.view ?: return null
        return ProgressAnimator.ofFloat {
            withAnimator(start, end) {
                setter(view, it)
            }
        }
    }

    private fun startValue(transitionValues: TransitionValues?) =
        transitionValues?.values?.get(PROPNAME_VALUE) as? Float ?: start

    private fun endValue(transitionValues: TransitionValues?) =
        transitionValues?.values?.get(PROPNAME_VALUE) as? Float ?: end

    override fun isTransitionRequired(startValues: TransitionValues?, endValues: TransitionValues?): Boolean {
        return startValue(startValues) != null && endValue(endValues) != null
    }

    companion object {
        private const val PROPNAME_VALUE = "$TAG:progress:transition:value"
    }
}

class ColorTransition(
    private val startColor: Int? = null,
    private val endColor: Int? = null,
    private val getter: ((view: View) -> Int?)? = null,
    private val setter: (view: View, color: Int) -> Unit
) : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        capture(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        capture(transitionValues)
    }

    private fun capture(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_COLOR] = getter?.invoke(transitionValues.view)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val start = startValue(startValues) ?: return null
        val end = endValue(endValues) ?: return null
        val view = startValues?.view ?: endValues?.view ?: return null
        val eval = ArgbEvaluator()
        return ProgressAnimator.ofFloat {
            withAnimator {
                val c = eval.evaluate(it, start, end)
                setter(view, c as Int)
            }
        }
    }

    private fun startValue(transitionValues: TransitionValues?) =
        transitionValues?.values?.get(PROPNAME_COLOR) as? Int ?: startColor

    private fun endValue(transitionValues: TransitionValues?) =
        transitionValues?.values?.get(PROPNAME_COLOR) as? Int ?: endColor

    override fun isTransitionRequired(startValues: TransitionValues?, endValues: TransitionValues?): Boolean {
        return startValue(startValues) != null && endValue(endValues) != null
    }

    override fun getTransitionProperties(): Array<String>? = arrayOf(PROPNAME_COLOR)

    companion object {
        private const val PROPNAME_COLOR = "$TAG:color:transition:value"
    }
}