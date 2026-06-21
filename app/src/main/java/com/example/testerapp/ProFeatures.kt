package com.mdstudio.closedtesttracker

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class GuideStep(val title: String, val body: String)
private enum class ProTab { GUIDE, COMMUNITY, TEMPLATES, TESTERS }

private const val STARTER_TEMPLATE_INTRO = "I am looking for {count} testers for {app}. Please use the app daily for 14 days and share brief feedback."
private const val STARTER_TEMPLATE_LINKS = "Google Group: {group}\nOpt-in: {optin}\nAndroid: {android}"
private const val STARTER_TEMPLATE_BODY = "$STARTER_TEMPLATE_INTRO\n\n$STARTER_TEMPLATE_LINKS"

private val redditCommunities = listOf(
    "r/12TesterTeam" to "https://www.reddit.com/r/12TesterTeam/",
    "r/20AndroidTesters" to "https://www.reddit.com/r/20AndroidTesters/",
    "r/alphaandbetausers" to "https://www.reddit.com/r/alphaandbetausers/",
    "r/android_devs" to "https://www.reddit.com/r/android_devs/",
    "r/AndroidAppTesters" to "https://www.reddit.com/r/AndroidAppTesters/",
    "r/AndroidClosedTesting" to "https://www.reddit.com/r/AndroidClosedTesting/",
    "r/AndroidTesting" to "https://www.reddit.com/r/AndroidTesting/",
    "r/App_Hive" to "https://www.reddit.com/r/App_Hive/",
    "r/AppBuilding" to "https://www.reddit.com/r/AppBuilding/",
    "r/AppsWebappsFullstack" to "https://www.reddit.com/r/AppsWebappsFullstack/",
    "r/TestersCommunity" to "https://www.reddit.com/r/TestersCommunity/",
    "r/TestMyApp" to "https://www.reddit.com/r/TestMyApp/"
)

private fun proText(language: AppLanguage, tr: String, en: String): String {
    if (language == AppLanguage.EN) return en
    return translatedCopy(language, en) ?: if (language == AppLanguage.TR) tr else en
}

private fun guideSteps(language: AppLanguage) = listOf(
    GuideStep(
        proText(language, "20 testçi hedefle", "Aim for 20 testers"),
        proText(language, "Zorunlu sayıyı yalnızca karşılamak yerine ayrılmaları hesaba katmak için yaklaşık 20 gerçek testçi bul. Güncel gereksinimi her zaman Play Console'dan doğrula.", "Recruit about 20 real testers so you have a buffer if someone leaves. Always confirm the current requirement in Play Console.")
    ),
    GuideStep(
        proText(language, "Her gün gerçek kullanım iste", "Ask for real daily usage"),
        proText(language, "Testçiler uygulamayı yalnızca açıp kapatmasın. Her gün en az 2 dakika kullanıp ana akışları denesin ve kısa geri bildirim bıraksın.", "Do not ask testers to only open and close the app. Ask them to use key flows for at least 2 minutes each day and leave brief feedback.")
    ),
    GuideStep(
        proText(language, "14 günü kesintisiz takip et", "Track all 14 days"),
        proText(language, "Katılım, günlük kullanım ve sorunları düzenli kontrol et. Eksik testçilere nazik bir hatırlatma gönder ve yedek testçi hazır tut.", "Check participation, daily usage and issues regularly. Send a polite reminder to missing testers and keep backup testers ready.")
    ),
    GuideStep(
        proText(language, "İzinleri ve beyanları kontrol et", "Review permissions and declarations"),
        proText(language, "Yalnızca gerekli Android izinlerini kullan. Veri güvenliği formu, gizlilik politikası, reklam ve ödeme beyanlarını uygulamadaki gerçek davranışla eşleştir.", "Use only necessary Android permissions. Match Data safety, privacy, ads and payment declarations with the app's actual behavior.")
    ),
    GuideStep(
        proText(language, "Yaygın hatalardan kaçın", "Avoid common mistakes"),
        proText(language, "Sahte veya yalnızca kurulum yapan testçiler kullanma. Çökme, ANR, cihaz modeli, Android sürümü ve tekrar adımlarını kaydetmeden üretime geçme.", "Avoid fake or install-only testers. Do not go to production without recording crashes, ANRs, device model, Android version and reproduction steps.")
    )
)

