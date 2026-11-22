package com.wangzan101.miga.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wangzan101.miga.groupchat.GroupChat
import com.wangzan101.miga.groupchat.GroupChatRunner
import com.wangzan101.miga.groupchat.GroupChatStore
import com.wangzan101.miga.groupchat.GroupMember
import com.wangzan101.miga.groupchat.GroupMessage
import com.wangzan101.miga.settings.Persona
import com.wangzan101.miga.settings.ProviderConfig
import com.wangzan101.miga.settings.ProviderStore
import com.wangzan101.miga.ui.components.GroupCard
import com.wangzan101.miga.ui.components.GroupLabel
import com.wangzan101.miga.ui.components.ListRow
import com.wangzan101.miga.ui.components.PageBackground
import com.wangzan101.miga.ui.components.PageScaffold
import com.wangzan101.miga.ui.components.RowChevron
import com.wangzan101.miga.ui.components.TopBar
import com.wangzan101.miga.ui.theme.MikaIcons
import com.wangzan101.miga.ui.theme.MikaSpace
import com.wangzan101.miga.ui.theme.MikaTheme
import com.wangzan101.miga.ui.theme.MikaType
import kotlinx.coroutines.launch

/** 没建过人格时也能开群:这三位是内置的,只写名字和一句话人设。 */
private val BUILTIN_MEMBERS = listOf(
    GroupMember(name = "小柯", prompt = "你是群里最讲证据的一位:先问清事实和条件,给结论时说依据,不空喊。"),
    GroupMember(name = "老严", prompt = "你负责工程落地:只关心能不能跑起来、要几步、坑在哪。说话直,爱挑毛病。"),
    GroupMember(name = "阿飘", prompt = "你负责脑洞和吐槽:提反常识的角度,一两句带过,不写长段。"),
)

/**
 * 群聊:多个 bot(成员=人格)在同一个会话里轮流说话。消息发出去后,会员按顺序各回一句。
 * 只做普通对话(不带工具),用的模型是该群指定的配置,没指定就用主模型。
 */
@Composable
fun GroupChatsScreen(
    onBack: () -> Unit,
    onNew: () -> Unit = {},
    store: GroupChatStore? = null,
    personas: List<Persona> = emptyList(),
    runner: GroupChatRunner? = null,
    providers: ProviderStore? = null,
) {
    var openId by remember { mutableStateOf<String?>(null) }
    val chats by (store?.chats?.collectAsState() ?: remember { mutableStateOf(emptyList<GroupChat>()) })
    val open = chats.firstOrNull { it.id == openId }

    if (store != null && open != null) {
        GroupChatView(
            chat = open, store = store, runner = runner, providers = providers,
            onBack = { openId = null },
        )
        return
    }
    GroupChatList(
        chats = chats,
        store = store,
        personas = personas,
        onBack = onBack,
        onNew = onNew,
        onOpen = { openId = it },
    )
}

@Composable
private fun GroupChatList(
    chats: List<GroupChat>,
    store: GroupChatStore?,
    personas: List<Persona>,
    onBack: () -> Unit,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val c = MikaTheme.colors
    var creating by remember { mutableStateOf(false) }

    PageScaffold("群聊", onBack) {
        if (store == null) {
            GroupLabel("群聊")
            GroupCard {
                ListRow(MikaIcons.Chats, "群聊还没准备好", "存储没有初始化,重开 App 试试", first = true)
            }
            return@PageScaffold
        }
        if (chats.isNotEmpty()) {
            GroupLabel("${chats.size} 个群聊")
            GroupCard {
                chats.forEachIndexed { i, g ->
                    ListRow(
                        icon = MikaIcons.Chats,
                        title = g.title,
                        subtitle = "${g.members.size} 位成员 · ${g.messages.size} 条消息"
                            + (g.messages.lastOrNull()?.let { " · 最近:" + it.text.take(12) } ?: ""),
                        first = i == 0,
                        onClick = { onOpen(g.id) },
                    ) { RowChevron() }
                }
            }
        } else {
            GroupLabel("群聊")
            Spacer(Modifier.height(MikaSpace.S16))
            com.wangzan101.miga.ui.kubai.EmptyState(
                icon = com.wangzan101.miga.ui.kubai.KubaiIcons.Users,
                title = "还没有群聊",
                description = "建一个群,把几位 bot 拉进来,大家轮流接话。",
                iconBase = c.ink3.copy(alpha = 0.18f),
                iconAccent = c.accent,
                titleColor = c.ink,
                descColor = c.ink3,
            )
        }
        Spacer(Modifier.height(MikaSpace.S16))
        GroupCard {
            ListRow(
                icon = MikaIcons.Plus,
                title = "新建群聊",
                subtitle = if (personas.isEmpty()) "用内置三位开始,或先去「更多 → 人格」建 bot" else "从人格里挑成员",
                first = true,
                onClick = { creating = true },
            ) { RowChevron() }
        }
        if (personas.isEmpty()) {
            Spacer(Modifier.height(MikaSpace.S12))
            Text(
                "人格(更多 → 人格)可以自定义每个 bot 的名字和提示词;没有也行,内置成员够开聊。",
                style = MikaType.Caption, color = c.ink3,
            )
        }
    }

    if (creating) {
        NewGroupDialog(
            personas = personas,
            onDismiss = { creating = false },
            onCreate = { title, members ->
                creating = false
                val chat = store?.create(title, members)
                if (chat != null) onOpen(chat.id)
            },
        )
    }
}

