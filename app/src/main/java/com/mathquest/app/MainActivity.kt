package com.mathquest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mathquest.app.navigation.Routes
import com.mathquest.app.ui.screens.CinematicIntroScreen
import com.mathquest.app.ui.screens.CreditsScreen
import com.mathquest.app.ui.screens.DivisionLessonScreen
import com.mathquest.app.ui.screens.FractionsLessonScreen
import com.mathquest.app.ui.screens.GuideDialogueScreen
import com.mathquest.app.ui.screens.IntroStoryScreen
import com.mathquest.app.ui.screens.LevelEarthScreen
import com.mathquest.app.ui.screens.LoginScreen
import com.mathquest.app.ui.screens.MainMenuScreen
import com.mathquest.app.ui.screens.MultiplicationLessonScreen
import com.mathquest.app.ui.theme.MathQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MathQuestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    MathQuestApp()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OST map: every route → its raw resource ID
// ─────────────────────────────────────────────────────────────────────────────
private fun ostForRoute(route: String?): Int? = when (route) {
    Routes.MAIN_MENU,
    Routes.LOGIN                 -> R.raw.starting
    Routes.CINEMATIC_INTRO,
    Routes.INTRO_STORY           -> R.raw.story
    Routes.GUIDE_DIALOGUE        -> R.raw.guide
    Routes.MULTIPLICATION_LESSON -> R.raw.lesson
    Routes.CHALLENGE_EARTH       -> R.raw.fight1
    Routes.DIVISION_LESSON       -> R.raw.lesson2
    Routes.CHALLENGE_AIR         -> R.raw.fight2
    Routes.FRACTIONS_LESSON      -> R.raw.lesson3
    Routes.CHALLENGE_WATER       -> R.raw.fight3
    Routes.VICTORY               -> R.raw.credits
    else                         -> null
}

