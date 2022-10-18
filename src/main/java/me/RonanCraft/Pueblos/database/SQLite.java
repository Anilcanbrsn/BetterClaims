package me.RonanCraft.Pueblos.database;

import me.RonanCraft.Pueblos.Pueblos;
import me.RonanCraft.Pueblos.resources.files.FileOther;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class SQLite {

    private static final String db_file_name = "database";
    //private final boolean sqlEnabled;
    String table;
    private String host, database, username, password;
    private int port;
    boolean sqlEnabled;
    Connection connection;

    public String addMissingColumns = "ALTER TABLE %table% ADD COLUMN %column% %type%";

    private final DATABASE_TYPE type;

    public SQLite(DATABASE_TYPE type) {
        this.type = type;
    }

    // SQL creation stuff
    public Connection getSQLConnection() {
        if (sqlEnabled) {
            try {
                return getOnline();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                Pueblos.getInstance().getLogger().info("MySQL setup is incorrect! Grabbing data from local database!");
                sqlEnabled = false;
            }
        }
        return getLocal();
    }

    private Connection getOnline() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database +
                    "?autoReconnect=true&useSSL=false", this.username, this.password);
        }
    }

    private Connection getLocal() {
        File dataFolder = new File(Pueblos.getInstance().getDataFolder().getPath() + File.separator + "data", db_file_name + ".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.getParentFile().mkdir();
                dataFolder.createNewFile();
            } catch (IOException e) {
                Pueblos.getInstance().getLogger().log(Level.SEVERE, "File write error: " + dataFolder.getPath());
                e.printStackTrace();
            }
        }
        try {
            if (connection!=null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            Pueblos.getInstance().getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            Pueblos.getInstance().getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        FileOther.FILETYPE sql = FileOther.FILETYPE.MYSQL;
        String pre = "MySQL.";
        sqlEnabled = sql.getBoolean(pre + "enabled");
        host = sql.getString(pre + "host");
        port = sql.getInt(pre + "port");
        database = sql.getString(pre + "database");
        username = sql.getString(pre + "username");
        password = sql.getString(pre + "password");
        connection = getSQLConnection();
        if (!sqlEnabled) { //Update table names back to default if online database fails
            if (type == DATABASE_TYPE.AUCTION)
                table = "Pueblos_Auction";
            else
                table = "Pueblos_Data";
        } else {
            if (type == DATABASE_TYPE.AUCTION)
                table = sql.getString(pre + "tablePrefix") + "auction";
            else
                table = sql.getString(pre + "tablePrefix") + "data";
        }

        try {
            Statement s = connection.createStatement();
            s.executeUpdate(getCreateTable());
            //s.executeUpdate(createTable_bank);
            for (Enum<?> c : getColumns(type)) { //Add missing columns dynamically
                try {
                    String _name = getColumnName(type, c);
                    String _type = getColumnType(type, c);
                    //System.out.println("Adding " + _name);
                    s.executeUpdate(addMissingColumns.replace("%table%", table).replace("%column%", _name).replace("%type%", _type));
                } catch (SQLException e) {
                    //e.printStackTrace();
                }
            }
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        initialize();
    }

    private String getCreateTable() {
        String str = "CREATE TABLE IF NOT EXISTS " + table + " (";
        Enum<?>[] columns = getColumns(type);
        for (Enum<?> c : columns) {
            String _name = getColumnName(type, c);
            String _type = getColumnType(type, c);
            str = str.concat("`" + _name + "` " + _type);
            if (c.equals(columns[columns.length - 1]))
                str = str.concat(")");
            else
                str = str.concat(", ");
        }
        //System.out.println("MySQL column string: `" + str + "`");
        return str;
    }

    private Enum<?>[] getColumns(DATABASE_TYPE type) {
        if (type == DATABASE_TYPE.AUCTION)
            return DatabaseAuctions.COLUMNS.values();
        return DatabaseClaims.COLUMNS.values();
    }

    private String getColumnName(DATABASE_TYPE type, Enum<?> column) {
        if (type == DATABASE_TYPE.AUCTION)
            return ((DatabaseAuctions.COLUMNS) column).name;
        return ((DatabaseClaims.COLUMNS) column).name;
    }

    private String getColumnType(DATABASE_TYPE type, Enum<?> column) {
        if (type == DATABASE_TYPE.AUCTION)
            return ((DatabaseAuctions.COLUMNS) column).type;
        return ((DatabaseClaims.COLUMNS) column).type;
    }

    //Processing
    boolean sqlUpdate(String statement, List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = true;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(statement);
            if (params != null) {
                Iterator<Object> it = params.iterator();
                int paramIndex = 1;
                while (it.hasNext()) {
                    ps.setObject(paramIndex, it.next());
                    paramIndex++;
                }
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            Pueblos.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            success = false;
        } finally {
            close(ps, null, conn);
        }
        return success;
    }

    boolean sqlUpdate(List<String> statement1, List<List<Object>> params1) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = true;
        try {
            conn = getSQLConnection();
            for (int i = 0; i < statement1.size(); i++) {
                String statement = statement1.get(i);
                List<Object> params = params1.get(i);
                ps = conn.prepareStatement(statement);
                if (params != null) {
                    Iterator<Object> it = params.iterator();
                    int paramIndex = 1;
                    while (it.hasNext()) {
                        ps.setObject(paramIndex, it.next());
                        paramIndex++;
                    }
                }
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException ex) {
            Pueblos.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            success = false;
        } finally {
            close(ps, null, conn);
        }
        return success;
    }

    public void initialize() { //Let in console know if its all setup or not
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + getColumnName(type, getColumns(type)[0]) + " = 0");

            rs = ps.executeQuery();
        } catch (SQLException ex) {
            Pueblos.getInstance().getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        } finally {
            close(ps, rs, conn);
        }
    }

    void close(PreparedStatement ps, ResultSet rs, Connection conn) {
        try {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
            if (rs != null) rs.close();
        } catch (SQLException ex) {
            Error.close(Pueblos.getInstance(), ex);
        }
    }

    public enum DATABASE_TYPE {
        CLAIMS, AUCTION
    }
}
