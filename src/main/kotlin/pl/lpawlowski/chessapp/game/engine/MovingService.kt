package pl.lpawlowski.chessapp.game.engine

class MovingService {
//    private var isMovingPiece = false
//    private var isPieceFromBoard = true
//    private var isGameStarted = false
//    private var allFields: ArrayList<Field> = arrayListOf()
//
//    init {
//        allFields = arrayListOf()
//    }
//
//    fun movePiece(piece: Piece, coordinateX: Int, coordinateY: Int, fieldId: Int, isFromBoard: Boolean) {
//        val isMoving = true
//
//        isPieceFromBoard = isFromBoard
//        isMovingPiece = isMoving
//
//    }
//
//    fun setCurrentPiecePosition(newId: String, selectedPiece: Piece, oldId: String) {
//        isMovingPiece = false
//        val currentPiecePosition: Field? = allFields.find { field -> field.id == newId }
//        val oldPiecePosition: Field? = allFields.find { field -> field.id == oldId }
//
//        if (isPieceFromBoard) {
//            if (currentPiecePosition != null && currentPiecePosition.piece == null) {
//                currentPiecePosition.setPiece(selectedPiece, false)
//                oldPiecePosition?.removePiece()
//            }
//            oldPiecePosition?.restorePiece()
//        } else if (currentPiecePosition != null && currentPiecePosition.piece == null && !isPieceFromBoard) {
//            currentPiecePosition.setPiece(selectedPiece, false)
//        }
//    }
//
//    fun addFieldToMovingService(field: Field) {
//        allFields.add(field)
//    }
//
//    fun setGameStarted(isStarted: Boolean) {
//        isGameStarted = isStarted
//    }
}