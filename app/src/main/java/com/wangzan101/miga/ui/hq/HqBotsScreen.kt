package com.wangzan101.miga.ui.hq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wangzan101.miga.ui.theme.MikaIcons

/** Bots 屏的一个 bot。 */
data class HqBot(
    val name: String,
    val group: String,
    val summary: String,
    val time: String,
    /** 对应的人格 id;主智能体为 null(走默认身份)。 */
    val personaId: String? = null,
)

/** 顶排 3 个置顶大 blob（pebble 55dp + 名称）。 */
/** 圆泡堆叠选择器(技巧 007):选项是大小不一的圆、紧密堆叠;
 *  点一下选中(放大、填充主色),周围的圆被挤开;再点选中的圆打开对话。 */
@Composable
fun HqPebblePicker(
    bots: List<HqBot>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onOpen: (HqBot) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = HqSpace.Page),
        horizontalArrangement = Arrangement.spacedBy((-14).dp),
    ) {
        bots.forEachIndexed { i, bot ->
            val sel = selected == i
            val scale by animateFloatAsState(if (sel) 1.18f else 0.94f, label = "pebble-scale")
            val fill by animateColorAsState(
                if (sel) Hq.accent else Hq.pebble(bot.name), label = "pebble-fill",
            )
            Box(
                modifier = Modifier
                    .size(if (sel) 66.dp else 50.dp)
                    .zIndex(if (sel) 2f else if (selected >= 0) 0f else 0f)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .background(fill)
                    .clickable {
                        if (sel) onOpen(bot) else onSelect(i)
                    },
            ) {
                PebbleAvatar(bot.name, Modifier.fillMaxSize())
                if (sel) {
                    Text(
                        "再点打开", style = HqType.Caption, color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.Center).offset(y = 4.dp),
                    )
                }
            }
        }
    }
}

/** Bots 列表屏：大标题 + 图标胶囊 + 顶排大 blob + 分组列表。 */
@Composable
fun HqBotsScreen(
    personas: List<com.wangzan101.miga.settings.Persona> = emptyList(),
    onOpenBot: (HqBot) -> Unit,
    modifier: Modifier = Modifier,
    onManageBots: () -> Unit = {},
    onEditBot: (HqBot) -> Unit = {},
) {
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var pickedBot by remember { mutableStateOf(0) }
    val q = query.trim()
    val shown = if (q.isEmpty()) personas else personas.filter {
        it.name.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true)
    }
    // 主 agent(本能全能) + 分出的 bot(personas 真实数据)
    val mainBot = HqBot("Miga", "主智能体", "本能全能 · 随时可分 bot 干活", "在线")
    val botList = shown.map {
        HqBot(it.name, "分出的 bot", it.description.ifBlank { "自定义人格" }, "就绪", it.id)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Hq.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
    ) {
        // 标题行：Bots ⌄ + Mac mini ｜ 漏斗/搜索/编辑 胶囊
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HqSpace.Page),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("机器人", style = HqType.LargeTitle, color = Hq.ink)
                    Spacer(Modifier.width(6.dp))
                    Icon(MikaIcons.Down, null, tint = Hq.ink2, modifier = Modifier.size(14.dp))
                }
                Text("Mac mini", style = HqType.Caption, color = Hq.ink2)
            }
            // 3 图标胶囊
            Row(
                modifier = Modifier
                    .clip(HqShape.Capsule)
                    .background(Hq.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Icon(
                    MikaIcons.ListPlus, "新建 bot", tint = Hq.ink,
                    modifier = Modifier.size(20.dp).clickable { onManageBots() },
                )
                Icon(
                    MikaIcons.Search, "搜索 bot", tint = if (searching) Hq.accent else Hq.ink,
                    modifier = Modifier.size(20.dp).clickable { searching = !searching },
                )
                Icon(
                    MikaIcons.Edit, "管理 bot", tint = Hq.ink,
                    modifier = Modifier.size(20.dp).clickable { onManageBots() },
                )
            }
        }

        if (searching) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("搜索 bot") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = HqSpace.Page),
            )
        }

        Spacer(Modifier.height(20.dp))
        val pickBots = listOf(mainBot) + botList
        HqPebblePicker(
            bots = pickBots,
            selected = pickedBot,
            onSelect = { pickedBot = it },
            onOpen = onOpenBot,
        )
        Spacer(Modifier.height(24.dp))

        if (botList.isEmpty()) {
            com.wangzan101.miga.ui.kubai.EmptyState(
                icon = if (q.isEmpty()) com.wangzan101.miga.ui.kubai.KubaiIcons.Bot
                else com.wangzan101.miga.ui.kubai.KubaiIcons.SearchSparkle,
                title = if (q.isEmpty()) "还没有分出的 bot" else "没有匹配「$q」的 bot",
                description = if (q.isEmpty())
                    "在「更多 → 人格」里建一个,它会出现在这里,点开就能单独对话。"
                else "换个关键词,或者看看有没有拼错。",
                action = if (q.isEmpty()) "去建一个" else null,
                onAction = if (q.isEmpty()) onManageBots else null,
                iconBase = Hq.ink2.copy(alpha = 0.16f),
                iconAccent = Hq.accent,
                titleColor = Hq.ink2,
                descColor = Hq.ink3,
                actionColor = Hq.accent,
                modifier = Modifier.fillMaxWidth().padding(horizontal = HqSpace.Page, vertical = 20.dp),
            )
        }

        // 分组列表:主智能体 + 分出的 bot
        listOf("主智能体" to listOf(mainBot), "分出的 bot" to botList).forEach { (group, items) ->
            if (items.isEmpty()) return@forEach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HqSpace.Page, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$group · ${items.size}", style = HqType.Section, color = Hq.ink2, modifier = Modifier.weight(1f))
                if (group == "分出的 bot") {
                    Icon(
                        MikaIcons.Plus, "新建 bot", tint = Hq.ink2,
                        modifier = Modifier.size(16.dp).clickable { onManageBots() },
                    )
                }
            }
            items.forEach { bot ->
                HqBotRow(bot, onOpenBot, onEdit = if (bot.personaId != null) onEditBot else null)
                Box(
                    Modifier
                        .padding(start = 44.dp, end = HqSpace.Page)
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(Hq.edge.copy(alpha = 0.5f)),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** 列表项：blob 28dp + 标题 + 右上时间 + 摘要 2 行 + 行尾 ⋯。 */
@Composable
fun HqBotRow(
    bot: HqBot,
    onOpen: (HqBot) -> Unit,
    modifier: Modifier = Modifier,
    onEdit: ((HqBot) -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen(bot) }
            .padding(horizontal = HqSpace.Page, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PebbleAvatar(bot.name, Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bot.name, style = HqType.RowTitle, color = Hq.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(bot.time, style = HqType.Caption, color = Hq.ink2)
            }
            Spacer(Modifier.height(2.dp))
            Text(bot.summary, style = HqType.Body, color = Hq.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (onEdit != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                MikaIcons.More, "编辑 ${bot.name}", tint = Hq.ink2,
                modifier = Modifier.size(16.dp).clickable { onEdit(bot) },
            )
        }
    }
}
