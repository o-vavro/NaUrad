package com.atlasstudio.naurad.di

import android.app.Application
import androidx.room.Room
import com.atlasstudio.naurad.BuildConfig
import com.atlasstudio.naurad.data.OfficeDatabase
import com.atlasstudio.naurad.net.service.ApiTalksService
import com.atlasstudio.naurad.net.service.EpsgService
import com.atlasstudio.naurad.net.service.RuianService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val apiTalksKey: String = "O6BwsGTZzTaekkB2gm6wX9f6zZlYLr6c5wuIutaB"

    @Singleton
    @Provides
    fun provideDatabase(
        app: Application,
//        callback: OfficeDatabase.Callback
    ) = Room.databaseBuilder(app, OfficeDatabase::class.java, "office_database")
        .fallbackToDestructiveMigration()
        //.addCallback(callback)
        .build()

    @Provides
    fun provideOfficeDao(db: OfficeDatabase) = db.officeDao()

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("x-api-key", apiTalksKey)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    } else {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("x-api-key", apiTalksKey)
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .build()
    }

    @Singleton
    @Provides
    @Named("EpsgRetrofit")
    fun provideEpsgRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://epsg.io/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideEpsgService(@Named("EpsgRetrofit") epsgRetrofit: Retrofit) = epsgRetrofit.create(EpsgService::class.java)

    @Singleton
    @Provides
    @Named("RuianRetrofit")
    fun provideRuianRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/tables/1/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideRuianService(@Named("RuianRetrofit") ruianRetrofit: Retrofit) = ruianRetrofit.create(RuianService::class.java)

    @Singleton
    @Provides
    @Named("ApiTalksRetrofit")
    fun provideApiTalksRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.apitalks.store/apitalks.com/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideApiTalksService(@Named("ApiTalksRetrofit") apiTalksRetrofit: Retrofit) = apiTalksRetrofit.create(ApiTalksService::class.java)

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope