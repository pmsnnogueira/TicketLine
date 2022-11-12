package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.server.Server;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ServerUI {
    private final ModelManager data;

    public ServerUI(ModelManager data){
        this.data = data;
    }

    private void listInformation(){
        int input = InputProtection.chooseOption(null, "List shows", "List reservations",
                                                 "List seats", "List users");
        switch (input){
            case 1 -> {
                int id = InputProtection.readInt("Show ID (-1 for all shows): ");
                System.out.println(this.data.listShows(id == -1 ? null : id));
            }
            case 2 ->{
                int id = InputProtection.readInt("Reservation ID (-1 for all reservations): ");
                System.out.println(this.data.listReservations(id == -1 ? null : id));
            }
            case 3 ->{
                int id = InputProtection.readInt("Seats ID (-1 for all seats): ");
                System.out.println(this.data.listSeats(id == -1 ? null : id));
            }
            case 4 ->{
                int id = InputProtection.readInt("User ID (-1 for all users): ");
                System.out.println(this.data.listUsers(id == -1 ? null : id));
            }
            default -> {
                System.out.println("Not a valid option! Try again!");
                listInformation();
            }
        }
    }

    public void insertData(){
        int input = InputProtection.chooseOption(null, "Insert a seat", "Insert a reservation", "Insert an user", "Insert Show");

        switch (input){
            case 1 ->{
                ArrayList<ArrayList<String>> parameters = new ArrayList<>();
                String fila = InputProtection.readString("Row: ", true);
                String assento = InputProtection.readString("Seat: ", true);
                double preco = InputProtection.readNumber("Price: ");
                int espetaculo_id = InputProtection.readInt("Show ID: ");
                ArrayList<String> enviar = new ArrayList<>();
                Collections.addAll(enviar, fila , assento , Double.toString(preco) );
                parameters.add(enviar);
                
                if (!this.data.insertSeat(parameters , espetaculo_id)){
                    System.out.println("Could not insert data");
                }
            }
            case 2 ->{
                String data_hora = InputProtection.readString("Date_Hour: ", false);
                int pago = InputProtection.readInt("Paid?: ");
                int id_utilizador = InputProtection.readInt("User ID: ");
                int id_espetaculo = InputProtection.readInt("Show ID: ");

                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(data_hora);
                parameters.add(Integer.toString(pago));
                parameters.add(Integer.toString(id_utilizador));
                parameters.add(Integer.toString(id_espetaculo));

                if (!this.data.insertReservation(parameters)) {
                    System.out.println("Could not insert data");
                }
            }
            case 3 ->{
                String username = InputProtection.readString("Username: ", false);
                String nome = InputProtection.readString("Name: ", false);
                String password = InputProtection.readString("Password: ", false);
                int administrador = InputProtection.readInt("Admin: ");
                int autenticado = InputProtection.readInt("Authenticated: ");

                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(username);
                parameters.add(nome);
                parameters.add(password);
                parameters.add(Integer.toString(administrador));
                parameters.add(Integer.toString(autenticado));

                if (!this.data.insertUser(parameters)) {
                    System.out.println("Could not insert data");
                }
            }
            case 4 ->{
                data.insertShow();
            }
            default -> {
                System.out.println("Not a valid option!");
                insertData();
            }
        }
    }

    private void deleteData() {
        int input = InputProtection.chooseOption(null, "Delete show", "Delete seat", "Delete reservation", "Delete user");

        switch (input){
            case 1 -> {
                int id = InputProtection.readInt("Show ID: ");
                if (!this.data.deleteShow(id)){
                    System.out.println("Could not delete show");
                }
            }
            case 2 -> {
                int id = InputProtection.readInt("Seat ID: ");
                if (!this.data.deleteSeat(id)){
                    System.out.println("Could not delete seat");
                }
            }
            case 3 -> {
                int id = InputProtection.readInt("Reservation ID: ");
                if (!this.data.deleteReservations(id)){
                    System.out.println("Could not delete reservation");
                }
            }
            case 4 -> {
                int id = InputProtection.readInt("User ID: ");
                if (!this.data.deleteUsers(id)){
                    System.out.println("Could not delete user");
                }
            }
            default -> {
                System.out.println("Not a valid option");
                deleteData();
            }
        }
    }


    private void updateData() {
        int input =InputProtection.chooseOption(null, "Update show", "Update seat", "Update reservation", "Update user");

        switch (input){
            case 1->{
                int id = InputProtection.readInt("Show ID: ");
                HashMap<String, String> newData = new HashMap<>();
                while(true){
                    String parameter = InputProtection.readString("Choose one (end to stop):\ndescricao/tipo/data_hora/" +
                                                                  "duracao/local/localidade/pais/" +
                                                                  "classificacao_etaria/visivel: ", true);
                    if (parameter.equals("end")){
                        break;
                    }
                    String newValue = InputProtection.readString("New data: ", false);
                    newData.put(parameter, newValue);
                }

                if (!this.data.updateShows(id, newData)){
                    System.out.println("Could not update show");
                }
            }
            case 2->{
                int id = InputProtection.readInt("Seat ID: ");
                HashMap<String, String> newData = new HashMap<>();
                while(true){
                    String parameter = InputProtection.readString("Choose one (end to stop):\nfila/" +
                                                                   "assento/preco/espetaculo_id: ", true);
                    if (parameter.equals("end")){
                        break;
                    }
                    String newValue = InputProtection.readString("New data: ", false);
                    newData.put(parameter, newValue);
                }

                if (!this.data.updateSeats(id, newData)){
                    System.out.println("Could not update seat");
                }
            }
            case 3->{
                int id = InputProtection.readInt("Reservation ID: ");
                HashMap<String, String> newData = new HashMap<>();
                while(true){
                    String parameter = InputProtection.readString("Choose one (end to stop):\ndata_hora/" +
                            "pago/id_utilizador/id_espetaculo: ", true);
                    if (parameter.equals("end")){
                        break;
                    }
                    String newValue = InputProtection.readString("New data: ", false);
                    newData.put(parameter, newValue);
                }

               if (!this.data.updateReservation(id, newData)){
                   System.out.println("Could not update reservation");
               }
            }
            case 4->{
                int id = InputProtection.readInt("User ID: ");
                HashMap<String, String> newData = new HashMap<>();
                while(true){
                    String parameter = InputProtection.readString("Choose one (end to stop):\nusername/" +
                            "nome/password/administrador/autenticado: ", true);
                    if (parameter.equals("end")){
                        break;
                    }
                    String newValue = InputProtection.readString("New data: ", false);
                    newData.put(parameter, newValue);
                }

                if(!this.data.updateUser(id, newData)){
                    System.out.println("Could not update user");
                }
            }
            default -> {
                System.out.println("Not a valid option");
                updateData();
            }
        }
    }

    private void listAllAvailableServers() {
        System.out.println(this.data.listAllAvailableServers());
    }

    public void start(){
        while (true){
            int input = InputProtection.chooseOption("Choose an action:", "List information",
                                                     "Insert data","Delete data",
                                                     "Update data", "List available servers","Exit");

            switch (input){
                case 1 -> listInformation();
                case 2 -> insertData();
                case 3 -> deleteData();
                case 4 -> updateData();
                case 5 -> listAllAvailableServers();
                case 6 -> {
                    try{
                        this.data.closeServer();
                        this.data.closeDB();
                    }catch (SQLException | IOException | InterruptedException ignored){}
                    return;
                }
            }
        }
    }

}
