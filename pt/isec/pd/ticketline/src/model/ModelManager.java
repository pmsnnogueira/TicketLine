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

    public String listUsers(Integer userID){
        return this.server.listUsers(userID);
    }

    public String listShows(Integer showID){
        return this.server.listShows(showID);
    }

    public String listReservations(Integer reservationID){
        return this.server.listReservations(reservationID);
    }

    public String listSeats(Integer seatID){
        return this.server.listSeats(seatID);
    }

    public void insertShow(){
        this.server.insertShow();
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        return this.server.insertSeat(parameters, numShow);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.server.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.server.insertUser(parameters);
    }

    public boolean deleteShow(int id){
        return this.server.deleteShow(id);
    }

    public boolean deleteReservations(int id){
        return this.server.deleteReservations(id);
    }

    public boolean deleteSeat(int id){
        return this.server.deleteSeat(id);
    }

    public boolean deleteUsers(int id){
        return this.server.deleteUsers(id);
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        return this.server.updateShows(id, newData);
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        return this.server.updateSeats(id, newData);
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        return this.server.updateReservation(id, newData);
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        return this.server.updateUser(id, newData);
    }
    public String listAllAvailableServers(){
        return this.server.listAllAvailableServers();
    }
    public void closeServer() throws IOException, InterruptedException, SQLException {
        this.server.closeServer();
    }
}
