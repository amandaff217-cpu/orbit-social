package com.orbitsocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Night = Color(0xFF090B0C)
private val Panel = Color(0xFF171C1D)
private val Lime = Color(0xFFD8FF49)
private val Sky = Color(0xFFB8EFFF)
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
    val category: String, // "Romance", "Fama", "Lifestyle"
    val followersLabel: String,
    val isFollowing: Boolean = false
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
    val likes: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val comments: List<CommentItem> = emptyList()
)

data class ChatMessage(val id: String = System.nanoTime().toString(), val senderIsMe: Boolean, val text: String)

data class AiChatThread(
    val user: UserProfile,
    val messages: List<ChatMessage>
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { FirebaseApp.initializeApp(this) }
        setContent { OrbitSocialApp() }
    }
}

@Composable
fun OrbitSocialApp() {
    var tab by remember { mutableStateOf("Início") }
    var myName by remember { mutableStateOf("Novo Usuário") }
    var myHandle by remember { mutableStateOf("@voce.orbit") }
    var myBio by remember { mutableStateOf("Começando agora no Orbit ✨") }
    val myAvatar = "https://unsplash.com"

    // Começa literalmente do ZERO!
    var followersCount by remember { mutableIntStateOf(0) }
    var notifications by remember { mutableStateOf(listOf("Bem-vindo ao Orbit Social! Publique sua primeira foto ou siga pessoas para começar.")) }

    var people by remember {
        mutableStateOf(
            listOf(
                UserProfile("u1", "Lucas Andrade", "@lucas.oficial", "Ator & Modelo • SP 🎬", "https://unsplash.com", "https://unsplash.com", true, "Romance", "1.4M"),
                UserProfile("u2", "Sofia Mendes", "@sofiamendes", "Fashion & Lifestyle ✨ Paris / Rio", "https://unsplash.com", "https://unsplash.com", true, "Romance", "980K"),
                UserProfile("u3", "Gabriel Costa", "@gabriel.music", "Cantor & Compositor 🎸 Turnê 2026", "https://unsplash.com", "https://unsplash.com", true, "Romance", "2.1M"),
                UserProfile("u4", "Isabella Rocha", "@bellarocha", "Modelo Internacional 📸", "https://unsplash.com", "https://unsplash.com", true, "Romance", "750K"),
                UserProfile("u5", "Marcos Venture", "@marcos.vip", "Empresário & Caça-Talentos 💼", "https://unsplash.com", "https://unsplash.com", true, "Fama", "410K"),
                UserProfile("u6", "Vibe Pop News", "@vibepop", "Tudo sobre os famosos do Orbit 🚨", "https://unsplash.com", "https://unsplash.com", true, "Fama", "3.8M"),
                UserProfile("u7", "Rafael Torres", "@rafatorres.fit", "Atleta & Surfista 🌊☀️", "https://unsplash.com", "https://unsplash.com", false, "Romance", "85K"),
                UserProfile("u8", "Camila Duarte", "@camiladuarte", "Fotógrafa de Celebridades 📷", "https://unsplash.com", "https://unsplash.com", true, "Fama", "320K"),
                UserProfile("u9", "Thiago Alves", "@thiago.chef", "Gastronomia & Viagens 🍷", "https://unsplash.com", "https://unsplash.com", false, "Lifestyle", "42K"),
                UserProfile("u10", "Helena Martins", "@helenamartins", "Atriz de Cinema 🎭", "https://unsplash.com", "https://unsplash.com", true, "Fama", "1.9M")
            )
        )
    }

    var posts by remember {
        mutableStateOf(
            listOf(
                SocialPost("p1", "u1", "Lucas Andrade", "@lucas.oficial", "https://unsplash.com", true, "São Paulo, Brasil", "Fim de tarde depois do set de filmagem. Quem topa um café? ☕✨", "https://unsplash.com", 4820, comments = listOf(CommentItem("sofiamendes", "Foto incrível Lucas! 🔥"), CommentItem("marina_fc", "Lindo demais 😍"))),
                SocialPost("p2", "u2", "Sofia Mendes", "@sofiamendes", "https://unsplash.com", true, "Rio de Janeiro", "Pronta para a festa VIP de hoje à noite 🖤✨", "https://unsplash.com", 9310, comments = listOf(CommentItem("lucas.oficial", "Maravilhosa como sempre!"))),
                SocialPost("p3", "u6", "Vibe Pop News", "@vibepop", "https://unsplash.com", true, "Orbit Trends", "EXCLUSIVO 🚨 Agências estão de olho em novos perfis anônimos para transformar na próxima grande estrela!", "https://unsplash.com", 15400, comments = listOf(CommentItem("marcos.vip", "Estamos recrutando novos talentos hoje!"))),
                SocialPost("p4", "u3", "Gabriel Costa", "@gabriel.music", "https://unsplash.com", true, "Estúdio Central", "Escrevendo uma música nova pensando em alguém especial... 🎸💫", "https://unsplash.com", 12100, comments = listOf(CommentItem("helenamartins", "Já quero ouvir essa!"))),
                SocialPost("p5", "u4", "Isabella Rocha", "@bellarocha", "https://unsplash.com", true, "Milão, Itália", "Bastidores do ensaio de hoje 📸✨", "https://unsplash.com", 6740, comments = listOf(CommentItem("camiladuarte", "A luz ficou perfeita!")))
            )
        )
    }

    var chats by remember { mutableStateOf<Map<String, List<ChatMessage>>>(emptyMap()) }
    var activeChatUser by remember { mutableStateOf<UserProfile?>(null) }
    var activeStoryUser by remember { mutableStateOf<UserProfile?>(null) }
    var commentTargetPost by remember { mutableStateOf<SocialPost?>(null) }
    var composeOpen by remember { mutableStateOf(false) }

    fun toggleFollow(userId: String) {
        people = people.map { u ->
            if (u.id == userId) {
                val nowFollowing = !u.isFollowing
                if (nowFollowing && (1..10).random() > 4) {
                    followersCount += (1..3).random()
                    notifications = listOf("${u.name} (${u.handle}) começou a te seguir de volta! 🔥") + notifications
                }
                u.copy(isFollowing = nowFollowing)
            } else u
        }
    }

    fun openDmWith(user: UserProfile) {
        if (!chats.containsKey(user.id)) {
            val opener = if (user.category == "Romance") {
"Oi! Vi que você passou pelo meu perfil... adorei sua vibe 😉✨"
} else {
"Olá! Aqui é ${user.name}. Vi que você está começando no Orbit agora, como posso ajudar?"
}
chats = chats + (user.id to listOf(ChatMessage(senderIsMe = false, text = opener)))
}
activeChatUser = user
tab = "DM"
    }
    ---

#### Parte 2 (Estrutura Visual, Feed estilo Instagram, Stories e Explorar)
```kotlin
    MaterialTheme(colorScheme = darkColorScheme(background = Night, surface = Panel, primary = Lime, onPrimary = Night, onBackground = Color.White, onSurface = Color.White)) {
        Scaffold(
            containerColor = Night,
            topBar = {
                Row(Modifier.fillMaxWidth().background(Night).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.clickable { activeChatUser = null; tab = "Início" }) {
                        Text("Orbit", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("•", fontSize = 24.sp, color = Lime)
                        Text(" Social", fontSize = 13.sp, color = Muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { activeChatUser = null; tab = "Atividade" }) {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Notificações", tint = Color.White)
                        }
                        IconButton(onClick = { activeChatUser = null; tab = "DM" }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Direct", tint = Lime)
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Color.White) {
                    listOf(
                        "Início" to Icons.Default.Home,
                        "Explorar" to Icons.Default.Search,
                        "Criar" to Icons.Default.AddBox,
                        "Salvos" to Icons.Default.BookmarkBorder,
                        "Perfil" to Icons.Default.Person
                    ).forEach { (label, icon) ->
                        NavigationBarItem(
                            selected = tab == label,
                            onClick = { if (label == "Criar") composeOpen = true else { activeChatUser = null; tab = label } },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = Color(0xFF303A28), unselectedIconColor = Muted, unselectedTextColor = Muted)
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).background(Night)) {
                when (tab) {
                    "Início" -> InstaFeedScreen(
                        people = people,
                        posts = posts,
                        onOpenStory = { activeStoryUser = it },
                        onToggleFollow = { toggleFollow(it) },
                        onLike = { target ->
                            posts = posts.map {
                                if (it.id == target.id) {
                                    val liked = !it.isLiked
                                    it.copy(isLiked = liked, likes = if (liked) it.likes + 1 else it.likes - 1)
                                } else it
                            }
                        },
                        onSavePost = { target ->
                            posts = posts.map { if (it.id == target.id) it.copy(isSaved = !it.isSaved) else it }
                        },
                        onOpenComments = { commentTargetPost = it },
                        onShareDm = { userId -> people.find { it.id == userId }?.let { openDmWith(it) } }
                    )
                    "Explorar" -> InstaExploreScreen(
                        people = people,
                        posts = posts,
                        onToggleFollow = { toggleFollow(it) },
                        onMessageUser = { openDmWith(it) }
                    )
                    "Salvos" -> {
                        val saved = posts.filter { it.isSaved }
                        if (saved.isEmpty()) {
                            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.BookmarkBorder, null, tint = Lime, modifier = Modifier.size(48.dp))
                                Text("Nenhuma publicação salva", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp))
                                Text("Toque no ícone de marcador nas fotos do feed para salvar aqui.", color = Muted, fontSize = 13.sp)
                            }
                        } else {
                            InstaFeedScreen(emptyList(), saved, {}, { toggleFollow(it) }, { t -> posts = posts.map { if (it.id == t.id) it.copy(isLiked = !it.isLiked) else it } }, { t -> posts = posts.map { if (it.id == t.id) it.copy(isSaved = !it.isSaved) else it } }, { commentTargetPost = it }, {})
                        }
                    }
                    "Atividade" -> {
                        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item { Text("Notificações & Fama", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                            items(notifications) { note ->
                                Surface(color = Panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(note, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                                }
                            }
                        }
                    }
                    "Perfil" -> InstaProfileScreen(
                        name = myName,
                        handle = myHandle,
                        bio = myBio,
                        avatarUrl = myAvatar,
                        myPosts = posts.filter { it.userId == "me" },
                        followers = followersCount,
                        following = people.count { it.isFollowing },
                        isVerified = followersCount >= 1000,
                        onUpdateProfile = { n, b -> myName = n; myBio = b }
                    )
                    "DM" -> {
                        val currentChatUser = activeChatUser
                        if (currentChatUser != null) {
                            ChatDetailScreen(
                                user = currentChatUser,
                                messages = chats[currentChatUser.id] ?: emptyList(),
                                onBack = { activeChatUser = null },
                                onSend = { userText, aiReply ->
                                    val oldList = chats[currentChatUser.id] ?: emptyList()
                                    chats = chats + (currentChatUser.id to (oldList + ChatMessage(senderIsMe = true, text = userText) + ChatMessage(senderIsMe = false, text = aiReply)))
                                    val gained = (2..12).random()
                                    followersCount += gained
                                    notifications = listOf("Você ganhou +$gained seguidores após interagir com ${currentChatUser.name}! ★") + notifications
                                }
                            )
                        } else {
                            DirectInboxScreen(people = people, chats = chats, onSelectUser = { openDmWith(it) })
                        }
                    }
                }
            }
        }

        // Visualizador de Story em Tela Cheia
        activeStoryUser?.let { storyUser ->
            var storyReply by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { activeStoryUser = null },
                containerColor = Color.Black,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = storyUser.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(34.dp).clip(CircleShape))
                        Text("  ${storyUser.handle}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        AsyncImage(model = storyUser.storyUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(14.dp)))
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = storyReply, onValueChange = { storyReply = it }, placeholder = { Text("Responder ao story...") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val txt = storyReply.trim().ifEmpty { "Amei seu story! 😍" }
                        activeStoryUser = null
                        openDmWith(storyUser)
                        val oldList = chats[storyUser.id] ?: emptyList()
                        val reply = generateInstaAiReply(storyUser, txt)
                        chats = chats + (storyUser.id to (oldList + ChatMessage(senderIsMe = true, text = "Respondeu ao story: $txt") + ChatMessage(senderIsMe = false, text = reply)))
                    }) { Text("Enviar na DM", color = Lime) }
                },
                dismissButton = { TextButton(onClick = { activeStoryUser = null }) { Text("Fechar", color = Muted) } }
            )
        }
                // Modal de Comentários
        commentTargetPost?.let { post ->
            var newComment by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { commentTargetPost = null }, containerColor = Panel,
                title = { Text("Comentários", color = Color.White, fontSize = 16.sp) },
                text = {
                    Column {
                        val currentPost = posts.find { it.id == post.id } ?: post
                        LazyColumn(Modifier.heightIn(max = 230.dp)) {
                            items(currentPost.comments) { c ->
                                Column(Modifier.padding(vertical = 5.dp)) {
                                    Text(c.author, color = Lime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(c.text, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = newComment, onValueChange = { newComment = it }, placeholder = { Text("Adicione um comentário...") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val txt = newComment.trim()
                        if (txt.isNotEmpty()) {
                            posts = posts.map {
                                if (it.id == post.id) it.copy(comments = it.comments + CommentItem(myHandle, txt) + CommentItem(post.handle, "Valeu pelo carinho! Me chama na DM 😉"))
                                else it
                            }
                            followersCount += (1..5).random()
                            newComment = ""
                        }
                    }) { Text("Publicar", color = Lime) }
                },
                dismissButton = { TextButton(onClick = { commentTargetPost = null }) { Text("Fechar", color = Muted) } }
            )
        }

        // Modal de Nova Publicação (Começando do zero e ganhando fãs!)
        if (composeOpen) {
            var caption by remember { mutableStateOf("") }
            var selectedImg by remember { mutableStateOf("https://unsplash.com") }
            val gallery = listOf(
                "Look do Dia ✨" to "https://unsplash.com",
                "Café & Estilo ☕" to "https://unsplash.com",
                "Pôr do Sol 🌅" to "https://unsplash.com",
                "Noite VIP 🥂" to "https://unsplash.com"
            )
            AlertDialog(
                onDismissRequest = { composeOpen = false }, containerColor = Panel,
                title = { Text("Nova publicação", color = Color.White, fontSize = 17.sp) },
                text = {
                    Column {
                        AsyncImage(model = selectedImg, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(gallery) { (label, url) ->
                                FilterChip(selected = selectedImg == url, onClick = { selectedImg = url }, label = { Text(label, fontSize = 11.sp) })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = caption, onValueChange = { caption = it }, placeholder = { Text("Escreva uma legenda...") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val gainedFans = if (followersCount == 0) (12..35).random() else (45..180).random()
                        val newPost = SocialPost(
                            id = System.nanoTime().toString(),
                            userId = "me",
                            author = myName,
                            handle = myHandle,
                            avatarUrl = myAvatar,
                            verified = followersCount >= 1000,
                            location = "Brasil",
                            caption = caption.trim().ifEmpty { "Primeiro passo rumo ao topo ✨" },
                            imageUrl = selectedImg,
                            likes = gainedFans,
                            comments = listOf(CommentItem("@lucas.oficial", "Bem-vindo ao Orbit! Foto top 🔥"))
                        )
                        posts = listOf(newPost) + posts
                        followersCount += gainedFans
                        notifications = listOf("Sua nova foto viralizou! Você ganhou +$gainedFans seguidores ★") + notifications
                        composeOpen = false
                        tab = "Início"
                    }) { Text("Compartilhar", color = Lime) }
                },
                dismissButton = { TextButton(onClick = { composeOpen = false }) { Text("Cancelar", color = Muted) } }
            )
        }
    }
}

@Composable
fun InstaFeedScreen(
    people: List<UserProfile>,
    posts: List<SocialPost>,
    onOpenStory: (UserProfile) -> Unit,
    onToggleFollow: (String) -> Unit,
    onLike: (SocialPost) -> Unit,
    onSavePost: (SocialPost) -> Unit,
    onOpenComments: (SocialPost) -> Unit,
    onShareDm: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (people.isNotEmpty()) {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(people) { user ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable { onOpenStory(user) }) {
                            Box(Modifier.size(66.dp).border(2.5.dp, InstaGradient, CircleShape).padding(4.dp)) {
                                AsyncImage(model = user.avatarUrl, contentDescription = user.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape).background(Panel))
                            }
                            Text(user.handle.removePrefix("@"), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFF202627))
            }
        }
        items(posts, key = { it.id }) { post ->
            val person = people.find { it.id == post.userId }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    AsyncImage(model = post.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(38.dp).clip(CircleShape).background(Panel))
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.handle.removePrefix("@"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (post.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(14.dp).padding(start = 4.dp))
                        }
                        Text(post.location, color = Muted, fontSize = 11.sp)
                    }
                    if (post.userId != "me" && person != null) {
                        TextButton(onClick = { onToggleFollow(person.id) }) {
                            Text(if (person.isFollowing) "Seguindo" else "Seguir", color = if (person.isFollowing) Muted else Lime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(340.dp).background(Panel).pointerInput(post.id) {
                        detectTapGestures(onDoubleTap = { if (!post.isLiked) onLike(post) })
                    }
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        IconButton(onClick = { onLike(post) }) {
                            Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (post.isLiked) Color(0xFFFF3B5C) else Color.White)
                        }
                        IconButton(onClick = { onOpenComments(post) }) {
                            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White)
                        }
                        IconButton(onClick = { onShareDm(post.userId) }) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                        }
                    }
                    IconButton(onClick = { onSavePost(post) }) {
                        Icon(if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (post.isSaved) Lime else Color.White)
                    }
                }
                Column(Modifier.padding(horizontal = 14.dp)) {
                    Text("${post.likes} curtidas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${post.handle.removePrefix("@")}  ${post.caption}", fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                    if (post.comments.isNotEmpty()) {
                        Text("Ver todos os ${post.comments.size} comentários", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp).clickable { onOpenComments(post) })
                    }
                }
            }
        }
    }
}

@Composable
fun InstaExploreScreen(people: List<UserProfile>, posts: List<SocialPost>, onToggleFollow: (String) -> Unit, onMessageUser: (UserProfile) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = people.filter { it.name.contains(query, ignoreCase = true) || it.handle.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Pesquisar pessoas, romance ou fama...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Text("Sugestões para seguir (${filtered.size} perfis)", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        }
        items(filtered) { user ->
            Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = user.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape))
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (user.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(14.dp).padding(start = 4.dp))
                        }
                        Text("${user.handle} • ${user.followersLabel} fãs", color = Lime, fontSize = 11.sp)
                        Text(user.bio, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Button(
                            onClick = { onToggleFollow(user.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.isFollowing) Color(0xFF2B3234) else Lime, contentColor = if (user.isFollowing) Color.White else Night),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) { Text(if (user.isFollowing) "Seguindo" else "Seguir", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        Text("DM", color = Sky, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp).clickable { onMessageUser(user) })
                    }
                }
            }
        }
    }
}

@Composable
fun DirectInboxScreen(people: List<UserProfile>, chats: Map<String, List<ChatMessage>>, onSelectUser: (UserProfile) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Mensagens (Direct)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Toque em qualquer perfil para iniciar um romance ou parceria", color = Muted, fontSize = 12.sp)
        }
        items(people) { user ->
            val lastMsg = chats[user.id]?.lastOrNull()?.text ?: "Toque para enviar uma mensagem..."
            Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onSelectUser(user) }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = user.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape))
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (user.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(14.dp).padding(start = 4.dp))
                        }
                        Text(user.category, color = Lime, fontSize = 11.sp)
                        Text(lastMsg, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(user: UserProfile, messages: List<ChatMessage>, onBack: () -> Unit, onSend: (String, String) -> Unit) {
    var input by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            AsyncImage(model = user.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(36.dp).clip(CircleShape))
            Column(Modifier.padding(start = 10.dp)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(if (typing) "digitando..." else user.handle, color = Lime, fontSize = 11.sp)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.senderIsMe) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (msg.senderIsMe) Lime else Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 260.dp)) {
                        Text(msg.text, color = if (msg.senderIsMe) Night else Color.White, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(value = input, onValueChange = { input = it }, placeholder = { Text("Mensagem...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp))
            IconButton(onClick = {
                val txt = input.trim()
                if (txt.isNotEmpty() && !typing) {
                    input = ""
                    typing = true
                    scope.launch {
                        delay(800)
                        val reply = generateInstaAiReply(user, txt)
                        typing = false
                        onSend(txt, reply)
                    }
                }
            }) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Lime) }
        }
    }
}

fun generateInstaAiReply(user: UserProfile, text: String): String {
    val msg = text.lowercase()
    return if (user.category == "Romance") {
        when {
            listOf("oi", "olá", "tudo bem", "hey").any { it in msg } -> "Estava esperando você aparecer por aqui 🥰 Como está sendo seu dia?"
            listOf("sair", "jantar", "café", "encontro", "ver").any { it in msg } -> "Topo na hora! Me diz o horário que eu já começo a me arrumar ✨🥂"
            listOf("lindo", "linda", "gosto", "amo", "beijo", "crush").any { it in msg } -> "Você me deixa sem palavras desse jeito... 😍 Acho que estou me apaixonando por você!"
            else -> "Adoro conversar com você 💕 Me conta mais sobre você, quero saber tudo!"
        }
    } else {
        when {
            listOf("fama", "contrato", "parceria", "marca", "divulgar").any { it in msg } -> "Acabei de compartilhar seu perfil nos meus stories! Já já seus seguidores vão disparar 🚀📸"
            else -> "Seu perfil tem muito potencial para viralizar! Continua postando fotos e interagindo nos comentários 🔥"
        }
    }
}

@Composable
fun InstaProfileScreen(
    name: String,
    handle: String,
    bio: String,
    avatarUrl: String,
    myPosts: List<SocialPost>,
    followers: Int,
    following: Int,
    isVerified: Boolean,
    onUpdateProfile: (String, String) -> Unit
) {
    var editOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AsyncImage(model = avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Lime, CircleShape))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${myPosts.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Posts", color = Muted, fontSize = 12.sp) }
Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("$followers", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Seguidores", color = Muted, fontSize = 12.sp) }
Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("$following", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Seguindo", color = Muted, fontSize = 12.sp) }
}
}
Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
if (isVerified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(16.dp).padding(start = 4.dp))
}
Text(handle, color = Muted, fontSize = 12.sp)
Text(bio, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
Text(if (isVerified) "★ Conta Verificada Conquistada!" else "Conquiste 1.000 seguidores para ganhar o selo verificado", color = Lime, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
OutlinedButton(onClick = { editOpen = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), shape = RoundedCornerShape(10.dp)) {
Text("Editar perfil", color = Color.White)
}
HorizontalDivider(color = Color(0xFF202627))
if (myPosts.isEmpty()) {
Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
Text("Você ainda não publicou fotos.\nToque em 'Criar' abaixo para começar do zero!", color = Muted, fontSize = 13.sp)
}
} else {
LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
items(myPosts) { p ->
AsyncImage(model = p.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.aspectRatio(1f).background(Panel))
}
}
}
}
if (editOpen) {
var newName by remember { mutableStateOf(name) }
var newBio by remember { mutableStateOf(bio) }
AlertDialog(
onDismissRequest = { editOpen = false }, containerColor = Panel,
title = { Text("Editar Perfil", color = Color.White) },
text = {
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nome") })
OutlinedTextField(value = newBio, onValueChange = { newBio = it }, label = { Text("Biografia") })
}
},
confirmButton = {
TextButton(onClick = {
onUpdateProfile(newName, newBio)
editOpen = false
}) { Text("Salvar", color = Lime) }
},
dismissButton = {
TextButton(onClick = { editOpen = false }) { Text("Cancelar", color = Muted) }
}
)
}
}
