package com.nbradbury.tic_tac_troll

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nbradbury.tic_tac_troll.game.GameViewModel
import com.nbradbury.tic_tac_troll.game.Screen
import com.nbradbury.tic_tac_troll.ui.BackgroundMusic
import com.nbradbury.tic_tac_troll.ui.GameScreen
import com.nbradbury.tic_tac_troll.ui.MenuScreen
import com.nbradbury.tic_tac_troll.ui.TrollVoices
import com.nbradbury.tic_tac_troll.ui.backdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { TicTacTrollApp() }
    }
}

@Composable
fun TicTacTrollApp(vm: GameViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackgroundMusic(R.raw.where_the_trolls_tread)
    TrollVoices(state)
    Box(Modifier.fillMaxSize().backdrop()) {
        when (state.screen) {
            Screen.MENU -> MenuScreen(
                state = state,
                onMode = vm::setMode,
                onDifficulty = vm::setDifficulty,
                onStart = vm::start,
                modifier = Modifier.systemBarsPadding(),
            )
            Screen.GAME -> {
                BackHandler(onBack = vm::toMenu)
                GameScreen(
                    state = state,
                    onCell = vm::play,
                    onMenu = vm::toMenu,
                    onRestart = vm::restart,
                    onRematch = vm::rematch,
                )
            }
        }
    }
}
