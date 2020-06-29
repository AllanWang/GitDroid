package ca.allanwang.gitdroid.utils

import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.utils.fastAdapter
import ca.allanwang.kau.adapters.SingleFastAdapter
import ca.allanwang.kau.animators.*
import ca.allanwang.kau.utils.KAU_BOTTOM


enum class RvAnimation(val animator: RecyclerView.ItemAnimator) {
    SMOOTH(
        KauAnimator(
            addAnimator = SlideAnimatorAdd(KAU_BOTTOM, slideFactor = 2f),
            removeAnimator = FadeScaleAnimatorRemove(),
            changeAnimator = NoAnimatorChange
        ).apply {
            addDuration = 500L
            interpolator = FastOutSlowInInterpolator()
        }),
    FAST(
        KauAnimator(
            addAnimator = FadeScaleAnimatorAdd(itemDelayFactor = 0f),
            removeAnimator = FadeScaleAnimatorRemove(itemDelayFactor = 0f),
            changeAnimator = NoAnimatorChange
        )
    ),
    INSTANT(
        KauAnimator(
            addAnimator = NoAnimatorAdd(),
            removeAnimator = NoAnimatorRemove(),
            changeAnimator = NoAnimatorChange
        )
    );

    fun set(recyclerView: RecyclerView) {
        if (recyclerView.itemAnimator !== animator) {
            recyclerView.itemAnimator = animator
        }
    }

    companion object {
        const val THRESHOLD = 300L

        fun set(recyclerView: RecyclerView, adapter: SingleFastAdapter = recyclerView.fastAdapter) {
            val anim = if (System.currentTimeMillis() - adapter.lastClearTime > THRESHOLD) SMOOTH else FAST
            anim.set(recyclerView)
        }

    }
}