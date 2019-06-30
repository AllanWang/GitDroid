package ca.allanwang.gitdroid.data.gql

import github.GetProfileQuery
import github.MeQuery

 interface GitGraphqlProfileScreen : GitGraphQlBase {
    fun me(): GitCall<MeQuery.Data?> = query(MeQuery())

    fun getProfile(login: String): GitCall<GetProfileQuery.User?> =
        query(GetProfileQuery(login)) {
            it?.user
        }
}