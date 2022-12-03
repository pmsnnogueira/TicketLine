package pt.isec.pd.ticketline.src.model.server.heartbeat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

public class HeartBeat implements Serializable{
    @Serial
    private static final long serialVersionUID = 2L;

    private String message;
    private int portTcp;
    private String ip;
    private boolean available;
    private int databaseVersion;
    private int numberOfConnections;
    private String dbPath;
    private LocalTime timeCreated;
    private String queries;

    public HeartBeat(int portTcp, boolean available, int databaseVersion,
                     int numberOfConnections, String dbPath,
                     String ip)
    {
        this.portTcp = portTcp;
        this.available = available;
        this.databaseVersion = databaseVersion;
        this.numberOfConnections = numberOfConnections;
        this.timeCreated = LocalTime.now();
        this.dbPath = dbPath;
        this.ip = ip;
        this.message = "";
    }

    public void setQueries(String newQuery){
        this.queries = newQuery;
    }
    public void resetMostRecentQuery(){
        this.queries = null;
    }
    public String getQueries() {
        return queries;
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
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public void setNumberOfConnections(int numberOfConnections) {
        this.numberOfConnections = numberOfConnections;
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
