package org.genova.gui;

import java.util.*;
import java.util.regex.*;

public record Results(Table anova, Table components, Table decisions, String report, String notices, Table setup) {
    public Results(Table anova, Table components, Table decisions, String report, String notices) {
        this(anova,components,decisions,report,notices,new Table(List.of(),List.of()));
    }
    public record Table(List<String> headers, List<List<String>> rows) {
        public String csv() {
            StringBuilder s = new StringBuilder(DataTable.csvRow(headers));
            rows.forEach(r -> s.append(DataTable.csvRow(r))); return s.toString();
        }
    }
    private static final String NUM = "(-?\\d+(?:\\.\\d+)?(?:[Ee][+-]?\\d+)?|NaN|Infinity|\\*+)";
    private static String section(String text, String start, String end) {
        int a = text.indexOf(start); if (a < 0) return "";
        int b = text.indexOf(end, a+start.length()); return text.substring(a, b < 0 ? text.length() : b);
    }
    private static String effect(String shortName, Map<Character,String> names) {
        String[] sides = shortName.split(":"); List<String> parsed = new ArrayList<>();
        for (String side : sides) {
            List<String> words = new ArrayList<>();
            for (char c : side.toCharArray()) words.add(names.getOrDefault(c, ""+c));
            parsed.add(String.join(" × ", words));
        }
        return String.join(" : ", parsed);
    }
    public static Results parse(String raw, Design.Prepared design) {
        List<List<String>> a = new ArrayList<>(), g = new ArrayList<>(), d = new ArrayList<>();
        Matcher am = Pattern.compile("(?m)^\\s*([A-Z:]+)\\s+(\\d+)\\s+"+NUM+"\\s+"+NUM+"\\s+"+NUM).matcher(section(raw, "ANOVA TABLE", "G Study Results"));
        while (am.find()) a.add(List.of(effect(am.group(1), design.names()), am.group(2), am.group(4), am.group(5)));
        Matcher gm = Pattern.compile("(?m)^\\s*([A-Z:]+)\\s+(\\d+)\\s+"+NUM+"(\\s+QFM)?\\s+"+NUM+"(\\s+QFM)?(?:[ \\t]+"+NUM+")?").matcher(section(raw, "G Study Results", "Expected Mean Square Equations"));
        while (gm.find()) g.add(List.of(effect(gm.group(1), design.names()), gm.group(2), gm.group(3), gm.group(5), gm.group(7) == null ? "—" : gm.group(7), gm.group(4) == null ? "Random effect" : "Fixed effect QFM"));
        // Read labelled per-scenario lines, not incidental numbers in pagination.
        Pattern dp = Pattern.compile("Universe Score\\s+"+NUM+"[^\\r\\n]*\\R\\s*Expected Observed Score\\s+"+NUM+"[^\\r\\n]*\\R\\s*Lower Case Delta\\s+"+NUM+"[^\\r\\n]*?Generalizability Coefficient =\\s*"+NUM+"[^\\r\\n]*\\R\\s*Upper Case Delta\\s+"+NUM+"[^\\r\\n]*?Phi =\\s*"+NUM);
        Matcher dm = dp.matcher(raw); int scenario = 0;
        while (dm.find()) {
            if (scenario >= design.scenarios().size()) throw new IllegalArgumentException("The engine returned an unexpected number of D-study scenarios.");
            List<String> sizes = new ArrayList<>(); int[] count = design.scenarios().get(scenario);
            for (int j = 1; j < count.length; j++) sizes.add(design.facets().get(j).column()+"="+count[j]);
            d.add(List.of(""+(++scenario), String.join(" · ", sizes), dm.group(1), dm.group(3), dm.group(5), dm.group(4), dm.group(6)));
        }
        if (a.isEmpty() || g.isEmpty() || d.size() != design.scenarios().size())
            throw new IllegalArgumentException("The engine did not return complete expected results; this analysis cannot be marked successful.");
        StringBuilder report = new StringBuilder("GENOVA Studio · 1.0.0 Preview\n\nDesign: "+design.description()+"\n");
        design.names().forEach((c,n) -> report.append(c).append(" = ").append(n).append('\n'));
        report.append("\nEstimates and treatment of negative components follow jGENOVA 1.0. QFM for fixed effects is not a random variance component.\n\n");
        for (String page : raw.split("\f")) {
            if (page.contains("CONTROL CARD INPUT LISTING") || page.contains("GGGGGGGGGG")) continue;
            report.append(page).append("\n\n");
        }
        List<String> notices = new ArrayList<>();
        for (String line : raw.lines().toList())
            if (line.stripLeading().startsWith("***") && !line.contains("GRAND MEAN")) notices.add(line.trim());
        if (g.stream().anyMatch(r -> r.get(2).startsWith("-") || r.get(3).startsWith("-")))
            notices.add("Negative estimates occur. Review both original and adjusted estimates in light of the research design; the engine output is preserved.");
        if (Pattern.compile("NaN|Infinity|\\*{5,}").matcher(String.join(" ", d.stream().flatMap(List::stream).toList())).find())
            notices.add("Some coefficients are undefined or exceed the report's numeric range. See the full report.");
        return new Results(new Table(List.of("Effect", "df", "SS", "MS"), a),
            new Table(List.of("Effect", "df", "Algorithm", "EMS", "SE", "Type"), g),
            new Table(List.of("Scenario", "Facet levels", "Universe σ²", "Relative error σ²δ", "Absolute error σ²Δ", "G coefficient", "Φ"), d),
            report.toString(), String.join("\n", notices));
    }
}
