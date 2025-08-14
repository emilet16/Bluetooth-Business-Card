package com.quartier.quartier

import com.quartier.quartier.auth.AuthManager
import com.quartier.quartier.auth.AuthManagerImpl
import com.quartier.quartier.auth.LoginViewModel
import com.quartier.quartier.database.AuthRepository
import com.quartier.quartier.database.AuthRepositoryImpl
import com.quartier.quartier.database.Connection
import com.quartier.quartier.database.ConnectionRequestResult
import com.quartier.quartier.database.ConnectionsDatabase
import com.quartier.quartier.database.ConnectionsRepository
import com.quartier.quartier.database.Socials
import com.quartier.quartier.database.SocialsDatabase
import com.quartier.quartier.database.SocialsRepository
import com.quartier.quartier.database.UserDatabase
import com.quartier.quartier.database.UserRepository
import com.quartier.quartier.mock_models.MockAuthManager
import io.github.jan.supabase.auth.auth
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URL
import kotlin.math.log
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SupabaseTests {
    private lateinit var authManager: AuthManager
    private lateinit var authRepo: AuthRepository
    private lateinit var userRepo: UserRepository
    private lateinit var connectRepo: ConnectionsRepository
    private lateinit var socialsRepo: SocialsRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authManager = AuthManagerImpl()
        authRepo = AuthRepositoryImpl()
        userRepo = UserDatabase(authRepo)
        connectRepo = ConnectionsDatabase(authRepo)
        socialsRepo = SocialsDatabase(authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun createUser() : String { //Helper function to create a user for tests
        val email = Uuid.random().toString()+"@example.com"
        authManager.emailSignUp("Test User", email, "Password123")
        authRepo.updateUserId(supabase.auth.currentUserOrNull()!!.id)
        return email
    }

    suspend fun loginAs(email: String) { //Helper function to login for tests
        authManager.emailSignIn(email, "Password123")
        authRepo.updateUserId(supabase.auth.currentUserOrNull()!!.id)
    }

    // -------- Auth Tests --------
    @Test fun signup_valid() = runTest { //Create a user and ensure it is signed up
        val email = createUser()
        testScheduler.advanceUntilIdle()

        assert(supabase.auth.currentUserOrNull()?.id != null)
    }

    @Test fun signIn_valid() = runTest { //Create a user, log out, then make sure login works.
        val email = createUser()
        supabase.auth.signOut()
        authManager.emailSignIn(email, "Password123")
        testScheduler.advanceUntilIdle()

        assert(supabase.auth.currentUserOrNull()?.id != null)
    }

    // -------- User Table Tests --------
    @Test fun getUser_profile() = runTest { //Get a user's profile
        createUser()
        val user = userRepo.getUser()

        assertEquals("Test User", user.name) //Default test user name
    }

    @Test fun getUsers() = runTest { //Create 2 users and get their profiles by id
        createUser()
        val user1 = userRepo.getUser()

        createUser()
        val user2 = userRepo.getUser()

        val users = userRepo.getUsers(listOf(user1.id, user2.id))
        testScheduler.advanceUntilIdle()

        assertContains(users, user1)
        assertContains(users, user2)
    }

    @Test fun updateUserProfile() = runTest { //Make sure a user's profile is edited properly
        createUser()

        userRepo.updateUser("New Name", "Amazing Job Title")
        val updatedUser = userRepo.getUser()
        testScheduler.advanceUntilIdle()

        assertEquals("New Name", updatedUser.name)
        assertEquals("Amazing Job Title", updatedUser.job)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test fun uploadUserImage() = runTest { //Make sure a pfp can be updated properly
        createUser()

        val imageUrl = "https://picsum.photos/400/400.jpg"
        val url = URL(imageUrl)
        val data = url.readBytes() //Read a random image and upload it

        userRepo.uploadPfp(Uuid.random().toString()+".jpg", data)
        val profile = userRepo.getUser()

        testScheduler.advanceUntilIdle()

        assert(profile.pfp_url != null)
    }

    // -------- Connections Table Tests --------
    @Test fun requestConnection_andGet() = runTest { //Create connections and get them all
        createUser()
        val id1 = userRepo.getUser().id

        createUser()
        val id2 = userRepo.getUser().id
        connectRepo.requestConnection(id1)

        val connection = connectRepo.getConnectionWithUser(id1)
        testScheduler.advanceUntilIdle()

        assertEquals(id2, connection?.requested_by)
        assertEquals(id1, connection?.requested_for)
        assertEquals("pending", connection?.status)
    }

    @Test fun tryConnect_withSelf() = runTest { //User attempts to connect with themselves
        createUser()
        val id = userRepo.getUser().id

        val result = connectRepo.requestConnection(id)
        testScheduler.advanceUntilIdle()

        assertEquals(ConnectionRequestResult.CannotConnectWithSelf, result)
    }

    @Test fun acceptConnection_andGet() = runTest { //Send a request, accept it, and get the connection
        val email = createUser()
        val id1 = userRepo.getUser().id

        createUser()
        val id2 = userRepo.getUser().id
        connectRepo.requestConnection(id1)

        loginAs(email)
        connectRepo.acceptConnection(id2)

        val connection = connectRepo.getConnectionWithUser(id2)
        testScheduler.advanceUntilIdle()

        assertEquals(id2, connection?.requested_by)
        assertEquals(id1, connection?.requested_for)
        assertEquals("accepted", connection?.status)
    }

    @Test fun deleteConnection() = runTest { //Make sure a connection is deleted properly
        val email = createUser()
        val id1 = userRepo.getUser().id

        createUser()
        val id2 = userRepo.getUser().id
        connectRepo.requestConnection(id1)

        loginAs(email)
        connectRepo.deleteConnection(id2)

        val connection = connectRepo.getConnectionWithUser(id2)
        testScheduler.advanceUntilIdle()

        assertEquals(null, connection)
    }

    @Test fun getAllConnections() = runTest { //Get all of a user's connections
        val email = createUser()
        val id1 = userRepo.getUser().id

        createUser()
        val id2 = userRepo.getUser().id
        connectRepo.requestConnection(id1)

        createUser()
        val id3 = userRepo.getUser().id
        connectRepo.requestConnection(id1)

        loginAs(email)
        val connections = connectRepo.getConnections()
        testScheduler.advanceUntilIdle()

        assertContains(connections, Connection(id2, id1, "pending"))
        assertContains(connections, Connection(id3, id1, "pending"))
    }

    // -------- Socials Table Tests --------
    @Test fun upsert_getSocials() = runTest { //Edit the socials and get the changes
        createUser()
        socialsRepo.upsertSocials("https://www.linkedin.com/in/user")

        val socials = socialsRepo.getUserSocials()
        assertEquals("https://www.linkedin.com/in/user", socials.linkedin_url)
    }

    @Test fun getConnectedSocials() = runTest { //Get all connections' socials
        val email = createUser()
        val id1 = userRepo.getUser().id

        createUser()
        val id2 = userRepo.getUser().id
        socialsRepo.upsertSocials("https://www.linkedin.com/in/user2")
        connectRepo.requestConnection(id1)

        createUser()
        val id3 = userRepo.getUser().id
        socialsRepo.upsertSocials("https://www.linkedin.com/in/user3")
        connectRepo.requestConnection(id1)

        loginAs(email)
        connectRepo.acceptConnection(id2)
        connectRepo.acceptConnection(id3)

        val socials = socialsRepo.getUserSocialsList()
        testScheduler.advanceUntilIdle()

        assertContains(socials, Socials(id2, "https://www.linkedin.com/in/user2"))
        assertContains(socials, Socials(id3, "https://www.linkedin.com/in/user3"))
    }
}