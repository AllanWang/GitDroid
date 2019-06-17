package ca.allanwang.gitdroid.sql.internal

import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.Db
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class DbBaseTest {

    @BeforeTest
    fun before() {
        JdbcSqliteDriver().also {
            Database.Schema.create(it)
            Db.dbSetup(it)
        }
    }


    @AfterTest
    fun after() {
        Db.dbClear()
    }

}