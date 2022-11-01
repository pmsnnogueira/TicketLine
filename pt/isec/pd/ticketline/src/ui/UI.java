package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.sql.SQLException;
import java.util.ArrayList;

public class UI {
    private final ModelManager data;

    public UI(ModelManager data){
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
                String fila = InputProtection.readString("Row: ", true);
                String assento = InputProtection.readString("Seat: ", true);
                double preco = InputProtection.readNumber("Price: ");
                int espetaculo_id = InputProtection.readInt("Show ID: ");

                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(fila);
                parameters.add(assento);
                parameters.add(Double.toString(preco));
                parameters.add(Integer.toString(espetaculo_id));

                if (!this.data.insertSeat(parameters)){
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

                if (!this.data.insertReservation(parameters)) {
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

    public void start(){
        while (true){
            int input = InputProtection.chooseOption("Choose an action:", "List information",
                                                     "Insert data",
                                                     "Delete data", "Exit");

            switch (input){
                case 1 -> listInformation();
                case 2 -> insertData();
                case 3 -> deleteData();
                case 4 -> {
                    System.out.println("HASTA LA VISTA BABY!");
                    return;
                }
            }
        }
    }
}
