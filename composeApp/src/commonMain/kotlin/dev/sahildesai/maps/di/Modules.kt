package dev.sahildesai.maps.di

import dev.sahildesai.maps.data.AddressSearchService
import dev.sahildesai.maps.data.IAddressSearchService
import dev.sahildesai.maps.domain.IPermissionUseCase
import dev.sahildesai.maps.domain.PermissionUseCase
import dev.sahildesai.maps.ui.MapSearchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal expect fun platformHttpEngine(): HttpClientEngine

val httpEngineModule = module {
    single<HttpClientEngine> { platformHttpEngine() }
}

val mapModule = module {
    // HTTP client with ContentNegotiation/json
    single {
        HttpClient(get<HttpClientEngine>()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
    // Geocoding service
    single<IAddressSearchService> {
        AddressSearchService(get())
    }
    // Permission Use case
    single <IPermissionUseCase> { PermissionUseCase() }
    // ViewModel factory
    factory {
        MapSearchViewModel()
    }
}

fun initKoin() = startKoin {
    modules(mapModule, httpEngineModule)
}
