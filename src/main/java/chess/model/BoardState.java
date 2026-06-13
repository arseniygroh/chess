package chess.model;

import chess.model.pieces.*;

import java.io.Serializable;

public class BoardState implements Serializable {
    private Piece[][] board;
    private PlayerColor activeColor;
    private Position enPassantTarget = null;

    private boolean whiteCastleKingside = true;
    private boolean whiteCastleQueenside = true;
    private boolean blackCastleKingside = true;
    private boolean blackCastleQueenside = true;

    public BoardState() {
        this.board = new Piece[8][8];
        this.activeColor = PlayerColor.WHITE;
        boardInit();
    }

    private BoardState(boolean isCopy) {
        this.board = new Piece[8][8];
    }

    public boolean canWhiteCastleKingside() { return whiteCastleKingside; }
    public boolean canWhiteCastleQueenside() { return whiteCastleQueenside; }
    public boolean canBlackCastleKingside() { return blackCastleKingside; }
    public boolean canBlackCastleQueenside() { return blackCastleQueenside; }

    public void revokeWhiteKingside() { this.whiteCastleKingside = false; }
    public void revokeWhiteQueenside() { this.whiteCastleQueenside = false; }
    public void revokeBlackKingside() { this.blackCastleKingside = false; }
    public void revokeBlackQueenside() { this.blackCastleQueenside = false; }

    public BoardState copy() {
        BoardState clone = new BoardState(true);
        clone.activeColor = this.activeColor;

        if (this.enPassantTarget != null) {
            clone.enPassantTarget = new Position(this.enPassantTarget.row(), this.enPassantTarget.col());
        }

        clone.whiteCastleKingside = this.whiteCastleKingside;
        clone.whiteCastleQueenside = this.whiteCastleQueenside;
        clone.blackCastleKingside = this.blackCastleKingside;
        clone.blackCastleQueenside = this.blackCastleQueenside;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                clone.board[r][c] = this.board[r][c];
            }
        }
        return clone;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(Position target) {
        this.enPassantTarget = target;
    }

    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) return null;
        return board[pos.row()][pos.col()];
    }

    public void executeMove(Move move) {
        Piece movingPiece = getPieceAt(move.start());

        if (movingPiece.getType() == PieceType.KING) {
            if (movingPiece.getColor() == PlayerColor.WHITE) {
                revokeWhiteKingside();
                revokeWhiteQueenside();
            } else {
                revokeBlackKingside();
                revokeBlackQueenside();
            }
        } else if (movingPiece.getType() == PieceType.ROOK) {
            if (move.start().equals(new Position(7, 0))) revokeWhiteQueenside();
            if (move.start().equals(new Position(7, 7))) revokeWhiteKingside();
            if (move.start().equals(new Position(0, 0))) revokeBlackQueenside();
            if (move.start().equals(new Position(0, 7))) revokeBlackKingside();
        }

        if (move.end().equals(new Position(7, 0))) revokeWhiteQueenside();
        if (move.end().equals(new Position(7, 7))) revokeWhiteKingside();
        if (move.end().equals(new Position(0, 0))) revokeBlackQueenside();
        if (move.end().equals(new Position(0, 7))) revokeBlackKingside();

        if (movingPiece.getType() == PieceType.PAWN && move.end().equals(enPassantTarget)) {
            board[move.start().row()][move.end().col()] = null;
        }

        setEnPassantTarget(null);

        if (movingPiece.getType() == PieceType.PAWN && Math.abs(move.end().row() - move.start().row()) == 2) {
            int targetRow = (move.start().row() + move.end().row()) / 2;
            setEnPassantTarget(new Position(targetRow, move.start().col()));
        }

        if (movingPiece.getType() == PieceType.KING && Math.abs(move.end().col() - move.start().col()) == 2) {
            int row = move.start().row();
            boolean isKingside = move.end().col() > move.start().col();
            int rookStartCol = isKingside ? 7 : 0;
            int rookEndCol = isKingside ? 5 : 3;

            Piece rook = board[row][rookStartCol];
            board[row][rookStartCol] = null;
            board[row][rookEndCol] = rook;
        }

        board[move.start().row()][move.start().col()] = null;

        if (move.promotion() != null) {
            board[move.end().row()][move.end().col()] = promotePiece(move.promotion(), movingPiece.getColor());
        } else {
            board[move.end().row()][move.end().col()] = movingPiece;
        }

        activeColor = activeColor == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    public PlayerColor getActiveColor() {
        return activeColor;
    }

    private Piece promotePiece(PieceType type, PlayerColor color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> throw new IllegalArgumentException("Invalid promotion type");
        };
    }

    private void boardInit() {
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(PlayerColor.BLACK);
            board[6][col] = new Pawn(PlayerColor.WHITE);
        }
        board[0][0] = new Rook(PlayerColor.BLACK);
        board[0][1] = new Knight(PlayerColor.BLACK);
        board[0][2] = new Bishop(PlayerColor.BLACK);
        board[0][3] = new Queen(PlayerColor.BLACK);
        board[0][4] = new King(PlayerColor.BLACK);
        board[0][5] = new Bishop(PlayerColor.BLACK);
        board[0][6] = new Knight(PlayerColor.BLACK);
        board[0][7] = new Rook(PlayerColor.BLACK);

        board[7][0] = new Rook(PlayerColor.WHITE);
        board[7][1] = new Knight(PlayerColor.WHITE);
        board[7][2] = new Bishop(PlayerColor.WHITE);
        board[7][3] = new Queen(PlayerColor.WHITE);
        board[7][4] = new King(PlayerColor.WHITE);
        board[7][5] = new Bishop(PlayerColor.WHITE);
        board[7][6] = new Knight(PlayerColor.WHITE);
        board[7][7] = new Rook(PlayerColor.WHITE);
    }
}