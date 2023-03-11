package pl.lpawlowski.chessapp.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.UserExistsException
import pl.lpawlowski.chessapp.exception.WrongCredentialsException
import pl.lpawlowski.chessapp.model.user.*
import pl.lpawlowski.chessapp.service.UserService


@CrossOrigin(origins = ["http://localhost:3000/"])
@RestController
@RequestMapping(value = ["/users"])
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun saveUser(@RequestBody userDto: UserDto): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(userService.saveUser(userDto))
        } catch (e: UserExistsException) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        }
    }

    @GetMapping
    fun getUserByLogin(@RequestParam("login") login: String): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(userService.getUserByLogin(login))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("$login not found!")
        }
    }

    @PostMapping("/log-in")
    fun logIn(@RequestBody userLogInRequest: UserLogInRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(userService.logIn(userLogInRequest))
        } catch (e: WrongCredentialsException) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
        }
    }

    @PutMapping("/new-login")
    fun editUserLogin(
        @RequestBody changeLoginRequest: ChangeLoginRequest,
        @RequestHeader("Authorization") authorization: String
    ): UserDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return userService.updateUserLogin(user.login, changeLoginRequest.login)
    }

    @PutMapping("/new-email")
    fun editUserEmail(
        @RequestBody changeEmailRequest: ChangeEmailRequest,
        @RequestHeader("Authorization") authorization: String
    ): UserDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return userService.updateUserEmail(user.login, changeEmailRequest.email)
    }

    @PutMapping("/new-password")
    fun editUserPassword(
        @RequestBody changePasswordRequest: ChangePasswordRequest,
        @RequestHeader("Authorization") authorization: String
    ): UserDto {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return userService.updateUserPassword(user.login, changePasswordRequest.password)
    }

    @GetMapping("/get-all")
    fun getAllUsers(): List<UserDto?>? {
        return userService.getAllUsers()
    }
}