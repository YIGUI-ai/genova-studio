package org.genova.gui;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.imageio.ImageIO;

public final class SelfTest {
    private static int checks;
    private static String category = "Uncategorized";
    private static final Map<String,Integer> categoryChecks = new LinkedHashMap<>();
    private static void category(String value) { category = value; }
    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
        checks++;
        categoryChecks.merge(category, 1, Integer::sum);
    }
    private static void near(String x, double value, String name) { check(Math.abs(Double.parseDouble(x)-value)<0.000006,name+": "+x); }
    private static void reject(Runnable r, String text) {
        try { r.run(); throw new AssertionError("Did not reject: "+text); }
        catch (IllegalArgumentException ex) { check(ex.getMessage().contains(text),"Wrong validation: "+ex.getMessage()); }
    }
    private static List<Design.Facet> nested() { return List.of(new Design.Facet("rater","",false,""),new Design.Facet("item","rater",false,"1,2,3,4")); }
    private static Design.Prepared prep(DataTable t) { return Design.prepare(t,"score","person",nested(),true); }
    public static void main(String[] args) throws Exception {
        category("Balanced GENOVA reference and D-study visualization");
        DataTable t;
        try (var in = SelfTest.class.getResourceAsStream("/samples/official_nested.csv")) { t = DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
        Design.Prepared plan = prep(t);
        Results result = Engine.run(plan);
        check(result.anova().rows().size()==5,"ANOVA rows");
        check(result.components().rows().size()==5,"variance rows");
        double[] v = {0.4731481,0.3251543,0.6475309,0.5595679,2.3802469};
        for (int i = 0; i < v.length; i++) near(result.components().rows().get(i).get(2),v[i],"official component");
        // The same 10-person synthetic data are printed in urGENOVA 2.1 Appendix C.
        double[] manualCSS={92.66667,48.2,79.7,83.13333,192.8};
        double[] manualCMS={10.29630,24.1,8.85556,4.61852,2.38025};
        for(int i=0;i<5;i++){
            near(result.anova().rows().get(i).get(2),manualCSS[i],"manual C SS cross-engine");
            near(result.anova().rows().get(i).get(3),manualCMS[i],"manual C MS cross-engine");
        }
        double[] gs={0.32562,0.44790,0.51199,0.55144}, ph={0.26622,0.37177,0.42839,0.46370};
        for (int i=0;i<4;i++) { near(result.decisions().rows().get(i).get(5),gs[i],"official G"); near(result.decisions().rows().get(i).get(6),ph[i],"official Phi"); }
        DStudyGrid grid = DStudyGrid.run(t,"score","person",plan,1,5,1,5);
        check(grid.xLevels().length==5 && grid.yLevels().length==5,"25-cell D-study grid");
        for(int i=0;i<4;i++) {
            check(Math.abs(grid.g()[i][2]-gs[i])<0.000006,"heatmap G agrees with official scenario "+i);
            check(Math.abs(grid.phi()[i][2]-ph[i])<0.000006,"heatmap Phi agrees with official scenario "+i);
        }
        check(grid.csv().lines().count()==26,"grid CSV includes all points");
        SurfacePanel surface = new SurfacePanel(() -> {}, () -> {});
        surface.setAvailable(true); surface.setGrid(grid);
        var surfaceImage = surface.image(800, 600);
        int colored = 0;
        for (int y=0; y<600; y+=8) for (int x=0; x<800; x+=8) {
            int rgb = surfaceImage.getRGB(x,y);
            int red = (rgb>>16)&255, green = (rgb>>8)&255, blue = rgb&255;
            if (Math.max(red, Math.max(green,blue)) - Math.min(red, Math.min(green,blue)) > 35) colored++;
        }
        check(colored > 100,"3D surface renders colored grid from engine coefficients");
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        check(ImageIO.write(surfaceImage,"png",png) && png.size()>10000,"3D plot exports a PNG image");
        check(!result.report().contains("CONTROL CARD INPUT LISTING") && !result.report().contains("DEFFECT"),"report hides control cards");
        List<List<String>> shuffled = new ArrayList<>(t.rows()); Collections.shuffle(shuffled,new Random(42));
        check(prep(new DataTable(t.headers(),shuffled)).cards().equals(plan.cards()),"input row order invariance");
        List<List<String>> reverse = t.rows().stream().map(r -> List.of(r.get(3),r.get(2),r.get(0),r.get(1))).toList();
        check(prep(new DataTable(List.of("score","item","person","rater"),reverse)).cards().equals(plan.cards()),"column order invariance");
        reject(()->prep(new DataTable(t.headers(),t.rows().subList(1,t.rows().size()))),"missing combinations");
        List<List<String>> dup = new ArrayList<>(t.rows()); dup.add(t.rows().get(0)); reject(()->prep(new DataTable(t.headers(),dup)),"duplicates a design cell");
        List<List<String>> missing = new ArrayList<>(t.rows()); missing.set(0,List.of("P01","R1","I1","")); reject(()->prep(new DataTable(t.headers(),missing)),"not numeric");
        List<List<String>> nan = new ArrayList<>(t.rows()); nan.set(0,List.of("P01","R1","I1","NaN")); reject(()->prep(new DataTable(t.headers(),nan)),"finite number");
        reject(()->Design.prepare(t,"score","person",List.of(new Design.Facet("rater","item",false,""),new Design.Facet("item","rater",false,"")),true),"cycle");
        reject(()->Design.prepare(t,"score","person",List.of(new Design.Facet("rater","",false,"2,3"),new Design.Facet("item","rater",false,"1,2,3")),true),"same number of values");
        reject(()->Design.prepare(t,"score","person",List.of(new Design.Facet("rater","",true,"2"),new Design.Facet("item","rater",false,"")),true),"Fixed facet");
        List<List<String>> uneven = t.rows().stream().filter(r -> !(r.get(1).equals("R1") && r.get(2).equals("I4"))).toList();
        reject(()->prep(new DataTable(t.headers(),uneven)),"differs across parent groups");
        category("Tabular import");
        DataTable csv=DataTable.parse("\uFEFFperson,rater,score\r\n\"A, B\",X,1\r\n\"C\"\"D\",Y,2\r\n");
        check(csv.rows().get(0).get(0).equals("A, B"),"quoted comma"); check(csv.rows().get(1).get(0).equals("C\"D"),"escaped quote");
        check(DataTable.parse("人\t评分者\t分数\n甲\t乙\t1\n").rows().get(0).get(0).equals("甲"),"Unicode TSV");
        reject(()->DataTable.parse("p,p,s\na,b,1"),"unique column names"); reject(()->DataTable.parse("p,r,s\na,b,\"1"),"unclosed quote");

        category("Balanced GENOVA independent and robustness checks");
        // Independent two-way ANOVA expectations for a crossed P x R design.
        DataTable crossed=DataTable.parse("person,rater,score\nA,X,1\nA,Y,2\nB,X,3\nB,Y,5\nC,X,6\nC,Y,8\n");
        Results c=Engine.run(Design.prepare(crossed,"score","person",List.of(new Design.Facet("rater","",false,"1,2")),true));
        near(c.components().rows().get(0).get(2),7.5,"crossed person VC");
        near(c.components().rows().get(1).get(2),4.0/3,"crossed rater VC");
        near(c.components().rows().get(2).get(2),1.0/6,"crossed residual VC");
        near(c.decisions().rows().get(1).get(5),90.0/91,"crossed G");
        near(c.decisions().rows().get(1).get(6),10.0/11,"crossed Phi");
        Results fixed=Engine.run(Design.prepare(t,"score","person",List.of(new Design.Facet("rater","",true,""),new Design.Facet("item","rater",false,"1,2")),true));
        check(fixed.components().rows().stream().anyMatch(r->r.get(5).contains("QFM")),"fixed QFM parsed");
        check(fixed.decisions().rows().size()==2,"fixed scenarios");
        check(Engine.run(Design.prepare(t,"score","person",nested(),false)).decisions().rows().isEmpty(),"G only");
        StringBuilder chainCsv = new StringBuilder("person,occasion,task,item,score\n");
        for(int p=1;p<=2;p++) for(int o=1;o<=2;o++) for(int h=1;h<=2;h++) for(int i=1;i<=2;i++)
            chainCsv.append("P").append(p).append(",O").append(o).append(",T").append(h)
                    .append(",I").append(i).append(',').append(10*p+3*o+2*h+i+p*o*h+i*p).append('\n');
        DataTable chain = DataTable.parse(chainCsv.toString());
        List<Design.Facet> chainFacets = List.of(new Design.Facet("occasion","",false,""),
            new Design.Facet("task","occasion",false,""),new Design.Facet("item","task",false,""));
        reject(()->Design.prepare(chain,"score","person",chainFacets,true),"D-study for nesting chains is not supported");
        check(Engine.run(Design.prepare(chain,"score","person",chainFacets,false)).components().rows().size()==7,
            "nesting-chain G-study remains available");
        Locale prior=Locale.getDefault(); Locale.setDefault(Locale.GERMANY);
        Results german=Engine.run(plan); Locale.setDefault(prior);
        check(german.decisions().equals(result.decisions()),"locale stability");
        check(Engine.run(plan).components().equals(result.components()),"independent repeated runs");
        category("urGENOVA one-way pilot");
        DataTable ur = DataTable.parse("person,replicate,score\nA,A1,1\nA,A2,2\nB,B1,3\nB,B2,5\nB,B3,7\nC,C1,6\nC,C2,8\n");
        UrDesign.Prepared urPlan = UrDesign.prepare(ur,"score","person","replicate");
        Results urResult = UrEngine.run(urPlan);
        near(urResult.anova().rows().get(0).get(2),437.0/14,"unbalanced between SS");
        near(urResult.anova().rows().get(1).get(2),10.5,"unbalanced within SS");
        near(urResult.components().rows().get(0).get(2),5.6796875,"unbalanced object variance");
        near(urResult.components().rows().get(1).get(2),2.625,"unbalanced residual variance");
        check(urResult.decisions().rows().isEmpty(),"ur pilot has no D-study");
        List<List<String>> urShuffled = new ArrayList<>(ur.rows()); Collections.shuffle(urShuffled,new Random(19));
        check(UrEngine.run(UrDesign.prepare(new DataTable(ur.headers(),urShuffled),"score","person","replicate")).components().equals(urResult.components()),"ur row-order invariance");
        List<List<String>> urDuplicate = new ArrayList<>(ur.rows()); urDuplicate.add(ur.rows().get(0));
        reject(()->UrDesign.prepare(new DataTable(ur.headers(),urDuplicate),"score","person","replicate"),"duplicates an object/replicate cell");
        reject(()->UrDesign.prepare(ur,"score","person","person"),"three different existing columns");
        reject(()->UrDesign.prepare(DataTable.parse("person,replicate,group,score\nA,A1,X,1\nA,A2,X,2\nB,B1,Y,3\nB,B2,Y,4\n"),"score","person","replicate"),"exactly three columns");
        Results urNegative = UrEngine.run(UrDesign.prepare(DataTable.parse("person,replicate,score\nA,A1,1\nA,A2,3\nB,B1,2\nB,B2,4\n"),"score","person","replicate"));
        near(urNegative.components().rows().get(0).get(2),-0.5,"negative component retained");
        check(urNegative.notices().contains("not been truncated"),"negative ur warning");
        category("urGENOVA two-way pilot (manual D/E)");
        DataTable manualD, manualE;
        try (var in = SelfTest.class.getResourceAsStream("/samples/ur_two_way_manual_d.csv"))
            { manualD = DataTable.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)); }
        try (var in = SelfTest.class.getResourceAsStream("/samples/ur_two_way_manual_e.csv"))
            { manualE = DataTable.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)); }
        UrTwoWayDesign.Prepared twoD = UrTwoWayDesign.prepare(manualD,"score","h","p","replicate");
        UrTwoWayDesign.Prepared twoE = UrTwoWayDesign.prepare(manualE,"score","h","p","replicate");
        UrTwoWayEngine.Analysis analysisD = UrTwoWayEngine.calculate(twoD);
        UrTwoWayEngine.Analysis analysisE = UrTwoWayEngine.calculate(twoE);
        for (int i = 0; i < 5; i++) near(Double.toString(analysisD.t()[i]),new double[]{400,398,448,458,392}[i],"manual D T term");
        for (int i = 0; i < 4; i++) {
            near(Double.toString(analysisD.ss()[i]),new double[]{8,6,42,10}[i],"manual D SS");
            near(Double.toString(analysisD.ms()[i]),new double[]{8,3,21,5}[i],"manual D MS");
            near(Double.toString(analysisD.vc()[i]),new double[]{-3.75702,-7.70248,13.30248,5}[i],"manual D VC");
            near(Double.toString(analysisE.vc()[i]),new double[]{-4.67840,-11.16287,13.24983,10}[i],"manual E VC");
        }
        for (int i = 0; i < 5; i++) near(Double.toString(analysisE.t()[i]),new double[]{364,362,412,422,2500.0/7}[i],"manual E T term");
        near(Double.toString(analysisD.et()[1][1]),13.0/3,"manual D ET h/ph coefficient");
        near(Double.toString(analysisE.et()[2][0]),6,"manual E nominal empty-cell ET");
        near(Double.toString(analysisE.ems()[2][1]),31.0/84,"manual E EMS ph coefficient");
        check(twoE.emptyCells()==1 && analysisE.results().decisions().rows().isEmpty(),"manual E empty cell and no D-study");
        DataTable balancedTwo = DataTable.parse("p,h,replicate,score\nP1,H1,R1,1\nP1,H1,R2,2\nP1,H2,R1,3\nP1,H2,R2,4\nP2,H1,R1,5\nP2,H1,R2,6\nP2,H2,R1,7\nP2,H2,R2,8\n");
        UrTwoWayEngine.Analysis balancedCheck = UrTwoWayEngine.calculate(UrTwoWayDesign.prepare(balancedTwo,"score","h","p","replicate"));
        for (int i = 0; i < 4; i++) {
            near(Double.toString(balancedCheck.ss()[i]),new double[]{32,8,0,2}[i],"balanced two-way SS");
            near(Double.toString(balancedCheck.vc()[i]),new double[]{8,2,-0.25,0.5}[i],"balanced two-way VC");
        }
        List<List<String>> shuffledTwo = new ArrayList<>(manualD.rows()); Collections.shuffle(shuffledTwo,new Random(37));
        UrTwoWayEngine.Analysis reordered = UrTwoWayEngine.calculate(UrTwoWayDesign.prepare(new DataTable(manualD.headers(),shuffledTwo),"score","h","p","replicate"));
        for (int i = 0; i < 4; i++) near(Double.toString(reordered.vc()[i]),analysisD.vc()[i],"two-way row-order invariance");
        List<List<String>> duplicateTwo = new ArrayList<>(manualD.rows()); duplicateTwo.add(manualD.rows().get(0));
        reject(()->UrTwoWayDesign.prepare(new DataTable(manualD.headers(),duplicateTwo),"score","h","p","replicate"),"duplicates a crossed-facet/object/replicate cell");
        reject(()->UrTwoWayDesign.prepare(manualD,"score","h","h","replicate"),"four different existing columns");
        reject(()->UrTwoWayDesign.prepare(new DataTable(manualD.headers(),manualD.rows().subList(0,6)),"score","h","p","replicate"),"residual degree of freedom");
        DataTable twoEmpty = DataTable.parse("p,h,replicate,score\nP1,H1,R1,1\nP1,H1,R2,2\nP2,H1,R1,3\nP2,H1,R2,4\nP2,H2,R1,5\nP2,H2,R2,6\nP2,H3,R1,7\nP2,H3,R2,8\n");
        reject(()->UrTwoWayDesign.prepare(twoEmpty,"score","h","p","replicate"),"at most one empty");
        category("urGENOVA nested-cross pilot (manual A)");
        DataTable manualA;
        try (var in = SelfTest.class.getResourceAsStream("/samples/ur_nested_cross_manual_a.csv"))
            { manualA = DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
        UrNestedCrossDesign.Prepared nestedCross = UrNestedCrossDesign.prepare(manualA,"score","p","c","r","i");
        UrNestedCrossEngine.Analysis analysisA = UrNestedCrossEngine.calculate(nestedCross);
        double[] manualAT = {2484.3,2501.72222,2572.16667,2538.05,2612.2,2567.41667,2673.66667,2691.16667,2976};
        for (int j = 0; j < 9; j++) near(Double.toString(analysisA.t()[j]),manualAT[j],"manual A T term");
        double[] manualASS = {17.42222,70.44444,53.75,74.15,11.94444,32.1,53.30556,178.58333};
        double[] manualAMS = {17.42222,8.80556,26.875,8.23889,5.97222,3.56667,3.33160,2.48032};
        double[] manualAVC = {0.09835,0.44198,0.42936,0.45817,0.08635,0.22632,0.22701,2.48032};
        for (int j = 0; j < 8; j++) {
            near(Double.toString(analysisA.ss()[j]),manualASS[j],"manual A SS");
            near(Double.toString(analysisA.ms()[j]),manualAMS[j],"manual A MS");
            near(Double.toString(analysisA.vc()[j]),manualAVC[j],"manual A VC");
        }
        near(Double.toString(analysisA.ems()[0][7]),1,"manual A EMS c residual");
        near(Double.toString(analysisA.ems()[0][6]),4.5,"manual A EMS c pr:c");
        near(Double.toString(analysisA.ems()[0][5]),4.8,"manual A EMS c ci:r");
        near(Double.toString(analysisA.ems()[0][4]),21.6,"manual A EMS c cr");
        near(Double.toString(analysisA.ems()[0][1]),12,"manual A EMS c p:c");
        near(Double.toString(analysisA.ems()[0][0]),57.6,"manual A EMS c c");
        check(analysisA.results().decisions().rows().isEmpty(),"manual A no D-study");
        List<List<String>> shuffledA = new ArrayList<>(manualA.rows()); Collections.shuffle(shuffledA,new Random(61));
        UrNestedCrossEngine.Analysis reorderedA = UrNestedCrossEngine.calculate(UrNestedCrossDesign.prepare(
            new DataTable(manualA.headers(),shuffledA),"score","p","c","r","i"));
        for (int j = 0; j < 8; j++) near(Double.toString(reorderedA.vc()[j]),analysisA.vc()[j],"nested-cross row-order invariance");
        reject(()->UrNestedCrossDesign.prepare(new DataTable(manualA.headers(),manualA.rows().subList(1,manualA.rows().size())),
            "score","p","c","r","i"),"requires every P × I cell");
        List<List<String>> duplicateA = new ArrayList<>(manualA.rows()); duplicateA.add(manualA.rows().get(0));
        reject(()->UrNestedCrossDesign.prepare(new DataTable(manualA.headers(),duplicateA),
            "score","p","c","r","i"),"duplicates a P × I design cell");
        category("urGENOVA crossed missing-cell pilot (manual F)");
        DataTable manualF;
        try (var in = SelfTest.class.getResourceAsStream("/samples/ur_crossed_missing_manual_f.csv"))
            { manualF = DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
        UrCrossedMissingDesign.Prepared missingCross = UrCrossedMissingDesign.prepare(manualF,"score","person","item");
        UrCrossedMissingEngine.Analysis analysisF = UrCrossedMissingEngine.calculate(missingCross);
        check(missingCross.missingCells()==13 && manualF.rows().size()==59,"manual F missing-cell count");
        for (int j = 0; j < 4; j++) near(Double.toString(analysisF.t()[j]),
            new double[]{27.8,24.74318,37,23.20339}[j],"manual F T term");
        for (int j = 0; j < 3; j++) {
            near(Double.toString(analysisF.ss()[j]),new double[]{4.59661,1.53979,7.66021}[j],"manual F SS");
            near(Double.toString(analysisF.ms()[j]),new double[]{0.41787,0.30796,0.18239}[j],"manual F MS");
            near(Double.toString(analysisF.vc()[j]),new double[]{0.04725,0.01174,0.18401}[j],"manual F VC");
        }
        check(Math.abs(analysisF.ems()[0][1]-0.186)<0.0006,"manual F EMS p/i coefficient");
        check(Math.abs(analysisF.ems()[2][1]+0.049)<0.0006,"manual F EMS interaction/i coefficient");
        check(analysisF.results().decisions().rows().isEmpty(),"manual F no D-study");
        List<List<String>> shuffledF = new ArrayList<>(manualF.rows()); Collections.shuffle(shuffledF,new Random(82));
        UrCrossedMissingEngine.Analysis reorderedF = UrCrossedMissingEngine.calculate(UrCrossedMissingDesign.prepare(
            new DataTable(manualF.headers(),shuffledF),"score","person","item"));
        for (int j = 0; j < 3; j++) near(Double.toString(reorderedF.vc()[j]),analysisF.vc()[j],"missing-cross row-order invariance");
        List<List<String>> duplicateF = new ArrayList<>(manualF.rows()); duplicateF.add(manualF.rows().get(0));
        reject(()->UrCrossedMissingDesign.prepare(new DataTable(manualF.headers(),duplicateF),
            "score","person","item"),"duplicates a P × I design cell");
        reject(()->UrCrossedMissingDesign.prepare(manualF,"score","person","person"),"three different existing columns");
        DataTable disconnectedF = DataTable.parse("person,item,score\nA,X,1\nA,Y,2\nB,X,3\nB,Y,4\nC,Z,5\nC,W,6\nD,Z,7\nD,W,8\n");
        reject(()->UrCrossedMissingDesign.prepare(disconnectedF,"score","person","item"),"must be connected");
        // Score scale is unrestricted; this binary manual fixture is not a binary-only algorithm.
        DataTable continuousF = DataTable.parse("person,item,score\nA,X,1.5\nA,Y,3.2\nB,X,2.7\nB,Y,4.4\nC,X,3.1\nC,Y,5.6\n");
        check(Double.isFinite(UrCrossedMissingEngine.calculate(UrCrossedMissingDesign.prepare(
            continuousF,"score","person","item")).vc()[0]),"continuous score accepted in crossed model");
        category("urGENOVA nested missing-cell pilot (manual G)");
        DataTable manualG;
        try (var in = SelfTest.class.getResourceAsStream("/samples/ur_nested_missing_manual_g.csv"))
            { manualG = DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
        UrNestedMissingDesign.Prepared nestedMissing = UrNestedMissingDesign.prepare(manualG,"score","person","h","item");
        UrNestedMissingEngine.Analysis analysisG = UrNestedMissingEngine.calculate(nestedMissing);
        check(nestedMissing.missingCells()==6 && manualG.rows().size()==114,"manual G missing-cell count");
        double[] manualGT={2308.5,2390.53788,2360.44089,2436.26667,2512,2787};
        for(int j=0;j<6;j++)near(Double.toString(analysisG.t()[j]),manualGT[j],"manual G T term");
        double[] manualGSS={82.03788,51.94089,75.82578,69.52123,199.17422};
        double[] manualGMS={9.11532,25.97044,8.42509,3.86229,2.65566};
        double[] manualGVC={0.43859,0.45415,0.60321,0.32937,2.67060};
        for(int j=0;j<5;j++){
            near(Double.toString(analysisG.ss()[j]),manualGSS[j],"manual G SS");
            near(Double.toString(analysisG.ms()[j]),manualGMS[j],"manual G MS");
            near(Double.toString(analysisG.vc()[j]),manualGVC[j],"manual G VC");
        }
        check(Math.abs(analysisG.ems()[0][0]-11.398)<0.0006,"manual G EMS p coefficient");
        check(Math.abs(analysisG.ems()[4][2]+0.016)<0.0006,"manual G EMS pi:h/i:h coefficient");
        check(analysisG.results().decisions().rows().isEmpty(),"manual G no D-study");
        List<List<String>> shuffledG=new ArrayList<>(manualG.rows());Collections.shuffle(shuffledG,new Random(115));
        UrNestedMissingEngine.Analysis reorderedG=UrNestedMissingEngine.calculate(UrNestedMissingDesign.prepare(
            new DataTable(manualG.headers(),shuffledG),"score","person","h","item"));
        for(int j=0;j<5;j++)near(Double.toString(reorderedG.vc()[j]),analysisG.vc()[j],"nested missing row-order invariance");
        List<List<String>> duplicateG=new ArrayList<>(manualG.rows());duplicateG.add(manualG.rows().get(0));
        reject(()->UrNestedMissingDesign.prepare(new DataTable(manualG.headers(),duplicateG),
            "score","person","h","item"),"duplicates a P × I:H design cell");
        reject(()->UrNestedMissingDesign.prepare(new DataTable(manualG.headers(),manualG.rows().subList(4,manualG.rows().size())),
            "score","person","h","item"),"Every P × H block");
        category("urGENOVA matrix-sampling pilot (manual H)");
        DataTable manualH;
        try(var in=SelfTest.class.getResourceAsStream("/samples/ur_matrix_sampling_manual_h.csv"))
            {manualH=DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8));}
        UrMatrixSamplingDesign.Prepared matrixSampling=UrMatrixSamplingDesign.prepare(manualH,"score","r","p","i");
        UrMatrixSamplingEngine.Analysis analysisH=UrMatrixSamplingEngine.calculate(matrixSampling);
        check(manualH.rows().size()==260 && matrixSampling.rLevels()==3
            && matrixSampling.pLevels()==24 && matrixSampling.iLevels()==30,"manual H group sizes");
        double[] manualHT={7261.06154,7278.90583,7456.3,7522.04167,8288};
        for(int j=0;j<5;j++)near(Double.toString(analysisH.t()[j]),manualHT[j],"manual H T term");
        double[] manualHSS={17.84429,177.39417,243.13583,588.56417};
        double[] manualHMS={8.92215,8.44734,9.00503,2.81610};
        double[] manualHVC={-0.05200,0.51416,0.70806,2.81610};
        for(int j=0;j<4;j++){
            near(Double.toString(analysisH.ss()[j]),manualHSS[j],"manual H SS");
            near(Double.toString(analysisH.ms()[j]),manualHMS[j],"manual H MS");
            near(Double.toString(analysisH.vc()[j]),manualHVC[j],"manual H VC");
        }
        check(Math.abs(analysisH.ems()[0][0]-72.692)<0.0006,"manual H EMS r coefficient");
        check(Math.abs(analysisH.ems()[1][1]-10.952)<0.0006,"manual H EMS p:r coefficient");
        check(analysisH.results().decisions().rows().isEmpty(),"manual H no D-study");
        List<List<String>> shuffledH=new ArrayList<>(manualH.rows());Collections.shuffle(shuffledH,new Random(199));
        UrMatrixSamplingEngine.Analysis reorderedH=UrMatrixSamplingEngine.calculate(UrMatrixSamplingDesign.prepare(
            new DataTable(manualH.headers(),shuffledH),"score","r","p","i"));
        for(int j=0;j<4;j++)near(Double.toString(reorderedH.vc()[j]),analysisH.vc()[j],"matrix row-order invariance");
        List<List<String>> duplicateH=new ArrayList<>(manualH.rows());duplicateH.add(manualH.rows().get(0));
        reject(()->UrMatrixSamplingDesign.prepare(new DataTable(manualH.headers(),duplicateH),
            "score","r","p","i"),"duplicates a P × I cell");
        reject(()->UrMatrixSamplingDesign.prepare(new DataTable(manualH.headers(),manualH.rows().subList(1,manualH.rows().size())),
            "score","r","p","i"),"Every P × I cell");
        category("mGENOVA P x I pilot (manual B/D)");
        DataTable manualM;
        try(var in=SelfTest.class.getResourceAsStream("/samples/m_pxi_manual_b.csv"))
            {manualM=DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8));}
        MpXiDesign.Prepared mPlan=MpXiDesign.prepare(manualM,"score","person","variable","item");
        MpXiEngine.Analysis m=MpXiEngine.calculate(mPlan,null);
        check(manualM.rows().size()==64 && Arrays.equals(mPlan.itemCounts(),new int[]{2,4,2}),"manual B structure");
        double[][] mMs={{3.71429,4,0.57143},{12.21429,2.66667,1.07143},{4.14286,4,0.42857}};
        double[][] mVc={{1.57143,0.42857,0.57143},{2.78571,0.19940,1.07143},{1.85714,0.44643,0.42857}};
        for(int mv=0;mv<3;mv++)for(int e=0;e<3;e++){
            near(Double.toString(m.ms()[mv][e]),mMs[mv][e],"manual B MS");
            near(Double.toString(m.vc()[mv][e]),mVc[mv][e],"manual B VC");
        }
        near(Double.toString(m.personCovariance()[0][1]),1.48214,"manual B P covariance first-second");
        near(Double.toString(m.personCovariance()[0][2]),0.5,"manual B P covariance first-third");
        near(Double.toString(m.personCovariance()[1][2]),0.94643,"manual B P covariance second-third");
        double[][] mCoefficients={{0.84615,0.75862},{0.91228,0.89763},{0.89655,0.80934}};
        for(int mv=0;mv<3;mv++){
            near(Double.toString(m.decisions()[mv][3]),mCoefficients[mv][0],"manual B G");
            near(Double.toString(m.decisions()[mv][4]),mCoefficients[mv][1],"manual B Phi");
        }
        for(int j=0;j<5;j++)near(Double.toString(m.composite()[j]),
            new double[]{1.58036,0.09821,0.13802,0.94149,0.91968}[j],"manual B composite");
        MpXiEngine.Analysis mScenario=MpXiEngine.calculate(mPlan,new int[]{3,4,3});
        for(int j=0;j<5;j++)near(Double.toString(mScenario.composite()[j]),
            new double[]{1.42714,0.07286,0.10708,0.95143,0.93020}[j],"manual B scenario 1");
        MpXiEngine.Analysis mEqual=MpXiEngine.calculate(mPlan,new int[]{3,4,3},
            new double[]{0.33333,0.33333,0.33333});
        for(int j=0;j<5;j++)near(Double.toString(mEqual.composite()[j]),
            new double[]{1.34124,0.06680,0.10474,0.95256,0.92756}[j],"manual D equal w=a");
        check(mEqual.results().report().contains("supplied w weights"),"custom weight provenance");
        MpXiEngine.Analysis mDifferent13=MpXiEngine.calculate(mPlan,new int[]{2,4,2},null,
            new double[]{0.33333,0.33333,0.33333});
        near(Double.toString(mDifferent13.composite()[0]),1.58036,"manual D 1.3 universe");
        near(Double.toString(mDifferent13.composite()[1]),0.12599,"manual D 1.3 relative MSE");
        near(Double.toString(mDifferent13.composite()[2]),0.18014,"manual D 1.3 absolute MSE");
        check(Double.isNaN(mDifferent13.composite()[3])&&Double.isNaN(mDifferent13.composite()[4])
            &&mDifferent13.results().decisions().rows().get(3).get(5).equals("N/A"),
            "unequal weights suppress composite coefficients");
        MpXiEngine.Analysis mDifferent14=MpXiEngine.calculate(mPlan,new int[]{2,4,2},
            new double[]{0.33333,0.33333,0.33333},new double[]{0.25,0.5,0.25});
        near(Double.toString(mDifferent14.composite()[1]),0.13889,"manual D 1.4 relative MSE");
        near(Double.toString(mDifferent14.composite()[2]),0.17870,"manual D 1.4 absolute MSE");
        MpXiEngine.Analysis mDifferent15=MpXiEngine.calculate(mPlan,new int[]{2,4,2},
            new double[]{0.33333,0.33333,0.33333},new double[]{0.2,0.5,0.3});
        near(Double.toString(mDifferent15.composite()[1]),0.13312,"manual D 1.5 relative MSE");
        near(Double.toString(mDifferent15.composite()[2]),0.21426,"manual D 1.5 absolute MSE includes mean bias");
        List<List<String>> mShuffled=new ArrayList<>(manualM.rows());Collections.shuffle(mShuffled,new Random(237));
        MpXiEngine.Analysis mReordered=MpXiEngine.calculate(MpXiDesign.prepare(
            new DataTable(manualM.headers(),mShuffled),"score","person","variable","item"),null);
        for(int j=0;j<5;j++)near(Double.toString(mReordered.composite()[j]),m.composite()[j],"m row-order invariance");
        List<List<String>> mDuplicate=new ArrayList<>(manualM.rows());mDuplicate.add(manualM.rows().get(0));
        reject(()->MpXiDesign.prepare(new DataTable(manualM.headers(),mDuplicate),"score","person","variable","item"),"duplicates a P × I cell");
        reject(()->MpXiDesign.prepare(new DataTable(manualM.headers(),manualM.rows().subList(1,manualM.rows().size())),
            "score","person","variable","item"),"missing cells");
        reject(()->MpXiEngine.calculate(mPlan,new int[]{3,4}),"one positive D-study item count");
        reject(()->MpXiEngine.calculate(mPlan,null,new double[]{0,0,0}),"At least one w weight");
        reject(()->MpXiEngine.calculate(mPlan,null,null,new double[]{0,0,0}),"At least one a weight");
        reject(()->MpXiEngine.calculate(mPlan,null,new double[]{1,2}),"one w weight per fixed variable");
        reject(()->MpXiEngine.calculate(mPlan,null,new double[]{1,Double.NaN,2}),"must be finite");
        DataTable independentM=DataTable.parse("person,variable,item,score\n"
            +"A,One,I1,1\nA,One,I2,2\nB,One,I1,3\nB,One,I2,4\nC,One,I1,5\nC,One,I2,6\n"
            +"A,Two,I1,2\nA,Two,I2,4\nB,Two,I1,4\nB,Two,I2,6\nC,Two,I1,6\nC,Two,I2,8\n");
        MpXiEngine.Analysis independentMA=MpXiEngine.calculate(
            MpXiDesign.prepare(independentM,"score","person","variable","item"),null);
        for(int mv=0;mv<2;mv++){
            near(Double.toString(independentMA.vc()[mv][0]),4,"independent m person variance");
            near(Double.toString(independentMA.vc()[mv][2]),0,"independent m interaction variance");
            near(Double.toString(independentMA.decisions()[mv][3]),1,"independent m G");
        }
        near(Double.toString(independentMA.vc()[0][1]),0.5,"independent m first item variance");
        near(Double.toString(independentMA.vc()[1][1]),2,"independent m second item variance");
        near(Double.toString(independentMA.personCovariance()[0][1]),4,"independent m person covariance");
        near(Double.toString(independentMA.composite()[0]),4,"independent m composite universe");
        near(Double.toString(independentMA.composite()[2]),0.3125,"independent m absolute error");
        near(Double.toString(independentMA.composite()[4]),4.0/4.3125,"independent m Phi");
        DataTable constantM=DataTable.parse("person,variable,item,score\n"
            +"A,One,I1,5\nA,One,I2,5\nB,One,I1,5\nB,One,I2,5\n"
            +"A,Two,I1,5\nA,Two,I2,5\nB,Two,I1,5\nB,Two,I2,5\n");
        Results constantMR=MpXiEngine.run(MpXiDesign.prepare(constantM,"score","person","variable","item"),null);
        check(constantMR.decisions().rows().get(2).get(5).equals("Undefined"),"zero-variance G undefined");
        check(constantMR.decisions().rows().get(2).get(6).equals("Undefined"),"zero-variance Phi undefined");
        check(constantMR.notices().contains("denominator is zero"),"zero-variance explanation");
        category("mGENOVA P x (I:R) pilot (manual C/D)");
        DataTable manualMC;
        try(var in=SelfTest.class.getResourceAsStream("/samples/m_pir_manual_c.csv"))
            {manualMC=DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8));}
        MpIrDesign.Prepared mNestedPlan=MpIrDesign.prepare(manualMC,"score","person","variable","group","item");
        MpIrEngine.Analysis mNested=MpIrEngine.calculate(mNestedPlan,null,null);
        check(manualMC.rows().size()==220&&mNestedPlan.persons().size()==10,"manual C records");
        check(Arrays.equals(mNestedPlan.itemCounts()[0],new int[]{3,6,3})
            &&Arrays.equals(mNestedPlan.itemCounts()[1],new int[]{4,3,3}),"manual C nested item counts");
        double[][] manualMCMS={{9.76296,26.875,8.23889,3.625,2.60103},
            {10.96,4.68667,2.49524,3.37,2.31270}};
        double[][] manualCVC={{0.49443,0.46966,0.56379,0.27306,2.60103},
            {0.75580,0.03437,0.01825,0.32039,2.31270}};
        for(int mv=0;mv<2;mv++)for(int e=0;e<5;e++){
            near(Double.toString(mNested.ms()[mv][e]),manualMCMS[mv][e],"manual C MS");
            near(Double.toString(mNested.vc()[mv][e]),manualCVC[mv][e],"manual C VC");
        }
        near(Double.toString(mNested.covP()[0][1]),0.56553,"manual C P covariance");
        near(Double.toString(mNested.covR()[0][1]),0.03328,"manual C R covariance");
        near(Double.toString(mNested.covPR()[0][1]),0.90792,"manual C PR covariance");
        for(int j=0;j<5;j++)near(Double.toString(mNested.composite()[j]),
            new double[]{0.58369,0.31156,0.38609,0.65198,0.60188}[j],"manual C default composite");
        MpIrEngine.Analysis mNestedD=MpIrEngine.calculate(mNestedPlan,
            new int[][]{{3,6,3,3,6,3},{8,3,3,8,3,3}},null);
        for(int j=0;j<5;j++)near(Double.toString(mNestedD.composite()[j]),
            new double[]{0.60555,0.14587,0.17442,0.80588,0.77638}[j],"manual C D-study 2.1");
        MpIrEngine.Analysis mNestedSingle=MpIrEngine.calculate(mNestedPlan,null,new double[]{1,0});
        for(int j=0;j<5;j++)near(Double.toString(mNestedSingle.composite()[j]),
            mNestedSingle.decisions()[0][j],"nested w=a selects first variable");
        reject(()->MpIrEngine.calculate(mNestedPlan,null,new double[]{0,0}),"At least one w weight");
        reject(()->MpIrEngine.calculate(mNestedPlan,new int[][]{{3,6,3},{4,3}},null),
            "one positive D-study item count");
        List<List<String>> mNestedShuffled=new ArrayList<>(manualMC.rows());
        Collections.shuffle(mNestedShuffled,new Random(319));
        MpIrEngine.Analysis mNestedReordered=MpIrEngine.calculate(MpIrDesign.prepare(
            new DataTable(manualMC.headers(),mNestedShuffled),"score","person","variable","group","item"),null,null);
        for(int j=0;j<5;j++)near(Double.toString(mNestedReordered.composite()[j]),
            mNested.composite()[j],"m nested row-order invariance");
        List<List<String>> mNestedDuplicate=new ArrayList<>(manualMC.rows());mNestedDuplicate.add(manualMC.rows().get(0));
        reject(()->MpIrDesign.prepare(new DataTable(manualMC.headers(),mNestedDuplicate),
            "score","person","variable","group","item"),"duplicates a P × I:R cell");
        reject(()->MpIrDesign.prepare(new DataTable(manualMC.headers(),manualMC.rows().subList(1,manualMC.rows().size())),
            "score","person","variable","group","item"),"missing cells");
        List<List<String>> independentNestedRows=new ArrayList<>();
        for(int personIndex=0;personIndex<3;personIndex++)for(int mv=0;mv<2;mv++)
            for(int groupIndex=0;groupIndex<2;groupIndex++)for(int itemIndex=0;itemIndex<2;itemIndex++){
                double value=(mv==0?10:20)+(mv==0?1:2)*personIndex
                    +(mv==0?2:6)*groupIndex+(mv==0?0.5:1)*(itemIndex==0?-1:1);
                independentNestedRows.add(List.of("P"+personIndex,"V"+mv,"R"+groupIndex,
                    "I"+itemIndex,Double.toString(value)));
            }
        DataTable independentNested=new DataTable(List.of("person","variable","group","item","score"),independentNestedRows);
        MpIrEngine.Analysis independentNestedA=MpIrEngine.calculate(MpIrDesign.prepare(independentNested,
            "score","person","variable","group","item"),null,null);
        near(Double.toString(independentNestedA.vc()[0][0]),1,"independent nested P variance 1");
        near(Double.toString(independentNestedA.vc()[1][0]),4,"independent nested P variance 2");
        near(Double.toString(independentNestedA.vc()[0][1]),1.75,"independent nested R variance 1");
        near(Double.toString(independentNestedA.vc()[1][1]),17,"independent nested R variance 2");
        near(Double.toString(independentNestedA.vc()[0][2]),0.5,"independent nested I:R variance 1");
        near(Double.toString(independentNestedA.vc()[1][2]),2,"independent nested I:R variance 2");
        near(Double.toString(independentNestedA.covP()[0][1]),2,"independent nested P covariance");
        near(Double.toString(independentNestedA.covR()[0][1]),6,"independent nested R covariance");
        near(Double.toString(independentNestedA.covPR()[0][1]),0,"independent nested PR covariance");
        near(Double.toString(independentNestedA.composite()[0]),2.25,"independent nested composite universe");
        near(Double.toString(independentNestedA.composite()[1]),0,"independent nested composite relative error");
        near(Double.toString(independentNestedA.composite()[2]),4,"independent nested composite absolute error");
        near(Double.toString(independentNestedA.composite()[3]),1,"independent nested G");
        near(Double.toString(independentNestedA.composite()[4]),0.36,"independent nested Phi");
        MpIrEngine.Analysis independentNestedB=MpIrEngine.calculate(MpIrDesign.prepare(independentNested,
            "score","person","variable","group","item"),null,null,new double[]{1,0});
        near(Double.toString(independentNestedB.composite()[0]),2.25,"nested unequal-weight universe");
        near(Double.toString(independentNestedB.composite()[1]),0.25,"nested unequal-weight relative MSE");
        near(Double.toString(independentNestedB.composite()[2]),43.5,"nested unequal-weight absolute MSE");
        check(Double.isNaN(independentNestedB.composite()[3]),"nested unequal-weight G unavailable");
        category("Published CiviConnections EPM component oracle");
        // Karatoprak Ersen, Lee, & Yarbrough (2026), Supplements 1 and 2.
        // Effects: s, G:s, P:G:s, I, sI, GI:s, PI:G:s.  Published G-study
        // inputs are rounded, so comparisons use the project's 6e-6 tolerance.
        double[][] civiG = {
            {0.01287,0.02431,0.05245}, {0.02487,0.03866,0.10614},
            {0.23430,0.26517,0.79315}, {0.12978,0.08231,0.41524},
            {0.01083,0.01888,0.05162}, {0.02310,0.01607,0.05763},
            {0.57662,0.49081,1.33374}
        };
        double nGs=2.348624,nPGs=8.276944,nI=25;
        double[] civiDivisors={1,nGs,nPGs,nI,nI,nGs*nI,nPGs*nGs*nI};
        boolean[] civiUniverse={true,false,false,false,false,false,false};
        boolean[] civiRelative={false,true,true,false,true,true,true};
        boolean[] civiAbsolute={false,true,true,true,true,true,true};
        MComponentDStudy.Analysis civi=MComponentDStudy.calculate(
            civiG,civiDivisors,civiUniverse,civiRelative,civiAbsolute);
        near(Double.toString(civiDivisors[6]),485.985733,"published corrected PI:G:s divisor");
        double[][] civiDExpected={
            {0.0128700000,0.0243100000,0.0076350000},
            {0.0105891790,0.0164607021,0.0090712690},
            {0.0283075493,0.0320371867,0.0177408473},
            {0.0051912000,0.0032924000,0.0040630000},
            {0.0004332000,0.0007552000,0.0004382000},
            {0.0003934219,0.0002736922,0.0001571984},
            {0.0011864957,0.0010099268,0.0002739895}
        };
        for(int effect=0;effect<civiDExpected.length;effect++)for(int column=0;column<3;column++)
            near(Double.toString(civi.dComponents()[effect][column]),civiDExpected[effect][column],
                "published multivariate D component");
        double[][] civiMetricsExpected={
            {0.01287000,0.04090985,0.04610105,0.23930898,0.21824270,0.31459419,0.27916937},
            {0.02431000,0.05053671,0.05382911,0.32479719,0.31111182,0.48103648,0.45161440},
            {0.02191000,0.03608355,0.03644115,0.37780067,0.37548535,0.60720198,0.60124346}
        };
        for(int row=0;row<3;row++)for(int column=0;column<7;column++)
            near(Double.toString(civi.metrics()[row][column]),civiMetricsExpected[row][column],
                "published multivariate D metric");
        double[] civiDirectDifference={0.02190,0.02093,0.20579,0.00894,0.00779,0.02072,0.80111};
        double[] civiUnivariate=MComponentDStudy.calculateUnivariate(civiDirectDifference,civiDivisors,
            civiUniverse,civiRelative,civiAbsolute);
        double[] civiUnivariateExpected={0.02190000,0.03608755,0.03644515,0.37766726,0.37535252,0.60685745,0.60090296};
        for(int column=0;column<7;column++)near(Double.toString(civiUnivariate[column]),
            civiUnivariateExpected[column],"published univariate difference metric");
        check(Math.abs(civi.metrics()[2][3]-civiUnivariate[3])<0.00014,
            "multivariate and direct-univariate difference G agree within published rounding");
        category("Gleser Cronbach Rajaratnam 1965 Table 3");
        DataTable gleser;
        try(var in=SelfTest.class.getResourceAsStream("/samples/gleser1965_table3.csv"))
            { gleser=DataTable.parse(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
        check(gleser.rows().size()==144,"Table 3 has 144 transcribed observations");
        Map<String,Integer> gleserPersonTotals=new LinkedHashMap<>();
        Map<String,Integer> gleserCellTotals=new LinkedHashMap<>();
        int gleserGrand=0;
        for(List<String> row:gleser.rows()) {
            int score=Integer.parseInt(row.get(3)); gleserGrand+=score;
            gleserPersonTotals.merge(row.get(0),score,Integer::sum);
            gleserCellTotals.merge(row.get(1)+"/"+row.get(2),score,Integer::sum);
        }
        check(gleserGrand==300,"Table 3 grand total");
        int[] gleserRowTotals={20,22,27,22,41,13,17,14,26,25,29,44};
        for(int p=0;p<12;p++)check(gleserPersonTotals.get(""+(char)('a'+p))==gleserRowTotals[p],
            "Table 3 person marginal " + p);
        int[] gleserColumnTotals={29,30,33,24,9,9,35,29,29,32,21,20};
        for(int i=0;i<6;i++)for(int j=0;j<2;j++)
            check(gleserCellTotals.get("I"+(i+1)+"/J"+(j+1))==gleserColumnTotals[2*i+j],
                "Table 3 item/judge marginal " + i + "/" + j);
        Results gleserResult=Engine.run(Design.prepare(gleser,"score","person",
            List.of(new Design.Facet("item","",false,""),new Design.Facet("judge","",false,"")),true));
        Map<String,double[]> gleserAnova=Map.of(
            "person",new double[]{11,84.1667,7.6515},
            "judge",new double[]{1,1.0000,1.0000},
            "item",new double[]{5,64.6667,12.9333},
            "person × judge",new double[]{11,19.5000,1.7727},
            "person × item",new double[]{55,81.1667,1.4758},
            "item × judge",new double[]{5,4.3333,0.8667},
            "person × item × judge",new double[]{55,34.1666,0.6212});
        check(gleserResult.anova().rows().size()==7,"Table 3 has seven ANOVA effects");
        for(List<String> row:gleserResult.anova().rows()) {
            double[] expected=gleserAnova.get(row.get(0));
            check(expected!=null,"Table 3 effect "+row.get(0));
            check(Integer.parseInt(row.get(1))==(int)expected[0],"Table 3 df "+row.get(0));
            check(Math.abs(Double.parseDouble(row.get(2))-expected[1])<0.00011,"Table 3 SS "+row.get(0));
            check(Math.abs(Double.parseDouble(row.get(3))-expected[2])<0.00011,"Table 3 MS "+row.get(0));
        }
        // The paper prints 0.471 for item, but its Eq. 17 and Table 3 MS give
        // (12.9333 - 1.4758 - 0.8667 + 0.6212) / 24 = 0.46717.
        Map<String,Double> gleserVC=Map.of(
            "person",0.419,"item",0.46717,"judge",-0.014,
            "person × item",0.427,"person × judge",0.192,
            "item × judge",0.021,"person × item × judge",0.621);
        check(gleserResult.components().rows().size()==7,"Table 3 has seven variance components");
        for(List<String> row:gleserResult.components().rows()) {
            Double expected=gleserVC.get(row.get(0));
            check(expected!=null,"Table 3 component effect "+row.get(0));
            check(Math.abs(Double.parseDouble(row.get(2))-expected)<0.001,
                "Table 3 VC "+row.get(0)+": engine="+row.get(2)+", paper="+expected);
        }
        // The paper's two universe definitions yield different D-study ratios.
        double gleserA=0.419/(0.419+0.427/6+0.192/2+0.621/12);
        double gleserB=(0.419+0.427/6)/(0.419+0.427/6+0.192/2+0.621/12);
        check(Math.abs(gleserA-0.657)<0.001,"paper universe A ratio");
        check(Math.abs(gleserB-0.768)<0.001,"paper universe B ratio");
        check(gleserResult.decisions().rows().size()==1,"Table 3 default D-study scenario");
        check(Math.abs(Double.parseDouble(gleserResult.decisions().rows().get(0).get(5))-0.657)<0.001,
            "Table 3 engine G matches paper universe A");
        Results gleserFixedItems=Engine.run(Design.prepare(gleser,"score","person",
            List.of(new Design.Facet("item","",true,""),new Design.Facet("judge","",false,"")),true));
        check(gleserFixedItems.decisions().rows().size()==1,"Table 3 fixed-item D-study scenario");
        check(Math.abs(Double.parseDouble(gleserFixedItems.decisions().rows().get(0).get(5))-0.768)<0.001,
            "Table 3 engine G matches paper universe B");
        category("Excel import and typed exports");
        if (args.length>0) {
            Path workbook=Path.of(args[0]);
            check(DataTable.sheets(workbook).equals(List.of("Nested","Other")),"Excel sheets");
            check(DataTable.read(workbook,"Nested").equals(t),"independently generated Excel equals CSV");
            if(args.length>1) {
                try { DataTable.read(Path.of(args[1]),null); throw new AssertionError("Excel formula accepted"); }
                catch(IllegalArgumentException ex) { check(ex.getMessage().contains("contains a formula"),"formula refusal"); }
            }
            if(args.length>2) {
                Path output=Path.of(args[2]);
                WorkbookExport.write(output,result,plan.description(),t.rows().size(),"person","score");
                check(Files.size(output)>0,"Excel results written");
                if(args.length>4) {
                    WorkbookExport.write(Path.of(args[3]),fixed,"fixed rater",t.rows().size(),"person","score");
                    WorkbookExport.write(Path.of(args[4]),Engine.run(Design.prepare(t,"score","person",nested(),false)),
                        "G-study only",t.rows().size(),"person","score");
                    check(Files.size(Path.of(args[3]))>0,"fixed-effect Excel written");
                    check(Files.size(Path.of(args[4]))>0,"G-only Excel written");
                }
                if(args.length>5) {
                    WorkbookExport.write(Path.of(args[5]),urResult,urPlan.description(),ur.rows().size(),
                        "person","score","urGENOVA one-way Java pilot");
                    check(Files.size(Path.of(args[5]))>0,"ur pilot Excel written");
                }
                if(args.length>6) {
                    WorkbookExport.write(Path.of(args[6]),analysisD.results(),twoD.description(),manualD.rows().size(),
                        "h","score","urGENOVA two-way Java pilot");
                    check(Files.size(Path.of(args[6]))>0,"two-way ur Excel written");
                }
                if(args.length>7) {
                    WorkbookExport.write(Path.of(args[7]),analysisA.results(),nestedCross.description(),manualA.rows().size(),
                        "p","score","urGENOVA nested-cross Java pilot");
                    check(Files.size(Path.of(args[7]))>0,"nested-cross ur Excel written");
                }
                if(args.length>8) {
                    WorkbookExport.write(Path.of(args[8]),analysisF.results(),missingCross.description(),manualF.rows().size(),
                        "person","score","urGENOVA missing-cross Java pilot");
                    check(Files.size(Path.of(args[8]))>0,"missing-cross ur Excel written");
                }
                if(args.length>9) {
                    WorkbookExport.write(Path.of(args[9]),analysisG.results(),nestedMissing.description(),manualG.rows().size(),
                        "person","score","urGENOVA nested missing-cell Java pilot");
                    check(Files.size(Path.of(args[9]))>0,"nested missing-cell ur Excel written");
                }
                if(args.length>10) {
                    WorkbookExport.write(Path.of(args[10]),analysisH.results(),matrixSampling.description(),manualH.rows().size(),
                        "p","score","urGENOVA matrix-sampling Java pilot");
                    check(Files.size(Path.of(args[10]))>0,"matrix-sampling ur Excel written");
                }
                if(args.length>11) {
                    WorkbookExport.write(Path.of(args[11]),m.results(),mPlan.description(),manualM.rows().size(),
                        "person","score","mGENOVA P × I Java pilot");
                    check(Files.size(Path.of(args[11]))>0,"mGENOVA Excel written");
                }
                if(args.length>12) {
                    WorkbookExport.write(Path.of(args[12]),mNested.results(),mNestedPlan.description(),manualMC.rows().size(),
                        "person","score","mGENOVA P × (I:R) Java pilot");
                    check(Files.size(Path.of(args[12]))>0,"mGENOVA nested Excel written");
                }
                if(args.length>13) {
                    WorkbookExport.write(Path.of(args[13]),mDifferent15.results(),mPlan.description(),manualM.rows().size(),
                        "person","score","mGENOVA P × I Java pilot");
                    check(Files.size(Path.of(args[13]))>0,"mGENOVA unequal-weight Excel written");
                }
            }
        }
        System.out.println("PASS: "+checks+" checks; balanced GENOVA, urGENOVA manual A/C/D/E/F/G/H, mGENOVA manual B/C/D, independent ANOVA, import, validation, Excel, locale.");
        categoryChecks.forEach((name,count)->System.out.println("CHECK CATEGORY: "+name+" = "+count));
    }
}
