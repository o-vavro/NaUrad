package com.atlasstudio.naurad.di

import android.app.Application
import androidx.room.Room
import com.atlasstudio.naurad.BuildConfig
import com.atlasstudio.naurad.data.OfficeDatabase
import com.atlasstudio.naurad.net.service.EpsgService
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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
            .build()
    } else {
        OkHttpClient
            .Builder()
            .build()
    }

    @Singleton
    @Provides
    fun provideEpsgRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://epsg.io/")
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideEpsgService(epsgRetrofit: Retrofit) = epsgRetrofit.create(EpsgService::class.java)

    @Singleton
    @Provides
    fun provideRuianRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/tables/1/")
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiTalksRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.apitalks.store/apitalks.com/")
        .client(okHttpClient)
        .build()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope