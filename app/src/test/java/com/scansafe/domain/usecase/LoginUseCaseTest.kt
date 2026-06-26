package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: LoginUseCase

    private val mockUser = User(id = "1", name = "Test User", email = "test@example.com")

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = LoginUseCase(authRepository)
    }

    @Test
    fun `returns error when email is blank`() = runTest {
        val result = useCase("", "password123")
        assertTrue(result is NetworkResult.Error)
        assertEquals("Email cannot be empty", (result as NetworkResult.Error).message)
    }

    @Test
    fun `returns error when password is blank`() = runTest {
        val result = useCase("test@example.com", "")
        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `returns error for invalid email format`() = runTest {
        val result = useCase("not-an-email", "password123")
        assertTrue(result is NetworkResult.Error)
        assertTrue((result as NetworkResult.Error).message.contains("Invalid"))
    }

    @Test
    fun `calls repository with trimmed email on valid input`() = runTest {
        coEvery { authRepository.login("test@example.com", "password123") } returns NetworkResult.Success(mockUser)
        val result = useCase("  test@example.com  ", "password123")
        assertTrue(result is NetworkResult.Success)
        coVerify { authRepository.login("test@example.com", "password123") }
    }

    @Test
    fun `returns error when repository fails`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns NetworkResult.Error("Invalid credentials", 401)
        val result = useCase("test@example.com", "wrongpassword")
        assertTrue(result is NetworkResult.Error)
        assertEquals(401, (result as NetworkResult.Error).code)
    }
}
