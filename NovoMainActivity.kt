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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Cores do Tema Escuro estilo Instagram
private val Night = Color(0xFF000000)
private val Panel = Color(0xFF121212)
private val BorderColor = Color(0xFF262626)
private val AccentBlue = Color(0xFF0095F6)
private val Muted = Color(0xFFA8A8A8)
private val StoryGradient = Brush.linearGradient(listOf(Color(0xFFF9CE34), Color(0xFFEE2A7B), Color(0xFF6228D7)))

data class IaCharacter(
    val id: String,
    val name: String,
    val handle: String,
    val bio: String,
    val avatarUrl: String,
    val category: String,
    val isVerified: Boolean = true,
    var relationshipLevel: Int = 0, // 0 = Desconhecido, 1 = Amigo, 2 = Paquera, 3 = Namorando
    var statusText: String = "Seguir"
)

data class SocialPost(
    val id: String,
    val characterId: String,
    val author: String,
    val handle: String,
    val avatarUrl: String,
    val location: String,
    val caption: String,
    val imageUrl: String,
    var likes: Int,
    var isLiked: Boolean = false
)

data class DirectMessage(
    val id: String = System.nanoTime().toString(),
    val senderIsMe: Boolean,
    val text: String,
    val attachedImageUrl: String? = null
)

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
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("Feed") }
    
    // Status do Jogador (Mecânica de Fama)
    var myName by remember { mutableStateOf("Seu Nome") }
    var myHandle by remember { mutableStateOf("@seu.perfil") }
    var myBio by remember { mutableStateOf("Buscando o topo do Orbitocial 🚀") }
    var followersCount by remember { mutableIntStateOf(150) }
    var followingCount by remember { mutableIntStateOf(84) }
    var editOpen by remember { mutableStateOf(false) }

    // Personagens de IA Realistas (Imagens do Unsplash simulando pessoas reais)
    var characters by remember {
        mutableStateOf(
            listOf(
                IaCharacter("c1", "Enzo Valentim", "@enzo.val", "Modelo & Atleta • SP 🌊\nVivendo intensamente.", "https://unsplash.com", "Romance"),
                IaCharacter("c2", "Clara Hoffmann", "@clara.hoff", "Fashion Designer | Paris - RJ ✨\nParcerias por DM", "https://unsplash.com", "Romance"),
                IaCharacter("c3", "Juliana Paiva", "@ju.paiva", "Cantora e Compositora 🎸\nNovas músicas vindo aí...", "https://unsplash.com", "Fama"),
                IaCharacter("c4", "Rodrigo Faro", "@rodrigo.ai", "Investidor & Caça-talentos 🕶️\nTe transformo em celebridade.", "https://unsplash.com", "Fama")
            )
        )
    }

    // Feed Estilo Instagram
    var posts by remember {
        mutableStateOf(
            listOf(
                SocialPost("p1", "c2", "Clara Hoffmann", "@clara.hoff", "https://unsplash.com", "Paris, France", "Look do dia para o desfile de outono 🖤✨ O que acharam?", "https://unsplash.com", 1420),
                SocialPost("p2", "c1", "Enzo Valentim", "@enzo.val", "https://unsplash.com", "Rio de Janeiro", "Mais um dia de treino concluído por aqui. 🏄‍♂️☀️", "https://unsplash.com", 982),
                SocialPost("p3", "c3", "Juliana Paiva", "@ju.paiva", "https://unsplash.com", "Estúdio", "Madrugada produtiva compondo faixas de amor... 🎤🎶", "https://unsplash.com", 2540)
            )
        )
    }

    // Sistema de Mensagens Diretas (DMs) com envio automático de fotos pela IA conforme o relacionamento aumenta
    var activeChatCharacterId by remember { mutableStateOf("c2") }
    var chatMessagesMap by remember {
        mutableStateOf(
            mapOf(
                "c1" to listOf(DirectMessage(senderIsMe = false, text = "E aí! Curti muito o seu perfil novo.")),
                "c2" to listOf(DirectMessage(senderIsMe = false, text = "Oii! Tudo bem? Vi que começou a me seguir agora...")),
                "c3" to listOf(DirectMessage(senderIsMe = false, text = "Ei! Você trabalha com música também?")),
                "c4" to listOf(DirectMessage(senderIsMe = false, text = "Seu perfil tem potencial. Quer fechar uma parceria publicitária?"))
            )
        )
    }
    var currentTypedMessage by remember { mutableStateOf("") }

    // Loop de simulação para ganhar fama/seguidores passivamente com o tempo
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000) // A cada 8 segundos novos seguidores chegam
            followersCount += (1..15).random()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Night, modifier = Modifier.border(0.5.dp, BorderColor, RoundedCornerShape(0.dp))) {
                NavigationBarItem(
                    selected = tab == "Feed",
                    onClick = { tab = "Feed" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) },
                    label = { Text("Feed", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = tab == "Explorar",
                    onClick = { tab = "Explorar" },
                    icon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    label = { Text("Descobrir", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = tab == "Mensagens",
                    onClick = { tab = "Mensagens" },
                    icon = { Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color.White) },
                    label = { Text("DMs", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = tab == "Perfil",
                    onClick = { tab = "Perfil" },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                    label = { Text("Perfil", color = Color.White, fontSize = 10.sp) }
                )
            }
        },
        containerColor = Night
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Night)) {
            when (tab) {
                "Feed" -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Instagram AI", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
                        }
                    }
                    // Stories dos personagens de IA
                    item {
                        LazyRow(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)) {
                            items(characters) { char ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 6.dp)) {
                                    Box(modifier = Modifier.size(68.dp).clip(CircleShape).background(StoryGradient).padding(2.5.dp)) {
                                        NetImage(char.avatarUrl, modifier = Modifier.fillMaxSize().clip(CircleShape))
                                    }
Text(char.handle, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(68.dp))
}
}
}
Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(BorderColor))
}
// Posts do Feed
items(posts) { post ->
Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
NetImage(post.avatarUrl, modifier = Modifier.size(34.dp).clip(CircleShape))
Spacer(modifier = Modifier.width(10.dp))
Column {
Row(verticalAlignment = Alignment.CenterVertically) {
Text(post.author, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
Spacer(modifier = Modifier.width(4.dp))
Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
}
Text(post.location, color = Muted, fontSize = 11.sp)
}
}
NetImage(post.imageUrl, modifier = Modifier.fillMaxWidth().height(380.dp))
Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
IconButton(onClick = {
posts = posts.map { if (it.id == post.id) it.copy(isLiked = !it.isLiked, likes = if (it.isLiked) it.likes - 1 else it.likes + 1) else it }
if (!post.isLiked) followersCount += (5..20).random() // Ganha fãs ao interagir!
}) {
Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (post.isLiked) Color.Red else Color.White)
}
Icon(Icons.Default.Comment, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
}
Text("${post.likes} curtidas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp))
Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
Text(post.handle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
Spacer(modifier = Modifier.width(6.dp))
Text(post.caption, color = Color.White, fontSize = 13.sp)
}
}
}
}
"Explorar" -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
item {
Text("Descobrir Criadores de IA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
}
items(characters) { char ->
Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Panel, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
NetImage(char.avatarUrl, modifier = Modifier.size(50.dp).clip(CircleShape))
Spacer(modifier = Modifier.width(12.dp))
Column(modifier = Modifier.weight(1f)) {
Row(verticalAlignment = Alignment.CenterVertically) {
Text(char.name, color = Color.White, fontWeight = FontWeight.Bold)
Spacer(modifier = Modifier.width(4.dp))
Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(12.dp))
}
Text(char.handle, color = Muted, fontSize = 12.sp)
Text(char.bio, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
Button(
onClick = {
characters = characters.map { if (it.id == char.id) it.copy(statusText = "Seguindo") else it }
followingCount += 1
},
colors = ButtonDefaults.buttonColors(containerColor = if (char.statusText == "Seguir") AccentBlue else BorderColor),
shape = RoundedCornerShape(8.dp),
contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
) {
Text(char.statusText, color = Color.White, fontSize = 12.sp)
}
}
}
}
"Mensagens" -> Row(modifier = Modifier.fillMaxSize()) {
// Lista Lateral de Contatos de IA
LazyColumn(modifier = Modifier.width(90.dp).fillMaxHeight().background(Panel).border(0.5.dp, BorderColor)) {
items(characters) { char ->
val statusColor = when(char.relationshipLevel) {
3 -> Color.Red // Namorados
2 -> Color.Magenta // Paquera
else -> Color.Transparent
}
Column(
horizontalAlignment = Alignment.CenterHorizontally,
modifier = Modifier.fillMaxWidth().clickable { activeChatCharacterId = char.id }.padding(vertical = 12.dp)
) {
Box(modifier = Modifier.size(46.dp).clip(CircleShape).border(2.dp, if (activeChatCharacterId == char.id) AccentBlue else statusColor, CircleShape)) {
NetImage(char.avatarUrl, modifier = Modifier.fillMaxSize())
}
Text(char.name.split(" ")[0], color = Color.White, fontSize = 11.sp, maxLines = 1)
}
}
}
// Tela de Conversa Ativa
val activeChar = characters.first { it.id == activeChatCharacterId }
val currentMessages = chatMessagesMap[activeChatCharacterId] ?: emptyList()
Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
Row(modifier = Modifier.fillMaxWidth().padding(12.dp).border(0.5.dp, BorderColor).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
Text(activeChar.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
Spacer(modifier = Modifier.width(6.dp))
val relText = when(activeChar.relationshipLevel) {
1 -> "❤️ Amigo"
2 -> "💖 Crush"
3 -> "🌹 Namorando"
else -> "💬 Conhecido"
}
Text(relText, color = Muted, fontSize = 11.sp)
}
LazyColumn(modifier = Modifier.weight(1f).padding(8.dp), reverseLayout = false) {
items(currentMessages) { msg ->
Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if (msg.senderIsMe) Alignment.CenterEnd else Alignment.CenterStart) {
Column(horizontalAlignment = if (msg.senderIsMe) Alignment.End else Alignment.Start) {
if (msg.text.isNotBlank()) {
Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (msg.senderIsMe) AccentBlue else Panel).padding(10.dp)) {
Text(msg.text, color = Color.White, fontSize = 14.sp)
}
}
msg.attachedImageUrl?.let { url ->
Spacer(modifier = Modifier.height(4.dp))
NetImage(url, modifier = Modifier.size(160.dp).clip(RoundedCornerShape(12.dp)))
}
}
}
}
}
Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
TextField(
value = currentTypedMessage,
onValueChange = { currentTypedMessage = it },
modifier = Modifier.weight(1f),
colors = TextFieldDefaults.colors(focusedContainerColor = Panel, unfocusedContainerColor = Panel, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
placeholder = { Text("Mandar mensagem...", color = Muted, fontSize = 13.sp) },
shape = RoundedCornerShape(24.dp)
)
IconButton(onClick = {
if (currentTypedMessage.isNotBlank()) {
val userMsg = DirectMessage(senderIsMe = true, text = currentTypedMessage)
val updatedList = currentMessages + userMsg
chatMessagesMap = chatMessagesMap + (activeChatCharacterId to updatedList)
currentTypedMessage = ""
// Lógica da IA responder e avançar o relacionamento
scope.launch {
delay(1500)
val nextLevel = (activeChar.relationshipLevel + 1).coerceAtMost(3)
characters = characters.map { if (it.id == activeChar.id) it.copy(relationshipLevel = nextLevel) else it }
// IA envia uma resposta com foto anexa dependendo do nível de intimidade!
val reply = when (nextLevel) {
1 -> DirectMessage(senderIsMe = false, text = "Adorei conversar com você! Olha essa foto que tirei hoje cedo 🥰", attachedImageUrl = activeChar.avatarUrl)
2 -> DirectMessage(senderIsMe = false, text = "Você é muito fofo(a)... estou começando a gostar bastante de você 😳❤️", attachedImageUrl = "unsplash.com")
3 -> DirectMessage(senderIsMe = false, text = "Oficialmente namorando! Te amo muito! Olha meu look de agora de noite exclusivo pra você 🌹🔥", attachedImageUrl = "unsplash.com")
else -> DirectMessage(senderIsMe = false, text = "Haha que legal!")
}
chatMessagesMap = chatMessagesMap + (activeChatCharacterId to (updatedList + reply))
followersCount += (50..200).random() // Estar em um relacionamento dá fama!
}
}
}) {
Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AccentBlue)
}
}
}
}
"Perfil" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
Box(modifier = Modifier.size(76.dp).clip(CircleShape).border(1.5.dp, BorderColor, CircleShape)) {
Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(8.dp))
}
Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
Column(horizontalAlignment = Alignment.CenterHorizontally) {
Text("$followersCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
Text("Seguidores", color = Muted, fontSize = 12.sp)
}
Column(horizontalAlignment = Alignment.CenterHorizontally) {
Text("$followingCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
Text("Seguindo", color = Muted, fontSize = 12.sp)
}
}
}
Spacer(modifier = Modifier.height(12.dp))
Text(myName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
Text(myHandle, color = Muted, fontSize = 13.sp)
Text(myBio, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
Spacer(modifier = Modifier.height(16.dp))
Button(
onClick = { editOpen = true },
modifier = Modifier.fillMaxWidth(),
colors = ButtonDefaults.buttonColors(containerColor = Panel),
shape = RoundedCornerShape(8.dp)
) {
Text("Editar Perfil", color = Color.White)
}
}
}
if (editOpen) {
AlertDialog(
onDismissRequest = { editOpen = false },
title = { Text("Configurar Seu Personagem") },
text = {
Column {
TextField(value = myName, onValueChange = { myName = it }, label = { Text("Nome Visual") })
Spacer(modifier = Modifier.height(8.dp))
TextField(value = myBio, onValueChange = { myBio = it }, label = { Text("Biografia") })
}
},
confirmButton = {
Button(onClick = { editOpen = false }) { Text("Confirmar") }
}
)
}
}
}
}
