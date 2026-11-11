package org.genova.gui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Values only: identifiers remain strings and scores are validated separately. */
public record DataTable(List<String> headers, List<List<String>> rows) {
    public DataTable {
        headers = List.copyOf(headers);
        rows = rows.stream().map(List::copyOf).toList();
        if (headers.size() < 2) throw new IllegalArgumentException("At least two columns are required: object of measurement and score.");
        if (headers.size() > 100) throw new IllegalArgumentException("At most 100 columns are supported.");
        if (headers.stream().anyMatch(String::isBlank) || new HashSet<>(headers).size() != headers.size())
            throw new IllegalArgumentException("The first row must contain nonblank, unique column names.");
        if (rows.isEmpty()) throw new IllegalArgumentException("No data rows were found.");
        if (rows.size() > 200_000) throw new IllegalArgumentException("This preview can import at most 200,000 rows.");
        for (int i = 0; i < rows.size(); i++)
            if (rows.get(i).size() != headers.size()) throw new IllegalArgumentException("Row " + (i + 2) + " has a different number of columns from the header.");
    }

    public static DataTable read(Path path, String sheet) throws Exception {
        if (Files.size(path) > 30_000_000) throw new IllegalArgumentException("The file size limit is 30 MB.");
        if (path.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) return xlsx(path, sheet);
        if (path.toString().toLowerCase(Locale.ROOT).endsWith(".xls"))
            throw new IllegalArgumentException("Save the older .xls file as .xlsx or UTF-8 CSV before importing.");
        return parse(Files.readString(path, StandardCharsets.UTF_8));
    }

