package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.ForbiddenUser
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class NormalMovesTests : BasicIntegrationTest() {
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
    fun testRookMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whiteRookId = "E1"
        val whiteRookId2 = "A4"
        val blackRookId = "H5"
        val blackRookId2 = "F7"
        val whiteFieldToId = "E7"
        val whiteFieldToId2 = "H4"
        val blackFieldToId = "A5"
        val blackFieldToId2 = "F1"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, blackRookId, PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, blackRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, whiteRookId, PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, whiteRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeWhiteMoveRequest = GameMakeMoveRequest(whiteRookId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, blackRookId, PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, blackRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, whiteFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, whiteRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("♖e7")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("Re1-e7")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeBlackMoveRequest = GameMakeMoveRequest(blackRookId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeBlackMoveRequest)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, blackFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, blackRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, whiteFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, whiteRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("♖a5")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("Rh5-a5")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)

        val gameMakeMoveRequestWhite2 = GameMakeMoveRequest(whiteRookId2, whiteFieldToId2, null)

        gameService.makeMove(userWhite, gameMakeMoveRequestWhite2)

        val gameAfterWhiteMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove2 = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, blackFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, blackRookId2, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, whiteFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterWhiteMove2 = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove2)

        assertThat(gameAfterWhiteMove2.get().history.split(",").last()).isEqualTo("♖h4")
        assertThat(gameAfterWhiteMove2.get().historyExtended.split(",").last()).isEqualTo("Ra4-h4")
        assertThat(gameAfterWhiteMove2.get().previousFen).isEqualTo(fenAfterBlackMove)
        assertThat(gameAfterWhiteMove2.get().currentFen).isEqualTo(fenAfterWhiteMove2)

        val gameMakeMoveRequestBlack2 = GameMakeMoveRequest(blackRookId2, blackFieldToId2, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack2)

        val gameAfterBlackMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove2 = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, blackFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, blackFieldToId2, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, whiteFieldToId, PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterBlackMove2 = fenConverter.convertPieceListToFen(piecesListAfterBlackMove2)

        assertThat(gameAfterBlackMove2.get().history.split(",").last()).isEqualTo("♖f1")
        assertThat(gameAfterBlackMove2.get().historyExtended.split(",").last()).isEqualTo("Rf7-f1")
        assertThat(gameAfterBlackMove2.get().previousFen).isEqualTo(fenAfterWhiteMove2)
        assertThat(gameAfterBlackMove2.get().currentFen).isEqualTo(fenAfterBlackMove2)
    }

    @Test
    fun testBishopMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whiteBishopId = "E1"
        val whiteBishopId2 = "A4"
        val blackBishopId = "H5"
        val blackBishopId2 = "F7"
        val whiteFieldToId = "A5"
        val whiteFieldToId2 = "E8"
        val blackFieldToId = "E2"
        val blackFieldToId2 = "H5"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Bishop(PlayerColor.BLACK, blackBishopId, PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, blackBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteBishopId, PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, whiteBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeWhiteMoveRequest = GameMakeMoveRequest(whiteBishopId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Bishop(PlayerColor.BLACK, blackBishopId, PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, blackBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, whiteBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("♗a5")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("Be1-a5")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeBlackMoveRequest = GameMakeMoveRequest(blackBishopId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeBlackMoveRequest)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Bishop(PlayerColor.BLACK, blackFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, blackBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, whiteBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("♗e2")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("Bh5-e2")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)

        val gameMakeMoveRequestWhite2 = GameMakeMoveRequest(whiteBishopId2, whiteFieldToId2, null)

        gameService.makeMove(userWhite, gameMakeMoveRequestWhite2)

        val gameAfterWhiteMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove2 = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Bishop(PlayerColor.BLACK, blackFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, blackBishopId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterWhiteMove2 = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove2)

        assertThat(gameAfterWhiteMove2.get().history.split(",").last()).isEqualTo("♗e8")
        assertThat(gameAfterWhiteMove2.get().historyExtended.split(",").last()).isEqualTo("Ba4-e8")
        assertThat(gameAfterWhiteMove2.get().previousFen).isEqualTo(fenAfterBlackMove)
        assertThat(gameAfterWhiteMove2.get().currentFen).isEqualTo(fenAfterWhiteMove2)

        val gameMakeMoveRequestBlack2 = GameMakeMoveRequest(blackBishopId2, blackFieldToId2, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack2)

        val gameAfterBlackMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove2 = listOf(
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Bishop(PlayerColor.BLACK, blackFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, blackFieldToId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.BLACK, "A7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B8", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId, PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.BISHOP),
            Pawn(PlayerColor.WHITE, "A2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B1", PiecesNames.PAWN),
        )
        val fenAfterBlackMove2 = fenConverter.convertPieceListToFen(piecesListAfterBlackMove2)

        assertThat(gameAfterBlackMove2.get().history.split(",").last()).isEqualTo("♗h5")
        assertThat(gameAfterBlackMove2.get().historyExtended.split(",").last()).isEqualTo("Bf7-h5")
        assertThat(gameAfterBlackMove2.get().previousFen).isEqualTo(fenAfterWhiteMove2)
        assertThat(gameAfterBlackMove2.get().currentFen).isEqualTo(fenAfterBlackMove2)
    }

    @Test
    fun testWrongMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "E2"
        val wrongWhiteFieldToId = "E3"
        val wrongWhiteFieldToId2 = "E4"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
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
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "A6", PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)

        createGame(userBlack, userWhite, 100, fen)

        val gameMakeWrongMoveRequest = GameMakeMoveRequest(whitePawnId, wrongWhiteFieldToId, null)
        val gameMakeWrongMoveRequest2 = GameMakeMoveRequest(whitePawnId, wrongWhiteFieldToId2, null)

        assertThrows<WrongMove> { gameService.makeMove(userWhite, gameMakeWrongMoveRequest) }
        assertThrows<WrongMove> { gameService.makeMove(userWhite, gameMakeWrongMoveRequest2) }
    }

    @Test
    fun testForbiddenUserMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)

        createGame(userBlack, userWhite, 100)

        val gameMakeWrongMoveRequest = GameMakeMoveRequest("A7", "A6", null)

        assertThrows<ForbiddenUser> { gameService.makeMove(userBlack, gameMakeWrongMoveRequest) }
    }
}