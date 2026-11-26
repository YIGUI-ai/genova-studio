package org.genova.gui;

import java.nio.file.Path;
import java.util.Locale;

/** Read-only external-data harness for the Vispoel et al. BFI-2 P x I case. */
public final class VispoelPxiExternalValidation {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Provide the generated long-format CSV path.");
        }
        DataTable data = DataTable.read(Path.of(args[0]), null);
        MpXiDesign.Prepared design = MpXiDesign.prepare(
            data, "score", "person", "variable", "item"
        );
        MpXiEngine.Analysis analysis = MpXiEngine.calculate(design, new int[]{4, 4, 4});

        System.out.printf(Locale.ROOT, "META\t%d\t%d\t%d%n",
            design.observations().size(), design.persons().size(), design.variables().size());
        for (int v = 0; v < design.variables().size(); v++) {
            double[] component = analysis.vc()[v];
            double[] decision = analysis.decisions()[v];
            System.out.printf(Locale.ROOT, "VC\t%s\t%.15g\t%.15g\t%.15g%n",
                design.variables().get(v), component[0], component[1], component[2]);
            System.out.printf(Locale.ROOT, "D\t%s\t%.15g\t%.15g\t%.15g\t%.15g\t%.15g%n",
                design.variables().get(v), decision[0], decision[1], decision[2],
                decision[3], decision[4]);
        }
        for (int a = 0; a < design.variables().size(); a++) {
            for (int b = a + 1; b < design.variables().size(); b++) {
                System.out.printf(Locale.ROOT, "COV\t%s\t%s\t%.15g%n",
                    design.variables().get(a), design.variables().get(b),
                    analysis.personCovariance()[a][b]);
            }
        }
        double[] composite = analysis.composite();
        System.out.printf(Locale.ROOT, "COMPOSITE\t%.15g\t%.15g\t%.15g\t%.15g\t%.15g%n",
            composite[0], composite[1], composite[2], composite[3], composite[4]);
    }
}
