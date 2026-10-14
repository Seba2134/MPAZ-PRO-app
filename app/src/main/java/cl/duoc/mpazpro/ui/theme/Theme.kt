package cl.duoc.mpazpro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    primaryContainer = AzulClaro,
    onPrimaryContainer = TextoOscuro,
    secondary = AmarilloSol,
    onSecondary = TextoOscuro,
    secondaryContainer = AmarilloClaro,
    onSecondaryContainer = TextoOscuro,
    tertiary = VerdeHoja,
    onTertiary = Blanco,
    tertiaryContainer = VerdeClaro,
    onTertiaryContainer = TextoOscuro,
    error = RojoError,
    onError = Blanco,
    background = FondoCrema,
    onBackground = TextoOscuro,
    surface = Blanco,
    onSurface = TextoOscuro
)

@Composable
fun MPAZPROTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}