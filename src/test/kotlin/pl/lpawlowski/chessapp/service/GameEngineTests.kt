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
import pl.lpawlowski.chessapp.game.engine.GameEngine
import pl.lpawlowski.chessapp.game.engine.MoveType
import pl.lpawlowski.chessapp.repositories.DrawOffersRepository
import pl.lpawlowski.chessapp.repositories.GamesRepository
import pl.lpawlowski.chessapp.web.pieces.*

class GameEngineTests : BasicIntegrationTest() {
    @Autowired
    lateinit var gameEngine: GameEngine

    @Autowired
    lateinit var gamesRepository: GamesRepository

    @Autowired
    lateinit var drawOffersRepository: DrawOffersRepository

    @AfterEach
    fun cleanUpDatabase() {
        userRepository.deleteAll()
        drawOffersRepository.deleteAll()
        gamesRepository.deleteAll()
    }

    @Test
    fun testConvertFenToPiecesList() {
        val testFen = "5k2/2Q5/P7/8/8/7P/PP3P1P/RN2KB1R"
        val pieces = gameEngine.convertFenToPiecesList(testFen)
        val blackPieces = pieces.filter { it.color == PlayerColor.BLACK }
        val whitePieces = pieces.filter { it.color == PlayerColor.WHITE }
        val piecesAtFirstRow = pieces.filter { it.id[1] == '1' }
        val whiteKing = pieces.find { it.name == PiecesNames.KING && it.color == PlayerColor.WHITE }
        val whitePawns = whitePieces.filter { it.name == PiecesNames.PAWN }
        val blackPawns = blackPieces.filter { it.name == PiecesNames.PAWN }

        assertThat(blackPieces.size).isEqualTo(1)
        assertThat(blackPieces[0].name).isEqualTo(PiecesNames.KING)
        assertThat(blackPieces[0].id).isEqualTo("F8")
        assertThat(whitePieces.size).isEqualTo(12)
        assertThat(whiteKing!!.id).isEqualTo("E1")
        assertThat(piecesAtFirstRow.size).isEqualTo(5)
        assertThat(whitePawns.size).isEqualTo(6)
        assertThat(blackPawns.size).isEqualTo(0)
    }

    @Test
    fun testConvertPieceListToFen() {
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B8", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A8", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C8", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D8", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "E8", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F8", PiecesNames.PAWN)
        )
        val pieceListToFen = gameEngine.convertPieceListToFen(testsPiecesList)
        val splitFen = pieceListToFen.split("/")
        val firstFenElement = splitFen[0]
        val restFenElement = splitFen.drop(1)

