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
import java.util.UUID

private val Night = Color(0xFF000000)
private val Panel = Color(0xFF121212)
private val BorderColor = Color(0xFF262626)
private val AccentBlue = Color(0xFF0095F6)
private val Muted = Color(0xFFA8A8A8)
private val StoryGradient = Brush.linearGradient(listOf(Color(0xFFF9CE34), Color(0xFFEE2A7B), Color(0xFF6228D7)))

data class IaCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val handle: String,
    val bio: String,
    val avatarUrl: String,
    val country: String,
    val ethnicity: String,
    var relationshipLevel: Int = 0,
    var isFollowingMe: Boolean = false
)

data class CommentItem(val author: String, val text: String)

data class SocialPost(
    val id: String = UUID.randomUUID().toString(),
    val character: IaCharacter,
    val location: String,
    val caption: String,
    val imageUrl: String,
    var likes: Int,
    var isLiked: Boolean = false,
    val comments: MutableList<CommentItem> = mutableListOf()
)

data class DirectMessage(
    val id: String = System.nanoTime().toString(),
    val senderIsMe: Boolean,
    val text: String,
    val attachedImageUrl: String? = null
)

object AiWorldGenerator {
    private val firstNames = listOf("Yuki", "Kofi", "Aanya", "Mateo", "Zuri", "Chloe", "Hans", "Ji-Woo", "Svetlana", "Aisha", "Pedro", "Amara", "Mei", "Liam", "Elena", "Tariq")
    private val lastNames = listOf("Tanaka", "Mensah", "Sharma", "Rodriguez", "Okoro", "Smith", "Müller", "Kim", "Ivanova", "Diallo", "Silva", "Diop", "Chen", "Johnson", "García", "Khan")
    private val countries = listOf("Japão 🇯🇵", "Gana 🇬🇭", "Índia 🇮🇳", "México 🇲🇽", "Nigéria 🇳🇬", "EUA 🇺🇸", "Alemanha 🇩🇪", "Coreia do Sul 🇰🇷", "Rússia 🇷🇺", "Senegal 🇸🇳", "Brasil 🇧🇷", "França 🇫🇷", "China 🇨🇳")
    private val bios = listOf("Model & Creator ✨ Travel lover", "Living life one photo at a time 🌌", "Singer/Songwriter 🎸 Stream my single!", "Fitness & Health Coach 🧘‍♂️", "Digital art & tech 🦾", "Actor based in the city 🎬")
    private val avatarIds = listOf(1025, 1012, 1062, 1084, 153, 177, 237, 338, 342, 365, 433, 447, 524, 532, 538, 646)

    fun createRandomCharacter(): IaCharacter {
        val fName = firstNames.random()
        val lName = lastNames.random()
        val randomId = avatarIds.random() + (1..50).random()
        return IaCharacter(
            name = "$fName $lName",
            handle = "@${fName.lowercase()}.${lName.lowercase()}",
            bio = bios.random(),
            avatarUrl = "https://picsum.photos{randomId % 1000}/150/150",
            country = countries.random(),
            ethnicity = "Diversa"
        )
    }

