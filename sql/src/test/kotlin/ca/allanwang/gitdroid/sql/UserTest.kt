package ca.allanwang.gitdroid.sql

import ca.allanwang.gitdroid.sql.internal.DbBaseTest
import github.sql.UserQueries
import kotlin.test.Test
import kotlin.test.assertEquals


class UserTest : DbBaseTest() {

    private val query: UserQueries
        get() = gitDb.userQueries

    private fun insert(i: Int, name: String = "name$i") {
        query.insert("id$i", name, "login$i", "email$i", "avatar$i", "token$i")
    }

    @Test
    fun addAndGet() {
        insert(1)
        insert(2)
        val result = query.selectAll().executeAsList()
        assertEquals(2, result.size, "Did not find 2 items")
    }

    @Test
    fun insertConflict() {
        insert(1)
        val user1 = query.select("token1").executeAsOne()
        assertEquals("name1", user1.name)
        insert(1, name = "name2")
        val user2 = query.select("token1").executeAsOne()
        assertEquals("name2", user2.name)
        val result = query.selectAll().executeAsList()
        assertEquals(listOf(user2), result, "Result mismatch")
    }
}