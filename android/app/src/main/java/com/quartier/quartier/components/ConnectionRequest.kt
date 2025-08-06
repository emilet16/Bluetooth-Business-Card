package com.quartier.quartier.components

import android.telecom.ConnectionRequest
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.quartier.quartier.R
import com.quartier.quartier.database.User
import com.quartier.quartier.ui.theme.Typography

//A component showcasing a user who sent a connection request

@Composable
fun ConnectionRequest(onAcceptConnection: (User) -> Unit, onDeclineConnection: (User)->Unit, user: User) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .wrapContentHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        user.pfp_url
                            ?: R.drawable.baseline_account_circle
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = LocalContext.current.getString(R.string.pfp_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, Color.Black, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
            )
            Text(
                user.name,
                style = Typography.titleSmall,
                textAlign = TextAlign.Center
            )
            Text(user.job, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.SpaceAround) {
                IconButton({ onAcceptConnection(user) }) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = LocalContext.current.getString(
                            R.string.accept_request
                        )
                    )
                }
                IconButton({ onDeclineConnection(user) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = LocalContext.current.getString(
                            R.string.decline_request
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewConnectionRequest() {
    ConnectionRequest(onAcceptConnection = {}, onDeclineConnection = {}, user = User(id = "", name = "Steve Jobs", job = "CEO"))
}