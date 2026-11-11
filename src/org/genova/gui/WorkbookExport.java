package org.genova.gui;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Writes a small, dependency-free XLSX workbook with typed numeric results. */
public final class WorkbookExport {
    private static final String MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String PKG = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String CONTENT = "http://schemas.openxmlformats.org/package/2006/content-types";

    private record Sheet(String name, Results.Table table, Set<Integer> numericColumns) {}

    public static void write(Path path, Results results, String design, int observations,
                             String objectColumn, String scoreColumn) throws IOException {
        write(path, results, design, observations, objectColumn, scoreColumn, "jGENOVA 1.0");
    }

    public static void write(Path path, Results results, String design, int observations,
                             String objectColumn, String scoreColumn, String engine) throws IOException {
        List<Sheet> sheets = new ArrayList<>();
        if (!results.decisions().headers().isEmpty())
            sheets.add(new Sheet("D-study", results.decisions(), Set.of(0,2,3,4,5,6)));
        if (!results.setup().headers().isEmpty())
            sheets.add(new Sheet("D-study setup", results.setup(), Set.of(2,3,4)));
        sheets.add(new Sheet("Variance components", results.components(), Set.of(1,2,3,4)));
        sheets.add(new Sheet("ANOVA", results.anova(), Set.of(1,2,3,4,5)));
        Results.Table metadata = new Results.Table(List.of("Field", "Value"), List.of(
            List.of("Design", design), List.of("Observations", Integer.toString(observations)),
            List.of("Object of measurement", objectColumn), List.of("Score column", scoreColumn),
            List.of("D-study scenarios", Long.toString(results.decisions().rows().stream().map(row->row.get(0)).distinct().count())),
            List.of("Statistical engine", engine),
            List.of("Estimation notes", engine.startsWith("urGENOVA")
                ? "Random-effects G-study pilot; raw estimates including negatives; no D-study"
                : engine.startsWith("mGENOVA")
                ? "Experimental multivariate G/D pilot; proportional or custom w/a weights; unequal weights yield composite MSEs, not G/Phi; raw estimates including negatives"
                : "QFM values for fixed effects are not random variance components"),
            List.of("Notices", results.notices())));
        sheets.add(new Sheet("Metadata", metadata, Set.of(1)));

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                "<Relationships xmlns=\""+PKG+"\"><Relationship Id=\"rId1\" Type=\""+REL+
                "/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            put(zip, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++)
                put(zip, "xl/worksheets/sheet"+(i+1)+".xml", worksheet(sheets.get(i)));
        }
    }

    private static void put(ZipOutputStream zip, String name, String xml) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(xml.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int count) {
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<Types xmlns=\""+CONTENT+"\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"+
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"+
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"+
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= count; i++) x.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
            .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        return x.append("</Types>").toString();
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
            "<workbook xmlns=\""+MAIN+"\" xmlns:r=\""+REL+"\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) x.append("<sheet name=\"").append(escape(sheets.get(i).name()))
            .append("\" sheetId=\"").append(i+1).append("\" r:id=\"rId").append(i+1).append("\"/>");
        return x.append("</sheets></workbook>").toString();
    }

    private static String workbookRels(int count) {
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\""+PKG+"\">");
        for (int i = 1; i <= count; i++) x.append("<Relationship Id=\"rId").append(i).append("\" Type=\""+REL+
            "/worksheet\" Target=\"worksheets/sheet").append(i).append(".xml\"/>");
        return x.append("<Relationship Id=\"rId").append(count+1).append("\" Type=\""+REL+
            "/styles\" Target=\"styles.xml\"/></Relationships>").toString();
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\""+MAIN+"\">"+
            "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Arial\"/></font>"+
            "<font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"11\"/><name val=\"Arial\"/></font></fonts>"+
            "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>"+
            "<fill><patternFill patternType=\"gray125\"/></fill>"+
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF243B53\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"+
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"+
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"+
            "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"+
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf></cellXfs>"+
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>";
    }

    private static String worksheet(Sheet sheet) {
        Results.Table table = sheet.table();
        int lastRow = table.rows().size() + 1;
        String lastCol = column(table.headers().size() - 1);
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\""+MAIN+"\">"+
            "<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>"+
            "<sheetFormatPr defaultRowHeight=\"16\"/><cols>");
        for (int i = 0; i < table.headers().size(); i++) {
            int width = Math.min(48, Math.max(14, table.headers().get(i).length() + 4));
            if (i == 0 && !sheet.name().equals("Metadata")) width = Math.max(width, 24);
            if (sheet.name().equals("Metadata")) width = i == 0 ? 27 : 54;
            if (sheet.name().equals("D-study") && i == 1) width = 36;
            x.append("<col min=\"").append(i+1).append("\" max=\"").append(i+1).append("\" width=\"").append(width).append("\" customWidth=\"1\"/>");
        }
        x.append("</cols><sheetData><row r=\"1\">");
        for (int i = 0; i < table.headers().size(); i++) cell(x, 1, i, table.headers().get(i), false, true);
        x.append("</row>");
        for (int r = 0; r < table.rows().size(); r++) {
            x.append("<row r=\"").append(r+2).append("\">");
            List<String> values = table.rows().get(r);
            for (int c = 0; c < values.size(); c++)
                cell(x, r+2, c, values.get(c), sheet.numericColumns().contains(c), false);
            x.append("</row>");
        }
        x.append("</sheetData><autoFilter ref=\"A1:").append(lastCol).append(lastRow).append("\"/></worksheet>");
        return x.toString();
    }

    private static void cell(StringBuilder x, int row, int col, String value, boolean numeric, boolean header) {
        x.append("<c r=\"").append(column(col)).append(row).append('"');
        if (header) x.append(" s=\"1\"");
        if (numeric && validNumber(value)) x.append("><v>").append(value).append("</v></c>");
        else x.append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(escape(value)).append("</t></is></c>");
    }

    private static boolean validNumber(String value) {
        if (!value.matches("[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[Ee][+-]?\\d+)?")) return false;
        try { new BigDecimal(value); return true; } catch (NumberFormatException ex) { return false; }
    }

    private static String column(int zeroBased) {
        StringBuilder s = new StringBuilder();
        for (int n = zeroBased + 1; n > 0; n = (n - 1) / 26) s.insert(0, (char)('A' + (n - 1) % 26));
        return s.toString();
    }

    private static String escape(String value) {
        if (value.length() > 32767) throw new IllegalArgumentException("A text value exceeds Excel's 32,767-character cell limit.");
        StringBuilder x = new StringBuilder();
        value.codePoints().forEach(c -> {
            if (!(c == 9 || c == 10 || c == 13 || c >= 32 && c <= 0xD7FF || c >= 0xE000 && c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF))
                throw new IllegalArgumentException("A text value contains a character that Excel cannot store.");
            switch (c) {
                case '&' -> x.append("&amp;");
                case '<' -> x.append("&lt;");
                case '>' -> x.append("&gt;");
                case '"' -> x.append("&quot;");
                case '\'' -> x.append("&apos;");
                default -> x.appendCodePoint(c);
            }
        });
        return x.toString();
    }
}
