package ca.allanwang.gitdroid.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import androidx.transition.TransitionSet.ORDERING_SEQUENTIAL
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.data.gitAuthRest
import ca.allanwang.gitdroid.ktx.browser.launchUrl
import ca.allanwang.gitdroid.ktx.transition.ColorTransition
import ca.allanwang.gitdroid.ktx.transition.add
import ca.allanwang.gitdroid.ktx.transition.transitionSet
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.awaitOptional
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.gitdroid.views.databinding.ViewLoginBinding
import ca.allanwang.gitdroid.views.databinding.ViewLoginContainerBinding
import ca.allanwang.gitdroid.views.databinding.ViewLoginSelectionBinding
import ca.allanwang.kau.utils.resolveColor
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.startActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get

class LoginActivity : BaseActivity() {

    private var gitState: String? = null
    private var loginPasswordPage = false
    lateinit var sceneRoot: ViewLoginContainerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gitState = null
        sceneRoot = bindContentView(R.layout.view_login_container)
        showSelectorScene(false)
    }

    companion object : KoinComponent {
        suspend fun login(token: String) {
            val prefs: Prefs = get()
            L.d { "Received new login" }
            prefs.token = token
            val gdd: GitDroidData = get()
            val result = gdd.me().call(forceRefresh = true).data ?: return
            val db: Database = get()
            with(result.viewer) {
                db.userQueries.insert(
                    id = id,
                    login = login,
                    email = email,
                    avatarUrl = avatarUrl.toString(),
                    name = name,
                    token = token
                )
            }
        }

        fun logout(context: Context) {
            L.d { "Logging out" }
            val prefs: Prefs = get()
            val db: Database = get()
            db.userQueries.delete(prefs.token)
            prefs.token = ""
            context.startActivity<LoginActivity>(clearStack = true)
        }
    }

    private fun handleIntent(intent: Intent?) {
        L.d { "Handle login intent" }
        val result = intent?.data ?: return
        if (!result.toString().startsWith(GitDroidData.REDIRECT_URL)) {
            return
        }
        L.d { "Received github oauth response" }
        L._d { result.toString() }
        setIntent(null)
        val code = result.getQueryParameter(GitDroidData.Query.CODE) ?: return
        val state = gitState
        if (state != null && state != result.getQueryParameter(GitDroidData.Query.STATE)) {
            snackbar(R.string.error_login_state)
            return
        }
        launch {
            val accessToken = gitAuthRest.accessToken(code, state)
            login(accessToken.token)
            gitState = null
            // Verify I exist
            val me = db.userQueries.select(prefs.token).awaitOptional()
            if (me == null) {
                snackbar(R.string.error_occurred)
            } else {
                startActivity<MainActivity>()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleIntent(intent)
    }

    /*
     * ------------------------------------------------------------
     * Animation and view related
     * ------------------------------------------------------------
     */

    private fun <T : ViewDataBinding> currentSubBinding(): T? {
        if (sceneRoot.loginContainerScene.childCount == 0) {
            return null
        }
        return DataBindingUtil.getBinding(sceneRoot.loginContainerScene.getChildAt(0))
    }

    private fun <T : ViewDataBinding> inflateSubBinding(layoutRes: Int): T {
        return DataBindingUtil.inflate(
            layoutInflater,
            layoutRes, sceneRoot.loginContainerScene, false
        )
    }

    private fun showSelectorScene(animate: Boolean = true) {
        loginPasswordPage = false
        val view: ViewLoginSelectionBinding = inflateSubBinding(R.layout.view_login_selection)
        view.loginSelectPassword.setOnClickListener {
            showPasswordScene()
        }
        view.loginSelectOauth.setOnClickListener {
            val request = gdd.oauthUrl()
            gitState = request.state
            launchUrl(Uri.parse(request.url))
        }
        val oldView: ViewLoginBinding? = currentSubBinding()
        val scene = Scene(sceneRoot.loginContainerScene, view.root)
        val transition = if (animate) selectorSceneTransition(oldView, view) else null
        TransitionManager.go(scene, transition)
    }

    private fun selectorSceneTransition(oldView: ViewLoginBinding?, view: ViewLoginSelectionBinding): Transition =
        transitionSet {
            val fosi = FastOutSlowInInterpolator()
            add(Fade(Visibility.MODE_IN), R.id.login_select_oauth) {
                duration = 200L
                startDelay = 300L
            }
            add(TransitionSet()) {
                add(Fade(Visibility.MODE_OUT), R.id.login_user, R.id.login_password, R.id.login_send) {
                    duration = 100L
                }
                add(TransitionSet()) {
                    val accentColor = resolveColor(R.attr.colorAccent)
                    val cardColor = resolveColor(android.R.attr.colorBackgroundFloating)
                    val textColor = resolveColor(android.R.attr.textColor)
                    view.loginSelectPassword.apply {
                        backgroundTintList = ColorStateList.valueOf(cardColor)
                        setTextColor(cardColor)
                    }
                    add(
                        ColorTransition(
                            startColor = cardColor,
                            endColor = accentColor,
                            setter = { v, c ->
                                (v as? MaterialButton)?.backgroundTintList = ColorStateList.valueOf(c)
                            }),
                        R.id.login_select_password
                    ) {
                        duration = 300L
                        startDelay = 200L
                    }
                    add(
                        ColorTransition(
                            startColor = cardColor,
                            endColor = textColor,
                            setter = { v, c ->
                                (v as? MaterialButton)?.setTextColor(c)
                            }),
                        R.id.login_select_password
                    ) {
                        duration = 200L
                        startDelay = 300L
                    }
                    add(ChangeBounds(), R.id.login_card, R.id.login_select_password) {
                        duration = 500L
                    }

                    ordering = ORDERING_TOGETHER
                }

                interpolator = fosi
                ordering = ORDERING_SEQUENTIAL
            }
            ordering = ORDERING_TOGETHER
        }

    private fun showPasswordScene(animate: Boolean = true) {
        loginPasswordPage = true
        val view: ViewLoginBinding = inflateSubBinding(R.layout.view_login)
        view.loginSend.setOnClickListener {
            showSelectorScene()
        }
        val oldView: ViewLoginSelectionBinding? = currentSubBinding()
        val scene = Scene(sceneRoot.loginContainerScene, view.root)
        val transition = if (animate) passwordSceneTransition(oldView, view) else null
        TransitionManager.go(scene, transition)
    }

    private fun passwordSceneTransition(oldView: ViewLoginSelectionBinding?, view: ViewLoginBinding): Transition =
        transitionSet {
            val fosi = FastOutSlowInInterpolator()
            add(Fade(Visibility.MODE_OUT), R.id.login_select_oauth) {
                duration = 200L
            }
            add(TransitionSet()) {
                val accentColor = resolveColor(R.attr.colorAccent)
                val cardColor = resolveColor(android.R.attr.colorBackgroundFloating)
                view.loginCard.setCardBackgroundColor(accentColor)
                add(
                    ColorTransition(
                        startColor = accentColor,
                        endColor = cardColor,
                        setter = { v, c ->
                            (v as? CardView)?.setCardBackgroundColor(c)
                        }),
                    R.id.login_card
                ) {
                    duration = 300L
                }
                add(ChangeBounds(), R.id.login_card, R.id.login_select_password) {
                    duration = 500L
                }

                ordering = ORDERING_TOGETHER
                interpolator = fosi
            }

            ordering = ORDERING_TOGETHER
        }

    override fun onBackPressed() {
        if (loginPasswordPage) {
            showSelectorScene()
        } else {
            super.onBackPressed()
        }
    }
}