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
        this.data.addShow();
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        return this.data.insertSeat(parameters , numShow);
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
