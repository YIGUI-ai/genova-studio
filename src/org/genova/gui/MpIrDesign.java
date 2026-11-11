package org.genova.gui;

import java.util.*;

/** Multivariate P x (I:R): P and R shared, I nested in R and variable. */
public final class MpIrDesign {
    public record Observation(int variable,int person,int group,int item,double score) {}
    public record Prepared(String personColumn,String variableColumn,String groupColumn,
                           String itemColumn,String scoreColumn,List<String> variables,
                           List<String> persons,List<String> groups,int[][] itemCounts,
                           List<Observation> observations,double[][][][] scores) {
        public Prepared {
            variables=List.copyOf(variables);persons=List.copyOf(persons);groups=List.copyOf(groups);
            itemCounts=Arrays.stream(itemCounts).map(int[]::clone).toArray(int[][]::new);
            observations=List.copyOf(observations);
        }
        public String description(){return "Multivariate random G/D-study: P × (I:R), with R shared and I nested in R and fixed variable";}
    }

    public static Prepared prepare(DataTable data,String score,String person,String variable,String group,String item){
        if(data==null)throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected=Arrays.asList(score,person,variable,group,item);
        if(selected.contains(null)||!data.headers().containsAll(selected)||new HashSet<>(selected).size()!=5)
            throw new IllegalArgumentException("Choose five different existing columns for score, person P, fixed variable, group R, and item I.");
        if(data.headers().size()!=5)
            throw new IllegalArgumentException("The multivariate P × (I:R) pilot accepts exactly five columns.");
        if(data.rows().size()>5000)
            throw new IllegalArgumentException("The multivariate P × (I:R) pilot supports at most 5,000 observations.");
        int si=data.headers().indexOf(score),pi=data.headers().indexOf(person),vi=data.headers().indexOf(variable),
            ri=data.headers().indexOf(group),ii=data.headers().indexOf(item);
        LinkedHashMap<String,Integer> ps=new LinkedHashMap<>(),vs=new LinkedHashMap<>(),rs=new LinkedHashMap<>();
        Map<List<String>,LinkedHashMap<String,Integer>> itemMaps=new LinkedHashMap<>();
        Set<List<String>> seen=new HashSet<>();
        List<Observation> observations=new ArrayList<>();
        for(int row=0;row<data.rows().size();row++){
            List<String> record=data.rows().get(row);
            String pv=record.get(pi),vv=record.get(vi),rv=record.get(ri),iv=record.get(ii);
            if(pv.isBlank()||vv.isBlank()||rv.isBlank()||iv.isBlank())
                throw new IllegalArgumentException("Row "+(row+2)+" has a blank person, variable, group, or item ID.");
            double value;
            try{value=Double.parseDouble(record.get(si));}
            catch(NumberFormatException ex){throw new IllegalArgumentException("Row "+(row+2)+" has a nonnumeric score.");}
            if(!Double.isFinite(value))throw new IllegalArgumentException("Row "+(row+2)+" must have a finite score.");
            if(!seen.add(List.of(pv,vv,rv,iv)))
                throw new IllegalArgumentException("Row "+(row+2)+" duplicates a P × I:R cell within a variable.");
            int p=ps.computeIfAbsent(pv,ignored->ps.size());
            int v=vs.computeIfAbsent(vv,ignored->vs.size());
            int r=rs.computeIfAbsent(rv,ignored->rs.size());
            var map=itemMaps.computeIfAbsent(List.of(vv,rv),ignored->new LinkedHashMap<>());
            int i=map.computeIfAbsent(iv,ignored->map.size());
            observations.add(new Observation(v,p,r,i,value));
        }
        if(ps.size()<2||vs.size()<2||vs.size()>10||rs.size()<2||rs.size()>20)
            throw new IllegalArgumentException("At least two persons, two fixed variables, and two R groups are required (at most ten variables and twenty groups).");
        int[][] counts=new int[vs.size()][rs.size()];
        for(var ve:vs.entrySet())for(var re:rs.entrySet()){
            var map=itemMaps.get(List.of(ve.getKey(),re.getKey()));
            if(map==null||map.size()<2)
                throw new IllegalArgumentException("Every fixed variable must contain every R group with at least two items per group.");
            counts[ve.getValue()][re.getValue()]=map.size();
        }
        long expected=0;
        for(int[] row:counts)for(int count:row)expected+=(long)ps.size()*count;
        if(expected!=observations.size())
            throw new IllegalArgumentException("Every person must respond to every item within every fixed-variable/R group; missing cells are not supported.");
        double[][][][] scores=new double[vs.size()][ps.size()][rs.size()][];
        for(int v=0;v<vs.size();v++)for(int p=0;p<ps.size();p++)for(int r=0;r<rs.size();r++)
            scores[v][p][r]=new double[counts[v][r]];
        for(Observation o:observations)scores[o.variable()][o.person()][o.group()][o.item()]=o.score();
        return new Prepared(person,variable,group,item,score,new ArrayList<>(vs.keySet()),
            new ArrayList<>(ps.keySet()),new ArrayList<>(rs.keySet()),counts,observations,scores);
    }
}
