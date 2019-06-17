package ca.allanwang.gitdroid.data

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import github.GetProfileQuery
import github.MeQuery

interface GitGraphQl {
    suspend fun <D : Operation.Data, T, V : Operation.Variables>
            query(query: com.apollographql.apollo.api.Query<D, T, V>): Response<T>

    suspend fun me() = query(MeQuery())

    suspend fun getProfile(login: String) = query(GetProfileQuery(login))
}