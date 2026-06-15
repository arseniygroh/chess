package chess.model;

import java.io.Serializable;

public record Move(Position start, Position end, PieceType promotion) implements Serializable {
}
