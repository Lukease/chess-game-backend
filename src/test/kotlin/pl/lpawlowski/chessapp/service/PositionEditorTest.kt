package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.EditForbidden
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.model.game.PieceDto
import pl.lpawlowski.chessapp.model.positionEditor.ChangePositionOfPieceInPositionEditor

class PositionEditorTest : BasicIntegrationTest() {
    private val fenConverter: FenConverter = FenConverter()

    @Autowired
    lateinit var positionEditorService: PositionEditorService

    @AfterEach
    fun cleanUpDatabase() {
        userRepository.deleteAll()
    }

    @Test
    fun testGetPositionEditorPieces() {
        val user = insertUser(testUserLogin2)
        val fen = "rnbqkbnr/8/8/8/8/8/8/RNBQKBNR"

        user.positionEditorFen = fen

        val positionEditorInfo = positionEditorService.getPositionEditorPieces(user)
        val piecesToFen = fenConverter.convertPieceListToFen(positionEditorInfo.pieces.map { PieceDto.toDomain(it) })

        assertThat(positionEditorInfo.pieces).hasSize(16)
        assertThat(positionEditorInfo.kingIsChecked).hasSize(0)
        assertThat(fen).isEqualTo(piecesToFen)
    }

    @Test
    fun testGetDefaultPiecesToPositionEditor() {
        val user = insertUser(testUserLogin2)
        val fen = "rnbqkbnr/8/8/8/8/8/8/RNBQKBNR"

        user.positionEditorFen = fen

        val positionEditorInfo = positionEditorService.getDefaultPiecesToPositionEditor(user)
        val piecesToFen = fenConverter.convertPieceListToFen(positionEditorInfo.pieces.map { PieceDto.toDomain(it) })

        assertThat(positionEditorInfo.pieces).hasSize(32)
        assertThat(positionEditorInfo.kingIsChecked).hasSize(0)
        assertThat(piecesToFen).isNotEqualTo(fen)
    }

