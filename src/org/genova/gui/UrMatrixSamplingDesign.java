package org.genova.gui;

import java.util.*;

/** Complete matrix-sampling (P x I):R design, unequal P and I counts by R. */
public final class UrMatrixSamplingDesign {
    public record Observation(int r,int p,int i,double score) {}
    public record Prepared(String rColumn,String pColumn,String iColumn,String scoreColumn,
                           List<Observation> observations,int rLevels,int pLevels,int iLevels,
                           int[] pPerR,int[] iPerR) {
        public Prepared {
            observations=List.copyOf(observations);
            pPerR=pPerR.clone();iPerR=iPerR.clone();
        }
        public String description(){return "Matrix-sampling random G-study: ("+pColumn+" × "+iColumn+"):"+rColumn;}
    }
    public static Prepared prepare(DataTable data,String score,String r,String p,String i){
        if(data==null)throw new IllegalArgumentException("Import a table before running the analysis.");
        List<String> selected=Arrays.asList(score,r,p,i);
        if(selected.contains(null)||!data.headers().containsAll(selected)||new HashSet<>(selected).size()!=4)
            throw new IllegalArgumentException("Choose four different existing columns for score, group R, object P, and facet I.");
        if(data.headers().size()!=4)
            throw new IllegalArgumentException("The matrix-sampling pilot accepts exactly four columns.");
        if(data.rows().size()>5000)
            throw new IllegalArgumentException("The matrix-sampling pilot supports at most 5,000 observations.");
        int si=data.headers().indexOf(score),ri=data.headers().indexOf(r),pi=data.headers().indexOf(p),ii=data.headers().indexOf(i);
        Map<String,Integer> rs=new LinkedHashMap<>();
        Map<List<String>,Integer> ps=new LinkedHashMap<>(),is=new LinkedHashMap<>();
        Map<Integer,Set<Integer>> pByR=new HashMap<>(),iByR=new HashMap<>();
        Set<Long> seen=new HashSet<>();
        List<Observation> observations=new ArrayList<>();
        for(int row=0;row<data.rows().size();row++){
            List<String> record=data.rows().get(row);
            if(record.get(ri).isBlank()||record.get(pi).isBlank()||record.get(ii).isBlank())
                throw new IllegalArgumentException("Row "+(row+2)+" has a blank group, object, or facet ID.");
            double value;
            try{value=Double.parseDouble(record.get(si));}
            catch(NumberFormatException ex){throw new IllegalArgumentException("Row "+(row+2)+" has a nonnumeric score.");}
            if(!Double.isFinite(value))throw new IllegalArgumentException("Row "+(row+2)+" must have a finite score.");
            int rv=rs.computeIfAbsent(record.get(ri),ignored->rs.size());
            int pv=ps.computeIfAbsent(List.of(record.get(ri),record.get(pi)),ignored->ps.size());
            int iv=is.computeIfAbsent(List.of(record.get(ri),record.get(ii)),ignored->is.size());
            pByR.computeIfAbsent(rv,ignored->new HashSet<>()).add(pv);
            iByR.computeIfAbsent(rv,ignored->new HashSet<>()).add(iv);
            if(!seen.add(((long)pv<<32)|(iv&0xffffffffL)))
                throw new IllegalArgumentException("Row "+(row+2)+" duplicates a P × I cell within an R group.");
            observations.add(new Observation(rv,pv,iv,value));
        }
        if(rs.size()<2||pByR.values().stream().anyMatch(levels->levels.size()<2)
            ||iByR.values().stream().anyMatch(levels->levels.size()<2))
            throw new IllegalArgumentException("At least two R groups, each with two P and two I levels, are required.");
        int[] pPerR=new int[rs.size()],iPerR=new int[rs.size()],nPerR=new int[rs.size()];
        for(int rv=0;rv<rs.size();rv++){
            pPerR[rv]=pByR.get(rv).size();iPerR[rv]=iByR.get(rv).size();
        }
        for(Observation o:observations)nPerR[o.r()]++;
        for(int rv=0;rv<rs.size();rv++)if((long)pPerR[rv]*iPerR[rv]!=nPerR[rv])
            throw new IllegalArgumentException("Every P × I cell within each R group must occur exactly once in the matrix-sampling pilot.");
        return new Prepared(r,p,i,score,observations,rs.size(),ps.size(),is.size(),pPerR,iPerR);
    }
}