    public static DataTable parse(String input) {
        input = input.replaceFirst("^\uFEFF", "");
        String first = input.lines().findFirst().orElse("");
        char sep = first.contains("\t") ? '\t' : ',';
        List<List<String>> all = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false, afterQuote = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < input.length() && input.charAt(i + 1) == '"') { cell.append('"'); i++; }
                    else { quoted = false; afterQuote = true; }
                } else cell.append(c);
            } else if (c == '"' && cell.length() == 0 && !afterQuote) quoted = true;
            else if (c == sep || c == '\r' || c == '\n') {
                row.add(cell.toString().trim()); cell.setLength(0); afterQuote = false;
                if (c != sep) {
                    if (row.stream().anyMatch(s -> !s.isBlank())) all.add(row);
                    row = new ArrayList<>();
                    if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n') i++;
                }
            } else {
                if (afterQuote && !Character.isWhitespace(c)) throw new IllegalArgumentException("Invalid character after a closing CSV quote.");
                if (!afterQuote) cell.append(c);
            }
        }
        if (quoted) throw new IllegalArgumentException("The CSV contains an unclosed quote.");
        row.add(cell.toString().trim());
        if (row.stream().anyMatch(s -> !s.isBlank())) all.add(row);
        if (all.isEmpty()) throw new IllegalArgumentException("The table is empty.");
        return new DataTable(all.get(0), all.subList(1, all.size()));
    }

    private static Document xml(ZipFile zip, String path) throws Exception {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) throw new IllegalArgumentException("The Excel workbook is missing " + path);
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        f.setNamespaceAware(true);
        try (InputStream in = zip.getInputStream(entry)) {
            byte[] bytes = in.readNBytes(32_000_001);
            if (bytes.length > 32_000_000) throw new IllegalArgumentException("The uncompressed Excel worksheet is too large.");
            return f.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        }
    }

    private static NodeList nodes(Element e, String tag) { return e.getElementsByTagNameNS("*", tag); }
    private static String value(Element e, String tag) {
        NodeList n = nodes(e, tag);
        return n.getLength() == 0 ? "" : n.item(0).getTextContent();
    }
    public static List<String> sheets(Path path) throws Exception {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            NodeList all = nodes(xml(zip, "xl/workbook.xml").getDocumentElement(), "sheet");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < all.getLength(); i++) result.add(((Element)all.item(i)).getAttribute("name"));
            return result;
        }
    }

    private static DataTable xlsx(Path path, String wantedSheet) throws Exception {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            NodeList sheets = nodes(xml(zip, "xl/workbook.xml").getDocumentElement(), "sheet");
            Element sheet = null;
            for (int i = 0; i < sheets.getLength(); i++) {
                Element candidate = (Element)sheets.item(i);
                if (wantedSheet == null || candidate.getAttribute("name").equals(wantedSheet)) { sheet = candidate; break; }
            }
            if (sheet == null) throw new IllegalArgumentException("The selected worksheet was not found.");
            String rid = sheet.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
            NodeList rels = nodes(xml(zip, "xl/_rels/workbook.xml.rels").getDocumentElement(), "Relationship");
            String target = null;
            for (int i = 0; i < rels.getLength(); i++) {
                Element r = (Element)rels.item(i);
                if (r.getAttribute("Id").equals(rid) && !r.getAttribute("TargetMode").equals("External")) target = r.getAttribute("Target");
            }
            if (target == null) throw new IllegalArgumentException("The Excel worksheet could not be located.");
            String sheetPath = target.startsWith("/") ? target.substring(1) : "xl/" + target;
            sheetPath = Path.of(sheetPath).normalize().toString();
            List<String> strings = new ArrayList<>();
            if (zip.getEntry("xl/sharedStrings.xml") != null) {
                NodeList items = nodes(xml(zip, "xl/sharedStrings.xml").getDocumentElement(), "si");
                for (int i = 0; i < items.getLength(); i++) {
                    NodeList texts = nodes((Element)items.item(i), "t");
                    StringBuilder s = new StringBuilder();
                    for (int j = 0; j < texts.getLength(); j++) s.append(texts.item(j).getTextContent());
                    strings.add(s.toString());
                }
            }
            Element doc = xml(zip, sheetPath).getDocumentElement();
            if (nodes(doc, "mergeCell").getLength() > 0) throw new IllegalArgumentException("Unmerge cells in the worksheet before importing.");
            NodeList rows = nodes(doc, "row");
            List<List<String>> all = new ArrayList<>();
            int width = 0;
            for (int ri = 0; ri < rows.getLength(); ri++) {
                Element r = (Element)rows.item(ri);
                NodeList cells = nodes(r, "c");
                Map<Integer, String> vals = new TreeMap<>();
                for (int ci = 0; ci < cells.getLength(); ci++) {
                    Element c = (Element)cells.item(ci);
                    if (nodes(c, "f").getLength() > 0) throw new IllegalArgumentException("Cell " + c.getAttribute("r") + " contains a formula. Paste formulas as values before importing to avoid stale cached results.");
                    String ref = c.getAttribute("r"), t = c.getAttribute("t");
                    int col = 0;
                    for (char ch : ref.toCharArray()) {
                        if (ch < 'A' || ch > 'Z') break;
                        col = col * 26 + ch - 'A' + 1;
                    }
                    if (col < 1 || col > 100) throw new IllegalArgumentException("The Excel column is outside the supported range.");
                    String v = t.equals("inlineStr") ? value(c, "is") : value(c, "v");
                    if (t.equals("s")) v = strings.get(Integer.parseInt(v));
                    if (t.equals("e")) throw new IllegalArgumentException("Excel contains an error value in cell " + ref);
                    if (!v.isBlank()) vals.put(col - 1, v.trim());
                }
                if (vals.isEmpty()) continue;
                int last = Collections.max(vals.keySet()) + 1;
                if (all.isEmpty()) width = last;
                if (last > width) throw new IllegalArgumentException("Excel data extends beyond the header columns.");
                List<String> record = new ArrayList<>();
                for (int j = 0; j < width; j++) record.add(vals.getOrDefault(j, ""));
                all.add(record);
            }
            if (all.isEmpty()) throw new IllegalArgumentException("The Excel worksheet is empty.");
            return new DataTable(all.get(0), all.subList(1, all.size()));
        }
    }

    public String csv() { StringBuilder s = new StringBuilder(csvRow(headers)); rows.forEach(r -> s.append(csvRow(r))); return s.toString(); }
    public static String csvRow(List<String> cells) {
        return String.join(",", cells.stream().map(v -> "\"" + v.replace("\"", "\"\"") + "\"").toList()) + "\n";
    }
}
