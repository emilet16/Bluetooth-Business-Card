package com.quartier.quartier.connections

import com.quartier.quartier.R
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.User
import com.quartier.quartier.mock_models.MockAuthRepo
import com.quartier.quartier.mock_models.MockBleRepo
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
    private lateinit var bleRepo: MockBleRepo

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = MockUserRepo()
        connectionsRepo = MockConnectionsRepo()
        socialsRepo = MockSocialsRepo()
        authRepo = MockAuthRepo()
        bleRepo = MockBleRepo()
        viewModel = ConnectionsViewModel(userRepo, connectionsRepo, socialsRepo, authRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshConnections() = runTest { //Test if the refresh connections method works (big tests testing for normal & unexpected behavior all at once), check MockUserRepo for cases
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
            Pair("1", Socials("1", null)),
            Pair("2", Socials("2", "link"))
        ), viewModel.uiState.value.connectionsSocials)
    }

    @Test
    fun acceptConnection() = runTest { //Make sure a connection is accepted properly
        viewModel.acceptConnection(User("4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("4", "0", "accepted"))
    }

    @Test
    fun deleteConnection() = runTest { //Make sure a connection is deleted properly
        viewModel.declineConnection(User("4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(null, connectionsRepo.getConnectionWithUser("4"))
    }

    @Test
    fun validUserIds() = runTest { //All valid user ids
        backgroundScope.launch {
            viewModel.nearbyUsersState.collect()
        }
        bleRepo.setUserIds(listOf("1", "2", "3"))
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                User("1", "name", "job"),
                User("2", "name", "job"),
                User("3", "name", "job")
            ), viewModel.nearbyUsersState.value.users
        )
    }

    @Test
    fun emptyUserIds() = runTest { //No user ids
        backgroundScope.launch {
            viewModel.nearbyUsersState.collect()
        }
        bleRepo.setUserIds(emptyList())
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), viewModel.nearbyUsersState.value.users)
    }

    @Test
    fun invalidUserId() = runTest { //One user id doesn't exist
        backgroundScope.launch {
            viewModel.nearbyUsersState.collect()
        }
        bleRepo.setUserIds(listOf("-1", "1", "2")) //-1 doesn't exist
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                User("1", "name", "job"),
                User("2", "name", "job"),
            ), viewModel.nearbyUsersState.value.users
        )
    }

    @Test
    fun connectWithAlreadyConnected_requestedBySelf() = runTest { //Try connecting with someone already connected
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("1", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.already_connected, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithAlreadyConnected_requestedByOther() = runTest { //Try connecting with someone already connected
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("2", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.already_connected, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithPending_requestedBySelf() = runTest { //Try sending a request when one is pending
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "3", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.connection_request_wait, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithPending_requestedByOther() = runTest { //Accept the connection request
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("4", "0", "accepted"))
        assertEquals(R.string.connection_request_accepted, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectionWithSelf() = runTest { //Try connecting with self
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "0", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.error_request_self, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectionWithInvalidStatus() = runTest { //Try connecting with someone with an invalid_status connection (see MockConnectionsRepo)
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "5", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.unexpected_error, viewModel.uiState.value.userMessage)
    }

    @Test
    fun requestConnectionWithNewUser() = runTest { //Send a connection request
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("6", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("0", "6", "pending"))
        assertEquals(R.string.connection_request_success, viewModel.uiState.value.userMessage)
    }
}