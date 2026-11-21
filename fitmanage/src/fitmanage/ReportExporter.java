package fitmanage;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Penulis file laporan dari sebuah TableModel, tanpa library tambahan:
 * - Excel (.xlsx) : file Office Open XML minimal (judul tebal, header berwarna)
 * - CSV (.csv) : UTF-8 dengan BOM, pemisah titik koma (cocok untuk Excel
 * Indonesia)
 * - HTML (.html) : laporan siap cetak; di browser klik Cetak lalu "Simpan
 * sebagai PDF"
 */
final class ReportExporter {

    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportExporter() {
    }

    // ---------------------------------------------------------------
    // Util
    // ---------------------------------------------------------------
    private static String nilai(TableModel m, int baris, int kolom) {
        Object v = m.getValueAt(baris, kolom);
        return v == null ? "" : v.toString();
    }

    private static String waktuSekarang() {
        return LocalDateTime.now().format(FORMAT_WAKTU);
    }

    // Membuang karakter kontrol yang tidak boleh ada di XML
    private static String bersihXml(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r' || c >= 0x20)
                sb.append(c);
        }
        return sb.toString();
    }

    private static String xml(String s) {
        return bersihXml(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ---------------------------------------------------------------
    // CSV
    // ---------------------------------------------------------------
    static void tulisCsv(File file, TableModel model) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM agar Excel membaca UTF-8 dengan benar
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0)
                    w.write(';');
                w.write(csv(model.getColumnName(c)));
            }
            w.write("\r\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0)
                        w.write(';');
                    w.write(csv(nilai(model, r, c)));
                }
                w.write("\r\n");
            }
        }
    }

    private static String csv(String s) {
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ---------------------------------------------------------------
    // HTML (cetak / PDF)
    // ---------------------------------------------------------------
    static void tulisHtml(File file, String judul, TableModel model) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"id\">\n<head>\n<meta charset=\"UTF-8\">\n<title>")
                .append(html(judul)).append("</title>\n<style>\n")
                .append("body{font-family:'Segoe UI',Arial,sans-serif;margin:32px;color:#1d2433}\n")
                .append("h1{font-size:22px;color:#0a193c;margin:0 0 4px}\n")
                .append(".meta{color:#6b7280;font-size:13px;margin-bottom:18px}\n")
                .append("table{border-collapse:collapse;width:100%;font-size:13px}\n")
                .append("th{background:#0a193c;color:#fff;text-align:left;padding:8px 10px}\n")
                .append("td{padding:7px 10px;border-bottom:1px solid #e5e7eb}\n")
                .append("tbody tr:nth-child(even) td{background:#f8fafc}\n")
                .append(".aksi{margin-bottom:18px}\n")
                .append("button{background:#0083b0;color:#fff;border:0;border-radius:8px;padding:10px 18px;font-size:14px;font-weight:600;cursor:pointer}\n")
                .append("@media print{.aksi{display:none}body{margin:12mm}thead{display:table-header-group}tr{page-break-inside:avoid}}\n")
                .append("</style>\n</head>\n<body>\n")
                .append("<div class=\"aksi\"><button onclick=\"window.print()\">Cetak / Simpan sebagai PDF</button></div>\n")
                .append("<h1>").append(html(judul)).append("</h1>\n")
                .append("<div class=\"meta\">Fit Manage &bull; Dibuat ").append(waktuSekarang())
                .append(" &bull; ").append(model.getRowCount()).append(" baris data</div>\n")
                .append("<table>\n<thead><tr>");
        for (int c = 0; c < model.getColumnCount(); c++) {
            sb.append("<th>").append(html(model.getColumnName(c))).append("</th>");
        }
        sb.append("</tr></thead>\n<tbody>\n");
        for (int r = 0; r < model.getRowCount(); r++) {
            sb.append("<tr>");
            for (int c = 0; c < model.getColumnCount(); c++) {
                sb.append("<td>").append(html(nilai(model, r, c))).append("</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</tbody>\n</table>\n</body>\n</html>\n");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(sb.toString());
        }
    }

    // ---------------------------------------------------------------
    // Excel (.xlsx)
    // ---------------------------------------------------------------
    static void tulisXlsx(File file, String judul, TableModel model) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            tambahBagian(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                            + "</Types>");

            tambahBagian(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                            + "</Relationships>");

            tambahBagian(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                            + "<sheets><sheet name=\"Laporan\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");

            tambahBagian(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                            + "</Relationships>");

            tambahBagian(zip, "xl/styles.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                            + "<fonts count=\"3\">"
                            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                            + "<font><b/><sz val=\"14\"/><name val=\"Calibri\"/></font>"
                            + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                            + "</fonts>"
                            + "<fills count=\"3\">"
                            + "<fill><patternFill patternType=\"none\"/></fill>"
                            + "<fill><patternFill patternType=\"gray125\"/></fill>"
                            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF0A193C\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                            + "</fills>"
                            + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                            + "<cellXfs count=\"3\">"
                            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                            + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                            + "</cellXfs>"
                            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                            + "</styleSheet>");

            tambahBagian(zip, "xl/worksheets/sheet1.xml", buatSheet(judul, model));
        }
    }

    private static void tambahBagian(ZipOutputStream zip, String nama, String isi) throws IOException {
        zip.putNextEntry(new ZipEntry(nama));
        zip.write(isi.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String buatSheet(String judul, TableModel model) {
        int kolom = model.getColumnCount();

        // Lebar kolom mengikuti isi terpanjang (dibatasi 10 - 45 karakter)
        int[] lebar = new int[kolom];
        for (int c = 0; c < kolom; c++) {
            int maks = model.getColumnName(c).length();
            for (int r = 0; r < model.getRowCount(); r++) {
                maks = Math.max(maks, nilai(model, r, c).length());
            }
            lebar[c] = Math.max(10, Math.min(45, maks + 2));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><cols>");
        for (int c = 0; c < kolom; c++) {
            sb.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(lebar[c]).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");

        baris(sb, 1, new String[] { judul }, 1);
        baris(sb, 2, new String[] { "Dibuat: " + waktuSekarang() }, 0);

        String[] header = new String[kolom];
        for (int c = 0; c < kolom; c++)
            header[c] = model.getColumnName(c);
        baris(sb, 4, header, 2);

        for (int r = 0; r < model.getRowCount(); r++) {
            String[] isi = new String[kolom];
            for (int c = 0; c < kolom; c++)
                isi[c] = nilai(model, r, c);
            baris(sb, 5 + r, isi, 0);
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static void baris(StringBuilder sb, int nomor, String[] sel, int gaya) {
        sb.append("<row r=\"").append(nomor).append("\">");
        for (int c = 0; c < sel.length; c++) {
            sb.append("<c r=\"").append(namaKolom(c)).append(nomor).append("\" t=\"inlineStr\"");
            if (gaya > 0)
                sb.append(" s=\"").append(gaya).append("\"");
            sb.append("><is><t xml:space=\"preserve\">").append(xml(sel[c])).append("</t></is></c>");
        }
        sb.append("</row>");
    }

    // 0 -> A, 25 -> Z, 26 -> AA
    private static String namaKolom(int indeks) {
        StringBuilder sb = new StringBuilder();
        int n = indeks;
        do {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }
}