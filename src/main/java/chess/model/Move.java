package chess.model;

public record Move(Position start, Position end, PieceType promotion) {
}
