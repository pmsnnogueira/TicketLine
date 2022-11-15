package pt.isec.pd.ticketline.src.model.server.heartbeat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;

public class HeartBeat implements Serializable{
    @Serial
    private static final long serialVersionUID = 2L;

    private int portTcp;
    private String ip;
    private boolean available;
    private int databaseVersion;
    private int numberOfConnections;
    private ArrayList<String> queries;
    private String dbPath;
    private LocalTime timeCreated;

    public HeartBeat(int portTcp, boolean available, int databaseVersion,
                     int numberOfConnections, String dbPath,
                     String ip)
    {
        this.portTcp = portTcp;
        this.available = available;
        this.databaseVersion = databaseVersion;
        this.numberOfConnections = numberOfConnections;
        this.queries = new ArrayList<>();
        this.timeCreated = LocalTime.now();
        this.dbPath = dbPath;
        this.ip = ip;
    }

    public int getPortTcp(){return this.portTcp;}

    public boolean getAvailable(){return this.available;}

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setDatabaseVersion(int databaseVersion){this.databaseVersion = databaseVersion;}

    public int getDatabaseVersion(){return this.databaseVersion;}

    public int getNumberOfConnections(){return this.numberOfConnections;}

    public String getDbPath(){return this.dbPath;}

    public String getIp(){return this.ip;}

    public void setTimeCreated() {
        this.timeCreated = LocalTime.now();
    }

    public LocalTime getTimeCreated() {
        return LocalTime.of(timeCreated.getHour(), timeCreated.getMinute(), timeCreated.getSecond());
    }

    @Override
    public String toString() {
        return "Port:[" + portTcp + "] Available -> [" + available + "] Database version -> [" +
                databaseVersion + "] Number of connections -> [" + numberOfConnections + "] " +
                "Created at -> [" + timeCreated + "]";
    }

    @Override
    public int hashCode() {
        return portTcp;
    }
}
