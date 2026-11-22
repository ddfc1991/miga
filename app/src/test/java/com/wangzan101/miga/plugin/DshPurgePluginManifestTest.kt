package com.wangzan101.miga.plugin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** dsh-plugin 破甲插件:manifest 必须通过安装校验,且声明了 rewrite-prompt + promptTransform。 */
class DshPurgePluginManifestTest {
    private fun dshManifest(): PluginManifest {
        // 测试可执行目录(沙箱)。真机上以实际 <miga-root> 为准,这里指向上游 dsh-plugin 工程目录。
        val candidates = listOf(
            "dsh-plugin/manifest.json",
            "../dsh-plugin/manifest.json",
            "../../dsh-plugin/manifest.json",
            "/tmp/Miga/dsh-plugin/manifest.json",
        )
        val f = candidates.map { File(it) }.firstOrNull { it.isFile }
            ?: error("cannot find dsh-plugin/manifest.json (tried: $candidates)")
        return PluginManifest.parse(f.readText())
    }

    @Test fun dshManifestValidatesAndDeclaresPromptTransform() {
        val m = dshManifest()
        val problems = m.validate("0.1.45")
        assertTrue("validate problems: $problems", problems.isEmpty())
        // 必须声明改提示词权限和注入脚本,否则破甲不会生效
        assertTrue("rewrite-prompt permission missing", m.permissions.contains("rewrite-prompt"))
        assertEquals("scripts/rewrite_prompt.sh", m.promptTransform)
        assertTrue(m.skills.any { it.name == "dsh-purge" })
    }
}