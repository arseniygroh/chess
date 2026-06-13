module chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    exports chess;
    exports chess.ui;
    exports chess.network.protocol;
    exports chess.network.client;
    exports chess.server;

    opens chess.model to java.base;
    opens chess.model.pieces to java.base;
    opens chess.network.protocol to java.base;
}