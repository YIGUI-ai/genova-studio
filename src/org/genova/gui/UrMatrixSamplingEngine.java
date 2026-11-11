package org.genova.gui;

import java.util.*;

/** Analogous ANOVA / Henderson Method 1 for complete matrix-sampling (P x I):R. */
public final class UrMatrixSamplingEngine {
    /** T order mean,r,p:r,i:r,pi:r; VC order r,p:r,i:r,pi:r. */
    public record Analysis(Results results,double[] t,double[] ss,double[] ms,double[] vc,double[][] et,double[][] ems) {}
    private static final int[][] CONTRASTS={
        {-1,1,0,0,0}, {0,-1,1,0,0}, {0,-1,0,1,0}, {0,1,-1,-1,1}
    };
    private static String number(double x){return String.format(Locale.ROOT,"%.8f",x);}
    private static String coefficient(double x){return String.format(Locale.ROOT,"%.6f",x);}
    private static long group(UrMatrixSamplingDesign.Observation o,int kind){
        return switch(kind){
            case 1->o.r();case 2->o.p();case 3->o.i();
            case 4->((long)o.p()<<32)|(o.i()&0xffffffffL);
            default->0;
        };
    }
    private static double trace(List<UrMatrixSamplingDesign.Observation> observations,int tKind,int vcKind){
        Map<Long,Integer> sizes=new HashMap<>();
        Map<Long,Map<Long,Integer>> intersections=new HashMap<>();
        for(var o:observations){
            long g=group(o,tKind),v=group(o,vcKind);
            sizes.merge(g,1,Integer::sum);
            intersections.computeIfAbsent(g,ignored->new HashMap<>()).merge(v,1,Integer::sum);
        }
        double result=0;
        for(var entry:intersections.entrySet()){
            double squares=0;
            for(int count:entry.getValue().values())squares+=(double)count*count;
            result+=squares/sizes.get(entry.getKey());
        }
        return result;
    }
    private static double[] solve(double[][] ems,double[] ms){
        int n=4;double[][] a=new double[n][n+1];
        for(int row=0;row<n;row++){System.arraycopy(ems[row],0,a[row],0,n);a[row][n]=ms[row];}
        for(int col=0;col<n;col++){
            int pivot=col;
            for(int row=col+1;row<n;row++)if(Math.abs(a[row][col])>Math.abs(a[pivot][col]))pivot=row;
            if(Math.abs(a[pivot][col])<1e-10)throw new IllegalArgumentException("Expected-mean-square equations are singular for this matrix-sampling design.");
            double[] swap=a[col];a[col]=a[pivot];a[pivot]=swap;
            double scale=a[col][col];for(int j=col;j<=n;j++)a[col][j]/=scale;
            for(int row=0;row<n;row++)if(row!=col){
                double factor=a[row][col];for(int j=col;j<=n;j++)a[row][j]-=factor*a[col][j];
            }
        }
        double[] result=new double[n];
        for(int row=0;row<n;row++){
            result[row]=a[row][n];
            if(!Double.isFinite(result[row]))throw new IllegalArgumentException("Variance-component equations yielded nonfinite estimates.");
        }
        return result;
    }
    private static String equation(double[] row,String[] effects){
        StringBuilder text=new StringBuilder();
        for(int col=3;col>=0;col--){
            double value=row[col];if(Math.abs(value)<1e-10)continue;
            if(!text.isEmpty())text.append(value<0?" - ":" + ");else if(value<0)text.append('-');
            text.append(coefficient(Math.abs(value))).append(" × VC(").append(effects[col]).append(')');
        }
        return text.toString();
    }
    public static Results run(UrMatrixSamplingDesign.Prepared design){return calculate(design).results();}
    public static Analysis calculate(UrMatrixSamplingDesign.Prepared design){
        var observations=design.observations();int n=observations.size();
        int residualDf=0;
        for(int j=0;j<design.rLevels();j++)residualDf+=(design.pPerR()[j]-1)*(design.iPerR()[j]-1);
        int[] df={design.rLevels()-1,design.pLevels()-design.rLevels(),
            design.iLevels()-design.rLevels(),residualDf};
        double[] t=new double[5];double[][] et=new double[5][4];
        for(int term=0;term<5;term++){
            Map<Long,double[]> groups=new HashMap<>();
            for(var o:observations){
                double[] values=groups.computeIfAbsent(group(o,term),ignored->new double[2]);
                values[0]+=o.score();values[1]++;
            }
            for(double[] values:groups.values())t[term]+=values[0]*values[0]/values[1];
            for(int component=0;component<4;component++)et[term][component]=trace(observations,term,component+1);
        }
        double[] ss=new double[4],ms=new double[4];double[][] ems=new double[4][4];
        for(int effect=0;effect<4;effect++){
            for(int term=0;term<5;term++){
                ss[effect]+=CONTRASTS[effect][term]*t[term];
                for(int component=0;component<4;component++)
                    ems[effect][component]+=CONTRASTS[effect][term]*et[term][component];
            }
            ms[effect]=ss[effect]/df[effect];
            for(int component=0;component<4;component++)ems[effect][component]/=df[effect];
        }
        double[] vc=solve(ems,ms);
        String r=design.rColumn(),p=design.pColumn(),i=design.iColumn();
        String[] effects={r,p+":"+r,i+":"+r,p+"×"+i+":"+r};
        List<List<String>> anovaRows=new ArrayList<>(),componentRows=new ArrayList<>();
        for(int effect=0;effect<4;effect++){
            anovaRows.add(List.of(effects[effect],Integer.toString(df[effect]),number(t[effect+1]),
                number(ss[effect]),number(ms[effect]),number(vc[effect])));
            componentRows.add(List.of(effects[effect],Integer.toString(df[effect]),number(vc[effect]),
                equation(ems[effect],effects),"Random effect"));
        }
        Results.Table anova=new Results.Table(List.of("Effect","df","T","SS","MS","VC"),anovaRows);
        Results.Table components=new Results.Table(List.of("Effect","df","Estimate","EMS","Type"),componentRows);
        String notice="Experimental complete matrix-sampling (P × I):R G-study with unequal P and I counts by R; no D-study. Benchmarked against urGENOVA manual Appendix H, not the original executable.";
        if(Arrays.stream(vc).anyMatch(value->value<0))notice+=" Negative variance estimates are retained, not truncated.";
        double total=observations.stream().mapToDouble(UrMatrixSamplingDesign.Observation::score).sum();
        StringBuilder report=new StringBuilder("GENOVA Studio · urGENOVA matrix-sampling Java pilot\n\n")
            .append(design.description()).append("\nMethod: analogous ANOVA / Henderson Method 1.\n")
            .append("Levels: R=").append(design.rLevels()).append(", P=").append(design.pLevels())
            .append(", I=").append(design.iLevels()).append("; observations: ").append(n)
            .append("\nGrand mean: ").append(number(total/n)).append("\nT(mean): ").append(number(t[0]))
            .append("; total SS: ").append(number(t[4]-t[0])).append("\n\n")
            .append(anova.csv()).append("\nExpected mean square equations:\n");
        for(int effect=0;effect<4;effect++)report.append(effects[effect]).append(": ")
            .append(equation(ems[effect],effects)).append('\n');
        report.append("\n").append(notice).append('\n');
        return new Analysis(new Results(anova,components,new Results.Table(List.of(),List.of()),
            report.toString(),notice),t,ss,ms,vc,et,ems);
    }
}
