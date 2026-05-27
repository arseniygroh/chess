module chess {
    requires javafx.controls;
    requires javafx.fxml;

    opens chess to javafx.fxml;
    exports chess;
}