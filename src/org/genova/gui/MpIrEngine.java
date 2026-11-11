package org.genova.gui;

import java.util.*;

/** Limited mGENOVA P x (I:R) random-effects G/D pilot. */
public final class MpIrEngine {
    /** Per-variable effects in order P, R, I:R, P×R, P×I:R. */
    public record Analysis(Results results,double[][] ss,double[][] ms,double[][] vc,
                           double[][] covP,double[][] covR,double[][] covPR,
                           double[][] decisions,double[] composite,double[] weights,double[] estimationWeights) {}
    private static String number(double x){return Double.isNaN(x)?"Undefined":String.format(Locale.ROOT,"%.8f",x);}
    private static double coefficient(double universe,double error){
        double denominator=universe+error;
        return denominator==0?Double.NaN:universe/denominator;
    }
    public static Results run(MpIrDesign.Prepared design,int[][] dItems,double[] weights){
        return calculate(design,dItems,weights).results();
    }
    public static Results run(MpIrDesign.Prepared design,int[][] dItems,double[] wWeights,double[] aWeights){
        return calculate(design,dItems,wWeights,aWeights).results();
    }
    public static Analysis calculate(MpIrDesign.Prepared design,int[][] requestedItems,double[] requestedWeights){
        return calculate(design,requestedItems,requestedWeights,null);
    }
    public static Analysis calculate(MpIrDesign.Prepared design,int[][] requestedItems,
                                     double[] requestedW,double[] requestedA){
        int vCount=design.variables().size(),pCount=design.persons().size(),rCount=design.groups().size();
        int[][] gItems=design.itemCounts();
        int[][] dItems=requestedItems==null?Arrays.stream(gItems).map(int[]::clone).toArray(int[][]::new)
            :Arrays.stream(requestedItems).map(int[]::clone).toArray(int[][]::new);
        if(dItems.length!=vCount)throw new IllegalArgumentException("Provide one D-study R-group item-count row per fixed variable.");
        int dRCount=dItems[0].length;
        if(dRCount<1||dRCount>100)throw new IllegalArgumentException("A D-study must have between one and 100 R groups.");
        for(int[] row:dItems)if(row.length!=dRCount||Arrays.stream(row).anyMatch(value->value<1||value>100_000))
            throw new IllegalArgumentException("Provide one positive D-study item count per R group and fixed variable.");
        double[][] ss=new double[vCount][5],ms=new double[vCount][5],vc=new double[vCount][5];
        double[][][] cellMeans=new double[vCount][pCount][rCount];
        int[] gTotals=new int[vCount],dTotals=new int[vCount];
        double[] grandMeans=new double[vCount];
        for(int v=0;v<vCount;v++){
            int n=Arrays.stream(gItems[v]).sum();gTotals[v]=n;dTotals[v]=Arrays.stream(dItems[v]).sum();
            double[][] itemMeans=new double[rCount][];
            double[] personMeans=new double[pCount],groupMeans=new double[rCount];
            for(int r=0;r<rCount;r++)itemMeans[r]=new double[gItems[v][r]];
            double total=0;
            for(int p=0;p<pCount;p++)for(int r=0;r<rCount;r++){
                double cellTotal=0;
                for(int i=0;i<gItems[v][r];i++){
                    double x=design.scores()[v][p][r][i];
                    cellTotal+=x;itemMeans[r][i]+=x;
                }
                cellMeans[v][p][r]=cellTotal/gItems[v][r];
                personMeans[p]+=cellTotal;groupMeans[r]+=cellTotal;total+=cellTotal;
            }
            double grand=total/(pCount*n);
            grandMeans[v]=grand;
            for(int p=0;p<pCount;p++)personMeans[p]/=n;
            for(int r=0;r<rCount;r++){
                groupMeans[r]/=pCount*gItems[v][r];
                for(int i=0;i<gItems[v][r];i++)itemMeans[r][i]/=pCount;
            }
            for(int p=0;p<pCount;p++)ss[v][0]+=n*Math.pow(personMeans[p]-grand,2);
            for(int r=0;r<rCount;r++){
                ss[v][1]+=pCount*gItems[v][r]*Math.pow(groupMeans[r]-grand,2);
                for(int i=0;i<gItems[v][r];i++)ss[v][2]+=pCount*Math.pow(itemMeans[r][i]-groupMeans[r],2);
                for(int p=0;p<pCount;p++){
                    ss[v][3]+=gItems[v][r]*Math.pow(cellMeans[v][p][r]-personMeans[p]-groupMeans[r]+grand,2);
                    for(int i=0;i<gItems[v][r];i++)
                        ss[v][4]+=Math.pow(design.scores()[v][p][r][i]-cellMeans[v][p][r]
                            -itemMeans[r][i]+groupMeans[r],2);
                }
            }
            int[] df={pCount-1,rCount-1,n-rCount,(pCount-1)*(rCount-1),(pCount-1)*(n-rCount)};
            for(int e=0;e<5;e++)ms[v][e]=ss[v][e]/df[e];
            double sumSquares=0;for(int count:gItems[v])sumSquares+=(double)count*count;
            double b=sumSquares/n,n0=(n-b)/(rCount-1);
            vc[v][4]=ms[v][4];
            vc[v][2]=(ms[v][2]-ms[v][4])/pCount;
            vc[v][3]=(ms[v][3]-ms[v][4])/n0;
            vc[v][1]=(ms[v][1]-ms[v][2]-ms[v][3]+ms[v][4])/(pCount*n0);
            vc[v][0]=(ms[v][0]-ms[v][4]-b*vc[v][3])/n;
            for(double x:vc[v])if(!Double.isFinite(x))
                throw new IllegalArgumentException("Score magnitudes are too large for finite variance components.");
        }
        double[][] covP=new double[vCount][vCount],covR=new double[vCount][vCount],covPR=new double[vCount][vCount];
        for(int v=0;v<vCount;v++){
            covP[v][v]=vc[v][0];covR[v][v]=vc[v][1];covPR[v][v]=vc[v][3];
        }
        for(int a=0;a<vCount;a++)for(int b=0;b<a;b++){
            double[] rowA=new double[pCount],rowB=new double[pCount],colA=new double[rCount],colB=new double[rCount];
            double grandA=0,grandB=0;
            for(int p=0;p<pCount;p++)for(int r=0;r<rCount;r++){
                double xa=cellMeans[a][p][r],xb=cellMeans[b][p][r];
                rowA[p]+=xa/rCount;rowB[p]+=xb/rCount;
                colA[r]+=xa/pCount;colB[r]+=xb/pCount;
                grandA+=xa/(pCount*rCount);grandB+=xb/(pCount*rCount);
            }
            double mpP=0,mpR=0,mpPR=0;
            for(int p=0;p<pCount;p++)mpP+=(rowA[p]-grandA)*(rowB[p]-grandB);
            mpP*=rCount/(double)(pCount-1);
            for(int r=0;r<rCount;r++)mpR+=(colA[r]-grandA)*(colB[r]-grandB);
            mpR*=pCount/(double)(rCount-1);
            for(int p=0;p<pCount;p++)for(int r=0;r<rCount;r++)
                mpPR+=(cellMeans[a][p][r]-rowA[p]-colA[r]+grandA)
                    *(cellMeans[b][p][r]-rowB[p]-colB[r]+grandB);
            mpPR/=(pCount-1)*(rCount-1);
            covP[a][b]=covP[b][a]=(mpP-mpPR)/rCount;
            covR[a][b]=covR[b][a]=(mpR-mpPR)/pCount;
            covPR[a][b]=covPR[b][a]=mpPR;
            if(!Double.isFinite(covP[a][b])||!Double.isFinite(covR[a][b])||!Double.isFinite(covPR[a][b]))
                throw new IllegalArgumentException("Score magnitudes are too large for finite covariance components.");
        }
        MCompositeWeights.Resolved weights=MCompositeWeights.resolve(dTotals,requestedW,requestedA);
        double[][] relative=new double[vCount][vCount],absolute=new double[vCount][vCount];
        for(int a=0;a<vCount;a++)for(int b=0;b<vCount;b++){
            double dot=0;
            for(int r=0;r<dRCount;r++)dot+=(double)dItems[a][r]*dItems[b][r];
            double factor=dot/((double)dTotals[a]*dTotals[b]);
            relative[a][b]=covPR[a][b]*factor;
            absolute[a][b]=(covPR[a][b]+covR[a][b])*factor;
            if(a==b){
                relative[a][b]+=vc[a][4]/dTotals[a];
                absolute[a][b]+=(vc[a][2]+vc[a][4])/dTotals[a];
            }
        }
        double[][] decisions=new double[vCount][5];
        for(int v=0;v<vCount;v++){
            decisions[v][0]=vc[v][0];decisions[v][1]=relative[v][v];decisions[v][2]=absolute[v][v];
            decisions[v][3]=coefficient(decisions[v][0],decisions[v][1]);
            decisions[v][4]=coefficient(decisions[v][0],decisions[v][2]);
        }
        double[] composite=MCompositeWeights.metrics(covP,relative,absolute,grandMeans,weights);
        List<List<String>> anovaRows=new ArrayList<>(),componentRows=new ArrayList<>(),decisionRows=new ArrayList<>();
        String[] effects={design.personColumn(),design.groupColumn(),design.itemColumn()+":"+design.groupColumn(),
            design.personColumn()+"×"+design.groupColumn(),design.personColumn()+"×"+design.itemColumn()+":"+design.groupColumn()};
        for(int v=0;v<vCount;v++){
            String name=design.variables().get(v);int n=gTotals[v];
            int[] df={pCount-1,rCount-1,n-rCount,(pCount-1)*(rCount-1),(pCount-1)*(n-rCount)};
            for(int e=0;e<5;e++){
                anovaRows.add(List.of(name,effects[e],Integer.toString(df[e]),number(ss[v][e]),number(ms[v][e])));
                componentRows.add(List.of(effects[e],name,name,number(vc[v][e]),"Variance"));
            }
            for(int other=0;other<v;other++)for(int e:new int[]{0,1,3}){
                double covariance=e==0?covP[other][v]:e==1?covR[other][v]:covPR[other][v];
                componentRows.add(List.of(effects[e],design.variables().get(other),name,number(covariance),"Covariance"));
            }
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
        StringBuilder report=new StringBuilder("GENOVA Studio · mGENOVA P × (I:R) Java pilot\n\n")
            .append(design.description()).append("\nPersons: ").append(pCount).append("; R groups: ").append(rCount)
            .append("; fixed variables: ").append(vCount).append("; observations: ").append(design.observations().size())
            .append("\n\nG-study ANOVA:\n").append(anova.csv())
            .append("\nG-study variance and covariance components:\n").append(components.csv())
            .append(requestedW==null?"\nD-study counts and proportional w weights:\n"
                :"\nD-study counts and supplied w weights:\n");
        for(int v=0;v<vCount;v++){
            report.append(design.variables().get(v)).append(": w=").append(number(weights.w()[v]))
                .append(", a=").append(number(weights.a()[v]));
            for(int r=0;r<dRCount;r++)report.append(", ")
                .append(r<rCount?design.groups().get(r):"R"+(r+1)).append("=").append(dItems[v][r]);
            report.append('\n');
        }
        report.append("\nD-study results:\n").append(decisionTable.csv());
        String notice="Experimental multivariate P × (I:R) random-effects pilot. P and R are shared across fixed variables; I is nested within R and variable. Benchmarked against mGENOVA manual Appendix C, not the original executable. Missing cells and other multivariate designs are not supported.";
        if(weights.different())notice+=" When a differs from w, composite relative/absolute entries are mean-squared errors (absolute includes squared mean bias); composite G and Φ are not reported. This unequal-weight case has not been compared with the original executable.";
        if(Arrays.stream(vc).flatMapToDouble(Arrays::stream).anyMatch(x->x<0))
            notice+=" Negative variance estimates are retained, not truncated.";
        if(Arrays.stream(decisions).flatMapToDouble(Arrays::stream).anyMatch(Double::isNaN)
            ||(!weights.different()&&(Double.isNaN(composite[3])||Double.isNaN(composite[4]))))
            notice+=" A coefficient is undefined when its universe-plus-error denominator is zero.";
        report.append('\n').append(notice).append('\n');
        List<List<String>> setupRows=new ArrayList<>();
        for(int v=0;v<vCount;v++)for(int r=0;r<dRCount;r++)setupRows.add(List.of(
            design.variables().get(v),r<rCount?design.groups().get(r):"R"+(r+1),
            Integer.toString(dItems[v][r]),number(weights.w()[v]),number(weights.a()[v])));
        Results.Table setup=new Results.Table(List.of("Variable","R group","D item count",
            "Universe weight w","Estimation weight a"),setupRows);
        return new Analysis(new Results(anova,components,decisionTable,report.toString(),notice,setup),
            ss,ms,vc,covP,covR,covPR,decisions,composite,weights.w(),weights.a());
    }
}
