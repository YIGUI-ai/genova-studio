package org.genova.gui;

import java.util.*;

/** Multivariate P x I: P shared across fixed variables, I nested in variable. */
public final class MpXiDesign {
    public record Observation(int variable,int person,int item,double score) {}
    public record Prepared(String personColumn,String variableColumn,String itemColumn,String scoreColumn,
                           List<String> variables,List<String> persons,int[] itemCounts,
                           List<Observation> observations,double[][][] scores) {
        public Prepared {
            variables=List.copyOf(variables);persons=List.copyOf(persons);
            itemCounts=itemCounts.clone();observations=List.copyOf(observations);
        }
        public String description(){return "Multivariate random G/D-study: P × I with I nested in fixed variable";}
    }
    public static Prepared prepare(DataTable data,String score,String person,String variable,String item){
        if(data==null)throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected=Arrays.asList(score,person,variable,item);
        if(selected.contains(null)||!data.headers().containsAll(selected)||new HashSet<>(selected).size()!=4)
            throw new IllegalArgumentException("Choose four different existing columns for score, person P, fixed variable, and item I.");
        if(data.headers().size()!=4)
            throw new IllegalArgumentException("The multivariate P × I pilot accepts exactly four columns.");
        if(data.rows().size()>5000)
            throw new IllegalArgumentException("The multivariate P × I pilot supports at most 5,000 observations.");
        int si=data.headers().indexOf(score),pi=data.headers().indexOf(person),vi=data.headers().indexOf(variable),ii=data.headers().indexOf(item);
        LinkedHashMap<String,Integer> ps=new LinkedHashMap<>(),vs=new LinkedHashMap<>();
        Map<String,LinkedHashMap<String,Integer>> items=new LinkedHashMap<>();
        Set<List<String>> seen=new HashSet<>();
        List<Observation> observations=new ArrayList<>();
        for(int row=0;row<data.rows().size();row++){
            List<String> record=data.rows().get(row);
            String pv=record.get(pi),vv=record.get(vi),iv=record.get(ii);
            if(pv.isBlank()||vv.isBlank()||iv.isBlank())
                throw new IllegalArgumentException("Row "+(row+2)+" has a blank person, variable, or item ID.");
            double value;
            try{value=Double.parseDouble(record.get(si));}
            catch(NumberFormatException ex){throw new IllegalArgumentException("Row "+(row+2)+" has a nonnumeric score.");}
            if(!Double.isFinite(value))throw new IllegalArgumentException("Row "+(row+2)+" must have a finite score.");
            if(!seen.add(List.of(pv,vv,iv)))
                throw new IllegalArgumentException("Row "+(row+2)+" duplicates a P × I cell within a variable.");
            int p=ps.computeIfAbsent(pv,ignored->ps.size());
            int v=vs.computeIfAbsent(vv,ignored->vs.size());
            var itemMap=items.computeIfAbsent(vv,ignored->new LinkedHashMap<>());
            int i=itemMap.computeIfAbsent(iv,ignored->itemMap.size());
            observations.add(new Observation(v,p,i,value));
        }
        if(ps.size()<2||vs.size()<2||vs.size()>10||items.values().stream().anyMatch(map->map.size()<2))
            throw new IllegalArgumentException("At least two persons and two fixed variables, each with two items, are required (at most ten variables).");
        int[] itemCounts=new int[vs.size()];
        for(var entry:vs.entrySet())itemCounts[entry.getValue()]=items.get(entry.getKey()).size();
        long expected=0;for(int count:itemCounts)expected+=(long)ps.size()*count;
        if(expected!=observations.size())
            throw new IllegalArgumentException("Every person must respond to every item within each fixed variable; missing cells are not supported.");
        double[][][] scores=new double[vs.size()][][];
        for(int v=0;v<vs.size();v++)scores[v]=new double[ps.size()][itemCounts[v]];
        for(Observation o:observations)scores[o.variable()][o.person()][o.item()]=o.score();
        return new Prepared(person,variable,item,score,new ArrayList<>(vs.keySet()),new ArrayList<>(ps.keySet()),itemCounts,observations,scores);
    }
}
