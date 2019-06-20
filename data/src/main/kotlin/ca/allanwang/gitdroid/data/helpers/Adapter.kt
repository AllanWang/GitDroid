package ca.allanwang.gitdroid.data.helpers

import ca.allanwang.gitdroid.data.GitObjectID
import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

internal object UriApolloAdapter : CustomTypeAdapter<URI> {
    override fun encode(value: URI): CustomTypeValue<String> = CustomTypeValue.GraphQLString(value.toString())
    override fun decode(value: CustomTypeValue<*>): URI = URI.create(value.value.toString())
}

internal object ObjectApolloAdapter : CustomTypeAdapter<Any> {
    override fun encode(value: Any): CustomTypeValue<String> = CustomTypeValue.GraphQLString(value.toString())
    override fun decode(value: CustomTypeValue<*>): Any = value.value
}

internal object DateApolloAdapter : CustomTypeAdapter<Date> {

    override fun encode(value: Date): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value)
    override fun decode(value: CustomTypeValue<*>): Date {
        return try {
            val date = value.value.toString()
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}

internal object DateTimeApolloAdapter : CustomTypeAdapter<Date> {

    override fun encode(value: Date): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value)
    override fun decode(value: CustomTypeValue<*>): Date {
        return try {
            val date = value.value.toString()
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}


internal object GitObjectIDApolloAdapter : CustomTypeAdapter<GitObjectID> {

    override fun encode(value: GitObjectID): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value.oid)
    override fun decode(value: CustomTypeValue<*>): GitObjectID {
        return GitObjectID(value.value.toString())
    }
}