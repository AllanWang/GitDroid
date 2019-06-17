package ca.allanwang.gitdroid.sql

import com.squareup.sqldelight.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun <T> dbContext(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block = block)

suspend fun <T : Any> Query<T>.awaitList(): List<T> = dbContext {
    executeAsList()
}

suspend fun <T : Any> Query<T>.await(): T = dbContext {
    executeAsOne()
}

suspend fun <T : Any> Query<T>.awaitOptional(): T? = dbContext {
    executeAsOneOrNull()
}