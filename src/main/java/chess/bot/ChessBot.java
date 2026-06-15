package chess.bot;

import chess.model.BoardState;
import chess.model.Move;

public interface ChessBot {

    Move calculateMove(BoardState board);

}
