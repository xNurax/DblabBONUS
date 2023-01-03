package de.hska.iwii.db1.weather;

import com.jcraft.jsch.JSchException;
import de.hska.iwii.db1.weather.model.Weather;
import de.hska.iwii.db1.weather.model.WeatherForecast;
import de.hska.iwii.db1.weather.reader.WeatherReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Bonusaufgabe {
    static Connection conn;
    static DatabaseConnection connection;

    public static void main(String[] args) throws JSchException, SQLException, IOException, ClassNotFoundException {
        connection = new DatabaseConnection();
        connection.connectDB();
        conn = DatabaseConnection.conn;
        connection.readDBInfo();
        //Actual Code
        int[] Stationen = {10519, 11032, 2712, 427, 1443};
        createTable();
        readWeatherAndInsertIntoTable(Stationen);
        readWeatherFromDatabase(10519);
        System.out.println("-------------------------------------------------------------------------------------------------\n\n\n");
        stationIdsForDateBetweenTemperature("2023-01-03", 0, 100);
        //Disconnect
        connection.disconnectDB();
    }

    public static void stationIdsForDateBetweenTemperature(String datum, int minTemp, int maxTemp) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("select w.wetterstation ,w.vorhersagedatum, w.minimaletemperatur, w.maximaletemperatur from wettervorhersage w where vorhersagedatum = cast(? as date) and (w.minimaletemperatur>= ? and w.maximaletemperatur<=?)");
        prep.setString(1, datum);
        prep.setInt(2, minTemp);
        prep.setInt(3, maxTemp);
        ResultSet rs = prep.executeQuery();
        connection.printResultSet(rs);
    }

    public static void readWeatherFromDatabase(int StationenId) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("Select * from wetterVorhersage where wetterStation = ?");
        prep.setInt(1, StationenId);
        ResultSet rs = prep.executeQuery();
        connection.printResultSet(rs);

    }

    public static void readWeatherAndInsertIntoTable(int[] stationenIds) throws SQLException {
        WeatherReader weatherReader = new WeatherReader();
        PreparedStatement prep = conn.prepareStatement("INSERT INTO wetterVorhersage (\n" +
                "    wetterStation,\n" +
                "    abrufDatum,\n" +
                "    vorhersageDatum,\n" +
                "    minimaleTemperatur,\n" +
                "    maximaleTemperatur,\n" +
                "    regen,\n" +
                "    sonnenStunden\n" +
                "  )\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");

        for (int stationenId : stationenIds) {
            WeatherForecast vorhersage = weatherReader.readWeatherForecast(stationenId);
            if (vorhersage != null) {
                for (Weather weather : vorhersage.getWeather()) {
                    prep.setLong(1, stationenId);
                    prep.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                    prep.setDate(3, new java.sql.Date(weather.getDate().getTime()));
                    prep.setFloat(4, weather.getMinTemp());
                    prep.setFloat(5, weather.getMaxTemp());
                    prep.setInt(6, weather.getPrecipitation());
                    prep.setInt(7, weather.getSunshine());
                    prep.addBatch();
                }
            }


        }
        prep.executeBatch();
        System.out.println("Tabelle mit Wetter gefüllt!");
    }

    public static void createTable() throws SQLException {
        String query = "DROP TABLE IF EXISTS wetterVorhersage;\n" +
                "        CREATE TABLE wetterVorhersage(\n" +
                "                id SERIAL PRIMARY KEY,\n" +
                "                wetterStation BIGINT NOT NULL,\n" +
                "                abrufDatum DATE NOT NULL,\n" +
                "                vorhersageDatum DATE NOT NULL,\n" +
                "                minimaleTemperatur REAL NOT NULL,\n" +
                "                maximaleTemperatur REAL NOT NULL,\n" +
                "                regen INTEGER NOT NULL,\n" +
                "                sonnenStunden INTEGER NOT NULL\n" +
                "        )";
        PreparedStatement prep = conn.prepareStatement(query);
        prep.executeUpdate();
        System.out.println("Tabelle hinzugefügt!");


    }
}
