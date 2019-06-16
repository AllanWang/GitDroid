package ca.allanwang.gitdroid.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.ktx.transition.auto
import ca.allanwang.gitdroid.ktx.transition.changeBounds
import ca.allanwang.gitdroid.ktx.transition.fade
import ca.allanwang.gitdroid.ktx.transition.transitionSet
import ca.allanwang.gitdroid.views.databinding.ViewLoginBinding
import ca.allanwang.gitdroid.views.databinding.ViewLoginSelectionBinding
import ca.allanwang.gitdroid.views.databinding.ViewLoginContainerBinding
import ca.allanwang.kau.internal.KauBaseActivity

class LoginActivity : KauBaseActivity() {

    lateinit var sceneRoot: ViewLoginContainerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sceneRoot = DataBindingUtil.setContentView(this, R.layout.view_login_container)
        showSelectorScene(false)
    }

    private fun showSelectorScene(animate: Boolean) {
        val view: ViewLoginSelectionBinding =
            DataBindingUtil.inflate(layoutInflater,
                R.layout.view_login_selection, sceneRoot.loginContainerScene, false)
        view.loginSelectPassword.setOnClickListener {
            showPasswordScene(true)
        }
        val scene = Scene(sceneRoot.loginContainerScene, view.root)
        val transition = if (animate) selectorSceneTransition(view) else null
        TransitionManager.go(scene, transition)
    }

    private fun selectorSceneTransition(view: ViewLoginSelectionBinding): Transition = transitionSet {
        fade(R.id.login_select_oauth) {
        }
        auto(R.id.login_select_password) {

        }
        duration = 1000L
    }

    private fun showPasswordScene(animate: Boolean) {
        val view: ViewLoginBinding =
            DataBindingUtil.inflate(layoutInflater,
                R.layout.view_login, sceneRoot.loginContainerScene, false)
        view.loginSend.setOnClickListener {
            showSelectorScene(true)
        }
        val scene = Scene(sceneRoot.loginContainerScene, view.root)
        val transition = if (animate) passwordSceneTransition(view) else null
        TransitionManager.go(scene, transition)
    }

    private fun passwordSceneTransition(view: ViewLoginBinding): Transition = transitionSet {
        fade(R.id.login_select_oauth) {
        }
        changeBounds(R.id.login_card, R.id.login_select_password) {

        }
        duration = 1000L
    }
}