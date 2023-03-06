package pl.lpawlowski.chessapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChessApp

fun main(args: Array<String>) {
	runApplication<ChessApp>(*args)
}
