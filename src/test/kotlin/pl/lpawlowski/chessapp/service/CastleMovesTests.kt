package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class CastleMovesTests : BasicIntegrationTest() {
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
    fun testSmallCastleMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whiteKingId = "E1"
        val blackKingId = "E8"
        val whiteFieldToId = "G1"
        val blackFieldToId = "G8"
        val newWhiteRookId = "F1"
        val newBlackRookId = "F8"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, blackKingId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, "H8", PiecesNames.ROOK),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteKingId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "H2", PiecesNames.PAWN),
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeWhiteMoveRequest = GameMakeMoveRequest(whiteKingId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeWhiteMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, blackKingId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, "H8", PiecesNames.ROOK),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteFieldToId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, newWhiteRookId, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "H2", PiecesNames.PAWN),
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("O-O")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("Ke1-g1")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeBlackMoveRequest = GameMakeMoveRequest(blackKingId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeBlackMoveRequest)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, blackFieldToId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, newBlackRookId, PiecesNames.ROOK),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteFieldToId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, newWhiteRookId, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "H2", PiecesNames.PAWN),
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history).isEqualTo("O-O,O-O")
        assertThat(gameAfterBlackMove.get().historyExtended).isEqualTo("Ke1-g1,Ke8-g8")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)
    }

    @Test
    fun testBigCastleMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whiteKingId = "E1"
        val whiteFieldToId = "C1"
        val blackKingId = "E8"
        val blackFieldToId = "C8"
        val newWhiteRookId = "D1"
        val newBlackRookId = "D8"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, blackKingId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "C7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteKingId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequest = GameMakeMoveRequest(whiteKingId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, blackKingId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "C7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteFieldToId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, newWhiteRookId, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN)
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("O-O-O")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("Ke1-c1")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackKingId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, blackFieldToId, PiecesNames.KING),
            Rook(PlayerColor.BLACK, newBlackRookId, PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "C7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, whiteFieldToId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, newWhiteRookId, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN)
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",")[1]).isEqualTo("O-O-O")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",")[1]).isEqualTo("Ke8-c8")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)
    }
}