package com.example.businesscard.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.businesscard.R
import com.example.businesscard.User
import com.example.businesscard.ui.theme.Typography

@Composable
fun UserCard(onClick: () -> Unit, user: User) {
    Card(onClick = onClick, modifier = Modifier.padding(5.dp).wrapContentHeight()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                .data(user.pfp_url ?: R.drawable.baseline_account_circle)
                .crossfade(true)
                .build(),
                contentDescription = LocalContext.current.getString(R.string.pfp_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).border(2.dp ,Color.Black, CircleShape).padding(2.dp).clip(CircleShape)
            )
            Text(user.name, style = Typography.titleSmall, textAlign = TextAlign.Center)
            Text(user.job, textAlign = TextAlign.Center)
        }
    }
}