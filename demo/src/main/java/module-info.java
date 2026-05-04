module window {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens window to javafx.fxml;
    exports window;
}
