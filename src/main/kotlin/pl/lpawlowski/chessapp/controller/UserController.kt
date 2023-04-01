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
    fun saveUser(@RequestBody userDto: UserDto) = userService.saveUser(userDto)

    @GetMapping
    fun getUserByLogin(@RequestParam("login") login: String) = userService.getUserByLogin(login)


    @PostMapping("/log-in")
    fun logIn(@RequestBody userLogInRequest: UserLogInRequest) = userService.logIn(userLogInRequest)


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

        return userService.updateUserPassword(user, changePasswordRequest)
    }

    @GetMapping("/get-all")
    fun getAllUsers(): List<UserDto?>? {
        return userService.getAllUsers()
    }
}