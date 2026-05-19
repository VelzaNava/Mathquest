package com.mathquest.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Baloo2
import com.mathquest.app.ui.theme.Nunito
import com.mathquest.app.ui.theme.TextDark

// bg_login_v2.png is 1920×1080 (landscape canvas, portrait notebook paper centred within it).
// Pixel-measured input box: topFrac=0.424  leftFrac=0.445  rightPadFrac=0.409  heightFrac=0.050
private const val LOGIN_PANEL_ASPECT = 1920f / 1080f
private const val START_BTN_ASPECT   = 538f  / 121f

@Composable
fun LoginScreen(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-bleed world background — no dark overlay
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Panel fills as much of the screen as possible while keeping the 16:9 ratio
            val maxPanelH = maxHeight * 0.96f
            val maxPanelW = maxWidth  * 0.96f
            val panelW: androidx.compose.ui.unit.Dp
            val panelH: androidx.compose.ui.unit.Dp
            if (maxPanelW / maxPanelH < LOGIN_PANEL_ASPECT) {
                panelW = maxPanelW
                panelH = panelW / LOGIN_PANEL_ASPECT
            } else {
                panelH = maxPanelH
                panelW = panelH * LOGIN_PANEL_ASPECT
            }

            Box(
                modifier = Modifier
                    .width(panelW)
                    .height(panelH)
            ) {
                // The PNG has a transparent canvas — notebook paper floats over the world bg
                Image(
                    painter = painterResource(id = R.drawable.bg_login_v2),
                    contentDescription = "Login Panel",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Overlay elements anchored by pixel-fraction measurements
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clears HELLO! and lands exactly on the top of the drawn input box
                    Spacer(modifier = Modifier.height(panelH * 0.424f))

                    // Text field — sits directly inside the pencil-drawn rectangle in the PNG
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = panelW * 0.445f,
                                end   = panelW * 0.409f
                            )
                            .height((panelH * 0.050f).coerceAtLeast(22.dp))
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it.take(12) },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 11.sp,
                                color      = TextDark,
                                textAlign  = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(TextDark),
                            singleLine  = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (name.isNotBlank()) onSubmit(name.trim())
                            }),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (name.isEmpty()) {
                                        Text(
                                            text       = "your name…",
                                            fontFamily = Nunito,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 10.sp,
                                            color      = Color(0xFF9B8FAD),
                                            textAlign  = TextAlign.Center,
                                            modifier   = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        // Lightly cover the lined paper so typed text reads cleanly
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.White.copy(alpha = 0.80f))
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(panelH * 0.036f))

                    Image(
                        painter = painterResource(id = R.drawable.btn_start),
                        contentDescription = "Start Adventure",
                        modifier = Modifier
                            .width(panelW * 0.28f)
                            .aspectRatio(START_BTN_ASPECT)
                            .alpha(if (name.isNotBlank()) 1f else 0.45f)
                            .clickable(
                                enabled = name.isNotBlank(),
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { if (name.isNotBlank()) onSubmit(name.trim()) },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Back button — dark pill for readability on the bright background
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 14.dp)
                    .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBack() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = "← Back",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = Color.White
                )
            }
        }
    }
}
