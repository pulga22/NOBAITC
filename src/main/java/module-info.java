module me.julionxn.nobaitc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.controlsfx.controls;

    exports me.julionxn.nobaitc;
    exports me.julionxn.nobaitc.controllers;
    exports me.julionxn.nobaitc.models;
    exports me.julionxn.nobaitc.lib;

    opens me.julionxn.nobaitc to javafx.fxml;
    opens me.julionxn.nobaitc.controllers to javafx.fxml;
    opens me.julionxn.nobaitc.models to javafx.fxml, javafx.base;

}