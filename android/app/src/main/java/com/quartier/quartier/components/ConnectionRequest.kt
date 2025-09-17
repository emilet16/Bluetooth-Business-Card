package com.quartier.quartier.components

import android.telecom.ConnectionRequest
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
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
import okhttp3.internal.notify

//A component showcasing a user who sent a connection request

@Composable
fun ConnectionRequest(onAcceptConnection: (User) -> Unit, onDeclineConnection: (User)->Unit, user: User) {
    Card(shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .width(225.dp)
            .height(180.dp)
            .dropShadow(RoundedCornerShape(15.dp), Shadow(2.dp, alpha = .25f))
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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
                        .size(80.dp)
                        .innerShadow(CircleShape,
                            Shadow(1.dp, alpha = .25f)
                        )
                        .clip(CircleShape)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        user.name,
                        style = Typography.titleSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(user.job, textAlign = TextAlign.Center, fontWeight = FontWeight.Normal)
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {onDeclineConnection(user)},
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Close, LocalContext.current.getString(R.string.decline_request))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = {onAcceptConnection(user)}) {
                    Icon(Icons.Outlined.Done, LocalContext.current.getString(R.string.accept_request))
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