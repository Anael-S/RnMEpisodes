package com.anael.rickandmorty.data.remote

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RnMApiRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: RnMApiService
    private lateinit var dataSource: RnMApiRemoteDataSource

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        service = Retrofit.Builder()
            .baseUrl(server.url("/")) // point retrofit to mock server
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RnMApiService::class.java)

        dataSource = RetrofitRnMApiRemoteDataSource(service)

        // Simple dispatcher routing files by path
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path!!.startsWith("/episode?page=1") ->
                        okJson("api/episodes_page1.json")
                    request.path == "/episode/1" ->
                        okJson("api/episode_1.json")
                    request.path == "/character/1" ->
                        okJson("api/character_1.json")
                    request.path == "/character/1,2" ->
                        okJson("api/characters_1_2.json")
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getEpisodesPage returns and parses page 1`() = runTest {
        val dto = dataSource.getEpisodesPage(page = 1)

        assertThat(dto.info.pages).isGreaterThan(0)
        assertThat(dto.results).hasSize(2)
        assertThat(dto.results.first().id).isEqualTo(1)
        assertThat(dto.results.first().name).isEqualTo("Pilot")
    }

    @Test
    fun `getEpisodeById returns single episode`() = runTest {
        val ep = dataSource.getEpisodeById(id = "1")
        assertThat(ep.id).isEqualTo(1)
        assertThat(ep.episode).isEqualTo("S01E01")
        assertThat(ep.name).isEqualTo("Pilot")
    }

    @Test
    fun `getCharacterById returns single character`() = runTest {
        val c = dataSource.getCharacterById(id = "1")
        assertThat(c.id).isEqualTo(1)
        assertThat(c.name).contains("Rick")
        assertThat(c.status).isEqualTo("Alive")
    }

    @Test
    fun `getCharactersByIds returns a list`() = runTest {
        val list = dataSource.getCharactersByIds(idsCsv = "1,2")
        assertThat(list).hasSize(2)
        assertThat(list.map { it.id }).containsExactly(1, 2).inOrder()
    }

    @Test(expected = retrofit2.HttpException::class)
    fun `non-matching path gives 404 which surfaces as HttpException`() = runTest {
        // For an unknown path; change dispatcher or call with weird id
        dataSource.getEpisodeById(id = "9999") // dispatcher returns 404
    }

    // ---- helpers ----

    private fun okJson(resourcePath: String): MockResponse {
        val cl = this::class.java.classLoader
        val stream = checkNotNull(cl?.getResourceAsStream(resourcePath)) {
            "Missing test fixture: $resourcePath. " +
                    "Place it under app/src/test/resources/$resourcePath"
        }
        val body = stream.source().buffer().readUtf8()
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
    }

}
