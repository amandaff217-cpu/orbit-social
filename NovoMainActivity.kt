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
        commentTargetPost?.let { post ->
            var newComment by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { commentTargetPost = null }, containerColor = Panel,
                title = { Text("Comentários • ${post.author}", color = Color.White, fontSize = 16.sp) },
                text = {
                    Column {
                        val currentPost = posts.find { it.id == post.id } ?: post
                        LazyColumn(Modifier.heightIn(max = 240.dp)) {
                            items(currentPost.comments) { c ->
                                Column(Modifier.padding(vertical = 6.dp)) {
                                    Text(c.author, color = Lime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(c.text, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = newComment, onValueChange = { newComment = it }, placeholder = { Text("Escreva um comentário...") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val txt = newComment.trim()
                        if (txt.isNotEmpty()) {
                            posts = posts.map { if (it.id == post.id) it.copy(comments = it.comments + CommentItem(displayName, txt) + CommentItem(post.author, "Amei seu comentário! Me chama na DM depois 😉✨")) else it }
                            followersCount += 15; newComment = ""
                        }
                    }) { Text("Enviar", color = Lime) }
                },
                dismissButton = { TextButton(onClick = { commentTargetPost = null }) { Text("Fechar", color = Muted) } }
            )
        }

        if (composeOpen) {
            val presetPhotos = listOf(
                "Ensaio ✨" to "https://unsplash.com",
                "Jantar VIP 🥂" to "https://unsplash.com",
                "Show 🎤" to "https://unsplash.com"
            )
            AlertDialog(
                onDismissRequest = { composeOpen = false }, containerColor = Panel,
                title = { Text("Nova publicação (+Seguidores!)", color = Color.White, fontSize = 17.sp) },
                text = {
                    Column {
                        OutlinedTextField(value = draftText, onValueChange = { draftText = it }, placeholder = { Text("Legenda da sua foto...") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            presetPhotos.forEach { (label, url) ->
                                FilterChip(selected = draftPhotoUrl == url, onClick = { draftPhotoUrl = url }, label = { Text(label, fontSize = 10.sp) })
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newPost = SocialPost(System.nanoTime().toString(), displayName, "@estrela.orbit", true, draftText.trim().ifEmpty { "Mais um momento incrível por aqui ✨" }, draftPhotoUrl.ifEmpty { presetPhotos.first().second }, (120..450).random().toLong(), false, false, listOf(CommentItem("lucas.oficial", "Arrasou demais nessa foto! 😍"), CommentItem("orbitstars.fc", "ÍCONE!! Já salvamos aqui 💚")))
                        posts = listOf(newPost) + posts; followersCount += (80..250).random(); draftText = ""; draftPhotoUrl = ""; composeOpen = false; tab = "Início"
                    }) { Text("Publicar e Ganhar Fãs", color = Lime) }
                },
                dismissButton = { TextButton(onClick = { composeOpen = false }) { Text("Cancelar", color = Muted) } }
            )
        }
    }
}
@Composable
fun FeedScreen(posts: List<SocialPost>, onPro: () -> Unit, onLike: (SocialPost) -> Unit, onFollow: (SocialPost) -> Unit, onOpenComments: (SocialPost) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Lime, Sky)), RoundedCornerShape(18.dp)).clickable { onPro() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Modo Celebridade & Romance ✨", color = Night, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Publique fotos e converse na DM com IA", color = Night, fontSize = 12.sp) }
                Text("→", color = Night, fontSize = 26.sp)
            }
        }
        items(posts, key = { it.id }) { post ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(Lime, Sky)), CircleShape), contentAlignment = Alignment.Center) { Text(post.author.take(1).uppercase(), color = Night, fontWeight = FontWeight.Bold) }
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.author, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (post.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(15.dp).padding(start = 4.dp))
                        }
                        Text(post.handle, color = Muted, fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { onFollow(post) }, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (post.isFollowing) Panel else Lime, contentColor = if (post.isFollowing) Color.White else Night)) {
                        Text(if (post.isFollowing) "Seguindo" else "Seguir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (post.imageUrl != null) AsyncImage(model = post.imageUrl, contentDescription = post.text, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(14.dp)).background(Panel))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    TextButton(onClick = { onLike(post) }) {
                        Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (post.isLiked) Color(0xFFFF4D6D) else Lime)
                        Text(" ${post.likes}", color = Color.White)
                    }
                    TextButton(onClick = { onOpenComments(post) }) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.White)
                        Text(" ${post.comments.size} Comentar", color = Color.White)
                    }
                }
                Text("${post.author}  ${post.text}", color = Color.White, fontSize = 13.sp)
                if (post.comments.isNotEmpty()) Text("Ver todos os ${post.comments.size} comentários...", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp).clickable { onOpenComments(post) })
                HorizontalDivider(Modifier.padding(top = 12.dp), color = Color(0xFF252B2C))
            }
        }
    }
}

