package de.hska.iwii.db1.weather;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    public static void main(String[] args) throws JSchException, SQLException, ClassNotFoundException, IOException {
        DatabaseConnection connection = new DatabaseConnection();
        connection.connectDB();
        connection.readDBInfo();
        connection.disconnectDB();
    }



    public static Connection conn;


    public void connectDB() throws ClassNotFoundException, SQLException, JSchException, IOException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.put("user", "postgres");
        props.put("password", "db1");
        conn = DriverManager.getConnection("jdbc:postgresql://ts.salzts.gay:5432/bonus", props);
    }

    public void disconnectDB() throws SQLException {
        conn.close();
    }

    public void readDBInfo() throws SQLException {
        DatabaseMetaData metadata = conn.getMetaData();
        ;
        System.out.println("Driver Name: " + metadata.getDriverName() + "\n" + "DBName     : " + metadata.getDatabaseProductName());

    }

    public void readResultSet() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT persnr, name, ort, aufgabe FROM personal");
        printResultSet(rs);
        System.out.println("\nNEXT TABLE\n");
        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT * FROM kunde ");
        printResultSet(rs2);
    }



    public  static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        String verticalPlaceholder = " |";
        int iterator = 1;
        while (rs.next()) {
            String result = "";
            String tableHeader = "";
            String tableTypeHeader = "";
            String seperator = "";
            for (int i = 1; i <= columnsNumber; i++) {
                int colWidth = rs.getMetaData().getColumnDisplaySize(i);
                String colName = rsmd.getColumnName(i);
                String colType = rsmd.getColumnTypeName(i);
                int colNameWidth = colName.length() >= colType.length() ? colName.length() : colType.length();
                colWidth = colWidth >= colNameWidth ? colWidth : colNameWidth;
                //Tabellen Struktur aufbauen mit Kopfzeilen etc
                if (iterator == 1) {
                    String table = String.format("%-" + colWidth + "s", rsmd.getColumnName(i));
                    String tableType = String.format("%-" + colWidth + "s", rsmd.getColumnTypeName(i));
                    String test = String.format("%-" + colWidth + "s", "-").replace(" ", "-");
                    table += verticalPlaceholder;
                    tableType += verticalPlaceholder;
                    test += "-+";
                    seperator += test;
                    tableTypeHeader += tableType;
                    tableHeader += table;
                }


                String append = String.format("%" + colWidth + "s", rs.getString(i));
                append += verticalPlaceholder;
                result += append;

            }
            if (iterator == 1) {
                System.out.println(tableHeader);
                System.out.println(tableTypeHeader);
                System.out.println(seperator);
            }
            System.out.println(result);
            iterator++;
        }
    }

    public static void testPrinter(Connection conn, String query) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        printResultSet(rs);
    }

    public static void insertKunde(Connection conn, int nr, String name, String strasse, int plz, String ort,
                                   String sperre) throws SQLException {
        String query = "INSERT INTO kunde VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, nr);
        st.setString(2, name);
        st.setString(3, strasse);
        st.setInt(4, plz);
        st.setString(5, ort);
        st.setString(6, sperre);
        st.executeUpdate();
    }

    public static void deleteKunde(Connection conn, int kundenNr) throws SQLException {
        //auftrag holen
        String query = "SELECT auftrnr FROM auftrag WHERE kundnr = ?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, kundenNr);
        ResultSet rs = st.executeQuery();
        rs.next();
        //erste spalte ist Auftragsnummer
        int auftragsNr = rs.getInt(1);


        //Auftragsposten entfernen
        st = conn.prepareStatement("DELETE FROM auftragsposten WHERE auftrnr = ?");
        st.setInt(1, auftragsNr);
        st.executeUpdate();

        //Auftrag entfernen
        st = conn.prepareStatement("DELETE FROM auftrag WHERE kundnr = ?");
        st.setInt(1, kundenNr);
        st.executeUpdate();
        st = conn.prepareStatement(query);
        st.setInt(1, 6);

        //Final Kunde entfernen
        st = conn.prepareStatement("DELETE FROM kunde WHERE nr = ?");
        st.setInt(1, kundenNr);
        st.executeUpdate();
    }


    public static void updateKunde(Connection conn, int nr, String sperre) throws SQLException {
        String query = "UPDATE kunde SET sperre = ? WHERE nr = ?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setString(1, sperre);
        st.setInt(2, nr);
        st.executeUpdate();
    }

    public static void insertAuftrag(Connection conn, int auftrnr, Date datum, int kundnr, int persnr)
            throws SQLException {
        String query = "INSERT INTO auftrag VALUES (?, ?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, auftrnr);
        st.setDate(2, datum);
        st.setInt(3, kundnr);
        st.setInt(4, persnr);
        st.executeUpdate();
    }

    public static void insertAuftragsposten(Connection conn, int posnr, int auftrnr, int teilnr, int anzahl,
                                            BigDecimal gesamtpreis) throws SQLException {
        String query = "insert into auftragsposten VALUES (?, ?, ?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, posnr);
        st.setInt(2, auftrnr);
        st.setInt(3, teilnr);
        st.setInt(4, anzahl);
        st.setBigDecimal(5, gesamtpreis);
        st.executeUpdate();
    }

}
