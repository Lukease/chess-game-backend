package pl.lpawlowski.chessapp.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.lpawlowski.chessapp.exception.*

@ControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler
    fun handleExceptions(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val code = when(ex) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            is NotFound -> HttpStatus.NOT_FOUND
            is UserExistsException -> HttpStatus.CONFLICT
            is WrongCredentialsException -> HttpStatus.BAD_REQUEST
            is PieceNotFound -> HttpStatus.BAD_REQUEST
            is ForbiddenUser -> HttpStatus.FORBIDDEN
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        val errorMessage = ErrorMessageModel(
            code.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, code)
    }
}

data class ErrorMessageModel(
    var status: Int,
    var message: String?
)