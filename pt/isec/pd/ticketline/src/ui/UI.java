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
                try{
                    System.out.println(this.data.listShows(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 2 ->{
                int id = InputProtection.readInt("Reservation ID (-1 for all reservations): ");
                try{
                    System.out.println(this.data.listReservations(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 3 ->{
                int id = InputProtection.readInt("Seats ID (-1 for all seats): ");
                try{
                    System.out.println(this.data.listSeats(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 4 ->{
                int id = InputProtection.readInt("User ID (-1 for all users): ");
                try{
                    System.out.println(this.data.listUsers(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            default -> {
                System.out.println("Not a valid option! Try again!");
                listInformation();
            }
        }
    }

    public void insertData(){
        int input = InputProtection.chooseOption(null, "Insert a seat", "Insert a reservation", "Insert an user");

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
        }
    }

    public void start(){
        while (true){
            int input = InputProtection.chooseOption("Choose an action:", "List information",
                                                     "Insert data",
                                                     "To be developed", "Exit");

            switch (input){
                case 1 -> listInformation();
                case 2 -> System.out.println("To be developed");
                case 3 -> {
                    System.out.println("HASTA LA VISTA BABY!");
                    return;
                }
            }
        }
    }
}
