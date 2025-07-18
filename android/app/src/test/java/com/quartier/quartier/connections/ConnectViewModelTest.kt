package com.quartier.quartier.connections

import com.quartier.quartier.R
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.User
import com.quartier.quartier.mock_models.MockBleRepo
import com.quartier.quartier.mock_models.MockConnectionsRepo
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
class ConnectViewModelTest {
    private lateinit var viewModel: ConnectViewModel
    private lateinit var connectionsRepo: MockConnectionsRepo
    private lateinit var bleRepo: MockBleRepo
    private lateinit var userRepo: MockUserRepo
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = MockUserRepo()
        connectionsRepo = MockConnectionsRepo()
        bleRepo = MockBleRepo()
        viewModel = ConnectViewModel(userRepository = userRepo, connectionsRepository = connectionsRepo, bleRepository = bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validUserIds() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        bleRepo.setUserIds(listOf("1", "2", "3"))
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                User("1", "name", "job"),
                User("2", "name", "job"),
                User("3", "name", "job")
            ), viewModel.uiState.value.users
        )
    }

    @Test
    fun emptyUserIds() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        bleRepo.setUserIds(emptyList())
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList<User>(), viewModel.uiState.value.users)
    }

    @Test
    fun invalidUserId() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }
        bleRepo.setUserIds(listOf("-1", "1", "2")) //-1 doesn't exist
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                User("1", "name", "job"),
                User("2", "name", "job"),
            ), viewModel.uiState.value.users
        )
    }

    @Test
    fun connectWithAlreadyConnected_requestedBySelf() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("1", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.already_connected, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithAlreadyConnected_requestedByOther() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("2", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.already_connected, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithPending_requestedBySelf() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "3", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.connection_request_wait, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectWithPending_requestedByOther() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "4", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("4", "0", "accepted"))
        assertEquals(R.string.connection_request_accepted, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectionWithSelf() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "0", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.error_request_self, viewModel.uiState.value.userMessage)
    }

    @Test
    fun connectionWithInvalidStatus() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User(id = "5", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertEquals(R.string.unexpected_error, viewModel.uiState.value.userMessage)
    }

    @Test
    fun requestConnectionWithNewUser() = runTest {
        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        viewModel.connectWith(User("6", "name", "job"))
        testScheduler.advanceUntilIdle()

        assertContains(connectionsRepo.getConnections(), Connection("0", "6", "pending"))
        assertEquals(R.string.connection_request_success, viewModel.uiState.value.userMessage)
    }
}