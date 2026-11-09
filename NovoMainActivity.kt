package com.orbitsocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.FirebaseApp

private val Night = Color(0xFF090B0C)
private val Panel = Color(0xFF171C1D)
private val Lime = Color(0xFFD8FF49)
private val Muted = Color(0xFF9AA3A5)
private val InstaGradient = Brush.linearGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF), Lime))

data class UserProfile(
    val id: String,
    val name: String,
    val handle: String,
    val bio: String,
    val avatarUrl: String,
    val storyUrl: String,
    val verified: Boolean,
    val category: String,
    val followersLabel: String,
    var isFollowing: Boolean = false
)

data class CommentItem(val author: String, val text: String)

data class SocialPost(
    val id: String,
    val userId: String,
    val author: String,
    val handle: String,
    val avatarUrl: String,
    val verified: Boolean,
    val location: String,
    val caption: String,
    val imageUrl: String,
    var likes: Int,
    var isLiked: Boolean = false,
    var isSaved: Boolean = false,
    val comments: List<CommentItem> = emptyList()
)

data class ChatMessage(val id: String = System.nanoTime().toString(), val senderIsMe: Boolean, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { FirebaseApp.initializeApp(this) }
        setContent { OrbitSocialApp() }
    }
}

@Composable
fun NetImage(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun OrbitSocialApp() {
    var tab by remember { mutableStateOf("Início") }
    var myName by remember { mutableStateOf("Novo Usuário") }
    var myHandle by remember { mutableStateOf("@voce.orbit") }
    var myBio by remember { mutableStateOf("Começando agora no Orbit ✨") }
    val myAvatar = "https://unsplash.com"

    var followersCount by remember { mutableIntStateOf(120) }
    var notifications by remember { mutableStateOf(listOf("Bem-vindo ao Orbit Social! Publique sua primeira foto ou siga pessoas para começar.")) }
    var editOpen by remember { mutableStateOf(false) }

    var people by remember {
        mutableStateOf(
            listOf(
                UserProfile("u1", "Lucas Andrade", "@lucas.oficial", "Ator & Modelo • SP 🎬", "https://unsplash.com", "https://unsplash.com", true, "Romance", "1.4M"),
                UserProfile("u2", "Sofia Mendes", "@sofiamendes", "Fashion & Lifestyle ✨ Paris", "https://unsplash.com", "https://unsplash.com", true, "Romance", "980K"),
                UserProfile("u3", "Gabriel Costa", "@gabriel.music", "Cantor & Compositor 🎸", "https://unsplash.com", "https://unsplash.com", true, "Romance", "2.1M")
            )
        )
    }

    var posts by remember {
        mutableStateOf(
            listOf(
                SocialPost("p1", "u1", "Lucas Andrade", "@lucas.oficial", "https://unsplash.com", true, "São Paulo, Brasil", "Fim de tarde depois do set de filmagem. ☕✨", "https://unsplash.com", 4820, comments = listOf(CommentItem("sofiamendes", "Foto incrível Lucas! 🔥"))),
                SocialPost("p2", "u2", "Sofia Mendes", "@sofiamendes", "https://unsplash.com", true, "Rio de Janeiro", "Pronta para a festa VIP de hoje à noite 🖤✨", "https://unsplash.com", 9310)
            )
        )
    }

    var chatText by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf(ChatMessage(senderIsMe = false, text = "Olá! Tudo bem? Vi seu perfil aqui!"))) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                NavigationBarItem(
                    selected = tab == "Início",
                    onClick = { tab = "Início" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Início", color = Color.White) }
                )
                NavigationBarItem(
                    selected = tab == "Mensagens",
                    onClick = { tab = "Mensagens" },
                    icon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                    label = { Text("Chats", color = Color.White) }
                )
                NavigationBarItem(
                    selected = tab == "Perfil",
                    onClick = { tab = "Perfil" },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil", color = Color.White) }
                )
            }
        },
        containerColor = Night
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Night)) {
            when (tab) {
                "Início" -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "Orbit Social",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    item {
                        LazyRow(modifier = Modifier.padding(bottom = 16.dp)) {
                            items(people) { user ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .background(InstaGradient)
                                            .padding(3.dp)
                                    ) {
                                        NetImage(
                                            url = user.avatarUrl,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    }
                                    Text(user.name, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(70.dp))
                                }
                            }
                        }
                    }
                    items(posts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Panel)
                        ) {
                            Column {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    NetImage(post.avatarUrl, modifier = Modifier.size(40.dp).clip(CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(post.author, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(post.location, color = Muted, fontSize = 12.sp)
                                    }
                                }
                                NetImage(post.imageUrl, modifier = Modifier.fillMaxWidth().height(250.dp))
                                Row(modifier = Modifier.padding(8.dp)) {
                                    IconButton(onClick = {
                                        posts = posts.map { if (it.id == post.id) it.copy(isLiked = !it.isLiked, likes = if (it.isLiked) it.likes - 1 else it.likes + 1) else it }
}) {
Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (post.isLiked) Color.Red else Color.White)
}
Text("${post.likes} curtidas", color = Color.White, modifier = Modifier.align(Alignment.CenterVertically))
}
Text(post.caption, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
}
}
}
}
"Mensagens" -> Column(modifier = Modifier.fillMaxSize()) {
LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
items(chatMessages) { msg ->
Box(
modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
contentAlignment = if (msg.senderIsMe) Alignment.CenterEnd else Alignment.CenterStart
) {
Box(
modifier = Modifier
.clip(RoundedCornerShape(12.dp))
.background(if (msg.senderIsMe) Lime else Panel)
.padding(12.dp)
) {
Text(msg.text, color = if (msg.senderIsMe) Color.Black else Color.White)
}
}
}
}
Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
TextField(
value = chatText,
onValueChange = { chatText = it },
modifier = Modifier.weight(1f),
placeholder = { Text("Digite sua mensagem...") }
)
IconButton(onClick = {
if (chatText.isNotBlank()) {
chatMessages = chatMessages + ChatMessage(senderIsMe = true, text = chatText)
chatText = ""
}
}) {
Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Lime)
}
}
}
"Perfil" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
NetImage(myAvatar, modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, Lime, CircleShape))
Spacer(modifier = Modifier.height(8.dp))
Text(myName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
Text(myHandle, color = Muted, fontSize = 16.sp)
Text(myBio, color = Color.White, modifier = Modifier.padding(12.dp))
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
Column(horizontalAlignment = Alignment.CenterHorizontally) {
Text("$followersCount", color = Color.White, fontWeight = FontWeight.Bold)
Text("Seguidores", color = Muted)
}
}
Spacer(modifier = Modifier.height(16.dp))
Button(onClick = { editOpen = true }, colors = ButtonDefaults.buttonColors(containerColor = Lime)) {
Text("Editar Perfil", color = Color.Black)
}
}
}
if (editOpen) {
AlertDialog(
onDismissRequest = { editOpen = false },
title = { Text("Editar Perfil") },
text = {
Column {
TextField(value = myName, onValueChange = { myName = it }, label = { Text("Nome") })
Spacer(modifier = Modifier.height(8.dp))
TextField(value = myBio, onValueChange = { myBio = it }, label = { Text("Bio") })
}
},
confirmButton = {
Button(onClick = { editOpen = false }) { Text("Salvar") }
}
)
}
}
}
}
