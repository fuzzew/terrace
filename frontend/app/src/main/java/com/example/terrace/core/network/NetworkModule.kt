package com.example.terrace.core.network

import com.example.terrace.core.network.repository.UsageRepository
import com.example.terrace.core.network.repository.UsageRepositoryImpl
import dagger.Binds
import com.example.terrace.core.auth.SessionManager
import com.example.terrace.features.leaderboard.repository.LeaderboardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.38.29:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAppService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }
}



@Module
@InstallIn(SingletonComponent::class)
object LeaderboardModule {

    @Provides
    @Singleton
    fun provideLeaderboardRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): LeaderboardRepository {
        return LeaderboardRepository(apiService, sessionManager)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        usageRepositoryImpl: UsageRepositoryImpl
    ): UsageRepository
}


