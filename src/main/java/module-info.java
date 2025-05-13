module me.julionxn.nobaitc {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.julionxn.nobaitc to javafx.fxml;
    exports me.julionxn.nobaitc;
}