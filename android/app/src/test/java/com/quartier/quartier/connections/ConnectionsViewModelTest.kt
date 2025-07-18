package com.quartier.quartier.connections

import com.quartier.quartier.R
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.User
import com.quartier.quartier.mock_models.MockAuthRepo
import com.quartier.quartier.mock_models.MockConnectionsRepo
import com.quartier.quartier.mock_models.MockSocialsRepo
import com.quartier.quartier.mock_models.MockUserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionsViewModelTest {
    private lateinit var viewModel: ConnectionsViewModel

    private lateinit var userRepo: MockUserRepo
    private lateinit var connectionsRepo: MockConnectionsRepo
    private lateinit var socialsRepo: MockSocialsRepo
    private lateinit var authRepo: MockAuthRepo

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = MockUserRepo()
        connectionsRepo = MockConnectionsRepo()
        socialsRepo = MockSocialsRepo()
        authRepo = MockAuthRepo()
        viewModel = ConnectionsViewModel(userRepo, connectionsRepo, socialsRepo, authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshConnections() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        viewModel.refreshConnections()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(
            User("1", "name", "job", connectionStatus = "accepted"),
            User("2", "name", "job", connectionStatus = "accepted")
        ), viewModel.uiState.value.connections)

        assertEquals(listOf(
            User("3", "name", "job", connectionStatus = "pending"),
            User("4", "name", "job", connectionStatus = "pending")
        ), viewModel.uiState.value.requests)

        assertEquals(mapOf(
            Pair("2", Socials("2", "link"))
        ), viewModel.uiState.value.connectionsSocials)
    }

    @Test
    fun acceptConnection() = runTest {
        viewModel.acceptConnection(User("4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("4", "0", "accepted"))
    }

    @Test
    fun deleteConnection() = runTest {
        viewModel.declineConnection(User("4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(null, connectionsRepo.getConnectionWithUser("4"))
    }
}