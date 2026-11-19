package org.genova.gui;

import java.nio.file.Path;
import java.util.List;

/** Small command-line bridge for independently checked public-data fixtures. */
public final class OsfSubdesignRunner {
    private OsfSubdesignRunner() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !List.of("rater", "item").contains(args[1]))
            throw new IllegalArgumentException("Usage: OsfSubdesignRunner prepared.csv rater|item");
        DataTable table = DataTable.read(Path.of(args[0]), null);
        if (!table.headers().equals(List.of("person", args[1], "score")))
            throw new IllegalArgumentException("Expected person," + args[1] + ",score columns.");
        Design.Prepared design = Design.prepare(table, "score", "person",
            List.of(new Design.Facet(args[1], "", false, "")), true);
        Results results = Engine.run(design);
        for (List<String> row : results.anova().rows())
            System.out.println("ANOVA\t" + String.join("\t", row));
        for (List<String> row : results.components().rows())
            System.out.println("COMPONENT\t" + String.join("\t", row));
        for (List<String> row : results.decisions().rows())
            System.out.println("DECISION\t" + String.join("\t", row));
    }
}
