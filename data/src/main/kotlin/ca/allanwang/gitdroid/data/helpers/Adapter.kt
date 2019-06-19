package ca.allanwang.gitdroid.data.helpers

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
            val date = value.value as String
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}

class DateWrapper(val date: Date)


internal object DateApolloAdapter2 : CustomTypeAdapter<DateWrapper> {

    override fun encode(value: DateWrapper): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value.date)
    override fun decode(value: CustomTypeValue<*>): DateWrapper {
        return DateWrapper(try {
            val date = value.value as String
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        })
    }
}

internal object DateTimeApolloAdapter : CustomTypeAdapter<Date> {

    override fun encode(value: Date): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value)
    override fun decode(value: CustomTypeValue<*>): Date {
        return try {
            val date = value.value as String
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}