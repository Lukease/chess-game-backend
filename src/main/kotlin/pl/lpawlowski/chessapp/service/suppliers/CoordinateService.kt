package pl.lpawlowski.chessapp.service.suppliers

import pl.lpawlowski.chessapp.model.chess_possible_move.Coordinate

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

        fun getCoordinateById(id: String) : Coordinate {
            val column = id[0].toString()
            val row = id[1].toString()

            return allCoordinate.find { it.boardColumn == column && it.boardRow == row }!!
        }

        fun getCoordinateByColumnAndRow(column: Int, row: Int): Coordinate{
            return allCoordinate.find { it.x == column && it.y == row }!!
        }
    }
}