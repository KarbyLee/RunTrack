package com.runtrack.data.repository

import com.runtrack.data.api.ShoeService
import com.runtrack.data.models.Shoe
import com.runtrack.data.models.ShoeStats
import com.runtrack.data.models.ShoeUpdateRequest
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class ShoeRepository @Inject constructor(private val shoeService: ShoeService) {
    suspend fun getShoe(id: Int): Result<Shoe> = request { shoeService.getShoe(id) }

    suspend fun updateShoe(id: Int, request: ShoeUpdateRequest): Result<Shoe> =
        request { shoeService.updateShoe(id, request) }

    suspend fun getShoeStats(id: Int): Result<ShoeStats> = request { shoeService.getShoeStats(id) }

    suspend fun deleteShoe(id: Int): Result<Unit> = try {
        val response = shoeService.deleteShoe(id)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(HttpException(response))
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    private suspend fun <T : Any> request(call: suspend () -> Response<T>): Result<T> = try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("서버 응답이 비어 있습니다."))
        } else {
            Result.failure(HttpException(response))
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }
}
