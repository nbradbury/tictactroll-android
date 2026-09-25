package com.nbradbury.tic_tac_troll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nbradbury.tic_tac_troll.R

val LilitaOne = FontFamily(Font(R.font.lilita_one))
val RubikDirt = FontFamily(Font(R.font.rubik_dirt))

private fun variable(res: Int, weight: FontWeight) =
    Font(res, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val DmSans = FontFamily(
    variable(R.font.dm_sans, FontWeight.Normal),
    variable(R.font.dm_sans, FontWeight.Medium),
    variable(R.font.dm_sans, FontWeight.Bold),
)
val JetBrainsMono = FontFamily(variable(R.font.jetbrains_mono, FontWeight.Medium))

fun sans(size: Int, weight: FontWeight = FontWeight.Medium, color: Color = Ink) =
    TextStyle(fontFamily = DmSans, fontSize = size.sp, fontWeight = weight, color = color)

fun mono(size: Int, color: Color) =
    TextStyle(fontFamily = JetBrainsMono, fontSize = size.sp, fontWeight = FontWeight.Medium, color = color)

fun lilita(size: Int, color: Color = Ink) = TextStyle(fontFamily = LilitaOne, fontSize = size.sp, color = color)

fun dirt(size: Int, color: Color = Cream) = TextStyle(fontFamily = RubikDirt, fontSize = size.sp, color = color)
