package pt.isec.pd.ticketline.src.model.server;

import java.io.Serial;
import java.io.Serializable;

public class HeartBeat implements Serializable{
    @Serial
    private static final long seralVersioUID = 2L;

    private int portTcp;
    private boolean available;
    private int databaseVersion;
    private int numberOfConnections;

    public HeartBeat(int portTcp, boolean available, int databaseVersion, int numberOfConnections)
    {
        this.portTcp = portTcp;
        this.available = available;
        this.databaseVersion = databaseVersion;
        this.numberOfConnections = numberOfConnections;
    }

    public int getPortTcp(){return this.portTcp;}

    public boolean getAvailable(){return this.available;}

    public int getdatabaseVersion(){return this.databaseVersion;}

    public int getnumberOfConnections(){return this.numberOfConnections;}
}
