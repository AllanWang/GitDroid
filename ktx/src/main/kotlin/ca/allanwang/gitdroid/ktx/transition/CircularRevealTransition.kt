package ca.allanwang.gitdroid.ktx.transition

import android.animation.Animator
import android.animation.TimeInterpolator
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionValues
import androidx.transition.Visibility

class CircularRevealTransition : Visibility() {
    override fun onAppear(
        sceneRoot: ViewGroup,
        startValues: TransitionValues,
        startVisibility: Int,
        endValues: TransitionValues,
        endVisibility: Int
    ): Animator {
        return super.onAppear(sceneRoot, startValues, startVisibility, endValues, endVisibility)
    }

    override fun onAppear(
        sceneRoot: ViewGroup?,
        view: View?,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): Animator {
        return super.onAppear(sceneRoot, view, startValues, endValues)
    }
}
