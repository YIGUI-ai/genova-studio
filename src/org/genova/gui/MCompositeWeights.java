package org.genova.gui;

import java.util.*;

/** Shared composite weighting for the bounded multivariate G/D pilots. */
final class MCompositeWeights {
    record Resolved(double[] w,double[] a,boolean different) {}

    static Resolved resolve(int[] itemTotals,double[] suppliedW,double[] suppliedA) {
        int count=itemTotals.length;
        double[] w;
        if(suppliedW==null){
            long total=0;for(int n:itemTotals)total+=n;
            w=new double[count];for(int i=0;i<count;i++)w[i]=itemTotals[i]/(double)total;
        }else w=checked(suppliedW,count,"w");
        double[] a=suppliedA==null?w.clone():checked(suppliedA,count,"a");
        return new Resolved(w,a,!Arrays.equals(w,a));
    }

    private static double[] checked(double[] input,int count,String label) {
        if(input.length!=count)
            throw new IllegalArgumentException("Provide one "+label+" weight per fixed variable, in the displayed order.");
        double[] values=input.clone();boolean nonzero=false;
        for(double value:values){
            if(!Double.isFinite(value)||Math.abs(value)>1_000_000)
                throw new IllegalArgumentException("Each "+label+" weight must be finite and no greater than 1,000,000 in magnitude.");
            nonzero|=value!=0;
        }
        if(!nonzero)throw new IllegalArgumentException("At least one "+label+" weight must be nonzero.");
        return values;
    }

    /** When a differs from w, entries 1/2 are MSEs, not error variances. */
    static double[] metrics(double[][] universe,double[][] relative,double[][] absolute,
                            double[] means,Resolved weights) {
        double[] w=weights.w(),a=weights.a(),difference=new double[w.length];
        for(int i=0;i<w.length;i++)difference[i]=a[i]-w[i];
        double[] values=new double[5];
        values[0]=quadratic(w,universe);
        values[1]=quadratic(a,relative);
        values[2]=quadratic(a,absolute);
        if(weights.different()){
            values[1]+=quadratic(difference,universe);
            values[2]+=quadratic(difference,universe);
            double meanDifference=0;
            for(int i=0;i<w.length;i++)meanDifference+=difference[i]*means[i];
            values[2]+=meanDifference*meanDifference;
            values[3]=Double.NaN;values[4]=Double.NaN;
        }else{
            values[3]=coefficient(values[0],values[1]);
            values[4]=coefficient(values[0],values[2]);
        }
        for(int i=0;i<3;i++)if(!Double.isFinite(values[i]))
            throw new IllegalArgumentException("The supplied scores or weights produce nonfinite composite values.");
        return values;
    }

    private static double quadratic(double[] weights,double[][] matrix) {
        double value=0;
        for(int i=0;i<weights.length;i++)for(int j=0;j<weights.length;j++)
            value+=weights[i]*weights[j]*matrix[i][j];
        return value;
    }

    private static double coefficient(double universe,double error) {
        double denominator=universe+error;
        return denominator==0?Double.NaN:universe/denominator;
    }
}
