package ca.allanwang.gitdroid.sql.internal

import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.GitDb
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class DbBaseTest {

    private lateinit var dbRef: GitDb
    protected val db: Database
        get() = dbRef.db

    @BeforeTest
    fun before() {
        JdbcSqliteDriver().also {
            Database.Schema.create(it)
            dbRef = GitDb(it)
        }
    }


    @AfterTest
    fun after() {
        dbRef.clear()
    }

}