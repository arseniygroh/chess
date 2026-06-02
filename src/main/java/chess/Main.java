package chess;

import chess.ui.MainMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 900;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 400;

    @Override
    public void start(Stage stage) {

        StackPane root = new StackPane();

        root.getChildren().add(
                new MainMenu(root)
        );

        Scene scene = new Scene(
                root,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );

        stage.setTitle("Chess");

        stage.setScene(scene);

        stage.setMinWidth(MIN_WIDTH);

        stage.setMinHeight(MIN_HEIGHT);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}