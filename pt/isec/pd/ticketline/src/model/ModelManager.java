package pt.isec.pd.ticketline.src.model;

import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelManager {
    private Data data;

    public ModelManager(int port) throws SQLException {
        this.data = new Data(port);
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
        this.data.addShow();
    }

    public boolean insertSeat(ArrayList<String> parameters){
        return this.data.insertSeat(parameters);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.data.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.data.insertUser(parameters);
    }

    public boolean deleteShow(int id){return this.data.deleteShow(id);}

    public boolean deleteReservations(int id){return this.data.deleteReservations(id);}

    public boolean deleteSeat(int id){return this.data.deleteSeat(id);}

    public boolean deleteUsers(int id){return this.data.deleteUsers(id);}

    public boolean updateShows(int id, HashMap<String, String> newData){
        return this.data.updateShows(id, newData);
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        return this.data.updateSeats(id, newData);
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        return this.data.updateReservation(id, newData);
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        return this.data.updateUser(id, newData);
    }

    public boolean processANewHeartBeat(HeartBeat heartBeat){
        return this.data.processANewHeartBeat(heartBeat);
    }
    public boolean checkFOrServerDeath(){
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
}
