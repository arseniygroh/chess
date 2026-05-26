package chess.model;

import chess.model.pieces.*;

public class BoardState {
    private Piece[][] board;
    private PlayerColor activeColor;
    private Position enPassantTarget = null;

    public BoardState() {
        this.board = new Piece[8][8];
        this.activeColor = PlayerColor.WHITE;
        boardInit();
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

        if (movingPiece.getType() == PieceType.PAWN && move.end().equals(enPassantTarget)) {
            board[move.start().row()][move.end().col()] = null;
        }

        setEnPassantTarget(null);

        if (movingPiece.getType() == PieceType.PAWN && Math.abs(move.end().row() - move.start().row()) == 2) {
            int targetRow = (move.start().row() + move.end().row()) / 2;
            setEnPassantTarget(new Position(targetRow, move.start().col()));
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
