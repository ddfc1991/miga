package com.wangzan101.miga.ui.hq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wangzan101.miga.ui.theme.MikaIcons
import com.wangzan101.miga.chat.ConversationSummary
import com.wangzan101.miga.core.record.RunSummary
import com.wangzan101.miga.task.TaskRun

/** Sessions 屏：Miga 真实会话列表（长按/⋯ 删除，溶解动效）。 */
@Composable
fun HqSessionsScreen(
    sessions: List<ConversationSummary> = emptyList(),
    onOpenSession: (ConversationSummary) -> Unit = {},
    onDeleteSession: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var deleteConfirm by remember { mutableStateOf<String?>(null) }
    var dissolving by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Hq.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HqSpace.Page),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("会话", style = HqType.ScreenTitle, color = Hq.ink)
                    Spacer(Modifier.width(6.dp))
                    Icon(MikaIcons.Down, null, tint = Hq.ink2, modifier = Modifier.size(14.dp))
                }
                Text("Miga", style = HqType.Caption, color = Hq.ink2)
            }
            Row(
                modifier = Modifier
                    .clip(HqShape.Capsule)
                    .background(Hq.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Icon(MikaIcons.ListPlus, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
                Icon(MikaIcons.Search, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
                Icon(MikaIcons.Edit, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Text("还没有会话", style = HqType.Caption, color = Hq.ink3, modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 24.dp))
        } else {
            sessions.forEach { s ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HqSessionRow(s, onClick = { onOpenSession(s) }, onDelete = { deleteConfirm = s.id })
                    if (dissolving == s.id) {
                        DissolveParticles(
                            onDone = { onDeleteSession(s.id); dissolving = null },
                            modifier = Modifier.matchParentSize(),
                            color = Hq.ink,
                        )
                    }
                }
            }
        }
    }
    deleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text("删除会话？") },
            text = { Text("删除后聊天记录一并清除，无法恢复。") },
            confirmButton = {
                TextButton(onClick = { deleteConfirm = null; dissolving = id }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun HqSessionRow(s: ConversationSummary, onClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = HqSpace.Page, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PebbleAvatar(s.title, Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.title, style = HqType.RowTitle, color = Hq.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(timeLabel(s.updatedAt), style = HqType.Caption, color = Hq.ink3)
            }
            Spacer(Modifier.height(2.dp))
            Text("点击打开", style = HqType.Caption, color = Hq.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Icon(MikaIcons.More, null, tint = Hq.ink2, modifier = Modifier.size(16.dp).clickable { onDelete() })
    }
}

private fun timeLabel(t: Long): String =
    if (t <= 0) "" else android.text.format.DateUtils.getRelativeTimeSpanString(t).toString()

/** Jobs 屏：agent 活动流 = 运行中任务 + 运行历史（Muse 对齐）。 */
@Composable
fun HqJobsScreen(
    jobs: List<TaskRun> = emptyList(),
    runs: List<RunSummary> = emptyList(),
    onOpenJob: (TaskRun) -> Unit = {},
    onOpenRun: (RunSummary) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Hq.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HqSpace.Page),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("任务", style = HqType.ScreenTitle, color = Hq.ink)
                Text("agent 活动流", style = HqType.Caption, color = Hq.ink2)
            }
            Row(
                modifier = Modifier
                    .clip(HqShape.Capsule)
                    .background(Hq.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Icon(MikaIcons.ListPlus, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
                Icon(MikaIcons.Search, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        if (jobs.isEmpty() && runs.isEmpty()) {
            Text("还没有任务——去对话里跟 Miga 聊聊吧", style = HqType.Caption, color = Hq.ink3, modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 24.dp))
        } else {
            // 运行中任务置顶
            jobs.forEach { job ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenJob(job) }
                        .padding(horizontal = HqSpace.Page, vertical = 3.dp)
                        .clip(HqShape.Card)
                        .background(Hq.surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(MikaIcons.Clock, null, tint = Hq.accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(job.goal, style = HqType.RowTitle, color = Hq.ink)
                        Text("运行中", style = HqType.Caption, color = Hq.ok)
                    }
                    Text("进行中", style = HqType.Caption, color = Hq.ink3)
                }
                Spacer(Modifier.height(6.dp))
            }
            if (jobs.isNotEmpty() && runs.isNotEmpty()) {
                Text("历史", style = HqType.RowTitle, color = Hq.ink2, modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 8.dp))
            }
            // 运行历史（agent 活动流主体）
            runs.forEach { run ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRun(run) }
                        .padding(horizontal = HqSpace.Page, vertical = 3.dp)
                        .clip(HqShape.Card)
                        .background(Hq.surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if (run.outcome == "ok") Hq.ok else Hq.warn))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(run.goal, style = HqType.RowTitle, color = Hq.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(relTime(run.startedAtMs), style = HqType.Caption, color = Hq.ink2)
                    }
                    Text(run.outcome ?: "完成", style = HqType.Caption, color = Hq.ink3)
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/** Kanban 屏：Miga 运行中任务卡片。 */
@Composable
fun HqKanbanScreen(tasks: List<TaskRun> = emptyList(), onOpenTask: (TaskRun) -> Unit = {}, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Hq.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HqSpace.Page),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("看板", style = HqType.ScreenTitle, color = Hq.ink)
                Text("Miga 任务", style = HqType.Caption, color = Hq.ink)
            }
            Icon(MikaIcons.ListPlus, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Icon(MikaIcons.Info, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(16.dp))
        if (tasks.isEmpty()) {
            Text("没有运行中的任务", style = HqType.Caption, color = Hq.ink3, modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 24.dp))
        } else {
            tasks.forEach { task ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTask(task) }
                        .padding(horizontal = HqSpace.Page, vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Hq.surface)
                        .padding(16.dp),
                ) {
                    Text(task.goal, style = HqType.RowTitle, color = Hq.ink)
                    Spacer(Modifier.height(4.dp))
                    Text("运行中", style = HqType.Caption, color = Hq.ink2)
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

/** 动态屏：Muse 信息茧房——agent 按你关注的社交/信源生成的分组帖子流。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HqFeedScreen(
    posts: List<FeedPost> = emptyList(),
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val today = remember { java.text.SimpleDateFormat("M月d日", java.util.Locale.getDefault()).format(java.util.Date()) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Hq.bg)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = HqSpace.Page),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("动态", style = HqType.ScreenTitle, color = Hq.ink)
                Text("你关注的 · $today", style = HqType.Caption, color = Hq.ink2)
            }
            Row(
                modifier = Modifier
                    .clip(HqShape.Capsule)
                    .background(Hq.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Icon(MikaIcons.ListPlus, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
                Icon(MikaIcons.Search, null, tint = Hq.ink, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        if (posts.isEmpty()) {
            Text("还没有动态——agent 按你的关注生成后会出现在这里", style = HqType.Caption, color = Hq.ink3, modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 24.dp))
        } else {
            posts.forEach { post ->
                // butler（短信管家）来源 tag，默认折叠提示
                if (post.source == "butler") {
                    Row(
                        modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp)).background(Color(0x33FF9F0A)).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Hq.warn))
                        Spacer(Modifier.width(6.dp))
                        Text("管家提醒", style = HqType.Caption, color = Hq.warn)
                    }
                }
                post.groups.forEach { group ->
                    // 分组悬浮小标签
                    Text(
                        group.h,
                        style = HqType.Caption,
                        color = Hq.ink2,
                        modifier = Modifier.padding(horizontal = HqSpace.Page, vertical = 8.dp)
                            .clip(RoundedCornerShape(6.dp)).background(Hq.surface2).padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    group.items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = HqSpace.Page, vertical = 4.dp)
                                .clip(HqShape.Card)
                                .background(Hq.surface)
                                .padding(16.dp),
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(item.e, style = HqType.RowTitle)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.t, style = HqType.RowTitle, color = Hq.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (item.b.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(item.b, style = HqType.Body, color = Hq.ink2, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            val link = item.s?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
                            if (link != null) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onOpenUrl(link) }
                                        .background(Hq.surface2).padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("🔗", style = HqType.Caption)
                                    Spacer(Modifier.width(6.dp))
                                    Text(link, style = HqType.Caption, color = Hq.accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

private fun relTime(ms: Long): String {
    val now = System.currentTimeMillis()
    val d = now - ms
    if (d < 60_000) return "刚刚"
    if (d < 3_600_000) return "${d / 60_000} 分钟前"
    if (d < 86_400_000) return "${d / 3_600_000} 小时前"
    return "${d / 86_400_000} 天前"
}