@Composable
fun ProOfferDialog(language: AppLanguage, state: ProBillingState, onBuy: () -> Unit, onLater: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
        title = { Text("Closed Test Tracker Pro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(proText(language, "Tek seferlik satın alma. Abonelik yok.", "One-time purchase. No subscription."))
                listOf(
                    proText(language, "Reklamsız kullanım", "No ads"),
                    proText(language, "Test kaynakları ve pratik rehberler", "Testing resources and practical guides"),
                    proText(language, "3 özel paylaşım şablonu", "3 custom sharing templates"),
                    proText(language, "Testçi profilleri ve notları", "Tester usernames, profiles and notes")
                ).forEach { feature ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Text(feature)
                    }
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = onBuy, enabled = state.isReady && !state.isLoading) { Text(state.price ?: proText(language, "Pro'yu al", "Get Pro")) } },
        dismissButton = { TextButton(onClick = onLater) { Text(proText(language, "Daha sonra", "Later")) } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProHubSheet(
    language: AppLanguage,
    isPro: Boolean,
    billingState: ProBillingState,
    storage: ProStorage,
    onBuy: () -> Unit,
    onDismiss: () -> Unit
) {
    var templates by remember { mutableStateOf(storage.templates()) }
    var testers by remember { mutableStateOf(storage.testers()) }
    var templateName by remember(language) { mutableStateOf(proText(language, "Reddit test gönderisi", "Reddit test post")) }
    var templateAppName by remember { mutableStateOf("") }
    var templateTesterCount by remember { mutableStateOf("20") }
    var templateGroupLink by remember { mutableStateOf("") }
    var templateOptInLink by remember { mutableStateOf("") }
    var templateAndroidLink by remember { mutableStateOf("") }
    var templateExtraMessage by remember { mutableStateOf("") }
    var editingTemplateId by remember { mutableStateOf<Long?>(null) }
    var testerName by remember { mutableStateOf("") }
    var testerProfile by remember { mutableStateOf("") }
    var testerNote by remember { mutableStateOf("") }
    var testerReliable by remember { mutableStateOf(true) }
    var editingTesterId by remember { mutableStateOf<Long?>(null) }
    var selectedTab by remember { mutableStateOf(ProTab.GUIDE) }
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        scrimColor = Color.Black.copy(alpha = 0.48f)
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.94f)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                Column {
                    Text("Closed Test Tracker Pro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (isPro) proText(language, "Tüm profesyonel test araçların hazır.", "Your complete professional testing toolkit.")
                        else proText(language, "Tek ödeme ile araçları aç ve reklamları kaldır.", "Unlock the toolkit and remove ads with one payment."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!isPro) {
                    item {
                        ProSection(proText(language, "Pro'yu aç", "Unlock Pro"), Icons.Rounded.Star) {
                            Text(proText(language, "Tek seferlik satın alma; abonelik yok.", "One-time purchase with no subscription."))
                            Button(onClick = onBuy, enabled = billingState.isReady, modifier = Modifier.fillMaxWidth()) {
                                Text(billingState.price ?: proText(language, "Pro'yu al", "Get Pro"))
                            }
                        }
                    }
                } else {
                    if (selectedTab == ProTab.GUIDE) item {
                        ProSection(proText(language, "Kapalı test yol haritası", "Closed testing roadmap"), Icons.Rounded.MenuBook) {
                            Text(
                                proText(language, "Bireysel geliştiriciler için uygulanabilir kontrol listesi", "A practical checklist for independent developers"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            guideSteps(language).forEachIndexed { index, step -> GuideStepRow(index + 1, step) }
                        }
                    }
                    if (selectedTab == ProTab.COMMUNITY) item {
                        ProSection(proText(language, "Reddit test toplulukları", "Reddit testing communities"), Icons.Rounded.Groups) {
                            Text(
                                proText(language, "Kuralları okuyup topluluğa özel, dürüst bir test talebi paylaş. Aynı gönderiyi art arda tüm topluluklara gönderme.", "Read each community's rules and post an honest, relevant testing request. Avoid posting the same message everywhere at once."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            redditCommunities.forEach { (name, url) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(url) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(name, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (selectedTab == ProTab.TEMPLATES) item {
                        ProSection(proText(language, "Özel post şablonları (${templates.size}/3)", "Custom post templates (${templates.size}/3)"), Icons.Rounded.Edit) {
                            Text(
                                proText(language, "Sık kullandığın test gönderisini bir kez hazırla; yeni uygulama paylaşırken alanlar otomatik doldurulsun.", "Prepare your usual testing post once; its fields will be filled automatically when you share a new app."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            templates.forEach { item ->
                                SavedTemplateCard(
                                    item = item,
                                    editLabel = proText(language, "Düzenle", "Edit"),
                                    deleteLabel = proText(language, "Sil", "Delete"),
                                    onEdit = {
                                        editingTemplateId = item.id
                                        templateName = item.name
                                        templateExtraMessage = extractTemplateExtraMessage(item.body)
                                    },
                                    onDelete = { storage.deleteTemplate(item.id); templates = storage.templates() }
                                )
                            }
                            if (editingTemplateId != null || templates.size < ProStorage.MAX_TEMPLATES) {
                                Spacer(Modifier.height(4.dp))
                                Text(proText(language, "Şablon ve gönderi bilgileri", "Template and post details"), fontWeight = FontWeight.Bold)
                                Text(
                                    proText(language, "Boş alanları doldur. Aşağıdaki önizleme anında güncellenir; teknik değişkenlerle uğraşman gerekmez.", "Fill in the fields. The preview updates immediately; you do not need to work with technical placeholders."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    templateName,
                                    { templateName = it },
                                    label = { Text(proText(language, "Şablon adı", "Template name")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateAppName,
                                    { templateAppName = it },
                                    label = { Text(proText(language, "Uygulama adı", "App name")) },
                                    placeholder = { Text(proText(language, "Örn. Kapalı Test Takibi", "e.g. My Test App")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateTesterCount,
                                    { templateTesterCount = it.filter(Char::isDigit).take(3) },
                                    label = { Text(proText(language, "Gerekli testçi sayısı", "Number of testers needed")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateGroupLink,
                                    { templateGroupLink = it },
                                    label = { Text(proText(language, "Google Group bağlantısı", "Google Group link")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateOptInLink,
                                    { templateOptInLink = it },
                                    label = { Text(proText(language, "Teste katılım bağlantısı", "Opt-in link")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateAndroidLink,
                                    { templateAndroidLink = it },
                                    label = { Text(proText(language, "Android uygulama bağlantısı", "Android app link")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    templateExtraMessage,
                                    { templateExtraMessage = it },
                                    label = { Text(proText(language, "Ek mesaj (isteğe bağlı)", "Extra message (optional)")) },
                                    placeholder = { Text(proText(language, "Testçilerden özellikle denemelerini istediğin özellikleri yaz.", "Mention any features you especially want testers to try.")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(proText(language, "Gönderi önizlemesi", "Post preview"), fontWeight = FontWeight.Bold)
                                        Text(
                                            renderTemplatePost(
                                                body = buildStoredTemplate(templateExtraMessage),
                                                appName = templateAppName,
                                                testerCount = templateTesterCount,
                                                groupLink = templateGroupLink,
                                                optInLink = templateOptInLink,
                                                androidLink = templateAndroidLink
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Button(onClick = {
                                    val storedBody = buildStoredTemplate(templateExtraMessage)
                                    if (templateName.isNotBlank() && storage.saveTemplate(templateName, storedBody, editingTemplateId)) {
                                        templates = storage.templates()
                                        templateName = proText(language, "Reddit test gönderisi", "Reddit test post")
                                        templateAppName = ""
                                        templateTesterCount = "20"
                                        templateGroupLink = ""
                                        templateOptInLink = ""
                                        templateAndroidLink = ""
                                        templateExtraMessage = ""
                                        editingTemplateId = null
                                    }
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (editingTemplateId == null) proText(language, "Şablonu kaydet", "Save template") else proText(language, "Değişiklikleri kaydet", "Save changes"))
                                }
                            }
                        }
                    }
                    if (selectedTab == ProTab.TESTERS) item {
                        ProSection(proText(language, "Testçi listesi", "Tester list"), Icons.Rounded.PersonAdd) {
                            if (testers.isEmpty()) {
                                Text(proText(language, "Henüz testçi kaydı yok.", "No tester records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            testers.forEach { tester ->
                                TesterCard(
                                    tester = tester,
                                    reliableLabel = proText(language, "Güvenilir testçi", "Reliable tester"),
                                    reviewLabel = proText(language, "Dikkatli değerlendir", "Review carefully"),
                                    onEdit = {
                                        editingTesterId = tester.id; testerName = tester.username; testerProfile = tester.profileUrl
                                        testerNote = tester.note; testerReliable = tester.reliable
                                    },
                                    onDelete = { storage.deleteTester(tester.id); testers = storage.testers() }
                                )
                            }
                            OutlinedTextField(testerName, { testerName = it }, label = { Text(proText(language, "Reddit kullanıcı adı", "Reddit username")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(testerProfile, { testerProfile = it }, label = { Text(proText(language, "Profil bağlantısı", "Profile link")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(testerNote, { testerNote = it }, label = { Text(proText(language, "Not", "Note")) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                            Row { Checkbox(testerReliable, { testerReliable = it }); Text(proText(language, "Düzenli/güvenilir testçi", "Regular/reliable tester"), modifier = Modifier.padding(top = 12.dp)) }
                            Button(onClick = {
                                if (testerName.isNotBlank()) {
                                    storage.saveTester(testerName, testerProfile, testerNote, testerReliable, editingTesterId)
                                    testers = storage.testers(); testerName = ""; testerProfile = ""; testerNote = ""; editingTesterId = null
                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (editingTesterId == null) proText(language, "Testçiyi ekle", "Add tester") else proText(language, "Testçiyi güncelle", "Update tester"))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(28.dp)) }
            }
            if (isPro) {
                ProTabBar(language = language, selected = selectedTab, onSelect = { selectedTab = it })
            }
        }
    }
}

@Composable
private fun ProTabBar(language: AppLanguage, selected: ProTab, onSelect: (ProTab) -> Unit) {
    val tabs = listOf(
        Triple(ProTab.GUIDE, Icons.Rounded.MenuBook, proText(language, "Test rehberi", "Test guide")),
        Triple(ProTab.COMMUNITY, Icons.Rounded.Groups, proText(language, "Topluluklar", "Community")),
        Triple(ProTab.TEMPLATES, Icons.Rounded.Edit, proText(language, "Şablonlar", "Templates")),
        Triple(ProTab.TESTERS, Icons.Rounded.PersonAdd, proText(language, "Testçiler", "Testers"))
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { (tab, icon, label) ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun ProSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            content()
        }
    }
}

@Composable
private fun GuideStepRow(number: Int, step: GuideStep) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text(number.toString(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(step.title, fontWeight = FontWeight.Bold)
            Text(step.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun buildStoredTemplate(extraMessage: String): String = buildString {
    append(STARTER_TEMPLATE_INTRO)
    if (extraMessage.isNotBlank()) append("\n\n${extraMessage.trim()}")
    append("\n\n$STARTER_TEMPLATE_LINKS")
}

private fun extractTemplateExtraMessage(body: String): String {
    if (!body.startsWith(STARTER_TEMPLATE_INTRO)) return ""
    return body.removePrefix(STARTER_TEMPLATE_INTRO)
        .substringBefore(STARTER_TEMPLATE_LINKS)
        .trim()
}

private fun renderTemplatePost(
    body: String,
    appName: String,
    testerCount: String,
    groupLink: String,
    optInLink: String,
    androidLink: String
): String = body
    .replace("{app}", appName.ifBlank { "My App" })
    .replace("{count}", testerCount.ifBlank { "20" })
    .replace("{group}", groupLink.ifBlank { "https://groups.google.com/g/example" })
    .replace("{optin}", optInLink.ifBlank { "https://play.google.com/apps/testing/example" })
    .replace("{android}", androidLink.ifBlank { "https://play.google.com/store/apps/details?id=example" })

@Composable
private fun SavedTemplateCard(item: PostTemplate, editLabel: String, deleteLabel: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = editLabel) }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = deleteLabel) }
            }
            Text(
                renderTemplatePost(item.body, "My App", "20", "Google Group link", "Opt-in link", "Android app link"),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun TesterCard(tester: TesterNote, reliableLabel: String, reviewLabel: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tester.username, fontWeight = FontWeight.Bold)
                if (tester.profileUrl.isNotBlank()) Text(tester.profileUrl, style = MaterialTheme.typography.bodySmall)
                if (tester.note.isNotBlank()) Text(tester.note, style = MaterialTheme.typography.bodySmall)
                Text(if (tester.reliable) reliableLabel else reviewLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = null) }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = null) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun TestShareSheet(language: AppLanguage, isPro: Boolean, storage: ProStorage, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var appName by remember { mutableStateOf("") }
    var testerCount by remember { mutableStateOf("20") }
    var groupLink by remember { mutableStateOf("") }
    var optInLink by remember { mutableStateOf("") }
    var androidLink by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<PostTemplate?>(null) }
    val templates = remember(isPro) { if (isPro) storage.templates() else emptyList() }
    val defaultText = buildString {
        append("I am looking for ${testerCount.ifBlank { "20" }} testers who can use ${appName.ifBlank { "my app" }} regularly for 14 days.")
        if (groupLink.isNotBlank()) append("\n\nGoogle Group: $groupLink")
        if (optInLink.isNotBlank()) append("\nJoin the test: $optInLink")
        if (androidLink.isNotBlank()) append("\nAndroid: $androidLink")
        append("\n\nTrack your test process with Closed Test Tracker: https://play.google.com/store/apps/details?id=com.mdstudio.closedtesttracker")
    }
    val body = selectedTemplate?.body
        ?.replace("{app}", appName)?.replace("{count}", testerCount)?.replace("{group}", groupLink)
        ?.replace("{optin}", optInLink)?.replace("{android}", androidLink) ?: defaultText

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background, scrimColor = Color.Black.copy(alpha = 0.42f)) {
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text(proText(language, "Yeni test uygulaması paylaş", "Share a new test app"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Text(proText(language, "Test bağlantılarını gir ve paylaşmaya hazır İngilizce gönderi oluştur.", "Enter your testing links and generate a ready-to-share English post."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedTextField(appName, { appName = it }, label = { Text(proText(language, "Uygulama adı", "App name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(testerCount, { testerCount = it.filter(Char::isDigit).take(3) }, label = { Text(proText(language, "Gerekli testçi sayısı", "Number of testers needed")) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(groupLink, { groupLink = it }, label = { Text(proText(language, "Google Group bağlantısı", "Google Group link")) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(optInLink, { optInLink = it }, label = { Text(proText(language, "Teste katılım bağlantısı", "Opt-in link")) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(androidLink, { androidLink = it }, label = { Text(proText(language, "Android uygulama bağlantısı", "Android app link")) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            if (templates.isNotEmpty()) {
                item { Text(proText(language, "Kayıtlı şablon", "Saved template"), fontWeight = FontWeight.Bold) }
                items(templates.size) { index -> OutlinedButton(onClick = { selectedTemplate = templates[index] }) { Text(templates[index].name) } }
            }
            if (appName.isNotBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(14.dp)) { Text(proText(language, "Gönderi önizlemesi", "Post preview"), fontWeight = FontWeight.Bold); Text(body) }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, appName); putExtra(Intent.EXTRA_TEXT, body) }
                        context.startActivity(Intent.createChooser(intent, proText(language, "Test gönderisini paylaş", "Share test post")))
                    },
                    enabled = appName.isNotBlank(), modifier = Modifier.fillMaxWidth()
                ) { Icon(Icons.Rounded.Share, contentDescription = null); Text(" ${proText(language, "Gönderiyi paylaş", "Share post")}") }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
