package chess.model;

import chess.model.pieces.Bishop;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

public class BoardState {
    private Piece[][] board;
    private PlayerColor activeColor;

    public BoardState() {
        this.board = new Piece[8][8];
        this.activeColor = PlayerColor.WHITE;
        // TODO: bordInit()
    }

    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) return null;
        return board[pos.row()][pos.col()];
    }

    public void executeMove(Move move) {
        Piece movingPiece = getPieceAt(move.start());

        board[move.start().row()][move.start().col()] = null;

        board[move.end().row()][move.end().col()] = movingPiece;
        activeColor = activeColor == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    public PlayerColor getActiveColor() {
        return activeColor;
    }

//    private void boardInit() {
//        for (int col = 0; col < 8; col++) {
//            board[1][col] = new Pawn(PlayerColor.BLACK);
//            board[6][col] = new Pawn(PlayerColor.WHITE);
//        }
//
//        board[0][0] = new Rook(PlayerColor.BLACK);
//        board[0][1] = new Knight(PlayerColor.BLACK);
//        board[0][2] = new Bishop(PlayerColor.BLACK);
//        board[0][3] = new Queen(PlayerColor.BLACK);
//        board[0][4] = new King(PlayerColor.BLACK);
//        board[0][5] = new Bishop(PlayerColor.BLACK);
//        board[0][6] = new Knight(PlayerColor.BLACK);
//        board[0][7] = new Rook(PlayerColor.BLACK);
//
//        board[7][0] = new Rook(PlayerColor.WHITE);
//        board[7][1] = new Knight(PlayerColor.WHITE);
//        board[7][2] = new Bishop(PlayerColor.WHITE);
//        board[7][3] = new Queen(PlayerColor.WHITE);
//        board[7][4] = new King(PlayerColor.WHITE);
//        board[7][5] = new Bishop(PlayerColor.WHITE);
//        board[7][6] = new Knight(PlayerColor.WHITE);
//        board[7][7] = new Rook(PlayerColor.WHITE);
//    }
}
