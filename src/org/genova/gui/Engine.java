package org.genova.gui;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class Engine {
    public static Results run(Design.Prepared design) throws Exception {
        Path work = Files.createTempDirectory("genova-studio-");
        Path input = work.resolve("input.txt"), output = work.resolve("output.txt"), log = work.resolve("engine.log");
        Process child = null;
        try {
            Files.writeString(input, design.cards(), StandardCharsets.UTF_8);
            Path java = Path.of(System.getProperty("java.home"), "bin", "java");
            String cp = Path.of(Engine.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            child = new ProcessBuilder(java.toString(), "-Djava.awt.headless=true", "-Duser.language=en", "-Duser.country=US",
                "-Dfile.encoding=UTF-8", "-Xmx512m", "-cp", cp, "initGen.GENOVA", input.toString(), output.toString())
                .directory(work.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(120, TimeUnit.SECONDS)) {
                child.destroyForcibly(); child.waitFor();
                throw new IllegalArgumentException("The analysis exceeded two minutes and was stopped. Reduce the data size or check the design.");
            }
            String raw = Files.exists(output) ? Files.readString(output) : "";
            if (child.exitValue() != 0 || !raw.contains(" jGENOVA has exited!!")) {
                String diagnostic = raw.lines().filter(l -> l.stripLeading().startsWith("***") && !l.contains("GRAND MEAN")).reduce("", (x,y)->x+y+"\n");
                if (diagnostic.isBlank()) diagnostic = "The engine did not complete the analysis. Check the design size and facet relationships.";
                throw new IllegalArgumentException(diagnostic.trim());
            }
            return Results.parse(raw, design);
        } finally {
            if (child != null && child.isAlive()) { child.destroyForcibly(); child.waitFor(); }
            for (Path p : List.of(input, output, log, work)) Files.deleteIfExists(p);
        }
    }
}
