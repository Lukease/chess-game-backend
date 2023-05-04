package pl.lpawlowski.chessapp.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pl.lpawlowski.chessapp.constants.PiecesNames
import pl.lpawlowski.chessapp.constants.PlayerColor
import pl.lpawlowski.chessapp.exception.WrongMove
import pl.lpawlowski.chessapp.game.engine.FenConverter
import pl.lpawlowski.chessapp.web.pieces.*

class FenConvertingTest {
    private var fenConverter: FenConverter = FenConverter()

    @Test
    fun testConvertPieceListToFen() {
        val testsPiecesList1 = listOf(
            Bishop(PlayerColor.WHITE, "B8", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "B6", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "E1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F7", PiecesNames.PAWN)
        )
        val pieceListToFen1 = fenConverter.convertPieceListToFen(testsPiecesList1)
        val fen1 = "1B6/5P2/1K6/8/8/2N5/3Q4/4R3"

        assertThat(pieceListToFen1).isEqualTo(fen1)

        val testsPiecesList2 = listOf(
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, "B7", PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, "B8", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "B6", PiecesNames.KING),
            King(PlayerColor.BLACK, "B2", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C4", PiecesNames.KNIGHT),
            Knight(PlayerColor.BLACK, "C6", PiecesNames.KNIGHT),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D8", PiecesNames.QUEEN),
            Queen(PlayerColor.BLACK, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "A1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "E4", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F7", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "F1", PiecesNames.PAWN)
        )
        val pieceListToFen2 = fenConverter.convertPieceListToFen(testsPiecesList2)
        val fen2 = "1b1Q4/1B3P2/1Kn5/8/2N1r3/2N5/1k1q4/RB3p2"

        assertThat(pieceListToFen2).isEqualTo(fen2)

        val testsPiecesList3 = listOf(
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            Bishop(PlayerColor.WHITE, "H1", PiecesNames.BISHOP),
            Bishop(PlayerColor.BLACK, "B8", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Knight(PlayerColor.BLACK, "C8", PiecesNames.KNIGHT),
            Knight(PlayerColor.WHITE, "D1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "E1", PiecesNames.QUEEN),
            Queen(PlayerColor.BLACK, "E8", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "F1", PiecesNames.ROOK),
            Rook(PlayerColor.BLACK, "F8", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            Pawn(PlayerColor.BLACK, "F7", PiecesNames.PAWN)
        )
        val pieceListToFen3 = fenConverter.convertPieceListToFen(testsPiecesList3)
        val fen3 = "1bn1qr1k/5p2/8/8/8/8/5P2/KBNNQR1B"

        assertThat(pieceListToFen3).isEqualTo(fen3)
    }

    @Test
    fun testConvertFenToPiecesList() {
        val testsPiecesList1 = listOf(
            Bishop(PlayerColor.BLACK, "B8", PiecesNames.BISHOP),
            Knight(PlayerColor.BLACK, "C8", PiecesNames.KNIGHT),
            Queen(PlayerColor.BLACK, "E8", PiecesNames.QUEEN),
            Rook(PlayerColor.BLACK, "F8", PiecesNames.ROOK),
            King(PlayerColor.BLACK, "H8", PiecesNames.KING),
            Pawn(PlayerColor.BLACK, "F7", PiecesNames.PAWN),
            Pawn(PlayerColor.WHITE, "F2", PiecesNames.PAWN),
            King(PlayerColor.WHITE, "A1", PiecesNames.KING),
            Bishop(PlayerColor.WHITE, "B1", PiecesNames.BISHOP),
            Knight(PlayerColor.WHITE, "C1", PiecesNames.KNIGHT),
            Knight(PlayerColor.WHITE, "D1", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "E1", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "F1", PiecesNames.ROOK),
            Bishop(PlayerColor.WHITE, "H1", PiecesNames.BISHOP)
        )
        val fen1 = "1bn1qr1k/5p2/8/8/8/8/5P2/KBNNQR1B"
        val fenToPieceList1 = fenConverter.convertFenToPiecesList(fen1)

        assertThat(testsPiecesList1.size).isEqualTo(fenToPieceList1.size)
        fenToPieceList1.map { fenPiece ->
            val matchingPiece = testsPiecesList1.any { testPiece -> testPiece.id == fenPiece.id }
            assertThat(matchingPiece).isTrue
        }

        val testsPiecesList2 = listOf(
            Bishop(PlayerColor.WHITE, "B8", PiecesNames.BISHOP),
            King(PlayerColor.WHITE, "B6", PiecesNames.KING),
            Knight(PlayerColor.WHITE, "C3", PiecesNames.KNIGHT),
            Queen(PlayerColor.WHITE, "D2", PiecesNames.QUEEN),
            Rook(PlayerColor.WHITE, "E1", PiecesNames.ROOK),
            Pawn(PlayerColor.WHITE, "F7", PiecesNames.PAWN)
        )
        val fen2 = "1B6/5P2/1K6/8/8/2N5/3Q4/4R3"
        val fenToPieceList2 = fenConverter.convertFenToPiecesList(fen2)

        assertThat(testsPiecesList2.size).isEqualTo(fenToPieceList2.size)
        fenToPieceList2.map { fenPiece ->
            val matchingPiece = testsPiecesList2.any { testPiece -> testPiece.id == fenPiece.id }
            assertThat(matchingPiece).isTrue
        }
    }

    @Test
    fun testGetPieceByChar() {
        val whiteKingChar = 'K'
        val kingId = "A1"
        val playerColor = PlayerColor.WHITE
        val whiteKing = King(playerColor, kingId, PiecesNames.KING)
        val getKingByChar = fenConverter.getPieceByChar(whiteKingChar, playerColor, kingId)

        assertThat(getKingByChar.name).isEqualTo(whiteKing.name)
        assertThat(getKingByChar.color).isEqualTo(whiteKing.color)
        assertThat(getKingByChar.id).isEqualTo(whiteKing.id)
        assertThat(getKingByChar).isInstanceOf(King::class.java)

        val blackKingChar = 'k'
        val blackKingId = "A1"
        val blackPlayerColor = PlayerColor.BLACK
        val blackKing = King(blackPlayerColor, blackKingId, PiecesNames.KING)
        val getKingByChar1 = fenConverter.getPieceByChar(blackKingChar, blackPlayerColor, blackKingId)

        assertThat(getKingByChar1.name).isEqualTo(blackKing.name)
        assertThat(getKingByChar1.color).isEqualTo(blackKing.color)
        assertThat(getKingByChar1.id).isEqualTo(blackKing.id)
        assertThat(getKingByChar1).isInstanceOf(King::class.java)

        val blackKnightChar = 'n'
        val blackKnightId = "A5"
        val blackKnight = Knight(blackPlayerColor, blackKnightId, PiecesNames.KNIGHT)
        val getKnightByChar = fenConverter.getPieceByChar(blackKnightChar, blackPlayerColor, blackKnightId)

        assertThat(getKnightByChar.name).isEqualTo(blackKnight.name)
        assertThat(getKnightByChar.color).isEqualTo(blackKnight.color)
        assertThat(getKnightByChar.id).isEqualTo(blackKnight.id)
        assertThat(getKnightByChar).isInstanceOf(Knight::class.java)

        val blackRookChar = 'r'
        val blackRookId = "H8"
        val blackRook = Rook(blackPlayerColor, blackRookId, PiecesNames.ROOK)
        val getRookByChar = fenConverter.getPieceByChar(blackRookChar, blackPlayerColor, blackRookId)

        assertThat(getRookByChar.name).isEqualTo(blackRook.name)
        assertThat(getRookByChar.color).isEqualTo(blackRook.color)
        assertThat(getRookByChar.id).isEqualTo(blackRook.id)
        assertThat(getRookByChar).isInstanceOf(Rook::class.java)

        assertThrows<WrongMove> { fenConverter.getPieceByChar('x', PlayerColor.WHITE, "A1") }

    }
}