    @Test
    fun testRemovePieceFromPositionEditor() {
        val user = insertUser(testUserLogin2)
        val removedRookId = "A1"
        val removedBishopId = "C8"

        val positionEditorInfoAfterFirst = positionEditorService.removePieceFromPositionEditor(removedRookId, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val numberOfRooks = piecesAfterFirst.filter { it.name == PiecesNames.ROOK }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(31)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(numberOfRooks).hasSize(3)

        val positionEditorInfoAfterSecond = positionEditorService.removePieceFromPositionEditor(removedBishopId, user)
        val piecesAfterSecond = positionEditorInfoAfterSecond.pieces.map { PieceDto.toDomain(it) }
        val secondPiecesToFen = fenConverter.convertPieceListToFen(piecesAfterSecond)
        val userFenAfterSecondRemove = userRepository.findByLogin(testUserLogin2)
        val numberOfBishops = piecesAfterSecond.filter { it.name == PiecesNames.BISHOP }

        assertThat(positionEditorInfoAfterSecond.pieces).hasSize(30)
        assertThat(secondPiecesToFen).isEqualTo(userFenAfterSecondRemove.get().positionEditorFen)
        assertThat(numberOfBishops).hasSize(3)
    }

    @Test
    fun testCantRemoveKingFromPositionEditor() {
        val user = insertUser(testUserLogin2)
        val whiteKingId = "E1"
        val blackKingId = "E8"

        assertThrows<EditForbidden> { positionEditorService.removePieceFromPositionEditor(whiteKingId, user) }
        assertThrows<EditForbidden> { positionEditorService.removePieceFromPositionEditor(blackKingId, user) }
    }

    @Test
    fun testChangeKingPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val blackKing = pieces.find { it.isKing() && it.color == PlayerColor.BLACK }
        val newKingId = "A5"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(blackKing!!), newKingId, true)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val blackKingWithNewId = piecesAfterFirst.find { it.isKing() && it.color == PlayerColor.BLACK }
        val emptyField = piecesAfterFirst.filter { it.id == blackKing.id }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(blackKingWithNewId!!.id).isEqualTo(newKingId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testChangePawnPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val whitePawn = pieces.find { it.name == PiecesNames.PAWN && it.color == PlayerColor.WHITE && it.id == "A2" }
        val newPawnId = "A3"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(whitePawn!!), newPawnId, true)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val blackPawnWithNewId =
            piecesAfterFirst.find { it.name == PiecesNames.PAWN && it.color == PlayerColor.WHITE && it.id == newPawnId }
        val emptyField = piecesAfterFirst.filter { it.id == whitePawn.id }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(blackPawnWithNewId!!.id).isEqualTo(newPawnId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testChangeRookPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val whiteRook = pieces.find { it.name == PiecesNames.ROOK && it.color == PlayerColor.WHITE && it.id == "A1" }
        val newRookId = "D3"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(whiteRook!!), newRookId, true)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val blackRookWithNewId =
            piecesAfterFirst.find { it.name == PiecesNames.ROOK && it.color == PlayerColor.WHITE && it.id == newRookId }
        val emptyField = piecesAfterFirst.filter { it.id == whiteRook.id }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(blackRookWithNewId!!.id).isEqualTo(newRookId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testChangeBishopPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val blackBishop =
            pieces.find { it.name == PiecesNames.BISHOP && it.color == PlayerColor.BLACK && it.id == "C8" }
        val newBishopId = "F5"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(blackBishop!!), newBishopId, true)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val blackBishopWithNewId =
            piecesAfterFirst.find { it.name == PiecesNames.BISHOP && it.color == PlayerColor.BLACK && it.id == newBishopId }
        val emptyField = piecesAfterFirst.filter { it.id == blackBishop.id }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(blackBishopWithNewId!!.id).isEqualTo(newBishopId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testChangeKnightPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val blackKnight =
            pieces.find { it.name == PiecesNames.KNIGHT && it.color == PlayerColor.BLACK && it.id == "B8" }
        val newKnightId = "H5"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(blackKnight!!), newKnightId, true)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val blackKnightWithNewId =
            piecesAfterFirst.find { it.name == PiecesNames.KNIGHT && it.color == PlayerColor.BLACK && it.id == newKnightId }
        val emptyField = piecesAfterFirst.filter { it.id == blackKnight.id }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(blackKnightWithNewId!!.id).isEqualTo(newKnightId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testChangeQueenPosition() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val blackQueen = pieces.find { it.name == PiecesNames.QUEEN && it.color == PlayerColor.BLACK && it.id == "D8" }
        val newQueenId = "B5"
        val changeFirstPosition =
            ChangePositionOfPieceInPositionEditor(PieceDto.fromDomain(blackQueen!!), newQueenId, true)
        val positionEditorInfo = positionEditorService.changePositionOfPiece(changeFirstPosition, user)
        val piecesAfter = positionEditorInfo.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfter)
        val userFenAfterRemove = userRepository.findByLogin(testUserLogin2)
        val blackQueenWithNewId =
            piecesAfter.find { it.name == PiecesNames.QUEEN && it.color == PlayerColor.BLACK && it.id == newQueenId }
        val emptyField = piecesAfter.filter { it.id == blackQueen.id }

