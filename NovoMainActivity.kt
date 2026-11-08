package com.orbitsocial.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
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

data class CommentItem(val author: String, val text: String)

data class SocialPost(
    val id: String = "",
    val author: String = "Orbit user",
    val handle: String = "@orbit",
    val verified: Boolean = false,
    val text: String = "",
    val imageUrl: String? = null,
    val likes: Long = 0,
    val isLiked: Boolean = false,
    val isFollowing: Boolean = false,
    val comments: List<CommentItem> = emptyList()
)

data class ChatMessage(val id: String = System.nanoTime().toString(), val senderIsMe: Boolean, val text: String)

data class AiContact(
    val id: String,
    val name: String,
    val role: String,
    val category: String,
    val verified: Boolean,
    val avatarLetter: String,
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
    var displayName by remember { mutableStateOf("Você (Estrela em Ascensão)") }
    var followersCount by remember { mutableIntStateOf(1240) }
    var followingCount by remember { mutableIntStateOf(3) }

    var posts by remember {
        mutableStateOf(
            listOf(
                SocialPost("p1", "Lucas Andrade", "@lucas.oficial", true, "Fim de tarde perfeito depois das gravações. Alguém livre pra tomar um café hoje? ☕✨", "https://unsplash.com", 342, false, false, listOf(CommentItem("marina_fc", "Lindo demais como sempre!! 😍"), CommentItem("leo.produtor", "O clipe ficou absurdo irmão 🔥"))),
                SocialPost("p2", "Revista Vibe Pop", "@vibepop", true, "BOMBA! 🚨 Fontes confirmam que o novo rosto mais comentado do Orbit Social está prestes a fechar contrato milionário!", "https://unsplash.com", 1890, false, true, listOf(CommentItem("ana_luiza", "Já virou meu perfil favorito desse app!"), CommentItem("pedro.h", "Merece demais o sucesso 👏👏"))),
                SocialPost("p3", "Sofia Mendes", "@sofiamendes", true, "Me arrumando para o evento VIP de hoje à noite... será que te encontro por lá? 🖤✨", "https://unsplash.com", 875, false, false, listOf(CommentItem("camila.s", "Perfeitaaa 💖"), CommentItem("rafael_dj", "Nos vemos no camarote!"))),
                SocialPost("p4", "Fã Clube Orbit Stars", "@orbitstars.fc", false, "A cada publicação nova a gente fica mais orgulhoso! Obrigado por responder a gente na DM 🥺💚", "https://unsplash.com", 512, false, true, listOf(CommentItem("julia_fã", "Melhor ídolo do mundo!!")))
            )
        )
    }

    var contacts by remember {
        mutableStateOf(
            listOf(
                AiContact("c1", "Lucas Andrade", "Crush • Ator & Modelo", "Romance", true, "L", listOf(ChatMessage(senderIsMe = false, text = "Oi! Vi que você curtiu minhas fotos... confesso que já estava de olho no seu perfil faz tempo 😉"))),
                AiContact("c2", "Sofia Mendes", "Crush • Influenciadora", "Romance", true, "S", listOf(ChatMessage(senderIsMe = false, text = "Ei, que bom falar com você! Estava esperando você me mandar mensagem hoje ✨"))),
                AiContact("c3", "Marcos (Empresário VIP)", "Fama • Agência de Estrelas", "Fama", true, "M", listOf(ChatMessage(senderIsMe = false, text = "Olá! Seu engajamento explodiu hoje. Tenho duas propostas de capa de revista e uma campanha de luxo para você!"))),
                AiContact("c4", "Central de Fãs", "Fama • Fã-Clube Oficial", "Fama", false, "F", listOf(ChatMessage(senderIsMe = false, text = "AAA VOCÊ RESPONDEU!! O grupo inteiro tá surtando aqui, posta mais foto hoje por favor!! 😭💚")))
            )
        )
    }

    var selectedContactId by remember { mutableStateOf<String?>(null) }
    var commentTargetPost by remember { mutableStateOf<SocialPost?>(null) }
    var composeOpen by remember { mutableStateOf(false) }
    var draftText by remember { mutableStateOf("") }
    var draftPhotoUrl by remember { mutableStateOf("") }

    MaterialTheme(colorScheme = darkColorScheme(background = Night, surface = Panel, primary = Lime, onPrimary = Night, onBackground = Color.White, onSurface = Color.White)) {
        Scaffold(
            containerColor = Night,
            topBar = {
                Row(Modifier.fillMaxWidth().background(Night).padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.clickable { selectedContactId = null; tab = "Início" }) {
                        Text("Orbit", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("•", fontSize = 25.sp, color = Lime)
                        Text(" Social", fontSize = 13.sp, color = Muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(end = 8.dp)) {
                            Text("★ $followersCount fãs", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                        IconButton(onClick = { selectedContactId = null; tab = "Mensagens" }) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "DM", tint = Lime)
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Color.White) {
                    listOf("Início" to Icons.Default.Home, "Explorar" to Icons.Default.Explore, "Criar" to Icons.Default.AddCircle, "Favoritos" to Icons.Default.FavoriteBorder, "Perfil" to Icons.Default.Person).forEach { (label, icon) ->
                        NavigationBarItem(
                            selected = tab == label,
                            onClick = { if (label == "Criar") composeOpen = true else { selectedContactId = null; tab = label } },
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
                    "Início" -> FeedScreen(posts, { tab = "Orbit Pro" }, { target ->
                        posts = posts.map { if (it.id == target.id) { val liked = !it.isLiked; it.copy(isLiked = liked, likes = if (liked) it.likes + 1 else it.likes - 1) } else it }
                    }, { target ->
                        posts = posts.map { if (it.author == target.author) { val f = !it.isFollowing; followingCount += if (f) 1 else -1; it.copy(isFollowing = f) } else it }
                    }, { commentTargetPost = it })
                    "Explorar" -> ExploreScreen { tab = "Mensagens" }
                    "Favoritos" -> {
                        val liked = posts.filter { it.isLiked }
                        if (liked.isEmpty()) SimpleScreen("Seus favoritos", "Curta qualquer foto no feed para salvá-la aqui!")
                        else FeedScreen(liked, { tab = "Orbit Pro" }, { target -> posts = posts.map { if (it.id == target.id) it.copy(isLiked = false, likes = it.likes - 1) else it } }, {}, { commentTargetPost = it })
                    }
                    "Perfil" -> ProfileScreen(displayName, posts.count { it.author == displayName }, followersCount, followingCount) { displayName = it }
                    "Mensagens" -> {
                        val active = contacts.find { it.id == selectedContactId }
                        if (active != null) ChatDetailScreen(active, { selectedContactId = null }) { u, ai ->
contacts = contacts.map { if (it.id == active.id) it.copy(messages = it.messages + ChatMessage(senderIsMe = true, text = u) + ChatMessage(senderIsMe = false, text = ai)) else it }
followersCount += (5..25).random()
} else MessagesListScreen(contacts) { selectedContactId = it.id }
}
"Orbit Pro" -> ProScreen { tab = "Início" }
}
}
        }
        Text(number, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(label, color = Muted, fontSize = 11.sp) } }
@Composable fun SimpleScreen(title: String, body: String) { Column(Modifier.fillMaxSize().padding(22.dp)) { Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text(body, color = Muted, modifier = Modifier.padding(top = 10.dp)) } }
@Composable fun ProScreen(onBack: () -> Unit) {
Column(Modifier.fillMaxSize().padding(20.dp)) {
TextButton(onClick = onBack) { Text("← Voltar", color = Muted) }
Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Lime, Sky)), RoundedCornerShape(22.dp)).padding(22.dp)) {
Column { Text("✦", fontSize = 35.sp, color = Night); Text("Orbit Pro", fontSize = 31.sp, fontWeight = FontWeight.Black, color = Night); Text("Fama ilimitada e encontros exclusivos.", color = Night) }
}
listOf("✓  Selo de celebridade verificado", "✓  Respostas prioritárias dos crushes na DM", "✓  Crescimento acelerado de fãs").forEach { Text(it, modifier = Modifier.padding(vertical = 13.dp), fontWeight = FontWeight.SemiBold) }
}
}
