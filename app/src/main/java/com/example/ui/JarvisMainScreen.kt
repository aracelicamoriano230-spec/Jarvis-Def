package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import com.example.data.*
import kotlinx.coroutines.launch

// Color Tokens (Artistic Flair Theme Mapping)
val IronRedDeep = Color(0xFF8B0000) // Deep crimson metallic red
val IronRedMedium = Color(0xFFB22222) // Firebrick scarlet red
val IronRedLight = Color(0xFFFF2400) // Neon Red energy glow
val StarkGoldDark = Color(0xFFC5A059) // Metallic gold bronze
val StarkGoldBright = Color(0xFFD4AF37) // Solid artistic gold
val HologramCyan = Color(0xFF00F2FF) // Intense cyan power cell core glow
val CarbonBlack = Color(0xFF050505) // Near pitch black obsidian background
val GlassGrey = Color(0x1B8B0000) // Translucent deep red glass (rgba(139, 0, 0, 0.1))

// Gradients and Borders for Artistic Flair
val GlassBorderColor = Color(0x4DD4AF37) // rgba(212, 175, 55, 0.3)
val MetallicRedBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF8B0000), Color(0xFFFF2400), Color(0xFF8B0000))
)
val MetallicGoldBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFC5A059), Color(0xFFFFD700), Color(0xFFC5A059))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JarvisMainScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val callMeSirOnly by viewModel.callMeSirOnly.collectAsStateWithLifecycle()
    
    // Config values
    val geminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val elevenKey by viewModel.elevenLabsApiKey.collectAsStateWithLifecycle()
    val openaiKey by viewModel.openAiApiKey.collectAsStateWithLifecycle()
    val anthropicKey by viewModel.anthropicApiKey.collectAsStateWithLifecycle()
    val groqKey by viewModel.groqApiKey.collectAsStateWithLifecycle()
    val modelName by viewModel.selectedModelName.collectAsStateWithLifecycle()

    var showConfigDialog by remember { mutableStateOf(false) }

    // Navigation state in screens: 0 = HUD, 1 = Chat, 2 = Projects, 3 = Encrypted Vault
    var currentTab by remember { mutableIntStateOf(0) }

    if (!isRegistered) {
        // Core diagnostic registration page with full frame mapping
        Box(modifier = Modifier.fillMaxSize()) {
            RegistrationScreen(
                onRegister = { name, sirOnly ->
                    viewModel.registerUser(name, sirOnly)
                }
            )
            VisualArmorFrame()
        }
    } else {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(CarbonBlack),
            containerColor = CarbonBlack,
            topBar = {
                JarvisTopBar(
                    viewModel = viewModel,
                    onOpenConfig = { showConfigDialog = true }
                )
            },
            bottomBar = {
                JarvisBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background Nano-Grid Dots (Artistic Flair matrix)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotRadius = 1.dp.toPx()
                    val spacing = 20.dp.toPx()
                    if (spacing > 1f) {
                        val cols = (size.width / spacing).toInt().coerceIn(0, 150)
                        val rows = (size.height / spacing).toInt().coerceIn(0, 150)
                        for (c in 0..cols) {
                            for (r in 0..rows) {
                                drawCircle(
                                    color = StarkGoldBright.copy(alpha = 0.12f),
                                    radius = dotRadius,
                                    center = Offset(c * spacing, r * spacing)
                                )
                            }
                        }
                    }
                }

                // Background nanotech matrix glow decoration
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    IronRedDeep.copy(alpha = 0.08f),
                                    Color.Transparent,
                                    CarbonBlack
                                )
                            )
                        )
                )

                // Screens
                when (currentTab) {
                    0 -> HudDashboardScreen(viewModel = viewModel)
                    1 -> CognitionAndTerminalScreen(viewModel = viewModel)
                    2 -> ChatAiScreen(viewModel = viewModel)
                    3 -> ProjectsScreen(viewModel = viewModel)
                    4 -> EncryptedVaultScreen(viewModel = viewModel)
                }

                // Top visual floating overlays
                VisualArmorFrame()

                // Configuration popup settings
                if (showConfigDialog) {
                    ConfigDialog(
                        currentGemini = geminiKey,
                        currentOpenAi = openaiKey,
                        currentClaude = anthropicKey,
                        currentGroq = groqKey,
                        currentEleven = elevenKey,
                        currentModel = modelName,
                        onDismiss = { showConfigDialog = false },
                        onSave = { g, o, c, gr, e, m ->
                            viewModel.configureKeys(g, o, c, gr, e)
                            viewModel.selectModel(m)
                            showConfigDialog = false
                        },
                        onTestVoice = {
                            viewModel.setAndPlayVoiceTest()
                        },
                        onClearChatLogs = {
                            viewModel.clearChatHistory()
                            showConfigDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    onRegister: (String, Boolean) -> Unit
) {
    var textVal by remember { mutableStateOf("") }
    var sirOnlyChecked by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Hologram visual shapes
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, GlassBorderColor, RoundedCornerShape(16.dp))
                .background(GlassGrey)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sci-fi micro chip avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(1.5.dp, GlassBorderColor, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ArcReactorCanvas(
                        modifier = Modifier.fillMaxSize(),
                        isSpeaking = false
                    )
                }

                Text(
                    text = "J.A.R.V.I.S. V8",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = MetallicGoldBrush,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "SISTEMAS EXCLUSIVOS EN LÍNEA\nPor favor, firme el protocolo de calibración biométrica.",
                    color = HologramCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    label = { Text("Nombre del Creador", color = StarkGoldDark) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = StarkGoldBright,
                        unfocusedBorderColor = StarkGoldDark,
                        cursorColor = StarkGoldBright
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sirOnlyChecked = !sirOnlyChecked }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sirOnlyChecked,
                        onCheckedChange = { sirOnlyChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = StarkGoldBright,
                            uncheckedColor = StarkGoldDark,
                            checkmarkColor = CarbonBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exigir trato preferencial de 'Señor'",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onRegister(textVal.ifEmpty { "Stark" }, sirOnlyChecked)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IronRedMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.5.dp, StarkGoldBright, RoundedCornerShape(25.dp))
                        .testTag("login_button")
                ) {
                    Text(
                        text = "SISTEMAS DE INICIACIÓN",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun JarvisTopBar(
    viewModel: JarvisViewModel,
    onOpenConfig: () -> Unit
) {
    val hmsTime by viewModel.currentHMS.collectAsStateWithLifecycle()
    val dateString by viewModel.currentDateString.collectAsStateWithLifecycle()
    val yearString by viewModel.currentTimeYear.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(CarbonBlack)
            .drawBehind {
                val thickness = 1.dp.toPx()
                drawLine(
                    color = GlassBorderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = thickness
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Designation and name
        Column {
            Text(
                text = "SYSTEM DESIGNATION",
                color = StarkGoldDark.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "JARVIS MARK-85",
                style = androidx.compose.ui.text.TextStyle(
                    brush = MetallicGoldBrush,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        // Right Column: Time, Date and settings trigger
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) StarkGoldBright else HologramCyan)
                    )
                    Text(
                        text = hmsTime.ifEmpty { "00:00:00" },
                        color = HologramCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = "$dateString, $yearString".uppercase(),
                    color = Color.Gray.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = onOpenConfig,
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, GlassBorderColor, CircleShape)
                    .testTag("app_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = StarkGoldBright,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun JarvisBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // Elegant custom bottom navigation respect safe insets
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(CarbonBlack)
            .drawBehind {
                val thickness = 1.dp.toPx()
                drawLine(
                    color = GlassBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = thickness
                )
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = Icons.Default.Home,
            label = "REACTOR HUD",
            isSelected = currentTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("nav_hud")
        )
        NavItem(
            icon = Icons.Default.Code,
            label = "CMD NÚCLEO",
            isSelected = currentTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("nav_core")
        )
        NavItem(
            icon = Icons.AutoMirrored.Filled.Chat,
            label = "JARVIS IA",
            isSelected = currentTab == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.testTag("nav_chat")
        )
        NavItem(
            icon = Icons.Default.Folder,
            label = "PROYECTOS",
            isSelected = currentTab == 3,
            onClick = { onTabSelected(3) },
            modifier = Modifier.testTag("nav_projects")
        )
        NavItem(
            icon = Icons.Default.Lock,
            label = "ENCRYPTED",
            isSelected = currentTab == 4,
            onClick = { onTabSelected(4) },
            modifier = Modifier.testTag("nav_vault")
        )
    }
}

@Composable
fun RowScope.NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier
                            .background(MetallicRedBrush)
                            .border(1.5.dp, StarkGoldBright, CircleShape)
                    } else {
                        Modifier
                            .background(GlassGrey)
                            .border(1.dp, GlassBorderColor, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else StarkGoldDark.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isSelected) StarkGoldBright else Color.Gray,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// SCREEN 1: REACTOR HUD DASHBOARD (ARC REACTOR AND SYSTEM FEED)
// -------------------------------------------------------------
@Composable
fun HudDashboardScreen(
    viewModel: JarvisViewModel
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val callMeSirOnly by viewModel.callMeSirOnly.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceStatus.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    
    // DB values
    val linkedProfiles by viewModel.linkedProfiles.collectAsStateWithLifecycle()
    val currentSub by viewModel.currentSubtitle.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // A. Header greeting card with dynamic treatment
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassGrey.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(IronRedMedium, StarkGoldDark)))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenido, " + (if (callMeSirOnly) "Señor" else userName),
                            color = StarkGoldBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Todos los servomecanismos están estables a temperatura ambiente con una calibración óptima. Armadura Mark 85 en reposo táctico.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.speakJarvisGreeting() },
                        modifier = Modifier.border(1.dp, StarkGoldBright, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Diagnóstico",
                            tint = StarkGoldBright
                        )
                    }
                }
            }
        }

        // B. Photorealistic custom animated Arc Reactor
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring decoration
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.dp, HologramCyan.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .border(1.dp, StarkGoldDark.copy(alpha = 0.3f), CircleShape)
                )

                // Master canvas code
                ArcReactorCanvas(
                    modifier = Modifier.size(170.dp),
                    isSpeaking = isSpeaking
                )

                // Subtitle overlays when active
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentSub,
                        color = HologramCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // C. Micro triggers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.triggerVoiceMicInputSimulated() },
                    colors = ButtonDefaults.buttonColors(containerColor = IronRedMedium),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, StarkGoldBright, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hey Jarvis", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                
                Button(
                    onClick = { viewModel.triggerVoiceMicInputSimulated("Activa calibración total y analiza sobrecarga de batería") },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassGrey),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, HologramCyan, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = HologramCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DIAGNÓSTICO", color = HologramCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // D. Smartphone integrated feeds (connected to device notifier block)
        item {
            Text(
                text = "SENSORES DE COMUNICACIÓN RADAR STARK",
                color = StarkGoldBright,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(linkedProfiles) { profile ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleProfileConnection(profile) },
                colors = CardDefaults.cardColors(containerColor = GlassGrey.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, if (profile.isConnected) IronRedMedium.copy(alpha = 0.8f) else Color.DarkGray)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val badgeIcon = when (profile.platform) {
                            "WhatsApp" -> Icons.Default.ChatBubble
                            "Email" -> Icons.Default.Email
                            else -> Icons.Default.Share
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (profile.isConnected) IronRedDeep else Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                tint = if (profile.isConnected) StarkGoldBright else Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = profile.platform,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (profile.isConnected) profile.lastNotification ?: "Recibiendo satélite estable..." else "Desconectado",
                                color = if (profile.isConnected) HologramCyan else Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (profile.isConnected) "ACTIVO" else "SINCRONIZAR",
                            color = if (profile.isConnected) StarkGoldBright else Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        if (profile.isConnected && profile.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .background(StarkGoldBright, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${profile.unreadCount} alerts",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // E. Static list of quick shortcuts/Terminal Help info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(0.5.dp, StarkGoldDark.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "[TERMINAL CON COGNICIÓN DE ATAJOS STARK]",
                        color = StarkGoldBright,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Diga 'enemy' en chat para desencriptar la base de datos de seguridad clasificada.\n" +
                               "• Cambie la pestaña en Jarvis IA para alternar promts especiales de programación avanzada o cuántica.\n" +
                               "• Sincronice Whatsapp / Correo tocando las tarjetas del radar para oír alertas acústicas.\n" +
                               "• Para iniciar Python en terminal de casa, descargue y ejecute: python.exe",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2: MULTIMODE IA SUITE (CHAT INTELLIGENT EXPERT AND Personas)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAiScreen(
    viewModel: JarvisViewModel
) {
    val currentCategory by viewModel.currentCategory.collectAsStateWithLifecycle()
    val modelName by viewModel.selectedModelName.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isResponseLoading by viewModel.aiResponseLoading.collectAsStateWithLifecycle()
    val geminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val elevenKey by viewModel.elevenLabsApiKey.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var selectedBlueprintIndex by remember { mutableIntStateOf(-1) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    var isKeyConfigExpanded by remember { mutableStateOf(geminiKey.isEmpty() || geminiKey.contains("PLACEHOLDER") || geminiKey == "MY_GEMINI_API_KEY") }
    var keyInputValue by remember(geminiKey) { mutableStateOf(geminiKey) }

    // Scroll chat to end when changes occur
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // A. Hologram horizontal categories
        val topics = listOf("Variado", "Trabajo", "Programación", "Quantica", "Estudio")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            topics.forEach { topic ->
                val isActive = currentCategory == topic
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) IronRedDeep else GlassGrey)
                        .border(
                            width = 1.dp,
                            color = if (isActive) StarkGoldBright else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setCategory(topic) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (topic == "Quantica") "CUÁNTICA" else topic.uppercase(),
                        color = if (isActive) StarkGoldBright else Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // B. Active brain & Voice canvas row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Mente: $modelName",
                    color = HologramCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Rol activo: " + when (currentCategory) {
                        "Trabajo" -> "Stark Business Analyst"
                        "Programación" -> "Nanotech Compiler"
                        "Quantica" -> "Theoretical Physicist"
                        "Estudio" -> "General Academy Tutor"
                        else -> "Carismatic Jarvis Mark 85"
                    },
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Small glowing neural avatar pulsing next to category title
            NeuralFaceCanvas(
                modifier = Modifier
                    .size(45.dp)
                    .border(0.5.dp, StarkGoldDark, CircleShape),
                isSpeaking = isSpeaking
            )
        }

        // Collapsible API Key Config Bounded Board (Stark HUD Design)
        AnimatedVisibility(
            visible = isKeyConfigExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") StarkGoldDark.copy(alpha = 0.5f) else IronRedLight.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") Color.Green else IronRedLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CALIBRACIÓN COGNITIVA GEMINI 3.5",
                                color = StarkGoldBright,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { isKeyConfigExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = "Colapsar",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Señor, para acceder a la inteligencia cuántica viva de Gemini 3.5 en Google AI Studio, guarde su API Key privada aquí. Se mantendrá protegida de forma local en los sistemas Stark del chasis.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keyInputValue,
                            onValueChange = { keyInputValue = it },
                            placeholder = {
                                Text(
                                    "AIzaSy...",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = StarkGoldBright,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gemini_direct_key_input")
                        )

                        Button(
                            onClick = {
                                viewModel.configureKeys(keyInputValue, elevenKey)
                                viewModel.selectModel("Gemini 3.5 Flash")
                                viewModel.voiceManager.speak("Canal satelital con Google AI Studio calibrado para Gemini 3.5 de forma exitosa, señor.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                "GUARDAR",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sleek badge showing key status if collapsed
        if (!isKeyConfigExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { isKeyConfigExpanded = true }
                    .border(0.5.dp, if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") StarkGoldDark.copy(alpha = 0.3f) else IronRedMedium.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = GlassGrey.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") Color.Green else IronRedLight)
                        )
                        Text(
                            text = if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") "NÚCLEO GEMINI 3.5 CONECTADO" else "CONFIGURAR ACCESO A GEMINI 3.5 (AI STUDIO)",
                            color = if (geminiKey.isNotEmpty() && !geminiKey.contains("PLACEHOLDER") && geminiKey != "MY_GEMINI_API_KEY") Color.LightGray else StarkGoldBright,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "AJUSTAR ACCESO ⚙️",
                        color = HologramCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // C. Chat Scroll window
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(0.5.dp, IronRedMedium.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            if (messages.isEmpty() || messages.none { it.category == currentCategory }) {
                // Empty instruction placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = StarkGoldDark,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Bitácora en blanco, creador.\nEnvíe un comando para iniciar el canal cognitivo.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter messages matching active category
                    val activeMessages = messages.filter { it.category == currentCategory }
                    items(activeMessages) { message ->
                        val isJarvis = message.sender == "jarvis"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isJarvis) Arrangement.Start else Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isJarvis) 0.dp else 12.dp,
                                            bottomEnd = if (isJarvis) 12.dp else 0.dp
                                        )
                                    )
                                    .background(if (isJarvis) GlassGrey else IronRedDeep)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isJarvis) StarkGoldDark else HologramCyan,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isJarvis) 0.dp else 12.dp,
                                            bottomEnd = if (isJarvis) 12.dp else 0.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isJarvis) "JARVIS V8" else "CREADOR",
                                            color = if (isJarvis) StarkGoldBright else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (message.fileAttachmentPath != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Attachment,
                                                    contentDescription = null,
                                                    tint = HologramCyan,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    "Plan_Stark.png",
                                                    color = HologramCyan,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = message.message,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.testTag("chat_bubble_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isResponseLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = StarkGoldBright,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Pensando...",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Small indicator about attachments
        if (selectedBlueprintIndex >= 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IronRedMedium.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = StarkGoldBright)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Documento adjunto: " + when (selectedBlueprintIndex) {
                            0 -> "Planos_Reactor_Arc_Mark85.png"
                            1 -> "Nanotecnologia_OroColoidal.file"
                            else -> "Informatica_Cuantica_Simulador.py"
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = { selectedBlueprintIndex = -1 },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray)
                }
            }
        }

        // D. Typing bar and attachment triggers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { showAttachmentDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .background(GlassGrey, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Adjuntar plano",
                    tint = StarkGoldBright
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Escriba su comando...", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = StarkGoldBright,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = StarkGoldBright
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("chat_input_text_field"),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (textInput.isNotEmpty()) {
                                val dummyBase64 = if (selectedBlueprintIndex >= 0) "DUMMY_IMAGE_BASE64_BLUEPRINT" else null
                                val dummyMime = if (selectedBlueprintIndex >= 0) "image/png" else null
                                viewModel.sendChatMessage(textInput, dummyBase64, dummyMime)
                                textInput = ""
                                selectedBlueprintIndex = -1
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = HologramCyan
                        )
                    }
                }
            )
        }
    }

    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            containerColor = GlassGrey,
            title = {
                Text(
                    text = "SELECCIONAR PLAN CARGADO STARK",
                    color = StarkGoldBright,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Seleccione un archivo de nanotecnología de alta prioridad militar para analizar con Jarvis.",
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            selectedBlueprintIndex = 0
                            showAttachmentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Planos_Reactor_Arc_Mark85.png", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            selectedBlueprintIndex = 1
                            showAttachmentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nanotecnologia_OroColoidal.file", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            selectedBlueprintIndex = 2
                            showAttachmentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Informatica_Cuantica_Simulador.py", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("CANCELAR", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// SCREEN 3: PROYECTOS CREATIVOS (SAVED GOALS ROOM LIST)
// -------------------------------------------------------------
@Composable
fun ProjectsScreen(
    viewModel: JarvisViewModel
) {
    val goals by viewModel.projectGoals.collectAsStateWithLifecycle()
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var flagCategory by remember { mutableStateOf("Proyecto") } // "Meta", "Proyecto", "Creatividad"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Form to enter project
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassGrey),
                border = BorderStroke(1.dp, StarkGoldDark.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTRAR NUEVA META O IDEA CREATIVA",
                        color = StarkGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_title_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Descripción", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Category choose
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val cats = listOf("Proyecto", "Meta", "Creatividad")
                        cats.forEach { cat ->
                            Button(
                                onClick = { flagCategory = cat },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (flagCategory == cat) IronRedDeep else Color.DarkGray
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(cat, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (newTitle.isNotEmpty()) {
                                viewModel.insertPersonalGoal(newTitle, newDesc, flagCategory)
                                newTitle = ""
                                newDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("REGISTRAR META", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "MISIÓN Y REGISTRO DE ARCHIVOS HOLOGRÁFICOS",
                color = StarkGoldBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Saved Room Goal Card Renderings
        items(goals) { goal ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassGrey.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, IronRedDeep.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(StarkGoldDark, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = goal.category.uppercase(),
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = goal.title,
                                color = StarkGoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.description,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                    IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 4: ENCRYPTED VAULT SCREEN (PASSWORD PROTECTED SECTOR)
// -------------------------------------------------------------
@Composable
fun EncryptedVaultScreen(
    viewModel: JarvisViewModel
) {
    val isUnlocked by viewModel.isClassifiedUnlocked.collectAsStateWithLifecycle()
    val statusText by viewModel.classifiedTerminalMessage.collectAsStateWithLifecycle()
    val memories by viewModel.classifiedMemories.collectAsStateWithLifecycle()

    var passwordInput by remember { mutableStateOf("") }
    var secretTitle by remember { mutableStateOf("") }
    var secretContent by remember { mutableStateOf("") }
    var secretCategory by remember { mutableStateOf("Trabajo") } // "Trabajo", "Estudio", "Personal"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isUnlocked) {
            // Screen passcode lock
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassGrey),
                border = BorderStroke(1.5.dp, IronRedMedium)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Cifrado",
                        tint = IronRedLight,
                        modifier = Modifier.size(54.dp)
                    )

                    Text(
                        text = "MODULO DE SEGURIDAD Stark Industries",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = statusText,
                        color = if (statusText.contains("DENEGADO")) IronRedLight else HologramCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Clave Privada", color = Color.Gray) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = IronRedLight,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_password_input")
                    )

                    Button(
                        onClick = {
                            viewModel.attemptClassifiedUnlock(passwordInput)
                            passwordInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedMedium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_unlock_button")
                    ) {
                        Text(
                            text = "BURLAR ENCRIPTADO",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "(Clave predeterminada del cómic: enemy)",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            // Unlocked classified secret records view
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NÚCLEO CLASIFICADO INTERMEDIO",
                    color = IronRedLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = { viewModel.lockClassified() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("CERRAR NÚCLEO", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Quick secret form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassGrey),
                border = BorderStroke(1.dp, IronRedLight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "INYECTAR NOTA ENCRIPTADA NUEVA",
                        color = StarkGoldBright,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = secretTitle,
                        onValueChange = { secretTitle = it },
                        label = { Text("Título Secreto", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = IronRedLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = secretContent,
                        onValueChange = { secretContent = it },
                        label = { Text("Contenido Clasificado", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = IronRedLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val secs = listOf("Trabajo", "Estudio", "Personal")
                        secs.forEach { s ->
                            Button(
                                onClick = { secretCategory = s },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (secretCategory == s) IronRedLight else Color.DarkGray
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(s, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (secretTitle.isNotEmpty()) {
                                viewModel.insertClassifiedMemory(secretTitle, secretContent, secretCategory)
                                secretTitle = ""
                                secretContent = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GUARDAR NOTA SECRETA", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Scrollable list of secrets
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(memories) { memo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, IronRedDeep)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(IronRedLight, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = memo.section.uppercase(),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = memo.title,
                                        color = StarkGoldBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = memo.content,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                            IconButton(onClick = { viewModel.deleteClassifiedMemory(memo.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER DIALOG: CONFIGURATION KEYS & MODELLING
// -------------------------------------------------------------
@Composable
fun ConfigDialog(
    currentGemini: String,
    currentOpenAi: String,
    currentClaude: String,
    currentGroq: String,
    currentEleven: String,
    currentModel: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit,
    onTestVoice: () -> Unit,
    onClearChatLogs: () -> Unit
) {
    var geminiInput by remember { mutableStateOf(currentGemini) }
    var openaiInput by remember { mutableStateOf(currentOpenAi) }
    var claudeInput by remember { mutableStateOf(currentClaude) }
    var groqInput by remember { mutableStateOf(currentGroq) }
    var elevenInput by remember { mutableStateOf(currentEleven) }
    var modelSelect by remember { mutableStateOf(currentModel) }

    val models = listOf("Gemini 3.5 Flash", "Gemini 3.1 Pro (Preview)", "Claude 3.5 Sonnet (Sim)", "GPT-4o (Sim)")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassGrey,
        title = {
            Text(
                text = "CALIBRACIÓN COGNITIVA STARK INDUSTRIES",
                color = StarkGoldBright,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Ingrese sus credenciales privadas para habilitar la sincronización asincrónica satelital de Jarvis de forma independiente en su armadura.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    OutlinedTextField(
                        value = geminiInput,
                        onValueChange = { geminiInput = it },
                        label = { Text("Google AI Studio (Gemini) Key", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                item {
                    OutlinedTextField(
                        value = openaiInput,
                        onValueChange = { openaiInput = it },
                        label = { Text("OpenAI API Key (GPT-4o)", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                item {
                    OutlinedTextField(
                        value = claudeInput,
                        onValueChange = { claudeInput = it },
                        label = { Text("Anthropic API Key (Claude)", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                item {
                    OutlinedTextField(
                        value = groqInput,
                        onValueChange = { groqInput = it },
                        label = { Text("Groq API Key (Llama 3)", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                item {
                    OutlinedTextField(
                        value = elevenInput,
                        onValueChange = { elevenInput = it },
                        label = { Text("ElevenLabs API Key (Voz)", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = StarkGoldBright,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                item {
                    Text(
                        text = "Seleccionar motor cerebral activo:",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    // Small local spinner radio buttons
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        models.forEach { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { modelSelect = model }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = modelSelect == model,
                                    onClick = { modelSelect = model },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = StarkGoldBright,
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = model,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PROBAR VOZ JARVIS BRYAN", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                item {
                    Button(
                        onClick = onClearChatLogs,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, IronRedMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BORRAR BITÁCORA CONTENIDOS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = IronRedLight)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(geminiInput, openaiInput, claudeInput, groqInput, elevenInput, modelSelect) },
                colors = ButtonDefaults.buttonColors(containerColor = IronRedMedium)
            ) {
                Text("GUARDAR", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun VisualArmorFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val redCorner = Color(0xFFB22222)
        val goldCorner = Color(0xFFD4AF37)
        
        // Top-Left (Red)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(20.dp)
                .drawBehind {
                    val thickness = 3.dp.toPx()
                    // top
                    drawLine(
                        color = redCorner,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = thickness
                    )
                    // left
                    drawLine(
                        color = redCorner,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = thickness
                    )
                }
        )
        // Top-Right (Red)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(20.dp)
                .drawBehind {
                    val thickness = 3.dp.toPx()
                    // top
                    drawLine(
                        color = redCorner,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = thickness
                    )
                    // right
                    drawLine(
                        color = redCorner,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = thickness
                    )
                }
        )
        // Bottom-Left (Gold)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .size(20.dp)
                .drawBehind {
                    val thickness = 3.dp.toPx()
                    // bottom
                    drawLine(
                        color = goldCorner,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = thickness
                    )
                    // left
                    drawLine(
                        color = goldCorner,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = thickness
                    )
                }
        )
        // Bottom-Right (Gold)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(20.dp)
                .drawBehind {
                    val thickness = 3.dp.toPx()
                    // bottom
                    drawLine(
                        color = goldCorner,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = thickness
                    )
                    // right
                    drawLine(
                        color = goldCorner,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = thickness
                    )
                }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CognitionAndTerminalScreen(
    viewModel: JarvisViewModel
) {
    val terminalBuffer by viewModel.terminalBuffer.collectAsStateWithLifecycle()
    val shellMode by viewModel.terminalShellMode.collectAsStateWithLifecycle()
    val intelligenceList by viewModel.intelligenceFunctions.collectAsStateWithLifecycle()
    val memoryList by viewModel.memoryFunctions.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Terminal, 1 = Inteligencia, 2 = Memoria
    var terminalInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to the bottom of the terminal when the buffer updates
    LaunchedEffect(terminalBuffer.size) {
        if (terminalBuffer.isNotEmpty()) {
            listState.animateScrollToItem(terminalBuffer.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Futuristic Tab Header Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassGrey.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, StarkGoldDark.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Triple(0, "TERMINAL", "Termux CMD"),
                    Triple(1, "INTELIGENCIA (50)", "Nodos Cognitivos"),
                    Triple(2, "MEMORIA (50)", "Bóvedas Quantum")
                )

                tabs.forEach { (index, label, desc) ->
                    val isTabSelected = activeSubTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isTabSelected) IronRedDeep else Color.Transparent
                            )
                            .clickable { activeSubTab = index }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isTabSelected) Color.White else StarkGoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = desc,
                            color = if (isTabSelected) StarkGoldBright.copy(alpha = 0.8f) else Color.Gray,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Sub tab contents
        when (activeSubTab) {
            0 -> {
                // TERMINAL SHELL VIEW
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTADO SHELL: " + if (shellMode == "NORMAL") "STARK-OS MAIN" else "SIMULADOR $shellMode",
                            color = if (shellMode == "NORMAL") HologramCyan else StarkGoldBright,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (shellMode == "NORMAL") HologramCyan else StarkGoldBright)
                        )
                    }

                    // Console Board
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.5.dp, GlassBorderColor)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(terminalBuffer) { line ->
                                Text(
                                    text = line,
                                    color = when {
                                        line.startsWith("jarvis@stark-armor-m85") -> HologramCyan
                                        line.startsWith(">>>") -> StarkGoldBright
                                        line.startsWith("jshell>") -> StarkGoldBright
                                        line.startsWith("ERROR") || line.contains("SyntaxError") || line.contains("compilation failed") -> IronRedLight
                                        line.startsWith("==") || line.startsWith("  [+]") || line.startsWith("  [!]") -> Color.Green
                                        else -> Color.Green.copy(alpha = 0.85f)
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Quick Command Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val quickCmds = if (shellMode == "NORMAL") {
                            listOf("help", "ls", "neofetch", "python", "java", "hack", "intel", "memo", "status", "whoami")
                        } else if (shellMode == "PYTHON") {
                            listOf("print('Hola Stark')", "2 + 2", "for x in range(3): print(x)", "exit")
                        } else {
                            listOf("System.out.println('Reactor Online');", "5 * 10 - 2", "exit")
                        }

                        quickCmds.forEach { cmd ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GlassGrey),
                                border = BorderStroke(0.5.dp, HologramCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    if (cmd == "exit") {
                                        viewModel.executeTerminalCommand("exit")
                                    } else if (shellMode != "NORMAL") {
                                        terminalInput = cmd
                                    } else {
                                        viewModel.executeTerminalCommand(cmd)
                                    }
                                }
                            ) {
                                Text(
                                    text = cmd,
                                    color = HologramCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Console Input Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = terminalInput,
                            onValueChange = { terminalInput = it },
                            placeholder = {
                                Text(
                                    text = when (shellMode) {
                                        "PYTHON" -> "Python comando... (print('hola'))"
                                        "JAVA" -> "Java comando... (System.out.println)"
                                        else -> "Comando... (help, ls, python, hack)"
                                    },
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HologramCyan,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("terminal_input")
                        )

                        Button(
                            onClick = {
                                if (terminalInput.isNotEmpty()) {
                                    viewModel.executeTerminalCommand(terminalInput)
                                    terminalInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IronRedDeep),
                            modifier = Modifier
                                .height(56.dp)
                                .border(1.dp, StarkGoldBright, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "EJECUTAR",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            1 -> {
                // 50 INTELLIGENCE CORES LIST
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CORE INTELIGENCIA: 50 NÓDULOS ACTIVOS",
                        color = StarkGoldBright,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(intelligenceList) { item ->
                            val isItemActive = item.status == "OPERATIVO"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isItemActive) StarkGoldDark.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.5f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isItemActive) GlassGrey.copy(alpha = 0.4f) else Color.Black
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "[NUDO ${item.index}]",
                                                color = if (isItemActive) HologramCyan else Color.Gray,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.category.uppercase(),
                                                color = StarkGoldBright,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.description,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = isItemActive,
                                            onCheckedChange = { viewModel.toggleIntelligenceFunction(item.index) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = StarkGoldBright,
                                                checkedTrackColor = IronRedDeep,
                                                uncheckedThumbColor = Color.DarkGray,
                                                uncheckedTrackColor = Color.Black
                                            )
                                        )

                                        IconButton(
                                            onClick = { viewModel.voiceManager.speak(item.name + ": " + item.description) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Oír módulo",
                                                tint = HologramCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // 50 QUANTUM MEMORIES VAULTS
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MEMORIA CUÁNTICA: 50 BÓVEDAS ACTIVAS",
                        color = StarkGoldBright,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(memoryList) { item ->
                            val isSinc = item.isSynchronized
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSinc) IronRedDeep.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.5f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSinc) GlassGrey.copy(alpha = 0.3f) else Color.Black
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "SECTOR: ${item.sector.uppercase()}",
                                                color = if (isSinc) StarkGoldBright else Color.Gray,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "ID: M-${item.index}",
                                                color = HologramCyan,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.description,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = isSinc,
                                            onCheckedChange = { viewModel.toggleMemoryFunction(item.index) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = StarkGoldBright,
                                                checkedTrackColor = IronRedDeep,
                                                uncheckedThumbColor = Color.DarkGray,
                                                uncheckedTrackColor = Color.Black
                                            )
                                        )

                                        IconButton(
                                            onClick = { viewModel.voiceManager.speak("Canal de memoria " + item.sector + ": " + item.name) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cached,
                                                contentDescription = "Sincronizar",
                                                tint = StarkGoldBright,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
