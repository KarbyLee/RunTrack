package com.runtrack.data.repository

import com.runtrack.data.api.ShoeService
import com.runtrack.data.models.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ShoeRepositoryTest {
    private open class FakeShoeService : ShoeService {
        override suspend fun createShoe(request: ShoeCreateRequest): Response<Shoe> = error("unused")
        override suspend fun getShoes(): Response<List<Shoe>> = error("unused")
        override suspend fun getShoe(id: Int): Response<Shoe> = error("unused")
        override suspend fun updateShoe(id: Int, request: ShoeUpdateRequest): Response<Shoe> = error("unused")
        override suspend fun deleteShoe(id: Int): Response<Unit> = error("unused")
        override suspend fun getShoeStats(id: Int): Response<ShoeStats> = error("unused")
    }

    @Test
    fun successfulStatsAreReturned() = runTest {
        val stats = ShoeStats(12.5f, 3, "good")
        val repository = ShoeRepository(object : FakeShoeService() {
            override suspend fun getShoeStats(id: Int): Response<ShoeStats> {
                assertEquals(7, id)
                return Response.success(stats)
            }
        })
        assertEquals(stats, repository.getShoeStats(7).getOrThrow())
    }

    @Test
    fun emptyShoeResponseIsReportedAsFailure() = runTest {
        val repository = ShoeRepository(object : FakeShoeService() {
            override suspend fun getShoe(id: Int): Response<Shoe> = Response.success(null)
        })
        assertTrue(repository.getShoe(1).exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun httpErrorsPreserveStatusCode() = runTest {
        val repository = ShoeRepository(object : FakeShoeService() {
            override suspend fun getShoe(id: Int): Response<Shoe> =
                Response.error(404, "{}".toResponseBody())
        })
        assertEquals(404, (repository.getShoe(1).exceptionOrNull() as HttpException).code())
    }

    @Test
    fun deleteAcceptsNoContentResponse() = runTest {
        val repository = ShoeRepository(object : FakeShoeService() {
            override suspend fun deleteShoe(id: Int): Response<Unit> = Response.success<Unit>(204, null)
        })
        assertEquals(Unit, repository.deleteShoe(1).getOrThrow())
    }

    @Test
    fun coroutineCancellationIsPropagated() = runTest {
        val cancellation = CancellationException("cancelled")
        val repository = ShoeRepository(object : FakeShoeService() {
            override suspend fun getShoe(id: Int): Response<Shoe> = throw cancellation
        })
        try {
            repository.getShoe(1)
            fail("Cancellation must propagate to the caller")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }
}
