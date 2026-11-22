package com.wangzan101.miga.groupchat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GroupChatStoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun store(dir: File = tmp.root) = GroupChatStore(dir)

    private fun members() = listOf(
        GroupMember("小柯", "讲证据"),
        GroupMember("老严", "讲落地"),
    )

    @Test fun createPersistsAcrossInstances() {
        val dir = tmp.newFolder("g")
        val a = store(dir)
        val chat = a.create("早会", members())
        assertEquals("早会", chat.title)
        assertEquals(2, chat.members.size)
        assertEquals(listOf(chat), a.chats.value)
        assertEquals(listOf(chat), store(dir).chats.value)
        assertTrue(File(dir, "groupchats.json").readText().contains("\"version\": 1"))
    }

    @Test fun blankTitleFallsBackToMemberNames() {
        val a = store(tmp.newFolder("g2"))
        assertEquals("小柯、老严", a.create("  ", members()).title)
    }

    @Test fun appendKeepsOrderAndSurvivesReload() {
        val dir = tmp.newFolder("g3")
        val a = store(dir)
        val chat = a.create("t", members())
        a.append(chat.id, GroupMessage(1L, "我", "在吗", fromBot = false))
        a.append(chat.id, GroupMessage(2L, "小柯", "在", fromBot = true))
        val reloaded = store(dir).get(chat.id)!!
        assertEquals(listOf("在吗", "在"), reloaded.messages.map { it.text })
        assertEquals(listOf("我", "小柯"), reloaded.messages.map { it.author })
        assertEquals(listOf(false, true), reloaded.messages.map { it.fromBot })
    }

    @Test fun appendToUnknownChatIsIgnored() {
        val a = store(tmp.newFolder("g4"))
        a.create("t", members())
        a.append("nope", GroupMessage(1L, "我", "hi"))
        assertEquals(1, a.chats.value.size)
        assertTrue(a.chats.value.single().messages.isEmpty())
    }

    @Test fun renameClearAndRemove() {
        val a = store(tmp.newFolder("g5"))
        val chat = a.create("t", members())
        a.append(chat.id, GroupMessage(1L, "我", "hi"))
        a.rename(chat.id, " 新名字 ")
        assertEquals("新名字", a.get(chat.id)?.title)
        a.rename(chat.id, "   ")
        assertEquals("新名字", a.get(chat.id)?.title)
        a.clear(chat.id)
        assertTrue(a.get(chat.id)!!.messages.isEmpty())
        a.remove(chat.id)
        assertNull(a.get(chat.id))
        assertTrue(a.chats.value.isEmpty())
    }

    @Test fun brokenFileIsMovedAsideAndStartsEmpty() {
        val dir = tmp.newFolder("g6")
        File(dir, "groupchats.json").writeText("{ not json")
        assertTrue(store(dir).chats.value.isEmpty())
        assertTrue(dir.listFiles()!!.any { it.name.startsWith("groupchats.json.bad-") })
    }

    @Test fun perChatConfigIdIsPersisted() {
        val dir = tmp.newFolder("g7")
        val a = store(dir)
        val chat = a.create("t", members(), configId = "cfg-1")
        assertEquals("cfg-1", store(dir).get(chat.id)?.configId)
    }
}
