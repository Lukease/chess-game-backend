package pl.lpawlowski.chessapp.game.engine

import pl.lpawlowski.chessapp.web.chess_possible_move.Coordinate

class CoordinateService {
    companion object {
        private fun createAllCoordinate(): List<Coordinate> {
            val arrayCoordinates = mutableListOf<Coordinate>()

            for (y in 1..8) {
                val boardColumn = ('A' + y - 1).toString()

                for (x in 1..8) {
                    arrayCoordinates.add(Coordinate(x, y, boardColumn, x.toString()))
                }
            }

            return arrayCoordinates
        }

        private val allCoordinate = createAllCoordinate()

        fun getCoordinateById(id: String): Coordinate {
            val column = id[0].toString()
            val row = id[1].toString()
            val x = id[0].uppercaseChar() - 'A' + 1
            val y = id[1].code

            return Coordinate(x, y, column, row)
        }
    }
}