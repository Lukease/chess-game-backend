package pl.lpawlowski.chessapp.service.suppliers

import pl.lpawlowski.chessapp.entities.Game
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.exception.GameNotFoundException
import pl.lpawlowski.chessapp.game.GameStatus
import pl.lpawlowski.chessapp.model.chess_possible_move.Coordinate
import pl.lpawlowski.chessapp.model.chess_possible_move.Vector2d
import pl.lpawlowski.chessapp.model.history.PlayerMove
import pl.lpawlowski.chessapp.model.pieces.Piece
import pl.lpawlowski.chessapp.repositories.GamesRepository
import java.util.*

class GameMoveService(
    private val gamesRepository: GamesRepository,
) {
    var possibleMoves: List<String> = emptyList()
    var previousMoveFields: List<String> = emptyList()
    var arrayOfPossibleMoves: List<PlayerMove> = emptyList()
    var allPieces: List<Piece> = emptyList()
    var arrayOfMoves: List<PlayerMove> = emptyList()
    private var coveringKingFields: List<String> = emptyList()
    var lastMove: PlayerMove? = null
    var kingCheck: String? = null

    fun createAndMakeMove(field: String) {
        val move = createMove(field)
    }

    private fun createMove(field: String) {
//        val specialMoveName = if (piece != null) {
//            return MoveType.PROM
//        } else {
//            arrayOfPossibleMoves.find { it.fieldTo == field }?.specialMove
//        }
//        val fieldFrom = fieldFrom(field)

    }

    fun getAllPossibleMovesOfPlayer(piece: List<Piece>, color: String): List<String> {
        val allPlayerPieces: List<Piece> = piece.map { piece: Piece ->
            if (piece.color == color) {
                return piece
            } else {
                return emptyList()
            }
        }
            .distinct()

        return getAllPossibleMoves(allPlayerPieces)
    }

    private fun getAllPossibleMoves(piecesArray: List<Piece>, game: Game): List<String> {
        return piecesArray.map { piece ->
            val correctId: List<String> = piece.getAllPossibleDirectionsWithColor()
                ?.map { direction -> this.getAllPossibleMovesFromDirection(piece, direction,game) }
                ?.flatten() ?: emptyList()

            if (piece.isPawn()) {
                return correctId.plus(isPawnCapturePossible(piece))
            } else {
                return correctId
            }
        }
            .flatten()
    }

    private fun isPawnCapturePossible(piece: Piece): Any {
        val direction = if(piece.color == "white"){return 1}else{-1}
        val currentCoordinate: Coordinate = piece.currentCoordinate

    }

    private fun getAllPossibleMovesFromDirection(currentPiece: Piece, direction: Vector2d, game: Game): List<String> {
        return if (currentPiece.canMoveMultipleSquares()) {
            getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(currentPiece, direction, game)
        } else {
            getPossibleMovesOfPawnAndKing(currentPiece.id,currentPiece, direction, game)
        }
    }

    private fun getPossibleMovesOfPawns(piece: Piece, currentPiece: Piece): List<String> {
        return if (piece.color == currentPiece.color) {
            listOf()
        } else {
            listOf(piece.id)
        }
    }

    private fun getPossibleMovesOfPawnAndKing(
        field: String,
        piece: Piece,
        direction: Vector2d,
        game: Game
    ): List<String> {
        val vector: Vector2d = convertIdToVector(field)
        val x = vector.x + direction.x
        val y = vector.y + direction.y
        val currentField = getFieldByXY(x, y)
        val currentPiece: Piece? = getPieceById(field!!, game)
        val isPawn = currentPiece?.isPawn()

        return if (!isPawn!!) {
            getPossibleMovesOfPawns(piece, currentPiece)
        } else {
            return if (currentPiece.isPawn()) {
                return listOf()
            } else {
                listOf(piece.id)
            }
        }
    }

    private fun getPossibleMovesOfPiecesWhoCanMoveMultipleSquares(
        currentPiece: Piece,
        direction: Vector2d,
        game: Game
    ): List<String> {
        var counter = 1
        var canGoFurther = true
        val vector: Vector2d = convertIdToVector(currentPiece.id)
        var x = direction.x * counter
        var y = direction.y * counter
        var field = getFieldByXY(vector.x + (direction.x * counter), vector.y + (direction.y * counter))
        var piece = getPieceById(field!!, game)

        val fields: MutableList<String> = mutableListOf()
            while (field != null && piece?.color != currentPiece.color && canGoFurther){
                if (piece != null){
                    canGoFurther = false
                }
                counter++
                fields.add(field)

                field = getFieldByXY(vector.x + (direction.x * counter), vector.y + (direction.y * counter))
                piece = getPieceById(field!!, game)
            }

        return fields
    }

    private fun getFieldByXY(x: Int, y: Int): String? {
        val column = numberToColumn(x)
        val row = y.toChar()

        return generateFields().find { field -> field[0] == column && field[1] == row }
    }

    private fun getFieldByID(id: String): String? {

        return generateFields().find { field -> field == id }
    }

    private fun convertIdToVector(id: String): Vector2d {
        val x = columnToNumber(id[0])

        return Vector2d(x, id[1].code)
    }

    private fun generateFields(): List<String> {
        val rows = listOf("1", "2", "3", "4", "5", "6", "7", "8")
        val columns = listOf("A", "B", "C", "D", "E", "F", "G", "H")

        val result = mutableListOf<String>()
        for (row in rows) {
            for (column in columns) {
                result.add(column + row)
            }
        }

        return result
    }

    private fun columnToNumber(column: Char): Int {
        return column.uppercaseChar() - 'A' + 1
    }

    private fun numberToColumn(column: Int): Char {
        return column.toChar().uppercaseChar()
    }

    private fun getPieceById(id: String, game: Game): Piece? {
        val pieces: List<Piece> = game.pieces

        return pieces.first { it.id == id }
    }
}