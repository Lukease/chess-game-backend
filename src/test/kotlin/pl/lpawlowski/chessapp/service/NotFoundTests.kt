package pl.lpawlowski.chessapp.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.NotFound
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class NotFoundTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    private val fenConverter: FenConverter = FenConverter()

    @AfterEach
    fun cleanUpDatabase() {
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun testActiveGameNotFound() {
        assertThrows<NotFound> { gameService.getUserActiveGameAndReturnMoves( insertUser(testUserLogin1)) }
    }
    @Test
    fun testDrawOfferNotFound() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        createGame(userBlack, userWhite, 100)

        assertThrows<NotFound> { gameService.getDrawOffer(userBlack) }
    }

    @Test
    fun testKingsNotFound() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val testsPiecesList = listOf(
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "E3", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "F4", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A6", PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)

        createGame(userBlack, userWhite, 100, fen)
        assertThrows<NotFound> { gameService.getUserActiveGameAndReturnMoves(userBlack) }
    }
}
