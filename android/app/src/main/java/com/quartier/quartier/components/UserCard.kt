package com.quartier.quartier.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.InnerShadowScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quartier.quartier.R
import com.quartier.quartier.database.User
import com.quartier.quartier.ui.theme.Typography

//A card shown to the connected users

@Composable
fun UserCard(onClick: () -> Unit, user: User) {
    Card(onClick = onClick, shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .dropShadow(RoundedCornerShape(30.dp), Shadow(2.dp, alpha = .25f))) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        user.pfp_url ?: R.drawable.baseline_account_circle
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = LocalContext.current.getString(R.string.pfp_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .innerShadow(CircleShape, Shadow(1.dp, alpha = .25f))
                    .clip(CircleShape)
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Text(user.name, style = Typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(user.job, style = Typography.titleMedium)
            }
        }
    }
}

@Preview
@Composable
fun PreviewPublicUserCard() {
    UserCard(onClick = {}, user = User(id = "", name = "Steve Jobs", job = "CEO"))
}