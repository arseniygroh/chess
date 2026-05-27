package chess;

import chess.ui.ChessBoard;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        ChessBoard board = new ChessBoard();

        StackPane root = new StackPane(board);

        root.setAlignment(Pos.CENTER);

        root.setStyle("-fx-background-color: #262421;");

        Scene scene = new Scene(root, 900, 900);

        stage.setTitle("Chess");

        stage.setScene(scene);

        stage.setMinWidth(400);

        stage.setMinHeight(400);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}