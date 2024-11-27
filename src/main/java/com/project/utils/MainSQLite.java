package com.project.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * Aquest exemple mostra les 
 * dades de SQLite quan hibernate
 * ja ha generat les taules
 */

public class MainSQLite {

    public static void main(String[] args) throws SQLException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "database.db";
        ResultSet rs = null;
    
        // Connectar (crea la BBDD si no existeix)
        Connection conn = UtilsSQLite.connect(filePath);
    
        // Llistar les taules
        ArrayList<String> taules = UtilsSQLite.listTables(conn);
        System.out.println("Taules: " + taules);
    
        for (String nomTaula : taules) {
            // Fer una única consulta per taula
            rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + nomTaula + ";");
            ResultSetMetaData rsmd = rs.getMetaData();
    
            // Mostrar les columnes de la taula
            System.out.println("Columnes de la taula " + nomTaula + ":");
            for (int cntCol = 1; cntCol <= rsmd.getColumnCount(); cntCol++) {
                String label = rsmd.getColumnLabel(cntCol);
                String name = rsmd.getColumnName(cntCol);
                int type = rsmd.getColumnType(cntCol);
                System.out.println("    " + label + ", " + name + ", " + type);
            }
    
            // Mostrar la informació de la taula
            System.out.println("Continguts de la taula " + nomTaula + ":");
            while (rs.next()) {
                StringBuilder txt = new StringBuilder();
                for (int cntCol = 1; cntCol <= rsmd.getColumnCount(); cntCol++) {
                    if (cntCol == 1) {
                        txt.append("    ");
                    } else {
                        txt.append(", ");
                    }
                    
                    String name = rsmd.getColumnName(cntCol);
                    switch (rsmd.getColumnType(cntCol)) {
                        case java.sql.Types.INTEGER -> txt.append(rs.getInt(name));
                        case java.sql.Types.VARCHAR -> txt.append(rs.getString(name));
                        case java.sql.Types.REAL -> txt.append(rs.getFloat(name));
                        case java.sql.Types.BIGINT -> txt.append(rs.getLong(name));
                        case java.sql.Types.BOOLEAN -> txt.append(rs.getBoolean(name));
                        default -> txt.append("???");
                    }
                }
                System.out.println(txt);
            }
            System.out.println(); // Línia en blanc entre taules
        }
    
        // Desconnectar
        UtilsSQLite.disconnect(conn);
    }
}