        assertThat(positionEditorInfo.pieces).hasSize(32)
        assertThat(piecesToFen).isEqualTo(userFenAfterRemove.get().positionEditorFen)
        assertThat(blackQueenWithNewId!!.id).isEqualTo(newQueenId)
        assertThat(emptyField).hasSize(0)
    }

    @Test
    fun testAddNewPieceToPositionEditor() {
        val user = insertUser(testUserLogin2)
        val firstNewPieceId = "A4"
        val firstNewPiece = PieceDto(PlayerColor.WHITE.name, "A0", PiecesNames.QUEEN.name)
        val firstAddPiecePosition =
            ChangePositionOfPieceInPositionEditor(firstNewPiece, firstNewPieceId, false)
        val positionEditorInfoAfterFirst = positionEditorService.changePositionOfPiece(firstAddPiecePosition, user)
        val piecesAfterFirst = positionEditorInfoAfterFirst.pieces.map { PieceDto.toDomain(it) }
        val piecesToFen = fenConverter.convertPieceListToFen(piecesAfterFirst)
        val userFenAfterFirstRemove = userRepository.findByLogin(testUserLogin2)
        val newPieceAdded =
            piecesAfterFirst.find { it.id == firstNewPieceId }

        assertThat(positionEditorInfoAfterFirst.pieces).hasSize(33)
        assertThat(piecesToFen).isEqualTo(userFenAfterFirstRemove.get().positionEditorFen)
        assertThat(newPieceAdded).isNotNull

        val secondNewPieceId = "C3"
        val secondNewPiece = PieceDto(PlayerColor.BLACK.name, "A0", PiecesNames.PAWN.name)
        val secondAddPiecePosition =
            ChangePositionOfPieceInPositionEditor(secondNewPiece, secondNewPieceId, false)
        val positionEditorInfoAfterSecond = positionEditorService.changePositionOfPiece(secondAddPiecePosition, user)
        val piecesAfterSecond = positionEditorInfoAfterSecond.pieces.map { PieceDto.toDomain(it) }
        val piecesToFenSecond = fenConverter.convertPieceListToFen(piecesAfterSecond)
        val userFenAfterSecondRemove = userRepository.findByLogin(testUserLogin2)
        val secondPieceAdded =
            piecesAfterSecond.find { it.id == secondNewPieceId }

        assertThat(positionEditorInfoAfterSecond.pieces).hasSize(34)
        assertThat(piecesToFenSecond).isEqualTo(userFenAfterSecondRemove.get().positionEditorFen)
        assertThat(secondPieceAdded).isNotNull
    }

    @Test
    fun testEditForbidden() {
        val user = insertUser(testUserLogin2)
        val pieces = fenConverter.convertFenToPiecesList(user.positionEditorFen)
        val blackKing = pieces.find { it.name == PiecesNames.KING && it.color == PlayerColor.BLACK }
        val whiteRook = pieces.find { it.name == PiecesNames.ROOK && it.color == PlayerColor.WHITE && it.id == "A1" }
        val blackQueen = PieceDto(PlayerColor.BLACK.name, "A0", PiecesNames.QUEEN.name)

        assertThrows<EditForbidden> {
            positionEditorService.changePositionOfPiece(
                ChangePositionOfPieceInPositionEditor(
                    blackQueen,
                    blackKing!!.id,
                    false
                ), user
            )
        }
        assertThrows<EditForbidden> {
            positionEditorService.changePositionOfPiece(
                ChangePositionOfPieceInPositionEditor(
                    blackQueen,
                    whiteRook!!.id,
                    false
                ), user
            )
        }
    }

    @Test
    fun testGetOwnPositionEditorPiecesFen() {
        val user = insertUser(testUserLogin2)
        val fen = "2bqk3/1ppppppp/1p6/5n2/8/8/2P1PP1P/2BK1B2"

        user.positionEditorFen = fen

        val positionEditorInfo = positionEditorService.setOwnFen(fen, user)
        val piecesToFen = fenConverter.convertPieceListToFen(positionEditorInfo.pieces.map { PieceDto.toDomain(it) })
        val whitePieces = positionEditorInfo.pieces.filter { it.color == PlayerColor.WHITE.name.lowercase() }
        val blackPieces = positionEditorInfo.pieces.filter { it.color == PlayerColor.BLACK.name.lowercase() }

        assertThat(positionEditorInfo.pieces).hasSize(19)
        assertThat(whitePieces).hasSize(7)
        assertThat(blackPieces).hasSize(12)
        assertThat(fen).isEqualTo(piecesToFen)
    }
}