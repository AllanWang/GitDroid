package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.internal.PrivProps
import com.apollographql.apollo.api.Response
import github.GetProfileQuery
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.*

class GitDroidDataTest : KoinTest {

    @BeforeTest
    fun before() {
        startKoin {
            // printLogger(Level.DEBUG)
            modules(
                module {
                    single<TokenSupplier> {
                        object : TokenSupplier {
                            override fun getToken(): String? = PrivProps.token
                        }
                    }
                }
            )
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    fun <T: Any> Response<T>.validate(): T {
        assertTrue(errors().isEmpty(), "Found errors: ${errors()}")
        return assertNotNull(data(), "Result is empty")
    }

    @Test
    fun me() {
        runBlocking {
            val r = GitDroidData.query(GetProfileQuery("allanwang")).validate()
            assertEquals("AllanWang", r.user?.login)
            println(r)
        }
        println("HI")
    }

    @Test
    fun oauthGen() {
        val oauth = GitDroidData.oauthUrl()
        assertNotNull(oauth)
    }
}