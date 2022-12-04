package pt.isec.pd.ticketline.src.model.data;

import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.resources.ResourcesManager;
import pt.isec.pd.ticketline.src.resources.files.FileOpener;

import java.net.MulticastSocket;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;

public class Data {
    private ResourcesManager resourcesManager;
    private ArrayList<HeartBeat> heartBeatsReceived;
    private HeartBeat serverHeartBeat;

    public Data() throws SQLException {
        this.resourcesManager = new ResourcesManager();
        this.heartBeatsReceived = new ArrayList<>();
    }

    public void setServerHeartBeat(HeartBeat serverHeartBeat){
        this.resourcesManager.setServerHB(serverHeartBeat);
        this.serverHeartBeat = serverHeartBeat;
    }

    public boolean connectToDB(int port, String DBDirectory){
        return this.resourcesManager.connectToDB(port, DBDirectory);
    }

    public void processNewQuerie(String newQuerie){
        this.resourcesManager.processNewQuerie(newQuerie);
    }

    public int getDatabaseVersion(){
        return this.resourcesManager.getDatabaseVersion();
    }


    public int testDatabaseVersion(String DBDirectory, int tcpPort){
        return this.resourcesManager.testDatabaseVersion(DBDirectory, tcpPort);
    }

    public String listUsers(Integer userID){
        return this.resourcesManager.listUsers(userID);
    }

    public String verifyUserLogin(ArrayList<String> parameters){return this.resourcesManager.verifyUserLogin(parameters);}
    public String listShows(Integer showID){
        return this.resourcesManager.listShows(showID);
    }

    public String listReservations(Integer reservationID){
        return this.resourcesManager.listReservations(reservationID);
    }

    public String listSeats(Integer seatID){
        return this.resourcesManager.listSeats(seatID);
    }

    public boolean insertShowSeatFile(ArrayList<String> parametersShow , ArrayList<ArrayList<String>> parametersSeats){
        return this.resourcesManager.insertShowSeatFile(parametersShow , parametersSeats);
    }

    public int insertShow(ArrayList<String> parameters){
        return this.resourcesManager.insertShow(parameters);
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        return this.resourcesManager.insertSeat(parameters , numShow);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.resourcesManager.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.resourcesManager.insertUser(parameters);
    }

    public void closeDB() throws SQLException{
        this.resourcesManager.closeDB();
    }

    public void addShow()
    {
        List<String> information = FileOpener.openFile("pt/isec/pd/ticketline/src/resources/files/teste.txt");
        ArrayList<String> parameters = new ArrayList<>();
        ArrayList<ArrayList<String>> arraySeats = new ArrayList<>();
        String dateHour = "";
        boolean seats = false;

        for(String string : information)
        {
            String newString = string.replaceAll("\"", "");
            String[] splitted = newString.split(";");
            if(!seats){
                if(newString.contains("Designação"))
                {
                    String designation = splitted[1];
                    parameters.add(designation);
                }
                else if(newString.contains("Tipo"))
                {
                    String type = splitted[1];
                    parameters.add(type);
                }
                else if(newString.contains("Data"))
                {
                    String day = splitted[1];
                    String month = splitted[2];
                    String year = splitted[3];
                    dateHour = day + ":" + month + ":" + year + "-";
                }
                else if(newString.contains("Hora"))
                {
                    String hour = splitted[1];
                    String minutes = splitted[2];
                    dateHour += hour + ":" + minutes;
                    parameters.add(dateHour);
                }
                else if(newString.contains("Duração"))
                {
                    String duration = splitted[1];
                    parameters.add(duration); 
                }
                else if(newString.contains("Local"))
                {
                    String place = splitted[1];
                    parameters.add(place);
                }
                else if(newString.contains("Localidade"))
                {
                    String local = splitted[1];
                    parameters.add(local);
                }
                else if(newString.contains("País"))
                {
                    String country = splitted[1];
                    parameters.add(country);
                }
                else if(newString.contains("Classificação etária"))
                {
                    String age = splitted[1];
                    parameters.add(age);
                }
                else if(newString.contains("Fila"))
                    seats = true;
                
                continue;
            }
             //Insert Seats to arraySeats
            if(seats){
                String newStringSeats = string.replaceAll("\"", "");
                newStringSeats = newStringSeats.replaceAll(";", ":").replaceAll("\\s+", "");
                String[] splittedSeats = newStringSeats.split(":");
                for(int i = 1 ; i < splittedSeats.length ; i++){
                    ArrayList<String> enviar = new ArrayList<>();
                    Collections.addAll(enviar, splittedSeats[0] , splittedSeats[i++] , splittedSeats[i]);
                    arraySeats.add(enviar);
                }
            }
        }

        insertShowSeatFile(parameters , arraySeats);
    }

    public boolean deleteShow(int id){return this.resourcesManager.deleteShow(id);}

    public boolean deleteReservations(int id){return this.resourcesManager.deleteReservations(id);}

    public boolean deleteSeat(int id){return this.resourcesManager.deleteSeat(id);}

    public boolean deleteUsers(int id){return this.resourcesManager.deleteUsers(id);}

    public boolean updateShows(int id, HashMap<String, String> newData){
        return this.resourcesManager.updateShows(id, newData);
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        return this.resourcesManager.updateSeats(id, newData);
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        return this.resourcesManager.updateReservation(id, newData);
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        return this.resourcesManager.updateUser(id, newData);
    }

    public boolean processANewHeartBeat(HeartBeat heartBeat){
        //if we already had a heartbeat from the same port
        //we will replace the old one with the new one
        heartBeatsReceived.removeIf(beat -> beat.getPortTcp() == heartBeat.getPortTcp());

        return heartBeatsReceived.add(heartBeat);
    }

    public boolean checkForServerDeath(){
        //if there is any heart beat not available
        return heartBeatsReceived.removeIf(hb -> !hb.getAvailable());
    }

    public String listAllAvailableServers(){
        StringBuilder sb = new StringBuilder();

        for (HeartBeat beat : heartBeatsReceived){
            sb.append(beat.toString()).append("\n");
        }

        return sb.toString();
    }

    public boolean serverLifeCheck(){
        return heartBeatsReceived.removeIf(beat -> LocalTime.now().isAfter(beat.getTimeCreated().plusSeconds(35)));
    }

    public String getOrderedServers(){
        ArrayList<HeartBeat> hb = new ArrayList<>(heartBeatsReceived);
        hb.sort(Comparator.comparingInt(HeartBeat::getNumberOfConnections));

        StringBuilder sb = new StringBuilder();

        for (HeartBeat beat : hb){
            sb.append(beat.getIp()).append("-").append(beat.getPortTcp()).append("|");
        }

        return sb.toString();
    }

    public int getNumberOfServersConnected(){
        return heartBeatsReceived.size();
    }
}
