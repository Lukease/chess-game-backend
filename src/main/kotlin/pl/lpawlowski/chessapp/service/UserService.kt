package pl.lpawlowski.chessapp.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.ChangePasswordRequest
import pl.lpawlowski.chessapp.model.user.UserDto
import pl.lpawlowski.chessapp.model.user.UserLogInRequest
import pl.lpawlowski.chessapp.repositories.UsersRepository
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    private val usersRepository: UsersRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun saveUser(userDto: UserDto): Long {
        if (userDto.email == null || userDto.password == null) {
            throw WrongCredentialsException("Password or email must not be null!")
        }

        if (usersRepository.existsByLogin(userDto.login)) {
            throw UserExistsException("User " + userDto.login + " exist")
        }

        val user: User = User().apply {
            login = userDto.login
            email = userDto.email
            password = userDto.password
        }

        val encodedPassword = passwordEncoder.encode(user.password)

        user.password = encodedPassword

        val save: User = usersRepository.save(user)

        return save.id!!
    }

    fun getUserByLogin(login: String): UserDto {
        return UserDto.fromDomain(
            usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") })
    }

    @Transactional
    fun logIn(userLogInRequest: UserLogInRequest): UserDto {
        val user: User = findUserByLogin(userLogInRequest.login)
        return if (passwordEncoder.matches(userLogInRequest.password, user.password)) {
            val token = "${LocalDateTime.now()}${user.password}${user.login}${Random().nextLong()}"
            val localDateTimePlusHour = LocalDateTime.now().plusHours(1)

            user.activeToken = token
            user.validUtil = localDateTimePlusHour
            UserDto.fromDomain(user)
        } else {
            throw WrongCredentialsException("Incorrect login or password!")
        }
    }

    @Transactional
    fun updateUserLogin(login: String, newLogin: String): UserDto {
        if (usersRepository.existsByLogin(newLogin)) {
            throw UserExistsException("User $newLogin exist")
        }
        val user: User = usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") }

        user.login = newLogin

        return UserDto.fromDomain(user)
    }

    @Transactional
    fun updateUserEmail(login: String, newEmail: String): UserDto {
        val user: User = usersRepository.findByLogin(login).orElseThrow { RuntimeException("User not found!") }

        user.email = newEmail

        return UserDto.fromDomain(user)
    }

    @Transactional
    fun updateUserPassword(user: User, changePasswordRequest: ChangePasswordRequest): UserDto {
        return if (passwordEncoder.matches(changePasswordRequest.oldPassword, user.password)) {
            val encodedPassword = passwordEncoder.encode(changePasswordRequest.password)

            user.password = encodedPassword

            UserDto.fromDomain(user)
        } else {
            throw WrongCredentialsException("Incorrect old password!")
        }
    }

    fun getAllUsers(): List<UserDto?>? {
        return usersRepository.findAll().map { UserDto.fromDomain(it) }
    }

    private fun findUserByLogin(login: String): User {
        return usersRepository.findByLogin(login)
            .orElseThrow { WrongCredentialsException("Incorrect login or password") }
    }

    fun findUserByAuthorizationToken(activeToken: String): User {
        val user: Optional<User> = usersRepository.findByActiveToken(activeToken)
        return if (user.isPresent && user.get().validUtil!!.isAfter(LocalDateTime.now())) {
            user.get()
        } else {
            throw WrongCredentialsException("No active Token found")
        }
    }
}