package pt.isec.pd.ticketline.src.model.data;

import pt.isec.pd.ticketline.src.resources.ResourcesManager;
import pt.isec.pd.ticketline.src.resources.files.FileOpener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.DefaultEditorKit.PasteAction;

public class Data {
    private ResourcesManager resourcesManager;
    private ArrayList<Show> shows;

    public Data() throws SQLException {
        this.resourcesManager = new ResourcesManager();
        this.shows = new ArrayList<>();
    }

    public String listUsers(Integer userID) throws SQLException{
        return this.resourcesManager.listUsers(userID);
    }

    public String listShows(Integer showID) throws SQLException{
        return this.resourcesManager.listShows(showID);
    }

    public String listReservations(Integer reservationID) throws SQLException{
        return this.resourcesManager.listReservations(reservationID);
    }

    public String listSeats(Integer seatID) throws SQLException{
        return this.resourcesManager.listSeats(seatID);
    }

    public boolean insertShow(ArrayList<String> parameters){
        return this.resourcesManager.insertShow(parameters);
    }

    public boolean insertSeat(ArrayList<String> parameters){
        return this.resourcesManager.insertSeat(parameters);
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
        String dateHour = "";

        for(String string : information)
        {
            String newString = string.replaceAll("\"", "");
            String[] splitted = newString.split(";");

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
            else if(newString.contains(":"))
            {
                String row = splitted.toString();
                parameters.add(row);
            }
            else
                continue;
        }
        insertShow(parameters);
    }

    public boolean deleteShow(int id){return this.resourcesManager.deleteShow(id);}

    public boolean deleteReservations(int id){return this.resourcesManager.deleteReservations(id);}

    public boolean deleteSeat(int id){return this.resourcesManager.deleteSeat(id);}

    public boolean deleteUsers(int id){return this.resourcesManager.deleteUsers(id);}
}
