package chess.bot;

import chess.model.Move;
import chess.ui.ChessBoard;

public interface ChessBot {

    Move calculateMove(ChessBoard board);

}
