package com.orbitsocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    val category: String,
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
    val myAvatar = "https://randomuser.me"

    var followersCount by remember { mutableIntStateOf(0) }
    var notifications by remember { mutableStateOf(listOf("Bem-vindo ao Orbit Social! Publique sua primeira foto ou siga pessoas para começar.")) }

    var people by remember {
        mutableStateOf(
            listOf(
                UserProfile("u1", "Lucas Andrade", "@lucas.oficial", "Ator & Modelo • SP 🎬\nSolteiro • Amante de café e praia", "https://randomuser.me", "https://picsum.photos", true, "Romance", "1.4M"),
                UserProfile("u2", "Sofia Mendes", "@sofiamendes", "Fashion & Lifestyle ✨ Paris / Rio\nParcerias via DM 💌", "https://randomuser.me", "https://picsum.photos", true, "Romance", "980K"),
                UserProfile("u3", "Gabriel Costa", "@gabriel.music", "Cantor & Compositor 🎸 Turnê 2026\nOuça meu novo álbum!", "https://randomuser.me", "https://picsum.photos", true, "Romance", "2.1M"),
                UserProfile("u4", "Isabella Rocha", "@bellarocha", "Modelo Internacional 📸\nMilão • São Paulo", "https://randomuser.me", "https://picsum.photos", true, "Romance", "750K"),
                UserProfile("u5", "Marcos Venture", "@marcos.vip", "Empresário & Caça-Talentos 💼\nDescobrindo as próximas estrelas do Orbit", "https://randomuser.me", "https://picsum.photos", true, "Fama", "410K"),
                UserProfile("u6", "Vibe Pop News", "@vibepop", "Tudo sobre os famosos e romances do Orbit 🚨", "https://picsum.photos", "https://picsum.photos", true, "Fama", "3.8M"),
                UserProfile("u7", "Rafael Torres", "@rafatorres.fit", "Atleta & Surfista 🌊☀️", "https://randomuser.me", "https://picsum.photos", false, "Romance", "85K"),
                UserProfile("u8", "Camila Duarte", "@camiladuarte", "Fotógrafa de Celebridades 📷", "https://randomuser.me", "https://picsum.photos", true, "Fama", "320K"),
                UserProfile("u9", "Thiago Alves", "@thiago.chef", "Gastronomia & Viagens 🍷", "https://randomuser.me", "https://picsum.photos", false, "Lifestyle", "42K"),
                UserProfile("u10", "Helena Martins", "@helenamartins", "Atriz de Cinema 🎭", "https://randomuser.me", "https://picsum.photos", true, "Fama", "1.9M")
            )
        )
    }

    var posts by remember {
        mutableStateOf(
            listOf(
                SocialPost("p1", "u1", "Lucas Andrade", "@lucas.oficial", "https://randomuser.me", true, "São Paulo, Brasil", "Fim de tarde depois do set de filmagem. Quem topa um café? ☕✨", "https://picsum.photos", 4820, comments = listOf(CommentItem("sofiamendes", "Foto incrível Lucas! 🔥"), CommentItem("marina_fc", "Lindo demais 😍"))),
                SocialPost("p2", "u2", "Sofia Mendes", "@sofiamendes", "https://randomuser.me", true, "Rio de Janeiro", "Pronta para a festa VIP de hoje à noite 🖤✨", "https://picsum.photos", 9310, comments = listOf(CommentItem("lucas.oficial", "Maravilhosa como sempre!"))),
                SocialPost("p3", "u6", "Vibe Pop News", "@vibepop", "https://picsum.photos", true, "Orbit Trends", "EXCLUSIVO 🚨 Agências estão de olho em novos perfis anônimos para transformar na próxima grande estrela!", "https://picsum.photos", 15400, comments = listOf(CommentItem("marcos.vip", "Estamos recrutando novos talentos hoje!"))),
                SocialPost("p4", "u3", "Gabriel Costa", "@gabriel.music", "https://randomuser.me", true, "Estúdio Central", "Escrevendo uma música nova pensando em alguém especial... 🎸💫", "https://picsum.photos", 12100, comments = listOf(CommentItem("helenamartins", "Já quero ouvir essa!"))),
                SocialPost("p5", "u4", "Isabella Rocha", "@bellarocha", "https://randomuser.me", true, "Milão, Itália", "Bastidores do ensaio de hoje 📸✨", "https://picsum.photos", 6740, comments = listOf(CommentItem("camiladuarte", "A luz ficou perfeita!"))),
                SocialPost("p6", "u1", "Lucas Andrade", "@lucas.oficial", "https://randomuser.me", true, "Angra dos Reis", "Dia de folga no mar 🌊☀️", "https://picsum.photos", 5390),
                SocialPost("p7", "u2", "Sofia Mendes", "@sofiamendes", "https://randomuser.me", true, "Paris, França", "Café da manhã com vista ✨🥐", "https://picsum.photos", 11200)
            )
        )
    }

    var chats by remember { mutableStateOf<Map<String, List<ChatMessage>>>(emptyMap()) }
    var activeChatUser by remember { mutableStateOf<UserProfile?>(null) }
    var inspectedUserId by remember { mutableStateOf<String?>(null) }
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
inspectedUserId = null
activeChatUser = user
tab = "DM"
    }
                            "Perfil" to Icons.Default.Person
                    ).forEach { (label, icon) ->
                        NavigationBarItem(
                            selected = tab == label && inspectedUserId == null,
                            onClick = { if (label == "Criar") composeOpen = true else { activeChatUser = null; inspectedUserId = null; tab = label } },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, selectedTextColor = Lime, indicatorColor = Color(0xFF303A28), unselectedIconColor = Muted, unselectedTextColor = Muted)
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).background(Night)) {
                val targetPerson = people.find { it.id == inspectedUserId }
                if (targetPerson != null) {
                    OtherUserProfileScreen(
                        user = targetPerson,
                        userPosts = posts.filter { it.userId == targetPerson.id },
                        onBack = { inspectedUserId = null },
                        onToggleFollow = { toggleFollow(targetPerson.id) },
                        onMessage = { openDmWith(targetPerson) }
                    )
                } else {
                    when (tab) {
                        "Início" -> InstaFeedScreen(
                            people = people,
                            posts = posts,
                            onOpenStory = { activeStoryUser = it },
                            onOpenUserProfile = { id -> if (id == "me") tab = "Perfil" else inspectedUserId = id },
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
                            onOpenUserProfile = { inspectedUserId = it.id },
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
                                InstaFeedScreen(emptyList(), saved, {}, { id -> if (id == "me") tab = "Perfil" else inspectedUserId = id }, { toggleFollow(it) }, { t -> posts = posts.map { if (it.id == t.id) it.copy(isLiked = !it.isLiked) else it } }, { t -> posts = posts.map { if (it.id == t.id) it.copy(isSaved = !it.isSaved) else it } }, { commentTargetPost = it }, {})
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
                                    onOpenProfile = { activeChatUser = null; inspectedUserId = currentChatUser.id },
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
        }

        activeStoryUser?.let { storyUser ->
            var storyReply by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { activeStoryUser = null },
                containerColor = Color.Black,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { activeStoryUser = null; inspectedUserId = storyUser.id }) {
                        NetImage(storyUser.avatarUrl, Modifier.size(34.dp).clip(CircleShape))
                        Text("  ${storyUser.handle}  (ver perfil)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        NetImage(storyUser.storyUrl, Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(14.dp)))
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

        if (composeOpen) {
            var caption by remember { mutableStateOf("") }
            var selectedImg by remember { mutableStateOf("https://picsum.photos") }
            val gallery = listOf(
                "Lifestyle ✨" to "https://picsum.photos",
                "Praia 🌊" to "https://picsum.photos",
                "Cidade 🌃" to "https://picsum.photos",
                "Natureza 🌿" to "https://picsum.photos"
            )
            AlertDialog(
                onDismissRequest = { composeOpen = false }, containerColor = Panel,
                title = { Text("Nova publicação", color = Color.White, fontSize = 17.sp) },
                text = {
                    Column {
                        NetImage(selectedImg, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)))
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
fun OtherUserProfileScreen(
    user: UserProfile,
    userPosts: List<SocialPost>,
    onBack: () -> Unit,
    onToggleFollow: () -> Unit,
    onMessage: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            Text(user.handle.removePrefix("@"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (user.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(16.dp).padding(start = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            NetImage(user.avatarUrl, Modifier.size(80.dp).clip(CircleShape).border(2.dp, InstaGradient, CircleShape))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${userPosts.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Posts", color = Muted, fontSize = 12.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(user.followersLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Seguidores", color = Muted, fontSize = 12.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("340", fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Seguindo", color = Muted, fontSize = 12.sp) }
            }
        }
        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 10.dp))
        Text(user.category, color = Lime, fontSize = 12.sp)
        Text(user.bio, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onToggleFollow,
                colors = ButtonDefaults.buttonColors(containerColor = if (user.isFollowing) Panel else Lime, contentColor = if (user.isFollowing) Color.White else Night),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) { Text(if (user.isFollowing) "Seguindo" else "Seguir", fontWeight = FontWeight.Bold) }
            OutlinedButton(
                onClick = onMessage,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Mensagem", color = Color.White) }
        }
        HorizontalDivider(color = Color(0xFF202627))
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val gallery = if (userPosts.isNotEmpty()) userPosts.map { it.imageUrl } else listOf(user.storyUrl, user.avatarUrl)
            items(gallery) { img ->
                NetImage(img, Modifier.aspectRatio(1f).background(Panel))
            }
        }
    }
}

@Composable
fun InstaFeedScreen(
    people: List<UserProfile>,
    posts: List<SocialPost>,
    onOpenStory: (UserProfile) -> Unit,
    onOpenUserProfile: (String) -> Unit,
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
                                NetImage(user.avatarUrl, Modifier.fillMaxSize().clip(CircleShape).background(Panel))
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
                color = Lime, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
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
NetImage(p.imageUrl, Modifier.aspectRatio(1f).background(Panel))
}
}
}
}
    if (editOpen) {
        var newName by remember { mutableStateOf(name) }
        var newBio by remember { mutableStateOf(bio) }
        AlertDialog(
            onDismissRequest = { editOpen = false },
            containerColor = Panel,
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
    }
