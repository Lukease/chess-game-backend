package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.GameMakeMoveRequest
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class PawnMovesTests : BasicIntegrationTest() {
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
    fun testMoveTwoMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "E2"
        val whiteFieldToId = "E4"
        val blackPawnId = "E7"
        val blackFieldToId = "E5"
        val whitePawnId2 = "A2"
        val blackPawnId2 = "H7"
        val whitePawnToId2 = "A4"
        val blackPawnToId2 = "H5"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN),
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequestWhite = GameMakeMoveRequest(whitePawnId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeMoveRequestWhite)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN),
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("e4")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("e2-e4")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackPawnId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN),
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("e5")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("e7-e5")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)

        val gameMakeMoveRequestWhite2 = GameMakeMoveRequest(whitePawnId2, whitePawnToId2, null)

        gameService.makeMove(userWhite, gameMakeMoveRequestWhite2)

        val gameAfterWhiteMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove2 = listOf(
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnToId2, PiecesNames.PAWN)
        )
        val fenAfterWhiteMove2 = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove2)

        assertThat(gameAfterWhiteMove2.get().history.split(",").last()).isEqualTo("a4")
        assertThat(gameAfterWhiteMove2.get().historyExtended.split(",").last()).isEqualTo("a2-a4")
        assertThat(gameAfterWhiteMove2.get().previousFen).isEqualTo(fenAfterBlackMove)
        assertThat(gameAfterWhiteMove2.get().currentFen).isEqualTo(fenAfterWhiteMove2)

        val gameMakeMoveRequestBlack2 = GameMakeMoveRequest(blackPawnId2, blackPawnToId2, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack2)

        val gameAfterBlackMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove2 = listOf(
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "B7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "D7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnToId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "G2", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnToId2, PiecesNames.PAWN)
        )
        val fenAfterBlackMove2 = fenConverter.convertPieceListToFen(piecesListAfterBlackMove2)

        assertThat(gameAfterBlackMove2.get().history.split(",").last()).isEqualTo("h5")
        assertThat(gameAfterBlackMove2.get().historyExtended.split(",").last()).isEqualTo("h7-h5")
        assertThat(gameAfterBlackMove2.get().previousFen).isEqualTo(fenAfterWhiteMove2)
        assertThat(gameAfterBlackMove2.get().currentFen).isEqualTo(fenAfterBlackMove2)
    }

    @Test
    fun testPromMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "E7"
        val whitePawnId2 = "A7"
        val whiteFieldToId = "E8"
        val whiteFieldToId2 = "A8"
        val blackPawnId = "E2"
        val blackPawnId2 = "D2"
        val blackFieldToId = "E1"
        val blackFieldToId2 = "C1"
        val whitePromName = "Queen"
        val whitePromName2 = "Bishop"
        val blackPromName = "Knight"
        val blackPromName2 = "Rook"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequest = GameMakeMoveRequest(whitePawnId, whiteFieldToId, whitePromName)

        gameService.makeMove(userWhite, gameMakeMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Queen(PlayerColor.WHITE, whiteFieldToId, PiecesNames.QUEEN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN)
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("e8=♕")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("e7-e8=Q")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackPawnId, blackFieldToId, blackPromName)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Knight(PlayerColor.BLACK, blackFieldToId, PiecesNames.KNIGHT),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Queen(PlayerColor.WHITE, whiteFieldToId, PiecesNames.QUEEN),
            Pawn(PlayerColor.WHITE, whitePawnId2, PiecesNames.PAWN)
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("e1=♘")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("e2-e1=N")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)

        val gameMakeMoveRequestWhite2 = GameMakeMoveRequest(whitePawnId2, whiteFieldToId2, whitePromName2)

        gameService.makeMove(userWhite, gameMakeMoveRequestWhite2)

        val gameAfterWhiteMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove2 = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Knight(PlayerColor.BLACK, blackFieldToId, PiecesNames.KNIGHT),
            Pawn(PlayerColor.BLACK, blackPawnId2, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Queen(PlayerColor.WHITE, whiteFieldToId, PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.BISHOP)
        )
        val fenAfterWhiteMove2 = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove2)

        assertThat(gameAfterWhiteMove2.get().history.split(",").last()).isEqualTo("a8=♗")
        assertThat(gameAfterWhiteMove2.get().historyExtended.split(",").last()).isEqualTo("a7-a8=B")
        assertThat(gameAfterWhiteMove2.get().previousFen).isEqualTo(fenAfterBlackMove)
        assertThat(gameAfterWhiteMove2.get().currentFen).isEqualTo(fenAfterWhiteMove2)

        val gameMakeMoveRequestBlack2 = GameMakeMoveRequest(blackPawnId2, blackFieldToId2, blackPromName2)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack2)

        val gameAfterBlackMove2 = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove2 = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Knight(PlayerColor.BLACK, blackFieldToId, PiecesNames.KNIGHT),
            Rook(PlayerColor.BLACK, blackFieldToId2, PiecesNames.ROOK),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Queen(PlayerColor.WHITE, whiteFieldToId, PiecesNames.QUEEN),
            Bishop(PlayerColor.WHITE, whiteFieldToId2, PiecesNames.BISHOP)
        )
        val fenAfterBlackMove2 = fenConverter.convertPieceListToFen(piecesListAfterBlackMove2)

        assertThat(gameAfterBlackMove2.get().history.split(",").last()).isEqualTo("xc1=♖")
        assertThat(gameAfterBlackMove2.get().historyExtended.split(",").last()).isEqualTo("d2xc1=R")
        assertThat(gameAfterBlackMove2.get().previousFen).isEqualTo(fenAfterWhiteMove2)
        assertThat(gameAfterBlackMove2.get().currentFen).isEqualTo(fenAfterBlackMove2)
    }

    @Test
    fun testPawnCapture() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "B3"
        val whiteFieldToId = "A4"
        val blackPawnId = "E5"
        val blackFieldToId = "F4"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, whiteFieldToId, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, blackFieldToId, PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequest = GameMakeMoveRequest(whitePawnId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, blackFieldToId, PiecesNames.PAWN)
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("xa4")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("b3xa4")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackPawnId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("xf4")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("e5xf4")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)
    }

    @Test
    fun testEnPassant() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "E2"
        val whiteFieldToId = "E4"
        val blackPawnId = "F4"
        val blackFieldToId = "E3"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequest = GameMakeMoveRequest(whitePawnId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("e4")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("e2-e4")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackPawnId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("exe3")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("f4xe3")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)
    }

    @Test
    fun testPawnNormalMove() {
        val userWhite = insertUser(testUserLogin1)
        val userBlack = insertUser(testUserLogin2)
        val whitePawnId = "E2"
        val whiteFieldToId = "E3"
        val blackPawnId = "F4"
        val blackFieldToId = "F3"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whitePawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fen = fenConverter.convertPieceListToFen(testsPiecesList)
        val game = createGame(userBlack, userWhite, 100, fen)
        val gameMakeMoveRequest = GameMakeMoveRequest(whitePawnId, whiteFieldToId, null)

        gameService.makeMove(userWhite, gameMakeMoveRequest)

        val gameAfterWhiteMove = gamesRepository.findById(game.id!!)
        val piecesListAfterWhiteMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackPawnId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fenAfterWhiteMove = fenConverter.convertPieceListToFen(piecesListAfterWhiteMove)

        assertThat(gameAfterWhiteMove.get().history).isEqualTo("e3")
        assertThat(gameAfterWhiteMove.get().historyExtended).isEqualTo("e2-e3")
        assertThat(gameAfterWhiteMove.get().previousFen).isEqualTo(game.currentFen)
        assertThat(gameAfterWhiteMove.get().currentFen).isEqualTo(fenAfterWhiteMove)

        val gameMakeMoveRequestBlack = GameMakeMoveRequest(blackPawnId, blackFieldToId, null)

        gameService.makeMove(userBlack, gameMakeMoveRequestBlack)

        val gameAfterBlackMove = gamesRepository.findById(game.id!!)
        val piecesListAfterBlackMove = listOf(
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Rook(PlayerColor.BLACK, "G8", PiecesNames.ROOK),
            Pawn(PlayerColor.BLACK, "G7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "H7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, blackFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "A5", PiecesNames.PAWN),
            Bishop(PlayerColor.WHITE, "H2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "H1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "G2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "G1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "C1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, whiteFieldToId, PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "B7", PiecesNames.PAWN)
        )
        val fenAfterBlackMove = fenConverter.convertPieceListToFen(piecesListAfterBlackMove)

        assertThat(gameAfterBlackMove.get().history.split(",").last()).isEqualTo("f3")
        assertThat(gameAfterBlackMove.get().historyExtended.split(",").last()).isEqualTo("f4-f3")
        assertThat(gameAfterBlackMove.get().previousFen).isEqualTo(fenAfterWhiteMove)
        assertThat(gameAfterBlackMove.get().currentFen).isEqualTo(fenAfterBlackMove)
    }
}