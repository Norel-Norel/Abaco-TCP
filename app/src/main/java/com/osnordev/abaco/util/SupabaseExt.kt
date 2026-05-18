package com.osnordev.abaco.util

import io.github.jan.supabase.exceptions.RestException
import java.net.UnknownHostException

suspend fun <T> safeSupabaseCall(call: suspend () -> T): Result<T> {
    return try {
        Result.Success(call())
    } catch (e: UnknownHostException) {
        Result.Failure(e, "No hay conexión a internet. Verifica tu red.")
    } catch (e: RestException) {
        Result.Failure(e, "Error de Supabase: ${e.message}")
    } catch (e: Exception) {
        Result.Failure(e, e.message ?: "Ocurrió un error inesperado")
    }
}
