package org.genova.gui;

import java.util.*;

/** Strict first urGENOVA subset: one-way random objects with replicates nested in each object. */
public final class UrDesign {
    public record Group(String id, List<Double> scores) {
        public Group { scores = List.copyOf(scores); }
    }
    public record Prepared(String objectColumn, String replicateColumn, String scoreColumn,
                           List<Group> groups, int observations) {
        public Prepared { groups = List.copyOf(groups); }
        public String description() {
            return "Unbalanced one-way random G-study: " + objectColumn + " with "
                + replicateColumn + " : " + objectColumn;
        }
    }

    public static Prepared prepare(DataTable data, String score, String object, String replicate) {
        if (data == null) throw new IllegalArgumentException("Import a table before running the analysis.");
        if (score == null || object == null || replicate == null ||
            !data.headers().containsAll(List.of(score, object, replicate)) ||
            new HashSet<>(List.of(score, object, replicate)).size() != 3)
            throw new IllegalArgumentException("Choose three different existing columns for object, replicate unit, and score.");
        if (data.headers().size() != 3)
            throw new IllegalArgumentException("The one-way pilot accepts exactly three columns: object, nested replicate unit, and score. Remove additional facets before using this model.");
        int s = data.headers().indexOf(score), o = data.headers().indexOf(object), r = data.headers().indexOf(replicate);
        Map<String, List<Double>> groups = new LinkedHashMap<>();
        Set<List<String>> seen = new HashSet<>();
        for (int row = 0; row < data.rows().size(); row++) {
            List<String> values = data.rows().get(row);
            String objectId = values.get(o), replicateId = values.get(r), raw = values.get(s);
            if (objectId.isBlank() || replicateId.isBlank())
                throw new IllegalArgumentException("Row " + (row + 2) + " has a blank object or replicate ID.");
            if (!seen.add(List.of(objectId, replicateId)))
                throw new IllegalArgumentException("Row " + (row + 2) + " duplicates an object/replicate cell.");
            double value;
            try { value = Double.parseDouble(raw); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Row " + (row + 2) + " has a nonnumeric score."); }
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Row " + (row + 2) + " must have a finite score.");
            groups.computeIfAbsent(objectId, ignored -> new ArrayList<>()).add(value);
        }
        if (groups.size() < 2) throw new IllegalArgumentException("At least two objects are required.");
        for (var entry : groups.entrySet())
            if (entry.getValue().size() < 2)
                throw new IllegalArgumentException("Object " + entry.getKey() + " needs at least two nested replicate observations.");
        List<Group> ordered = groups.entrySet().stream().map(e -> new Group(e.getKey(), e.getValue())).toList();
        return new Prepared(object, replicate, score, ordered, data.rows().size());
    }
}
