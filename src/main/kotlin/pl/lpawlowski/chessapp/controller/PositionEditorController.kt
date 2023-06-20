package pl.lpawlowski.chessapp.controller

import org.springframework.web.bind.annotation.*
import pl.lpawlowski.chessapp.entities.User
import pl.lpawlowski.chessapp.model.game.*
import pl.lpawlowski.chessapp.model.positionEditor.ChangePositionOfPieceInPositionEditor
import pl.lpawlowski.chessapp.model.positionEditor.NewFenRequest
import pl.lpawlowski.chessapp.model.positionEditor.PositionEditorResponse
import pl.lpawlowski.chessapp.model.user.*
import pl.lpawlowski.chessapp.service.PositionEditorService
import pl.lpawlowski.chessapp.service.UserService

@CrossOrigin(origins = ["http://localhost:3000/"])
@RestController
@RequestMapping(value = ["/position-editor"])
class PositionEditorController(
    private val positionEditorService: PositionEditorService,
    private val userService: UserService
) {
    @GetMapping("/default")
    fun getDefaultPiecesPosition(
        @RequestHeader("Authorization") authorization: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return positionEditorService.getDefaultPiecesToPositionEditor(user)
    }

    @GetMapping("/current")
    fun getCurrentPositionEditorPieces(
        @RequestHeader("Authorization") authorization: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return positionEditorService.getPositionEditorPieces(user)
    }

    @PutMapping("/remove-piece")
    fun removePieceFromPositionEditor(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam pieceId: String
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)
        return positionEditorService.removePieceFromPositionEditor(pieceId, user)
    }

    @PutMapping("/new-position")
    fun changePositionOfPiece(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody changesIds: ChangePositionOfPieceInPositionEditor
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)
        return positionEditorService.changePositionOfPiece(changesIds, user)
    }

    @PutMapping("/set-own")
    fun setOwnFen(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody newFenRequest: NewFenRequest
    ): PositionEditorResponse {
        val user: User = userService.findUserByAuthorizationToken(authorization)

        return positionEditorService.setOwnFen(newFenRequest.fen, user)
    }
}