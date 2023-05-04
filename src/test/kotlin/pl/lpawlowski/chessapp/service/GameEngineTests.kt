package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class GameEngineTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var gameEngine: GameEngine

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
    fun testKingIsChecked() {
        val playerColor = PlayerColor.BLACK
        val enemyColor = PlayerColor.WHITE
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D4", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "A7", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A3", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )
        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val isPlayerKingChecked = gameEngine.getTheKingIsChecked(playerColor, testsPiecesList, enemyPieces, "")
//        val isEnemyKingChecked = gameEngine.getTheKingIsChecked(enemyColor, testsPiecesList, "")

        assertTrue(isPlayerKingChecked)
//        assertFalse(isEnemyKingChecked)
    }

    @Test
    fun testDontCauseCheck() {
        val playerColor = PlayerColor.BLACK
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "E5", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "A7", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "B3", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, null)
                .plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        val dontCauseCheck =
            gameEngine.checkPieceIsCoveringKingAndFilterMoves(piecesWithCorrectMoves, enemyPieces, playerColor)
        val filteredRookMoves = dontCauseCheck.find { it.name == PiecesNames.ROOK && it.id == "A7" }!!.possibleMoves

        assertThat(filteredRookMoves.size).isEqualTo(6)
        assertTrue(filteredRookMoves.any { it.fieldId == "A6" })
        assertTrue(filteredRookMoves.any { it.fieldId == "A5" })
        assertTrue(filteredRookMoves.any { it.fieldId == "A4" })
        assertTrue(filteredRookMoves.any { it.fieldId == "A3" })
        assertTrue(filteredRookMoves.any { it.fieldId == "A2" })
        assertTrue(filteredRookMoves.any { it.fieldId == "A1" })
        assertFalse(filteredRookMoves.any { it.fieldId == "B7" })
    }

    @Test
    fun testKingCorrectMoves() {
        val playerColor = PlayerColor.BLACK
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "A1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "E5", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H7", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "A7", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "B3", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "D8", PiecesNames.KING),
        )
        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, null)
                .plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        val blackKingMoves = gameEngine.dontCauseCheck(piecesWithCorrectMoves, playerColor, enemyPieces)
            .find { it.name == PiecesNames.KING && it.id == "D8" }!!.possibleMoves

        assertThat(blackKingMoves.size).isEqualTo(0)
    }
}