package chess.server;

import chess.model.*;
import chess.network.protocol.GameStateUpdate;
import chess.network.protocol.MoveRequest;

import java.util.UUID;

public class ServerGameInstance {
    private final String gameId;
    private final BoardState boardState;
    private final String whitePlayer;
    private final String blackPlayer;
    private boolean isGameOver = false;
    private String winner = null;

    public ServerGameInstance(String whitePlayer, String blackPlayer) {
        this.gameId = UUID.randomUUID().toString();
        this.boardState = new BoardState();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public String getGameId() { return gameId; }
    public String getWhitePlayer() { return whitePlayer; }
    public String getBlackPlayer() { return blackPlayer; }

    public synchronized GameStateUpdate handleMoveRequest(MoveRequest request, String player) {
        if (isGameOver) return null;

        PlayerColor playerColor = player.equals(whitePlayer) ? PlayerColor.WHITE : PlayerColor.BLACK;
        if (boardState.getActiveColor() != playerColor) {
            return null; // Not this player's turn
        }

        if (RulesEngine.isLegalMove(boardState, request.move())) {
            boardState.executeMove(request.move());
            
            if (RulesEngine.isCheckMate(boardState)) {
                isGameOver = true;
                winner = player; 
            } else if (RulesEngine.isStaleMate(boardState)) {
                isGameOver = true;
                winner = "Draw";
            }

            return new GameStateUpdate(gameId, boardState.copy(), request.move(), isGameOver, winner);
        }
        return null; // Illegal move
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public String getWinner() {
        return winner;
    }
}
