package net.square.utilities.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQL {

    private Connection connection;
    public static MySQL mySQL;

    private final String address;
    private final String database;
    private final String password;
    private final String username;

    private final int port;
    public int logs = 0;
    public int bans = 0;

    public MySQL(String address, String database, String password, String username, int port) {

        mySQL = this;

        this.address = address;
        this.database = database;
        this.password = password;
        this.username = username;
        this.port = port;

        this.connectClient();
        this.createTables();

        if (isConnected()) {
            logs = getLogsCount();
            bans = getBanCount();
        }
    }

    public void createTables() {
        executeStatement(
            "CREATE TABLE IF NOT EXISTS bans(uuid VARCHAR(64), name VARCHAR(32), timestamp BIGINT, "
                + "checkType VARCHAR(64), PRIMARY KEY(uuid));");
        executeStatement(
            "CREATE TABLE IF NOT EXISTS logs(uuid VARCHAR(64), name VARCHAR(32), timestamp BIGINT, checkType "
                + "VARCHAR(64), violations BIGINT, ping INT, data TEXT);");

        executeStatement("CREATE TABLE IF NOT EXISTS verbose(uuid varchar(64), verbose BOOLEAN, PRIMARY KEY(uuid));");
        executeStatement(
            "CREATE TABLE IF NOT EXISTS module_count(module_name varchar(64), count BIGINT, PRIMARY KEY(module_name))"
                + ";");
        executeStatement(
            "CREATE TABLE IF NOT EXISTS playerdata(uuid varchar(64), name varchar(16), kicks BIGINT, PRIMARY KEY"
                + "(uuid));");
    }

    public int getModuleKicks(String module) {
        try (ResultSet rs = getResult("SELECT count FROM module_count WHERE module_name = '" + module + "';")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getPlayerKicks(String name) {
        try (ResultSet rs = getResult("SELECT kicks FROM playerdata WHERE name = '" + name + "';")) {
            if (rs.next()) {
                return rs.getInt("kicks");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateModuleData(String module) {
        executeStatement("UPDATE module_count SET count = count + 1 WHERE module_name = '" + module + "';");
    }

    public void updateData(UUID uuid) {
        executeStatement("UPDATE playerdata SET kicks = kicks + 1 WHERE uuid = '" + uuid + "';");
    }

    public void insertData(UUID uuid, String username) {
        executeStatement("INSERT INTO playerdata(uuid, name, kicks) VALUES ('" + uuid + "', '" + username + "', 0);");
    }

    public void insertModuleData(String module) {
        executeStatement("INSERT INTO module_count(module_name, count) VALUES ('" + module + "', 0);");
    }

    public boolean containsModuleData(String module) {
        try (ResultSet rs = getResult("SELECT count FROM module_count WHERE module_name = '" + module + "';")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean containsData(UUID uuid) {
        try (ResultSet rs = getResult("SELECT name FROM playerdata WHERE uuid = '" + uuid + "';")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean containsVerbose(UUID uuid) {
        try (ResultSet rs = getResult("SELECT verbose FROM verbose WHERE uuid = '" + uuid + "';")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertToVerbose(UUID uuid) {
        executeStatement("INSERT INTO verbose(uuid, verbose) VALUES ('" + uuid + "', false);");
    }

    public void updateVerbose(UUID uuid, boolean verbose) {
        executeStatement("UPDATE verbose SET verbose = " + verbose + " WHERE uuid = '" + uuid + "';");
    }

    public boolean isVerbose(UUID uuid) {
        try (ResultSet rs = getResult("SELECT verbose FROM verbose WHERE uuid = '" + uuid + "';")) {
            if (rs.next()) {
                return rs.getBoolean("verbose");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getBanCount() {
        try (ResultSet rs = getResult("SELECT COUNT(*) FROM bans;")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getLogsCount() {
        try (ResultSet rs = getResult("SELECT COUNT(*) FROM logs;")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void createLog(UUID uuid, String username, String check, int vio, int ping, String data) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                String.format(
                    "INSERT INTO logs(uuid, name, timestamp, checkType, violations, ping, data) "
                        + "VALUES ('%s', '%s', '%d', '%s', '%d', '%d', '%s');",
                    uuid, username, System.currentTimeMillis(), check, vio, ping, data
                ));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void banPlayer(UUID uuid, String username, String check) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                String.format(
                    "INSERT INTO bans(uuid, name, timestamp, checkType) VALUES ('%s', '%s', '%d', '%s');", uuid,
                    username, System.currentTimeMillis(), check
                ));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public List<String> getInformationOfPlayer(String playerName, int length) {
        List<String> informationList = new ArrayList<>();

        try (ResultSet rs = getResult(String.format(
            "SELECT * FROM logs WHERE name = '%s' ORDER BY timestamp DESC LIMIT %d;", playerName, length)
        )) {
            while (rs.next()) {
                String result = String.format(
                    "%s %s %s and failed %s | VL: +%d (Ping: %d)",
                    time(System.currentTimeMillis() - rs.getLong("timestamp")),
                    rs.getString("name"), rs.getString("data"),
                    rs.getString("checkType"), rs.getInt("violations"), rs.getInt("ping")
                );

                informationList.add(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return informationList;
    }

    public String time(long milliSecs) {
        long secs = milliSecs / 1000L;
        long min = secs / 60L;
        long hours = min / 60L;
        min %= 60L;
        long days = hours / 24L;
        hours %= 24L;
        return String.format(
            "[%s %s %s ago]",
            pluralFormat("d", days),
            pluralFormat("h", hours),
            pluralFormat("m", min)
        );
    }

    private String pluralFormat(final String word, final long value) {
        return value + word;
    }

    public ResultSet getResult(final String query) {
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void unbanPlayer(UUID uuid) {
        executeStatement("DELETE FROM bans WHERE uuid='" + uuid + "'");
    }

    public long isBanned(UUID uuid) {
        try (ResultSet rs = getResult("SELECT timestamp FROM bans WHERE uuid='" + uuid + "'")) {
            if (rs.next())
                return rs.getLong("timestamp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void executeStatement(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void connectClient() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + address + ":" + port + "/" + database + "?autoReconnect=true", username, password);

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
