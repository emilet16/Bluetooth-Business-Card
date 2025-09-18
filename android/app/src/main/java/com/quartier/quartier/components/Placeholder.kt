package com.quartier.quartier.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quartier.quartier.ui.theme.ExtendedTheme

@Composable
fun Placeholder(title: String, body: String, icon: ImageVector, buttonText: String? = null, buttonIcon: ImageVector? = null, action: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.fillMaxWidth(.7f),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                body,
                textAlign = TextAlign.Left
            )
            if(buttonIcon != null && buttonText != null) {
                Button(onClick = action, shape = RoundedCornerShape(10.dp)) {
                    Icon(buttonIcon, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(buttonText)
                }
            }
        }
        Icon(icon, null,
            modifier = Modifier.size(128.dp),
            tint = ExtendedTheme.colors.accent)
    }
}

@Composable
fun Placeholder(title: String, body: String, icon: Painter, buttonText: String? = null, buttonIcon: ImageVector? = null, action: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.fillMaxWidth(.7f),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                textAlign = TextAlign.Left,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                body,
                textAlign = TextAlign.Left
            )
            if(buttonIcon != null && buttonText != null) {
                Button(onClick = action, shape = RoundedCornerShape(10.dp)) {
                    Icon(buttonIcon, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(buttonText)
                }
            }
        }
        Icon(icon, null,
            modifier = Modifier.size(128.dp),
            tint = ExtendedTheme.colors.accent)
    }
}