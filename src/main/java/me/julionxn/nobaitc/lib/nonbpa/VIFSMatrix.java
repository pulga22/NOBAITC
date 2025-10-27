package me.julionxn.nobaitc.lib.nonbpa;

import me.julionxn.nobaitc.lib.MatlabFunctions;

public class VIFSMatrix {

    public double[] calculate(double[][] fraction){
        double[][] r2 = MatlabFunctions.corrcoef(fraction);
        double[][] r2Inverse = MatlabFunctions.inv(r2);
        if (r2Inverse == null) {
            throw new RuntimeException("No se logró calcular la inversa");
        }
        return MatlabFunctions.diag(r2Inverse);
    }

}
