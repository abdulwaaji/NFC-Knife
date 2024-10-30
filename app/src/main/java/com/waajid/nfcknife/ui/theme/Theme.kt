package com.waajid.nfcknife.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


val DarkColorScheme = darkColorScheme(
	primary = Blue500,
	secondary = Blue200,
	background = Blue500,
	onPrimary = White500,
	onSecondary = LightGray,
)

val AmoledDarkColorScheme = darkColorScheme(
	primary = Black500,
	secondary = Black200,
	background = Black500,
	onPrimary = White500,
	onSecondary = LightGray,
)

private val LightColorScheme = lightColorScheme(
	primary = White500,
	secondary = White200,
	background = White500,
	onPrimary = Black500,
	onSecondary = DarkGray,
)


@Composable
fun SnaptickTheme(
	theme: AppTheme = AppTheme.Dark,
	dynamicColor: Boolean = false,
	content: @Composable () -> Unit
) {
	val colorScheme = when (theme) {
//		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//			val context = LocalContext.current
//			if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//		}
		AppTheme.Light -> LightColorScheme
		AppTheme.Dark -> DarkColorScheme
		AppTheme.Amoled -> AmoledDarkColorScheme
	}
	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
			window.statusBarColor = colorScheme.primary.toArgb()
			WindowCompat.getInsetsController(
				window,
				view
			).isAppearanceLightStatusBars = theme == AppTheme.Light
		}
	}

	MaterialTheme(
		colorScheme = colorScheme,
		content = content
	)
}

enum class AppTheme {
	Light, Dark, Amoled;
}


@Composable
fun MyAppTheme(
	theme: AppTheme = AppTheme.Light,
	content: @Composable () -> Unit
) {
	val colors = if (theme == AppTheme.Dark) {
		darkColorScheme(
			// Define your dark theme colors here
		)
	} else {
		lightColorScheme(
			primary = Color(0xFF4CAF50),
			onPrimary = Color.White,
			secondary = Color(0xFF8BC34A),
			onSecondary = Color.Black,
			background = Color(0xFFF1FFFF),
			surface = Color.White,
			onSurface = Color.Black
		)
	}

	MaterialTheme(colorScheme = colors) {
		content()
	}

}