@Composable
fun MessagesListScreen(contacts: List<AiContact>, onSelectContact: (AiContact) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Mensagens Diretas (IA)", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Converse para viver um romance ou gerenciar sua fama", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }
        items(contacts) { contact ->
            Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable { onSelectContact(contact) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(if (contact.category == "Romance") Brush.linearGradient(listOf(Color(0xFFFF758F), Lime)) else Brush.linearGradient(listOf(Lime, Sky)), CircleShape), contentAlignment = Alignment.Center) { Text(contact.avatarLetter, color = Night, fontWeight = FontWeight.Black, fontSize = 18.sp) }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp); if (contact.verified) Icon(Icons.Default.Verified, null, tint = Sky, modifier = Modifier.size(14.dp).padding(start = 4.dp)) }
                        Text(contact.role, color = Lime, fontSize = 11.sp)
                        Text(contact.messages.lastOrNull()?.text ?: "", color = Muted, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(contact: AiContact, onBack: () -> Unit, onSendMessage: (String, String) -> Unit) {
    var input by remember { mutableStateOf("") }; var isTyping by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            Column { Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(if (isTyping) "digitando..." else contact.role, color = Lime, fontSize = 12.sp) }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(contact.messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.senderIsMe) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (msg.senderIsMe) Lime else Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 270.dp)) {
                        Text(msg.text, color = if (msg.senderIsMe) Night else Color.White, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(value = input, onValueChange = { input = it }, placeholder = { Text("Escreva sua mensagem...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp))
            IconButton(onClick = {
                val text = input.trim()
                if (text.isNotEmpty() && !isTyping) { input = ""; isTyping = true; scope.launch { delay(900); val reply = generateAiReply(contact, text); isTyping = false; onSendMessage(text, reply) } }
            }) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Lime) }
        }
    }
}
fun generateAiReply(contact: AiContact, userMessage: String): String {
    val msg = userMessage.lowercase()
    return if (contact.category == "Romance") {
        when {
            listOf("oi", "olá", "tudo bem", "como vai").any { it in msg } -> "Agora que você me mandou mensagem, meu dia ficou mil vezes melhor 🥰 O que você gosta de fazer quando quer relaxar?"
            listOf("sair", "encontro", "jantar", "café", "ver").any { it in msg } -> "Eu estava torcendo para você fazer esse convite! Me busca às 20h? Prometo que vai ser uma noite inesquecível ✨🥂"
            listOf("lindo", "linda", "gosto", "amo", "paixão", "beijo").any { it in msg } -> "Você sabe exatamente como deixar meu coração acelerado... 😍 Nunca senti uma conexão tão rápida com alguém aqui no Orbit."
            else -> "Adoro o jeito que você conversa comigo 💕 Me conta mais sobre você... quero conhecer cada detalhe seu."
        }
    } else {
        when {
            listOf("contrato", "dinheiro", "marca", "aceito", "fechado").any { it in msg } -> "Excelente! Acabei de fechar o cachê VIP para você. Prepare-se porque seu nome vai estar nos trending topics hoje à noite! 🚀📸"
            listOf("fã", "obrigado", "amo vocês", "show", "foto").any { it in msg } -> "GENTE OLHA ISSO!! Printamos sua resposta e postamos no fã-clube, você é a celebridade mais humilde e perfeita de todas! 😭💚✨"
            else -> "Todo mundo na internet só fala de você hoje! Continua postando no feed que seus seguidores estão subindo sem parar 🔥★"
        }
    }
}

@Composable
fun ExploreScreen(onSelectCategory: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Explorar", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Descubra romances, celebridades e tendências.", color = Muted, modifier = Modifier.padding(top = 5.dp, bottom = 20.dp))
        listOf("Romance & Flertes 💖" to "Converse com crushes famosos na DM", "Fama & Paparazzi 📸" to "Feche contratos e ganhe seguidores", "Eventos VIP 🥂" to "Festas exclusivas do Orbit Social", "Fã-Clubes Oficial ★" to "Interaja com seus admiradores").forEach { (title, sub) ->
            Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelectCategory() }) {
                Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(sub, color = Muted, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun ProfileScreen(initialName: String, postCount: Int, followers: Int, following: Int, onSave: (String) -> Unit) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(90.dp).background(Brush.linearGradient(listOf(Lime, Sky)), CircleShape), contentAlignment = Alignment.Center) { Text("★", color = Night, fontWeight = FontWeight.Black, fontSize = 34.sp) }
        Text(initialName, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Text("@estrela.orbit • Perfil Verificado", color = Sky, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Stat(postCount.toString(), "Publicações"); Stat(followers.toString(), "Seguidores"); Stat(following.toString(), "Seguindo") }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Seu nome artístico") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onSave(name) }, colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Night), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Salvar perfil") }
    }
}

@Composable fun Stat(number: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(number, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(label, color = Muted, fontSize = 11.sp) } }
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
