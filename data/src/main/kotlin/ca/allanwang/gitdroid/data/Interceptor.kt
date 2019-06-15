package ca.allanwang.gitdroid.data

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Helper to reveal an [apply] method for interceptors.
 * Each chain will repackage the request and proceed
 */
abstract class BaseInterceptor : Interceptor {

    abstract fun apply(request: Request.Builder, originalChain: Interceptor.Chain)

    override fun intercept(chain: Interceptor.Chain): Response {
        val origRequest = chain.request()
        val request = origRequest.newBuilder()
        apply(request, chain)
        return chain.proceed(request.build())
    }
}

class AuthInterceptor(private val tag: String, private val tokenSupplier: TokenSupplier) : BaseInterceptor() {

    override fun apply(request: Request.Builder, originalChain: Interceptor.Chain) {
        val token = tokenSupplier.getToken()
        if (token?.isNotBlank() == true)
            request.addHeader("Authorization", "$tag $token")
    }
}