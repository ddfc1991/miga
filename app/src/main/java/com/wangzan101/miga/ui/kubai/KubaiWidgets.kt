package com.wangzan101.miga.ui.kubai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 双色面性图标(Kubai 2.0):主体用 [base] 浅色,点缀用 [accent] 主色。
 * viewport 为 16×16,由 icons-duotone/*.json 转换而来。
 */
@Composable
fun KubaiIcon(
    icon: KubaiIconDef,
    base: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier.size(size)) {
        val scale = this.size.minDimension / 16f
        val parser = PathParser()
        icon.layers.forEach { l ->
            val path = parser.parsePathString(l.d).toPath()
            val c = if (l.tone == "f") accent else base
            if (l.sw > 0f) {
                drawPath(path, c, style = Stroke(l.sw * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))
            } else {
                drawPath(path, c)
            }
        }
    }
}

/**
 * 完整空状态(技巧 022):图标 + 标题 + 说明 + 至多一个主操作 + 至多一个次要操作。
 * 用于首次使用 / 无搜索结果 / 全部处理完 / 加载失败 / 无权限等场景。
 */
@Composable
fun EmptyState(
    icon: KubaiIconDef,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    secondary: String? = null,
    onSecondary: (() -> Unit)? = null,
    iconBase: Color = Color.White.copy(alpha = 0.10f),
    iconAccent: Color = Color(0xFF6BB6FF),
    titleColor: Color = Color.White,
    descColor: Color = Color.White.copy(alpha = 0.55f),
    actionColor: Color = Color(0xFF4A9DFF),
) {
    Column(
        modifier = modifier.fillMaxWidth().widthIn(max = 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KubaiIcon(icon, iconBase, iconAccent, size = 44.dp)
        Spacer(Modifier.height(14.dp))
        Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            description,
            color = descColor,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (action != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onAction) { Text(action, color = actionColor, fontSize = 13.sp) }
        }
        if (secondary != null && onSecondary != null) {
            Spacer(Modifier.height(2.dp))
            TextButton(onClick = onSecondary) { Text(secondary, color = descColor, fontSize = 12.sp) }
        }
    }
}