        assertThat(splitFen.size).isEqualTo(8)
        assertThat(firstFenElement.length).isEqualTo(7)
        assertTrue(firstFenElement.all { it.isUpperCase() || !it.isLetter() })
        assertThat(firstFenElement[0]).isEqualTo('K')
        assertThat(firstFenElement[1]).isEqualTo('B')
        assertThat(firstFenElement[2]).isEqualTo('N')
        assertThat(firstFenElement[3]).isEqualTo('Q')
        assertThat(firstFenElement[4]).isEqualTo('R')
        assertThat(firstFenElement[5]).isEqualTo('P')
        assertTrue(restFenElement.all { it.all { ch -> ch == '8' } })
    }

    @Test
    fun testSmallCastleMove() {
        val playerColor = PlayerColor.WHITE
        val kingId = "E1"
        val fieldToId = "G1"
        val testsPiecesList = listOf(
            King(PlayerColor.BLACK, "B8", PiecesNames.KING),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, kingId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, PlayerColor.WHITE, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            kingId,
            fieldToId,
            null,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.SMALL_CASTLE)
        assertThat(move.nameOfMove).isEqualTo(MoveType.SMALL_CASTLE.historyNotation)
        assertThat(move.promotedPiece).isEqualTo("")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(kingId)
    }

    @Test
    fun testBigCastleMove() {
        val playerColor = PlayerColor.WHITE
        val kingId = "E1"
        val fieldToId = "C1"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, kingId, PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, PlayerColor.WHITE, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            kingId,
            fieldToId,
            null,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.BIG_CASTLE)
        assertThat(move.nameOfMove).isEqualTo(MoveType.BIG_CASTLE.historyNotation)
        assertThat(move.promotedPiece).isEqualTo("")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(kingId)
    }

    @Test
    fun testMoveTwoMove() {
        val playerColor = PlayerColor.WHITE
        val pawnId = "E2"
        val fieldToId = "E4"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, pawnId, PiecesNames.PAWN),
            King(PlayerColor.BLACK, "E8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, PlayerColor.WHITE, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            pawnId,
            fieldToId,
            null,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.MOVE_TWO)
        assertThat(move.nameOfMove).isEqualTo("e4")
        assertThat(move.promotedPiece).isEqualTo("")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(pawnId)
    }

    @Test
    fun testPromMove() {
        val playerColor = PlayerColor.WHITE
        val pawnId = "E7"
        val fieldToId = "E8"
        val promotedPieceName = "Queen"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, pawnId, PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, PlayerColor.WHITE, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            pawnId,
            fieldToId,
            promotedPieceName,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.PROM)
        assertThat(move.nameOfMove).isEqualTo("e8=♕")
        assertThat(move.promotedPiece).isEqualTo("Queen")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(pawnId)
    }

    @Test
    fun testPawnCaptureMove() {
        val playerColor = PlayerColor.WHITE
        val pawnId = "E5"
        val fieldToId = "D6"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C2", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, fieldToId, PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, pawnId, PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            pawnId,
            fieldToId,
            null,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.NORMAL)
        assertThat(move.nameOfMove).isEqualTo("xd6")
        assertThat(move.promotedPiece).isEqualTo("")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(pawnId)
    }

    @Test
    fun testNormalMove() {
        val playerColor = PlayerColor.WHITE
        val queenId = "D1"
        val fieldToId = "A4"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, queenId, PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "A8", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A3", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, "")
        val move = gameEngine.convertStringToMove(
            queenId,
            fieldToId,
            null,
            piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        )

        assertThat(move.moveType).isEqualTo(MoveType.NORMAL)
        assertThat(move.nameOfMove).isEqualTo("♕${fieldToId.lowercase()}")
        assertThat(move.promotedPiece).isEqualTo("")
        assertThat(move.fieldTo).isEqualTo(fieldToId)
        assertThat(move.fieldFrom).isEqualTo(queenId)
    }

    @Test
    fun testWrongMove() {
        val playerColor = PlayerColor.WHITE
        val queenId = "D1"
        val wrongFieldId = "A0"
        val testsPiecesList = listOf(
            Bishop(PlayerColor.WHITE, "B2", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "E1", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, queenId, PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "H1", PiecesNames.ROOK),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "A7", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "A3", PiecesNames.PAWN),
            King(PlayerColor.BLACK, "A8", PiecesNames.KING),
            Rook(PlayerColor.WHITE, "H8", PiecesNames.ROOK)
        )

        val enemyPieces = gameEngine.calculateAndReturnCaptureMoveOfEnemy(testsPiecesList, playerColor)
        val piecesWithCorrectMoves =
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, "")
        assertThrows<WrongMove> {
            gameEngine.convertStringToMove(
                queenId,
                wrongFieldId,
                null,
                piecesWithCorrectMoves.plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
            )
        }
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
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, "")
                .plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        val dontCauseCheck = gameEngine.checkPieceIsCoveringKingAndFilterMoves(piecesWithCorrectMoves,enemyPieces, playerColor)
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
            gameEngine.calculateAndReturnAllPossibleMovesOfPlayer(testsPiecesList, playerColor, enemyPieces, "")
                .plus(gameEngine.getEnemyPieces(testsPiecesList, playerColor))
        val blackKingMoves = gameEngine.dontCauseCheck(piecesWithCorrectMoves, playerColor, enemyPieces)
            .find { it.name == PiecesNames.KING && it.id == "D8" }!!.possibleMoves

        assertThat(blackKingMoves.size).isEqualTo(0)
    }
}