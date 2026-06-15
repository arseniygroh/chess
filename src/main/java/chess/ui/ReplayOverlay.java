package chess.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ReplayOverlay extends StackPane {

    private int currentIndex = 0;

    public ReplayOverlay(ChessBoard board) {

        setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        VBox dialog = new VBox(15);
        dialog.setAlignment(Pos.CENTER);

        dialog.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #769656;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 20;"
        );

        Label moveLabel = new Label();
        moveLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20;"
        );

        Button prev = new Button("◀ Previous");
        Button next = new Button("Next ▶");
        Button close = new Button("Close");

        updateLabel(moveLabel, board);

        prev.setOnAction(e -> {

            if(currentIndex > 0) {

                currentIndex--;

                board.showPosition(currentIndex);

                updateLabel(moveLabel, board);
            }
        });

        next.setOnAction(e -> {

            if(currentIndex < board.getHistorySize() - 1) {

                currentIndex++;

                board.showPosition(currentIndex);

                updateLabel(moveLabel, board);
            }
        });

        close.setOnAction(e ->
                ((StackPane)getParent())
                        .getChildren()
                        .remove(this)
        );

        dialog.getChildren().addAll(
                moveLabel,
                prev,
                next,
                close
        );

        getChildren().add(dialog);
    }

    private void updateLabel(
            Label label,
            ChessBoard board
    ) {
        label.setText(
                "Move "
                        + currentIndex
                        + " / "
                        + (board.getHistorySize() - 1)
        );
    }
}
