package pt.isec.pd.ticketline.src.model;

import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.Server;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelManager {
    private Data data;
    private Server server;

    public ModelManager(int port, String DBDirectory) throws SQLException, IOException, InterruptedException {
        this.data = new Data();
        this.server = new Server(port, DBDirectory, this.data);
    }

    public String listUsers(Integer userID){
        return this.data.listUsers(userID);
    }

    public String listShows(Integer showID){
        return this.data.listShows(showID);
    }

    public String listReservations(Integer reservationID){
        return this.data.listReservations(reservationID);
    }

    public String listSeats(Integer seatID){
        return this.data.listSeats(seatID);
    }

    public void insertShow(){
        this.server.updateDBVersion();
        this.data.addShow();
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        boolean bool = this.data.insertSeat(parameters, numShow);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean insertReservation(ArrayList<String> parameters){
        boolean bool = this.data.insertReservation(parameters);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean insertUser(ArrayList<String> parameters){
        boolean bool = this.data.insertUser(parameters);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteShow(int id){
        boolean bool = this.data.deleteShow(id);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteReservations(int id){
        boolean bool = this.data.deleteReservations(id);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteSeat(int id){
        boolean bool = this.data.deleteSeat(id);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteUsers(int id){
        boolean bool = this.data.deleteUsers(id);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateShows(id, newData);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateSeats(id, newData);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateReservation(id, newData);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateUser(id, newData);

        if(bool){
            this.server.updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean processANewHeartBeat(HeartBeat heartBeat){
        return this.data.processANewHeartBeat(heartBeat);
    }
    public boolean checkForServerDeath(){
        return this.data.checkForServerDeath();
    }

    public String listAllAvailableServers(){
        return this.data.listAllAvailableServers();
    }

    public boolean serverLifeCheck(){
        return this.data.serverLifeCheck();
    }

    public void closeDB() throws SQLException{
        this.data.closeDB();
    }

    public void closeServer() throws IOException, InterruptedException {
        this.server.closeServer();
    }
}
