package org.genova.gui;

import java.util.*;

/** Validates a complete balanced long table and generates private engine input. */
public final class Design {
    public record Facet(String column, String parent, boolean fixed, String dSizes) {}
    public record Prepared(String cards, String description, Map<Character,String> names,
                           List<Facet> facets, int[] levels, List<int[]> scenarios) {}

    public static Prepared prepare(DataTable data, String score, String object, List<Facet> settings, boolean dStudy) {
        if (Objects.equals(score, object)) fail("The score and object-of-measurement columns must be different.");
        if (!data.headers().contains(score) || !data.headers().contains(object)) fail("Select a score column and an object-of-measurement column.");
        Map<String,Facet> selected = new LinkedHashMap<>();
        selected.put(object, new Facet(object, "", false, ""));
        for (Facet f : settings) {
            if (f.column().equals(object) || f.column().equals(score) || !data.headers().contains(f.column())) fail("A facet column conflicts with the score or object column.");
            if (selected.put(f.column(), f) != null) fail("A column cannot be selected as more than one facet.");
        }
        if (selected.size() < 2 || selected.size() > 6) fail("Select 1–5 facets (2–6 dimensions including the object of measurement).");
        List<Facet> order = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        for (Facet f : selected.values()) visit(f, selected, visiting, order);
        int n = order.size();
        List<String> cols = order.stream().map(Facet::column).toList();
        List<List<Integer>> ancestors = new ArrayList<>();
        for (Facet f : order) {
            List<Integer> a = new ArrayList<>(); String parent = f.parent();
            while (!parent.isBlank()) { a.add(cols.indexOf(parent)); parent = selected.get(parent).parent(); }
            Collections.sort(a); ancestors.add(a);
        }
        if (dStudy && ancestors.stream().anyMatch(a -> a.size() > 1))
            fail("D-study for nesting chains is not supported: an independent QC comparison found unreliable jGENOVA D-study coefficients for a two-level chain. Uncheck Run D-study to run the balanced G-study only.");
        int[] indices = cols.stream().mapToInt(data.headers()::indexOf).toArray();
        int scoreIndex = data.headers().indexOf(score);
        List<Map<List<String>,SortedSet<String>>> groups = new ArrayList<>();
        for (int f = 0; f < n; f++) groups.add(new HashMap<>());
        List<Double> scores = new ArrayList<>();
        for (int ri = 0; ri < data.rows().size(); ri++) {
            List<String> row = data.rows().get(ri);
            for (int f = 0; f < n; f++) {
                String id = row.get(indices[f]);
                if (id.isBlank()) fail("Row " + (ri+2) + ": " + cols.get(f) + " is blank.");
                List<String> context = ancestors.get(f).stream().map(j -> row.get(indices[j])).toList();
                groups.get(f).computeIfAbsent(context, k -> new TreeSet<>()).add(id);
            }
            double v;
            try { v = Double.parseDouble(row.get(scoreIndex)); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " + (ri+2) + ": score is not numeric. Missing values cannot be filled automatically."); }
            if (!Double.isFinite(v)) fail("Row " + (ri+2) + ": score must be a finite number.");
            scores.add(v);
        }
        int[] levels = new int[n];
        List<Map<List<String>,Map<String,Integer>>> mappings = new ArrayList<>();
        for (int f = 0; f < n; f++) {
            Map<List<String>,Map<String,Integer>> mapped = new HashMap<>();
            for (var g : groups.get(f).entrySet()) {
                int count = g.getValue().size();
                if (count < 2) fail("Each group of " + cols.get(f) + " needs at least two levels.");
                if (levels[f] != 0 && levels[f] != count) fail("The number of " + cols.get(f) + " levels differs across parent groups; this engine requires a balanced design.");
                levels[f] = count;
                Map<String,Integer> m = new HashMap<>(); int index = 0;
                for (String id : g.getValue()) m.put(id, index++);
                mapped.put(g.getKey(), m);
            }
            mappings.add(mapped);
        }
        long total = 1;
        for (int level : levels) { total *= level; if (total > 200_000) fail("The complete design exceeds the 200,000-cell limit, or the selected nesting is incorrect."); }
        Map<Integer,Double> values = new HashMap<>();
        for (int ri = 0; ri < data.rows().size(); ri++) {
            List<String> row = data.rows().get(ri); int key = 0;
            for (int f = 0; f < n; f++) {
                List<String> context = ancestors.get(f).stream().map(j -> row.get(indices[j])).toList();
                key = key * levels[f] + mappings.get(f).get(context).get(row.get(indices[f]));
            }
            if (values.put(key, scores.get(ri)) != null) fail("Row " + (ri+2) + " duplicates a design cell. Check duplicate records or model repeated measurements as a separate facet.");
        }
        if (values.size() != total) fail("The design requires " + total + " observation cells, but only " + values.size() + " are present. Check for missing combinations or incorrect crossed/nested settings.");
        Map<Character,String> names = new LinkedHashMap<>();
        List<Character> letters = new ArrayList<>();
        for (String col : cols) {
            char c = Character.toUpperCase(col.charAt(0));
            if (c < 'A' || c > 'Z' || names.containsKey(c)) {
                c = 'A'; while (names.containsKey(c)) c++;
            }
            names.put(c, col); letters.add(c);
        }
        StringBuilder cards = new StringBuilder("GSTUDY GUI ANALYSIS\nOPTIONS RECORDS 0 NEGATIVE\n");
        List<String> effects = new ArrayList<>();
        for (int f = 0; f < n; f++) {
            String effect = "" + letters.get(f);
            if (!ancestors.get(f).isEmpty()) {
                effect += ":";
                for (int a : ancestors.get(f)) effect += letters.get(a);
            }
            effects.add(effect);
            cards.append("EFFECT ").append(f == 0 ? "* " : "+ ").append(effect).append(' ').append(levels[f]).append(' ')
                 .append(order.get(f).fixed() ? levels[f] : 0).append('\n');
        }
        cards.append("FORMAT 0 0\nPROCESS\n");
        int recordSize = (int)total / levels[0];
        for (int i = 0; i < total; i++) {
            cards.append(Double.toString(values.get(i)));
            // Physical records can wrap; never mix two logical records on a line.
            cards.append((i+1) % recordSize == 0 || (i % recordSize + 1) % 8 == 0 ? '\n' : ' ');
        }
        List<int[]> scenarios = new ArrayList<>();
        if (dStudy) {
            List<List<Integer>> requested = new ArrayList<>(); int count = 1;
            for (int f = 0; f < n; f++) {
                String text = order.get(f).dSizes().trim();
                List<Integer> sizes = new ArrayList<>();
                if (text.isBlank()) sizes.add(levels[f]);
                else for (String t : text.split("[,，\\s]+")) {
                    int v; try { v = Integer.parseInt(t); } catch (NumberFormatException ex) { throw new IllegalArgumentException("D-study levels for " + cols.get(f) + " must be integers."); }
                    if (v < 1 || v > 10000) fail("Each D-study level count must be between 1 and 10,000.");
                    if (order.get(f).fixed() && v != levels[f]) fail("Fixed facet " + cols.get(f) + " must keep its observed level count of " + levels[f] + " in D-study.");
                    sizes.add(v);
                }
                if (sizes.size() > 30) fail("At most 30 D-study scenarios can be compared.");
                requested.add(sizes); count = Math.max(count, sizes.size());
            }
            for (List<Integer> sizes : requested)
                if (sizes.size() != 1 && sizes.size() != count) fail("Each facet's D-study list must contain one value or the same number of values as the other lists; values at the same position form one scenario.");
            for (int s = 0; s < count; s++) {
                int[] v = new int[n];
                for (int f = 0; f < n; f++) v[f] = requested.get(f).get(requested.get(f).size() == 1 ? 0 : s);
                long size = 1;
                for (int value : v) { size *= value; if (size > Integer.MAX_VALUE) fail("This D-study scenario exceeds the engine's integer capacity."); }
                scenarios.add(v);
            }
            cards.append("DSTUDY GUI SCENARIOS\n");
            for (int f = 0; f < n; f++) {
                cards.append("DEFFECT ").append(f == 0 ? "$ " : "").append(effects.get(f));
                int lineLength = 18;
                for (int v : requested.get(f)) {
                    if (lineLength > 85) { cards.append("\n- "); lineLength = 2; }
                    cards.append(' ').append(v); lineLength += 6;
                }
                if (order.get(f).fixed()) cards.append(" / ").append(levels[f]);
                cards.append('\n');
            }
            cards.append("ENDDSTUDY\n");
        }
        cards.append("FINISH\n");
        String description = String.join(" × ", order.stream().map(f -> f.column() + (f.parent().isBlank() ? "" : " (nested within " + f.parent() + ")")).toList());
        return new Prepared(cards.toString(), description, names, List.copyOf(order), levels, List.copyOf(scenarios));
    }

    private static void visit(Facet f, Map<String,Facet> selected, Set<String> visiting, List<Facet> order) {
        if (order.contains(f)) return;
        if (!visiting.add(f.column())) fail("The nesting relationships contain a cycle.");
        if (!f.parent().isBlank()) {
            Facet p = selected.get(f.parent());
            if (p == null) fail("The parent facet for " + f.column() + " is not selected.");
            visit(p, selected, visiting, order);
        }
        visiting.remove(f.column()); order.add(f);
    }
    private static void fail(String message) { throw new IllegalArgumentException(message); }
}
