package com.nbradbury.tic_tac_troll

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nbradbury.tic_tac_troll.game.GameViewModel
import com.nbradbury.tic_tac_troll.game.Rules
import com.nbradbury.tic_tac_troll.game.Screen
import com.nbradbury.tic_tac_troll.ui.BackgroundMusic
import com.nbradbury.tic_tac_troll.ui.GameHaptics
import com.nbradbury.tic_tac_troll.ui.GameScreen
import com.nbradbury.tic_tac_troll.ui.GameSounds
import com.nbradbury.tic_tac_troll.ui.MenuScreen
import com.nbradbury.tic_tac_troll.ui.backdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // Full screen: the status and navigation bars stay hidden, and a swipe from the edge shows them briefly.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent { TicTacTrollApp() }
    }
}

private const val KEY_SOUND = "sound_on"
private const val KEY_BORED_TROLLS = "bored_trolls"

@Composable
fun TicTacTrollApp() {
    val prefs = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
    // Created with the saved rules so the switch starts in place instead of animating on at launch.
    val vm = viewModel {
        GameViewModel(if (prefs.getBoolean(KEY_BORED_TROLLS, false)) Rules.ROLLING else Rules.CLASSIC)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    var soundOn by remember { mutableStateOf(prefs.getBoolean(KEY_SOUND, true)) }
    BackgroundMusic(R.raw.where_the_trolls_tread, enabled = soundOn)
    GameSounds(state, enabled = soundOn)
    GameHaptics(state)
    Box(Modifier.fillMaxSize().backdrop()) {
        when (state.screen) {
            Screen.MENU -> MenuScreen(
                state = state,
                onMode = vm::setMode,
                onDifficulty = vm::setDifficulty,
                onRules = {
                    vm.setRules(it)
                    prefs.edit { putBoolean(KEY_BORED_TROLLS, it == Rules.ROLLING) }
                },
                onStart = vm::start,
                soundOn = soundOn,
                onSoundChange = {
                    soundOn = it
                    prefs.edit { putBoolean(KEY_SOUND, it) }
                },
                modifier = Modifier.safeDrawingPadding(),
            )
            Screen.GAME -> {
                BackHandler(onBack = vm::toMenu)
                GameScreen(
                    state = state,
                    onCell = vm::play,
                    onFallenLanded = vm::popFallen,
                    onMenu = vm::toMenu,
                    onRestart = vm::restart,
                    onRematch = vm::rematch,
                )
            }
        }
    }
}
