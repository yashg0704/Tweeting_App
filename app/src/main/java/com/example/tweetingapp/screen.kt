package com.example.tweetingapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.Date

val TwitterBlue = Color(0xFF1DA1F2)
val TwitterDarkGray = Color(0xFF657786)

data class Tweet(
    val id: String = "",
    val username: String = "User",
    val message: String = "",
    val timestamp: Date = Date(),
    val likes: Int = 0
)

@Composable
fun TweetingApp() {
    var isDarkMode by remember { mutableStateOf(false) }
    val backgroundColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val textFieldBackground = if (isDarkMode) Color.DarkGray else Color.LightGray
    val labelColor = if (isDarkMode) Color.LightGray else TwitterDarkGray

    val db = FirebaseFirestore.getInstance()
    var tweetMessage by remember { mutableStateOf("") }
    var tweets by remember { mutableStateOf<List<Tweet>>(emptyList()) }
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("tweets")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                tweets = snapshot?.documents?.mapNotNull {
                    it.toObject<Tweet>()?.copy(id = it.id)
                } ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // App header with dark mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tweeting App",
                fontSize = 24.sp,
                color = TwitterBlue
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = { isDarkMode = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TwitterBlue,
                    checkedTrackColor = TwitterBlue.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Enter your name", color = labelColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = textFieldBackground,
                unfocusedContainerColor = textFieldBackground,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
                focusedIndicatorColor = TwitterBlue,
                unfocusedIndicatorColor = TwitterDarkGray,
                cursorColor = TwitterBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = tweetMessage,
            onValueChange = { tweetMessage = it },
            label = { Text("What's happening? ;)", color = labelColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = textFieldBackground,
                unfocusedContainerColor = textFieldBackground,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
                focusedIndicatorColor = TwitterBlue,
                unfocusedIndicatorColor = TwitterDarkGray,
                cursorColor = TwitterBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (tweetMessage.isNotBlank() && userName.isNotBlank()) {
                    val tweet = Tweet(username = userName, message = tweetMessage)
                    db.collection("tweets").add(tweet)
                    tweetMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TwitterBlue
            )
        ) {
            Text("Tweet", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(tweets) { tweet ->
                TweetCard(
                    tweet = tweet,
                    onLike = { isLiked ->
                        val newLikes = if (isLiked) tweet.likes + 1 else tweet.likes - 1
                        db.collection("tweets").document(tweet.id)
                            .update("likes", newLikes)
                    },
                    onDelete = {
                        db.collection("tweets").document(tweet.id).delete()
                    },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TweetCard(
    tweet: Tweet,
    onLike: (Boolean) -> Unit,
    onDelete: () -> Unit,
    isDarkMode: Boolean
) {
    var isLiked by remember { mutableStateOf(false) }
    val cardColor = if (isDarkMode) Color.DarkGray else Color.LightGray
    val textColor = if (isDarkMode) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tweet.username,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = tweet.timestamp.toString(),
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.LightGray else TwitterDarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tweet.message,
                fontSize = 16.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = {
                        isLiked = !isLiked
                        onLike(isLiked)
                    }) {
                        Icon(
                            imageVector = if (isLiked)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) TwitterBlue else TwitterDarkGray
                        )
                    }
                    Text(
                        text = "${tweet.likes} Likes",
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color.LightGray else TwitterDarkGray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TwitterDarkGray
                    )
                }
            }
        }
    }
}