@Composable
private fun NewGroupDialog(
    personas: List<Persona>,
    onDismiss: () -> Unit,
    onCreate: (String, List<GroupMember>) -> Unit,
) {
    val c = MikaTheme.colors
    var title by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf(setOf<String>()) }
    var builtin by remember { mutableStateOf(personas.isEmpty()) }
    var builtinPicked by remember { mutableStateOf(BUILTIN_MEMBERS.map { it.name }.toSet()) }

    val members: List<GroupMember> = if (builtin) BUILTIN_MEMBERS.filter { it.name in builtinPicked } else personas.filter { it.id in picked }.map {
        GroupMember(name = it.name, prompt = it.prompt, personaId = it.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.fill,
        title = { Text("新建群聊") },
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("群名(可留空)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(MikaSpace.S12))
                Text("成员", style = MikaType.Caption, color = c.ink3)
                if (personas.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (builtin) "用内置三位" else "从人格选",
                            style = MikaType.Body, color = c.accent,
                            modifier = Modifier.clickable { builtin = !builtin; picked = emptySet() },
                        )
                    }
                }
                Spacer(Modifier.height(MikaSpace.S8))
                if (builtin) {
                    BUILTIN_MEMBERS.forEach { m ->
                        val on = m.name in builtinPicked
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    builtinPicked =
                                        if (on) builtinPicked - m.name else builtinPicked + m.name
                                }
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                if (on) "✓" else "○",
                                color = if (on) c.accent else c.ink3,
                                modifier = Modifier.width(20.dp),
                            )
                            Column {
                                Text(m.name, style = MikaType.Body, color = c.ink)
                                Text(m.prompt, style = MikaType.Caption, color = c.ink3)
                            }
                        }
                    }
                } else {
                    personas.forEach { p ->
                        val on = p.id in picked
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { picked = if (on) picked - p.id else picked + p.id }
                                .padding(vertical = 6.dp),
                        ) {
                            Text(if (on) "✓" else "○", color = if (on) c.accent else c.ink3, modifier = Modifier.width(20.dp))
                            Column {
                                Text(p.name, style = MikaType.Body, color = c.ink)
                                if (p.description.isNotBlank()) {
                                    Text(p.description, style = MikaType.Caption, color = c.ink3)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = members.isNotEmpty(),
                onClick = { onCreate(title, members) },
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun GroupChatView(
    chat: GroupChat,
    store: GroupChatStore,
    runner: GroupChatRunner?,
    providers: ProviderStore?,
    onBack: () -> Unit,
) {
    val c = MikaTheme.colors
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) listState.animateScrollToItem(chat.messages.size - 1)
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || busy) return
        val r = runner
        if (r == null) { error = "群聊还没准备好(存储未初始化)"; return }
        input = ""
        error = null
        busy = true
        scope.launch {
            store.append(chat.id, GroupMessage(System.currentTimeMillis(), "我", text, fromBot = false))
            for (member in chat.members) {
                val current = store.get(chat.id) ?: break
                val res = r.ask(member, current)
                res.onSuccess { reply ->
                    store.append(chat.id, GroupMessage(System.currentTimeMillis(), member.name, reply, fromBot = true))
                }.onFailure { e ->
                    error = "${member.name}:${e.message ?: "调用失败"}"
                }
            }
            busy = false
        }
    }

    PageBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = chat.title,
                left = MikaIcons.Back,
                onLeft = onBack,
                leftLabel = "返回",
                subtitle = if (chat.members.isEmpty()) "没有成员"
                else chat.members.joinToString("、") { it.name }.let { if (it == chat.title) "${chat.members.size} 位成员" else it },
                right = {
                    TextButton(onClick = { settingsOpen = true }) { Text("设置", color = c.accent) }
                },
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = MikaSpace.Page),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (chat.messages.isEmpty()) {
                    item {
                        Text(
                            "给这个群发第一条消息,${chat.members.size} 位成员会依次回你。",
                            style = MikaType.Caption, color = c.ink3,
                            modifier = Modifier.padding(vertical = MikaSpace.S8),
                        )
                    }
                }
                items(chat.messages) { m ->
                    GroupBubble(message = m, isUser = !m.fromBot)
                }
                if (busy) {
                    item {
                        Text(
                            "成员在想…",
                            style = MikaType.Caption, color = c.ink3,
                            modifier = Modifier.padding(vertical = MikaSpace.S8),
                        )
                    }
                }
            }
            error?.let {
                Text(
                    it, style = MikaType.Caption, color = c.bad,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = MikaSpace.Page, vertical = 4.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                    .padding(horizontal = MikaSpace.Page, vertical = MikaSpace.S8),
            ) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text("说点什么…") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(MikaSpace.S8))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp))
                        .background(if (busy) c.fill else c.accent)
                        .clickable(enabled = !busy) { send() },
                ) {
                    Text("↑", color = if (busy) c.ink3 else c.onAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (settingsOpen) {
        GroupSettingsDialog(
            chat = chat,
            store = store,
            providers = providers,
            onDismiss = { settingsOpen = false },
            onDeleted = { settingsOpen = false; onBack() },
        )
    }
}

@Composable
private fun GroupSettingsDialog(
    chat: GroupChat,
    store: GroupChatStore,
    providers: ProviderStore?,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    val c = MikaTheme.colors
    val configs by (providers?.configs?.collectAsState() ?: remember { mutableStateOf(emptyList<ProviderConfig>()) })
    var title by remember(chat.id) { mutableStateOf(chat.title) }
    var confirmDelete by remember { mutableStateOf(false) }
    var editingIndex by remember(chat.id) { mutableStateOf(-1) }
    val current = chat.configId

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.fill,
        title = { Text("群设置") },
        text = {
            Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("群名") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            enabled = title.trim().isNotEmpty() && title.trim() != chat.title,
                            onClick = { store.rename(chat.id, title) },
                        ) { Text("保存") }
                    },
                )
                Spacer(Modifier.height(MikaSpace.S16))
                Text("这个群用哪个模型", style = MikaType.Caption, color = c.ink3)
                ModelChoice("跟随设置(主模型)", selected = current == null) { store.setConfig(chat.id, null) }
                configs.forEach { cfg ->
                    val label = listOfNotNull(cfg.name.takeIf { it.isNotBlank() }, cfg.model)
                        .joinToString(" / ")
                    ModelChoice(label, selected = current == cfg.id) { store.setConfig(chat.id, cfg.id) }
                }
                if (configs.isEmpty()) {
                    Text("还没有可选的模型,先去「设置 → 模型」配一个。", style = MikaType.Caption, color = c.ink3)
                }
                Spacer(Modifier.height(MikaSpace.S16))
                Text("成员", style = MikaType.Caption, color = c.ink3)
                chat.members.forEachIndexed { idx, m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { editingIndex = idx }
                            .padding(vertical = 6.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(m.name, style = MikaType.Body, color = c.ink)
                            if (m.prompt.isNotBlank()) Text(m.prompt, style = MikaType.Caption, color = c.ink3)
                        }
                        Text("✎ 编辑", style = MikaType.Caption, color = c.accent)
                    }
                }
                Text("点成员可以改它的名字和人设。", style = MikaType.Caption, color = c.ink3)
                Spacer(Modifier.height(MikaSpace.S16))
                TextButton(onClick = { store.clear(chat.id) }) { Text("清空消息", color = c.warn) }
                TextButton(onClick = { confirmDelete = true }) { Text("删除群聊", color = c.bad) }
                if (confirmDelete) {
                    Text("确定删除「${chat.title}」?消息记录会一起删掉。", style = MikaType.Caption, color = c.bad)
                    Row {
                        TextButton(onClick = { store.remove(chat.id); onDeleted() }) { Text("确定删除", color = c.bad) }
                        TextButton(onClick = { confirmDelete = false }) { Text("取消") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )

    if (editingIndex >= 0) {
        EditMemberDialog(chat = chat, store = store, index = editingIndex, onDismiss = { editingIndex = -1 })
    }
}

@Composable
private fun EditMemberDialog(
    chat: GroupChat,
    store: GroupChatStore,
    index: Int,
    onDismiss: () -> Unit,
) {
    val c = MikaTheme.colors
    val m = chat.members.getOrNull(index) ?: return
    var name by remember(chat.id, index) { mutableStateOf(m.name) }
    var prompt by remember(chat.id, index) { mutableStateOf(m.prompt) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.fill,
        title = { Text("编辑「${m.name}」") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("名字") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(MikaSpace.S12))
                OutlinedTextField(
                    value = prompt, onValueChange = { prompt = it },
                    label = { Text("提示词(人设)") }, minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(MikaSpace.S8))
                Text(
                    "提示词告诉这个 bot 它是谁、怎么说话;留空就用内置人设。",
                    style = MikaType.Caption, color = c.ink3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                store.setMember(chat.id, index, m.copy(
                    name = name.trim().ifBlank { m.name },
                    prompt = prompt.trim(),
                ))
                onDismiss()
            }) { Text("保存", color = c.accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun ModelChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = MikaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp),
    ) {
        Text(if (selected) "●" else "○", color = if (selected) c.accent else c.ink3, modifier = Modifier.width(20.dp))
        Text(label, style = MikaType.Body, color = if (selected) c.ink else c.ink2)
    }
}

@Composable
private fun GroupBubble(message: GroupMessage, isUser: Boolean) {
    val c = MikaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            if (!isUser) {
                Text(message.author, style = MikaType.Caption, color = c.ink3)
                Spacer(Modifier.height(2.dp))
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp,
                        )
                    )
                    .background(if (isUser) c.accent else c.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    message.text,
                    style = MikaType.Body,
                    color = if (isUser) c.onAccent else c.ink,
                )
            }
        }
    }
}
