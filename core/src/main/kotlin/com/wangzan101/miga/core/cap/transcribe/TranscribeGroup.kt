package com.wangzan101.miga.core.cap.transcribe

import com.wangzan101.miga.core.tool.AgentTool

internal object TranscribeGroup {
    val tools: List<AgentTool> = listOf(AudioTranscribe)
}
