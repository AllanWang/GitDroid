package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.internal.PrivProps
import com.apollographql.apollo.api.Response
import kotlinx.coroutines.runBlocking
import org.junit.Assume
import org.junit.BeforeClass
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.*

class GitDroidDataTest : KoinTest {

    val gdd: GitDroidData by inject()

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            Assume.assumeTrue(BuildConfig.GITHUB_CLIENT_ID.length > 10)
        }
    }

    @BeforeTest
    fun before() {
        startKoin {
            printLogger()
            modules(
                module {
                    single<TokenSupplier> {
                        object : TokenSupplier {
                            override fun getToken(): String? = PrivProps.token
                        }
                    }
                    single { GitDroidData() }
                }
            )
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    fun <T : Any> Response<T>.validate(): T {
        assertTrue(errors().isEmpty(), "Found errors: ${errors()}")
        return assertNotNull(data(), "Result is empty")
    }

    @Test
    fun me() {
        runBlocking {
            val r = gdd.getProfile("allanwang").validate()
            assertEquals("AllanWang", r.user?.login)
            println(r)
        }
    }

    @Test
    fun oauthGen() {
        val oauth = gdd.oauthUrl()
        assertNotNull(oauth)
    }
}