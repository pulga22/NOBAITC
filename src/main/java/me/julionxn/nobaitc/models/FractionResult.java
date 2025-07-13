package me.julionxn.nobaitc.models;

import javafx.beans.property.*;

public class FractionResult {
    private final IntegerProperty fractionNumber;
    private final StringProperty fractionData;
    private final DoubleProperty gbm;
    private final DoubleProperty j2;
    private final double[][] fraction; // Datos completos de la fracción

    public FractionResult(int fractionNumber, String fractionData, double gbm, double j2, double[][] fraction) {
        this.fractionNumber = new SimpleIntegerProperty(fractionNumber);
        this.fractionData = new SimpleStringProperty(fractionData);
        this.gbm = new SimpleDoubleProperty(gbm);
        this.j2 = new SimpleDoubleProperty(j2);
        this.fraction = fraction;
    }

    // Getters para las propiedades (requerido por TableView)
    public int getFractionNumber() {
        return fractionNumber.get();
    }

    public IntegerProperty fractionNumberProperty() {
        return fractionNumber;
    }

    public String getFractionData() {
        return fractionData.get();
    }

    public StringProperty fractionDataProperty() {
        return fractionData;
    }

    public double getGbm() {
        return gbm.get();
    }

    public DoubleProperty gbmProperty() {
        return gbm;
    }

    public double getJ2() {
        return j2.get();
    }

    public DoubleProperty j2Property() {
        return j2;
    }

    public double[][] getFraction() {
        return fraction;
    }

    // Setters
    public void setFractionNumber(int fractionNumber) {
        this.fractionNumber.set(fractionNumber);
    }

    public void setFractionData(String fractionData) {
        this.fractionData.set(fractionData);
    }

    public void setGbm(double gbm) {
        this.gbm.set(gbm);
    }

    public void setJ2(double j2) {
        this.j2.set(j2);
    }
}