    fun createPostForCharacter(char: IaCharacter): SocialPost {
        val imageId = (100..900).random()
        return SocialPost(
            character = char,
            location = char.country,
            caption = "Exploring the beauty of life! 🪐✨ #vibes",
            imageUrl = "https://picsum.photosimageId/500/500",
            likes = (100..50000).random(),
            comments = mutableListOf(CommentItem("@system.orbit", "Incrível! 🔥"))
        )
    }
}

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
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun OrbitSocialApp() {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("Feed") }
    
    var myName by remember { mutableStateOf("Seu Nome") }
    var myHandle by remember { mutableStateOf("@seu.perfil") }
    var myBio by remember { mutableStateOf("Criando conexões globais no Orbitocial 🌍🚀") }
    var editOpen by remember { mutableStateOf(false) }

    var followingList by remember { mutableStateOf(mutableListOf<IaCharacter>()) }
    var followersList by remember { mutableStateOf(mutableListOf<IaCharacter>()) }
    var viewListType by remember { mutableStateOf("") } 

    var infiniteDiscoverList by remember { mutableStateOf(List(15) { AiWorldGenerator.createRandomCharacter() }) }
    var posts by remember { mutableStateOf(infiniteDiscoverList.take(6).map { AiWorldGenerator.createPostForCharacter(it) }) }

    var commentModalPostId by remember { mutableStateOf<String?>(null) }
    var typedCommentText by remember { mutableStateOf("") }
    
    var activeChatCharacterId by remember { mutableStateOf<String?>(null) }
    var chatMessagesMap by remember { mutableStateOf(mapOf<String, List<DirectMessage>>()) }
    var currentTypedMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(12000)
            if ((1..100).random() > 40) {
                val newFan = AiWorldGenerator.createRandomCharacter().copy(isFollowingMe = true)
                followersList.add(newFan)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Night, modifier = Modifier.border(0.5.dp, BorderColor, RoundedCornerShape(0.dp))) {
                NavigationBarItem(selected = tab == "Feed", onClick = { tab = "Feed"; viewListType = "" }, icon = { Icon(Icons.Default.Home, null, tint = Color.White) }, label = { Text("Feed", color = Color.White, fontSize = 10.sp) })
                NavigationBarItem(selected = tab == "Explorar", onClick = { tab = "Explorar"; viewListType = "" }, icon = { Icon(Icons.Default.Search, null, tint = Color.White) }, label = { Text("Descobrir", color = Color.White, fontSize = 10.sp) })
                NavigationBarItem(selected = tab == "Mensagens", onClick = { tab = "Mensagens"; viewListType = "" }, icon = { Icon(Icons.Default.MailOutline, null, tint = Color.White) }, label = { Text("DMs", color = Color.White, fontSize = 10.sp) })
                NavigationBarItem(selected = tab == "Perfil", onClick = { tab = "Perfil"; viewListType = "" }, icon = { Icon(Icons.Default.Person, null, tint = Color.White) }, label = { Text("Perfil", color = Color.White, fontSize = 10.sp) })
            }
        },
        containerColor = Night
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Night)) {
            
            if (viewListType.isNotEmpty()) {
                val activeList = if (viewListType == "Seguidores") followersList else followingList
                Column(modifier = Modifier.fillMaxSize().background(Night).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewListType = "" }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                        Text(viewListType, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    LazyColumn {
                        items(activeList) { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                NetImage(item.avatarUrl, modifier = Modifier.size(44.dp).clip(CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(item.country, color = Muted, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        if (viewListType == "Seguindo") {
                                            followingList.remove(item)
                                        } else {
                                            followersList.remove(item)
                                        }
                                        viewListType = viewListType 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BorderColor)
                                ) {
Text("Remover", color = Color.White, fontSize = 12.sp)
}
}
}
}
}
} else {
when (tab) {
"Feed" -> LazyColumn(modifier = Modifier.fillMaxSize()) {
item {
Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
Text("Instagram Infinite", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
Icon(Icons.Default.FavoriteBorder, null, tint = Color.White)
}
}
item {
LazyRow(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)) {
items(followingList) { char ->
Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 6.dp)) {
Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(StoryGradient).padding(2.dp)) {
NetImage(char.avatarUrl, modifier = Modifier.fillMaxSize().clip(CircleShape))
}
Text(char.name.split(" ").firstOrNull() ?: "", color = Color.White, fontSize = 11.sp, maxLines = 1)
}
}
}
}
items(posts) { post ->
Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
NetImage(post.character.avatarUrl, modifier = Modifier.size(36.dp).clip(CircleShape))
Spacer(modifier = Modifier.width(10.dp))
Column {
Text(post.character.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
Text(post.location, color = Muted, fontSize = 11.sp)
}
}
NetImage(post.imageUrl, modifier = Modifier.fillMaxWidth().height(360.dp))
Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
IconButton(onClick = {
posts = posts.map { if (it.id == post.id) it.copy(isLiked = !it.isLiked, likes = if (it.isLiked) it.likes - 1 else it.likes + 1) else it }
}) {
Icon(if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (post.isLiked) Color.Red else Color.White)
}
IconButton(onClick = { commentModalPostId = post.id }) {
Icon(Icons.Default.Comment, null, tint = Color.White)
}
}
Text("${post.likes} curtidas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
Text(post.caption, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
if (post.comments.isNotEmpty()) {
Text("Ver todos os ${post.comments.size} comentários", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp).clickable { commentModalPostId = post.id })
}
}
}
}
"Explorar" -> LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
item {
Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
Text("Descobrir Pessoas Globais", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
Text("Mais 🔄", color = AccentBlue, modifier = Modifier.clickable {
infiniteDiscoverList = List(15) { AiWorldGenerator.createRandomCharacter() }
})
}
Spacer(modifier = Modifier.height(12.dp))
}
items(infiniteDiscoverList) { char ->
val isFollowing = followingList.any { it.id == char.id }
Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Panel, RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
NetImage(char.avatarUrl, modifier = Modifier.size(50.dp).clip(CircleShape))
Spacer(modifier = Modifier.width(12.dp))
Column(modifier = Modifier.weight(1f)) {
Text(char.name, color = Color.White, fontWeight = FontWeight.Bold)
Text("${char.handle} • ${char.country}", color = Muted, fontSize = 12.sp)
Text(char.bio, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
Button(
onClick = {
if (isFollowing) {
followingList.removeAll { it.id == char.id }
} else {
followingList.add(char)
if (posts.none { it.character.id == char.id }) {
posts = listOf(AiWorldGenerator.createPostForCharacter(char)) + posts
}
}
},
colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) BorderColor else AccentBlue),
shape = RoundedCornerShape(8.dp)
) {
Text(if (isFollowing) "Seguindo" else "Seguir", color = Color.White, fontSize = 12.sp)
}
}
}
}
"Mensagens" -> Row(modifier = Modifier.fillMaxSize()) {
LazyColumn(modifier = Modifier.width(90.dp).fillMaxHeight().background(Panel).border(0.5.dp, BorderColor)) {
items(followingList) { char ->
Column(
horizontalAlignment = Alignment.CenterHorizontally,
modifier = Modifier.fillMaxWidth().clickable { activeChatCharacterId = char.id }.padding(vertical = 12.dp)
) {
Box(modifier = Modifier.size(46.dp).clip(CircleShape).border(2.dp, if (activeChatCharacterId == char.id) AccentBlue else Color.Transparent, CircleShape)) {
NetImage(char.avatarUrl, modifier = Modifier.fillMaxSize())
}
Text(char.name.split(" ").firstOrNull() ?: "", color = Color.White, fontSize = 11.sp, maxLines = 1)
}
}
}
activeChatCharacterId?.let { targetId ->
val activeChar = followingList.firstOrNull { it.id == targetId }
val currentMessages = chatMessagesMap[targetId] ?: listOf(DirectMessage(senderIsMe = false, text = "Opa! Tudo bem? Vi seu perfil."))
activeChar?.let { char ->
Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
Text(char.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
items(currentMessages) { msg ->
Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if (msg.senderIsMe) Alignment.CenterEnd else Alignment.CenterStart) {
Column {
Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (msg.senderIsMe) AccentBlue else Panel).padding(10.dp)) {
Text(msg.text, color = Color.White, fontSize = 14.sp)
}
msg.attachedImageUrl?.let { url ->
NetImage(url, modifier = Modifier.size(140.dp).padding(top = 4.dp).clip(RoundedCornerShape(8.dp)))
}
}
}
}
}
Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
TextField(value = currentTypedMessage, onValueChange = { currentTypedMessage = it }, modifier = Modifier.weight(1f), placeholder = { Text("Mensagem...", color = Muted) })
IconButton(onClick = {
if (currentTypedMessage.isNotBlank()) {
val combined = currentMessages + DirectMessage(senderIsMe = true, text = currentTypedMessage)
chatMessagesMap = chatMessagesMap + (targetId to combined)
currentTypedMessage = ""
scope.launch {
delay(2000)
val finalLevel = (char.relationshipLevel + 1).coerceAtMost(3)
char.relationshipLevel = finalLevel
val reply = when(finalLevel) {
2 -> DirectMessage(senderIsMe = false, text = "Nossa, adorei conversar contigo! Olha essa foto!", attachedImageUrl = char.avatarUrl)
3 -> DirectMessage(senderIsMe = false, text = "Ficando sério! ❤️ Olha meu look!", attachedImageUrl = "picsum.photos")
else -> DirectMessage(senderIsMe = false, text = "Que massa! Curti!")
}
chatMessagesMap = chatMessagesMap + (targetId to (combined + reply))
}
}
}) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = AccentBlue) }
}
}
}
} ?: Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
Text("Selecione alguém que você segue para conversar", color = Muted, fontSize = 13.sp)
}
}
"Perfil" -> Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
Icon(Icons.Default.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(70.dp))
Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewListType = "Seguidores" }) {
Text("${followersList.size}", color = Color.White, fontWeight = FontWeight.Bold)
Text("Seguidores", color = Muted, fontSize = 12.sp)
}
Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewListType = "Seguindo" }) {
Text("${followingList.size}", color = Color.White, fontWeight = FontWeight.Bold)
Text("Seguindo", color = Muted, fontSize = 12.sp)
}
}
}
Spacer(modifier = Modifier.height(8.dp))
Text(myName, color = Color.White, fontWeight = FontWeight.Bold)
Text(myHandle, color = Muted, fontSize = 13.sp)
Text(myBio, color = Color.White, fontSize = 13.sp)
Spacer(modifier = Modifier.height(12.dp))
Button(onClick = { editOpen = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel)) {
Text("Editar Perfil", color = Color.White)
}
}
}
}
commentModalPostId?.let { postId ->
val activePost = posts.firstOrNull { it.id == postId }
activePost?.let { post ->
AlertDialog(
onDismissRequest = { commentModalPostId = null },
title = { Text("Comentários") },
text = {
Column {
LazyColumn(modifier = Modifier.height(180.dp)) {
items(post.comments) { c ->
Text("${c.author}: ${c.text}", color = Color.Black, modifier = Modifier.padding(vertical = 4.dp))
}
}
Row {
TextField(value = typedCommentText, onValueChange = { typedCommentText = it }, placeholder = { Text("Escreva...") }, modifier = Modifier.weight(1f))
Button(onClick = {
if (typedCommentText.isNotBlank()) {
post.comments.add(CommentItem(myHandle, typedCommentText))
typedCommentText = ""
}
}) { Text("Postar") }
}
}
},
confirmButton = { TextButton(onClick = { commentModalPostId = null }) { Text("Fechar") } }
)
}
}
if (editOpen) {
AlertDialog(
onDismissRequest = { editOpen = false },
title = { Text("Editar Perfil") },
text = {
Column {
TextField(value = myName, onValueChange = { myName = it }, label = { Text("Nome") })
Spacer(modifier = Modifier.height(6.dp))
TextField(value = myBio, onValueChange = { myBio = it }, label = { Text("Bio") })
}
},
confirmButton = { Button(onClick = { editOpen = false }) { Text("Salvar") } }
)
}
}
}
}
