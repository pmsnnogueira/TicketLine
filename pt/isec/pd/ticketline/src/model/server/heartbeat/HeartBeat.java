package pt.isec.pd.ticketline.src.model.server.heartbeat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

public class HeartBeat implements Serializable{
    @Serial
    private static final long serialVersionUID = 2L;

    private int portTcp;
    private boolean available;
    private int databaseVersion;
    private int numberOfConnections;
    private LocalTime timeCreated;

    public HeartBeat(int portTcp, boolean available, int databaseVersion, int numberOfConnections)
    {
        this.portTcp = portTcp;
        this.available = available;
        this.databaseVersion = databaseVersion;
        this.numberOfConnections = numberOfConnections;
        this.timeCreated = LocalTime.now();
    }

    public int getPortTcp(){return this.portTcp;}

    public boolean getAvailable(){return this.available;}

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getDatabaseVersion(){return this.databaseVersion;}

    public int getNumberOfConnections(){return this.numberOfConnections;}

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
