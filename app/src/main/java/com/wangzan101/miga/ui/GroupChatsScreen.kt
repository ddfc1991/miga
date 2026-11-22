package com.wangzan101.miga.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wangzan101.miga.ui.components.GroupCard
import com.wangzan101.miga.ui.components.GroupLabel
import com.wangzan101.miga.ui.components.ListRow
import com.wangzan101.miga.ui.components.PageScaffold
import com.wangzan101.miga.ui.components.RowChevron
import com.wangzan101.miga.ui.theme.MikaIcons
import com.wangzan101.miga.ui.theme.MikaTheme
import com.wangzan101.miga.ui.theme.MikaType

/** 群聊（占位：结构 + 空态，完整 GroupChat 编排逻辑后续接）。 */
@Composable
fun GroupChatsScreen(onBack: () -> Unit, onNew: () -> Unit = {}) {
    val c = MikaTheme.colors
    PageScaffold("群聊", onBack) {
        GroupLabel("群聊")
        GroupCard {
            ListRow(MikaIcons.Chats, "还没有群聊", "点「新建群聊」创建第一个（多 bot 协作）", first = true, onClick = onNew) {
                RowChevron()
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "完整群聊逻辑（多 bot 编排/LLM 后端）后续接入；当前为结构占位。",
            style = MikaType.Caption, color = c.ink.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
