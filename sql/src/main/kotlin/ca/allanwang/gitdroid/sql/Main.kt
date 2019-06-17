package ca.allanwang.gitdroid.sql

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

/**
 * Based on https://github.com/square/sqldelight/blob/master/sample/common/src/jvmMain/kotlin/com/example/sqldelight/hockey/data/Db.kt
 */
inline val gitDb: Database
    get() = Db.instance

object Db {
    private var driverRef: SqlDriver? = null
    private var dbRef: Database? = null

    internal fun dbSetup(driver: SqlDriver) {
        val db = createQueryWrapper(driver)
        driverRef = driver
        dbRef = db
    }

    private fun createQueryWrapper(driver: SqlDriver): Database {
        return Database(driver)
    }

    internal fun dbClear() {
        driverRef!!.close()
        dbRef = null
        driverRef = null
    }

    fun initialize(context: Context, name: String? = null) {
        dbSetup(AndroidSqliteDriver(Database.Schema, context, name))
    }

    val instance: Database
        get() = dbRef!!
}