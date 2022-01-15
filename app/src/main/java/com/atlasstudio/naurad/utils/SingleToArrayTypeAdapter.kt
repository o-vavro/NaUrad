package com.atlasstudio.naurad.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.util.*


class SingleToArrayTypeAdapter internal constructor(
    private val delegateAdapter: TypeAdapter<List<Any>>,
    private val elementAdapter: TypeAdapter<Any>
) : TypeAdapter<List<Any?>?>() {
    override fun read(reader: JsonReader): List<Any> {
        return if (reader.peek() !== JsonToken.BEGIN_ARRAY) {
            Collections.singletonList(elementAdapter.read(reader))
        } else delegateAdapter.read(reader)
    }

    override fun write(out: JsonWriter, value: List<Any?>?) {
        if (value?.size == 1) {
            elementAdapter.write(out, value[0])
        } else {
            delegateAdapter.write(out, value as List<Any>)
        }
    }

    companion object {
        val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                if (type.rawType != MutableList::class.java) {
                    return null
                }
                val elementType =
                    (type.type as ParameterizedType).actualTypeArguments[0]
                val delegateAdapter = gson.getDelegateAdapter(this, type) as TypeAdapter<List<Any>>
                val elementAdapter =
                    gson.getAdapter(TypeToken.get(elementType)) as TypeAdapter<Any>
                return SingleToArrayTypeAdapter(delegateAdapter, elementAdapter) as TypeAdapter<T>
            }
        }
    }
}