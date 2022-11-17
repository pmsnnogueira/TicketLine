package pt.isec.pd.ticketline.src.resources;

import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.resources.db.DBManager;

import java.net.MulticastSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourcesManager {
    private final DBManager dbManager;

    public ResourcesManager(MulticastSocket mcs) throws SQLException {
        this.dbManager = new DBManager(mcs);
    }

    public boolean connectToDB(int port, String DBDirectory){
        return this.dbManager.connectToDB(port, DBDirectory);
    }

    public void processNewQuerie(String newQuerie){
        this.dbManager.processNewQuerie(newQuerie);
    }

    public int testDatabaseVersion(String DBDirectory, int tcpPort){
        return this.dbManager.testDatabaseVersion(DBDirectory, tcpPort);
    }

    public int getDatabaseVersion(){
        return this.dbManager.getDatabaseVersion();
    }

    public boolean updateVersion(){
        return this.dbManager.updateVersion();
    }

    public boolean insertVersion(){
        return this.dbManager.insertVersion();
    }

    public String listUsers(Integer userID){
        return this.dbManager.listUsers(userID);
    }

    public String listShows(Integer showID){
        return this.dbManager.listShows(showID);
    }

    public String listReservations(Integer reservationID){
        return this.dbManager.listReservations(reservationID);
    }

    public String listSeats(Integer seatID){
        return this.dbManager.listSeats(seatID);
    }

    public int insertShow(ArrayList<String> parameters) {
        return this.dbManager.insertShow(parameters);
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        return this.dbManager.insertSeat(parameters , numShow);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.dbManager.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.dbManager.insertUser(parameters);
    }

    public boolean deleteShow(int id){return this.dbManager.deleteShow(id);}

    public boolean deleteReservations(int id){return this.dbManager.deleteReservations(id);}

    public boolean deleteSeat(int id){return this.dbManager.deleteSeat(id);}

    public boolean deleteUsers(int id){return this.dbManager.deleteUsers(id);}

    public boolean updateShows(int id, HashMap<String, String> newData){
        return this.dbManager.updateShows(id, newData);
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        return this.dbManager.updateSeats(id, newData);
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        return this.dbManager.updateReservation(id, newData);
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        return this.dbManager.updateUser(id, newData);
    }

    public void closeDB() throws SQLException {
        this.dbManager.close();
    }

    public void setServerHB(HeartBeat serverHB){
        this.dbManager.setServerHB(serverHB);
    }
}