@Composable
fun MathQuestApp(
    navController: NavHostController = rememberNavController(),
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // ── Music: pause / resume with app lifecycle ──────────────────────────────
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> MusicManager.pause()
                Lifecycle.Event.ON_RESUME -> MusicManager.resume()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            MusicManager.stop()
        }
    }

    // ── Music: switch track when the route changes ────────────────────────────
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route

    LaunchedEffect(route) {
        val resId = ostForRoute(route) ?: return@LaunchedEffect
        MusicManager.play(context, resId, state.musicVolume)
    }

    // ── Music: live volume update from Settings or Pause sliders ─────────────
    LaunchedEffect(state.musicVolume) {
        MusicManager.setVolume(state.musicVolume)
    }

    // ── Navigation graph ──────────────────────────────────────────────────────
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_MENU
    ) {
        // ── Main Menu ─────────────────────────────────────────────────────────
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                hasSavedProgress = state.hasSavedProgress,
                playerName = state.playerName,
                musicVolume = state.musicVolume,
                sfxVolume = state.sfxVolume,
                onMusicVolume = viewModel::setMusicVolume,
                onSfxVolume = viewModel::setSfxVolume,
                onStartAdventure = {
                    navController.navigate(Routes.LOGIN)
                },
                onContinue = {
                    navController.navigate(Routes.CHALLENGE_EARTH)
                },
                onViewProgress = {
                    if (state.hasSavedProgress) {
                        navController.navigate(Routes.CHALLENGE_EARTH)
                    }
                }
            )
        }

        // ── 1. Enter name ─────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onSubmit = { name ->
                    viewModel.setPlayerName(name)
                    navController.navigate(Routes.CINEMATIC_INTRO) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── 2. Story panels 1-7 (auto, 3 s, no UI) ───────────────────────────
        composable(Routes.CINEMATIC_INTRO) {
            CinematicIntroScreen(
                onComplete = {
                    navController.navigate(Routes.GUIDE_DIALOGUE) {
                        popUpTo(Routes.CINEMATIC_INTRO) { inclusive = true }
                    }
                }
            )
        }

        // ── 3. Guide talks to player ──────────────────────────────────────────
        composable(Routes.GUIDE_DIALOGUE) {
            GuideDialogueScreen(
                onComplete = {
                    navController.navigate(Routes.MULTIPLICATION_LESSON) {
                        popUpTo(Routes.GUIDE_DIALOGUE) { inclusive = true }
                    }
                }
            )
        }

        // ── 4. Multiplication lesson ──────────────────────────────────────────
        composable(Routes.MULTIPLICATION_LESSON) {
            MultiplicationLessonScreen(
                onComplete = {
                    navController.navigate(Routes.CHALLENGE_EARTH) {
                        popUpTo(Routes.MULTIPLICATION_LESSON) { inclusive = true }
                    }
                }
            )
        }

        // ── 5. Level 1 — Earth (Multiplication) ──────────────────────────────
        composable(Routes.CHALLENGE_EARTH) {
            LevelEarthScreen(
                playerName = state.playerName,
                startLevel = 0,
                musicVolume = state.musicVolume,
                sfxVolume = state.sfxVolume,
                onMusicVolume = viewModel::setMusicVolume,
                onSfxVolume = viewModel::setSfxVolume,
                onLevelComplete = {
                    viewModel.advanceLevel()
                    navController.navigate(Routes.DIVISION_LESSON) {
                        popUpTo(Routes.CHALLENGE_EARTH) { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }

        // ── 6. Division lesson ────────────────────────────────────────────────
        composable(Routes.DIVISION_LESSON) {
            DivisionLessonScreen(
                onComplete = {
                    navController.navigate(Routes.CHALLENGE_AIR) {
                        popUpTo(Routes.DIVISION_LESSON) { inclusive = true }
                    }
                }
            )
        }

        // ── 7. Level 2 — Air (Division) ───────────────────────────────────────
        composable(Routes.CHALLENGE_AIR) {
            LevelEarthScreen(
                playerName = state.playerName,
                startLevel = 1,
                musicVolume = state.musicVolume,
                sfxVolume = state.sfxVolume,
                onMusicVolume = viewModel::setMusicVolume,
                onSfxVolume = viewModel::setSfxVolume,
                onLevelComplete = {
                    viewModel.advanceLevel()
                    navController.navigate(Routes.FRACTIONS_LESSON) {
                        popUpTo(Routes.CHALLENGE_AIR) { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }

        // ── 8. Fractions lesson ───────────────────────────────────────────────
        composable(Routes.FRACTIONS_LESSON) {
            FractionsLessonScreen(
                onComplete = {
                    navController.navigate(Routes.CHALLENGE_WATER) {
                        popUpTo(Routes.FRACTIONS_LESSON) { inclusive = true }
                    }
                }
            )
        }

        // ── 9. Level 3 — Water (Fractions) ────────────────────────────────────
        composable(Routes.CHALLENGE_WATER) {
            LevelEarthScreen(
                playerName = state.playerName,
                startLevel = 2,
                musicVolume = state.musicVolume,
                sfxVolume = state.sfxVolume,
                onMusicVolume = viewModel::setMusicVolume,
                onSfxVolume = viewModel::setSfxVolume,
                onLevelComplete = {
                    viewModel.advanceLevel()
                    // After the final boss → Credits / Victory screen
                    navController.navigate(Routes.VICTORY) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = false }
                    }
                },
                onHome = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }

        // ── 10. Victory / Credits ─────────────────────────────────────────────
        composable(Routes.VICTORY) {
            CreditsScreen(
                playerName = state.playerName,
                onMainMenu = {
                    navController.navigate(Routes.MAIN_MENU) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }

        // ── Legacy (kept for compatibility) ───────────────────────────────────
        composable(Routes.INTRO_STORY) {
            IntroStoryScreen(
                playerName = state.playerName,
                onComplete = {
                    navController.navigate(Routes.CHALLENGE_EARTH) {
                        popUpTo(Routes.INTRO_STORY) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
