package org.genova.gui;

import java.util.*;

/** Limited mGENOVA P x I random-effects G-study and D-study. */
public final class MpXiEngine {
    /** Per-variable effects in order P, I, P×I. */
    public record Analysis(Results results,double[][] ss,double[][] ms,double[][] vc,
                           double[][] personCovariance,double[][] decisions,
                           double[] composite,double[] weights,double[] estimationWeights) {}
    private static String number(double x){return Double.isNaN(x)?"Undefined":String.format(Locale.ROOT,"%.8f",x);}
    private static double coefficient(double universe,double error){
        double denominator=universe+error;
        return denominator==0?Double.NaN:universe/denominator;
    }
    public static Results run(MpXiDesign.Prepared design,int[] dItems){return calculate(design,dItems).results();}
    public static Results run(MpXiDesign.Prepared design,int[] dItems,double[] wWeights){return calculate(design,dItems,wWeights).results();}
    public static Results run(MpXiDesign.Prepared design,int[] dItems,double[] wWeights,double[] aWeights){
        return calculate(design,dItems,wWeights,aWeights).results();
    }
    public static Analysis calculate(MpXiDesign.Prepared design,int[] requestedItems){
        return calculate(design,requestedItems,null);
    }
    public static Analysis calculate(MpXiDesign.Prepared design,int[] requestedItems,double[] requestedWeights){
        return calculate(design,requestedItems,requestedWeights,null);
    }
    public static Analysis calculate(MpXiDesign.Prepared design,int[] requestedItems,
                                     double[] requestedW,double[] requestedA){
        int vCount=design.variables().size(),pCount=design.persons().size();
        int[] gItems=design.itemCounts();
        int[] dItems=requestedItems==null?gItems.clone():requestedItems.clone();
        if(dItems.length!=vCount||Arrays.stream(dItems).anyMatch(value->value<1||value>100_000))
            throw new IllegalArgumentException("Provide one positive D-study item count per fixed variable, in the displayed order.");
        double[][] ss=new double[vCount][3],ms=new double[vCount][3],vc=new double[vCount][3];
        double[][] personMeans=new double[vCount][pCount];
        double[] grand=new double[vCount];
        for(int v=0;v<vCount;v++){
            int ni=gItems[v],n=pCount*ni;
            double[] itemSum=new double[ni];double total=0,rawSquares=0,tPerson=0,tItem=0;
            for(int p=0;p<pCount;p++){
                double sum=0;
                for(int i=0;i<ni;i++){
                    double x=design.scores()[v][p][i];
                    sum+=x;itemSum[i]+=x;rawSquares+=x*x;
                }
                personMeans[v][p]=sum/ni;total+=sum;tPerson+=sum*sum/ni;
            }
            for(double sum:itemSum)tItem+=sum*sum/pCount;
            double correction=total*total/n;
            ss[v][0]=tPerson-correction;
            ss[v][1]=tItem-correction;
            ss[v][2]=rawSquares-tPerson-tItem+correction;
            ms[v][0]=ss[v][0]/(pCount-1);
            ms[v][1]=ss[v][1]/(ni-1);
            ms[v][2]=ss[v][2]/((pCount-1)*(ni-1));
            vc[v][0]=(ms[v][0]-ms[v][2])/ni;
            vc[v][1]=(ms[v][1]-ms[v][2])/pCount;
            vc[v][2]=ms[v][2];
            grand[v]=total/n;
            for(int effect=0;effect<3;effect++)if(!Double.isFinite(vc[v][effect]))
                throw new IllegalArgumentException("Score magnitudes are too large for finite variance components.");
        }
        double[][] covP=new double[vCount][vCount];
        for(int a=0;a<vCount;a++)for(int b=0;b<vCount;b++){
            if(a==b){covP[a][b]=vc[a][0];continue;}
            double sum=0;
            for(int p=0;p<pCount;p++)sum+=(personMeans[a][p]-grand[a])*(personMeans[b][p]-grand[b]);
            covP[a][b]=sum/(pCount-1);
            if(!Double.isFinite(covP[a][b]))
                throw new IllegalArgumentException("Score magnitudes are too large for finite covariance components.");
        }
        MCompositeWeights.Resolved weights=MCompositeWeights.resolve(dItems,requestedW,requestedA);
        double[][] decisions=new double[vCount][5];
        for(int v=0;v<vCount;v++){
            decisions[v][0]=vc[v][0];
            decisions[v][1]=vc[v][2]/dItems[v];
            decisions[v][2]=(vc[v][1]+vc[v][2])/dItems[v];
            decisions[v][3]=coefficient(decisions[v][0],decisions[v][1]);
            decisions[v][4]=coefficient(decisions[v][0],decisions[v][2]);
        }
        double[][] relative=new double[vCount][vCount],absolute=new double[vCount][vCount];
        for(int v=0;v<vCount;v++){
            relative[v][v]=decisions[v][1];absolute[v][v]=decisions[v][2];
        }
        double[] composite=MCompositeWeights.metrics(covP,relative,absolute,grand,weights);
        List<List<String>> anovaRows=new ArrayList<>(),componentRows=new ArrayList<>(),decisionRows=new ArrayList<>();
        String[] effects={design.personColumn(),design.itemColumn(),design.personColumn()+"×"+design.itemColumn()};
        for(int v=0;v<vCount;v++){
            String name=design.variables().get(v);
            int[] df={pCount-1,gItems[v]-1,(pCount-1)*(gItems[v]-1)};
            for(int effect=0;effect<3;effect++){
                anovaRows.add(List.of(name,effects[effect],Integer.toString(df[effect]),number(ss[v][effect]),number(ms[v][effect])));
                componentRows.add(List.of(effects[effect],name,name,number(vc[v][effect]),"Variance"));
            }
            for(int other=0;other<v;other++)componentRows.add(List.of(design.personColumn(),
                design.variables().get(other),name,number(covP[other][v]),"Covariance"));
            decisionRows.add(List.of("1",name,number(decisions[v][0]),number(decisions[v][1]),
                number(decisions[v][2]),number(decisions[v][3]),number(decisions[v][4])));
        }
        decisionRows.add(List.of("1",weights.different()?"Composite (MSE)":"Composite",
            number(composite[0]),number(composite[1]),number(composite[2]),
            weights.different()?"N/A":number(composite[3]),weights.different()?"N/A":number(composite[4])));
        Results.Table anova=new Results.Table(List.of("Variable","Effect","df","SS","MS"),anovaRows);
        Results.Table components=new Results.Table(List.of("Effect","Variable A","Variable B","Estimate","Type"),componentRows);
        Results.Table decisionTable=new Results.Table(List.of("Scenario","Variable","Universe σ²",
            weights.different()?"Relative error / MSE":"Relative error σ²δ",
            weights.different()?"Absolute error / MSE":"Absolute error σ²Δ",
            "G coefficient","Φ"),decisionRows);
        StringBuilder report=new StringBuilder("GENOVA Studio · mGENOVA P × I Java pilot\n\n")
            .append(design.description()).append("\nPersons: ").append(pCount)
            .append("; fixed variables: ").append(vCount).append("; observations: ").append(design.observations().size())
            .append("\n\nG-study ANOVA:\n").append(anova.csv())
            .append("\nG-study variance and covariance components:\n").append(components.csv())
            .append(requestedW==null?"\nD-study item counts and default proportional w weights:\n"
                :"\nD-study item counts and supplied w weights:\n");
        for(int v=0;v<vCount;v++)report.append(design.variables().get(v)).append(": n=")
            .append(dItems[v]).append(", w=").append(number(weights.w()[v]))
            .append(", a=").append(number(weights.a()[v])).append('\n');
        report.append("\nD-study results:\n").append(decisionTable.csv());
        String notice="Experimental multivariate P × I random-effects pilot. P is shared across fixed variables; I is nested within each variable. D-study uses proportional or user-supplied w weights; a defaults to w. Benchmarked against mGENOVA manual Appendices B and D, not the original executable. Other multivariate designs are not supported.";
        if(weights.different())notice+=" When a differs from w, composite relative/absolute entries are mean-squared errors (absolute includes squared mean bias); composite G and Φ are not reported.";
        if(Arrays.stream(vc).flatMapToDouble(Arrays::stream).anyMatch(x->x<0))
            notice+=" Negative variance estimates are retained, not truncated.";
        if(Arrays.stream(decisions).flatMapToDouble(Arrays::stream).anyMatch(Double::isNaN)
            ||(!weights.different()&&(Double.isNaN(composite[3])||Double.isNaN(composite[4]))))
            notice+=" A coefficient is undefined when its universe-plus-error denominator is zero.";
        report.append("\n").append(notice).append('\n');
        List<List<String>> setupRows=new ArrayList<>();
        for(int v=0;v<vCount;v++)setupRows.add(List.of(design.variables().get(v),"—",
            Integer.toString(dItems[v]),number(weights.w()[v]),number(weights.a()[v])));
        Results.Table setup=new Results.Table(List.of("Variable","R group","D item count",
            "Universe weight w","Estimation weight a"),setupRows);
        return new Analysis(new Results(anova,components,decisionTable,report.toString(),notice,setup),
            ss,ms,vc,covP,decisions,composite,weights.w(),weights.a());
    }
}
