package pt.isec.pd.ticketline.src.model;

import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.Server;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelManager {
    private Server server;

    public ModelManager(int port, String DBDirectory) throws SQLException, IOException, InterruptedException {
        this.server = new Server(port, DBDirectory);
    }
    public String listAllAvailableServers(){
        return this.server.listAllAvailableServers();
    }
    public void closeServer() throws IOException, InterruptedException, SQLException {
        this.server.closeServer();
    }
}
