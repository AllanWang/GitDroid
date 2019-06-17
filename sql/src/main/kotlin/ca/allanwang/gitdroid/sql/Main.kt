package ca.allanwang.gitdroid.sql

import com.squareup.sqldelight.db.SqlDriver
import org.koin.dsl.module

/**
 * Based on https://github.com/square/sqldelight/blob/master/sample/common/src/jvmMain/kotlin/com/example/sqldelight/hockey/data/Db.kt
 */
class GitDb(private val driver: SqlDriver) {

    val db = Database(driver)

    internal fun clear() {
        driver.close()
    }

    companion object {
        fun module(driver: SqlDriver) = module {
            single { GitDb(driver).db }
        }
    }
}