package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.internal.PrivProps
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
                listOf(
                    module {
                        single<TokenSupplier> {
                            object : TokenSupplier {
                                override fun getToken(): String? = PrivProps.token
                            }
                        }
                    },
                    GitDroidData.module(null)
                )
            )
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    private suspend fun <T : Any> GitCall<T>.validate(): T = with(call(forceRefresh = true)) {
        assertTrue(errors().isEmpty(), "Found errors: ${errors()}")
        assertNotNull(data(), "Result is empty")
    }

    @Test
    fun me() {
        runBlocking {
            val r = gdd.getProfile("allanwang").validate()
//            r.user!!.contributionsCollection.fragments.shortContributions.contributionCalendar.months[0].firstDay
            assertEquals("AllanWang", r.login)
            println(r)
        }
    }

    @Test
    fun oauthGen() {
        val oauth = gdd.oauthUrl()
        assertNotNull(oauth)
    }
}