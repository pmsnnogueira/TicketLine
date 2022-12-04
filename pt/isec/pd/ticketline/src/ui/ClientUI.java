package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.client.Client;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.text.SimpleDateFormat;
import java.util.*;

public class ClientUI {

    private final Client client;
    private int admin = 0;
    public ClientUI(Client client){
        this.client = client;
    }

    public String art(){
        return ("""
                 _______ _____ _____ _  ________ _______ _      _____ _   _ ______    _____ _      _____ ______ _   _ _______\s
                |__   __|_   _/ ____| |/ /  ____|__   __| |    |_   _| \\ | |  ____|  / ____| |    |_   _|  ____| \\ | |__   __|
                   | |    | || |    | ' /| |__     | |  | |      | | |  \\| | |__    | |    | |      | | | |__  |  \\| |  | |  \s
                   | |    | || |    |  < |  __|    | |  | |      | | | . ` |  __|   | |    | |      | | |  __| | . ` |  | |  \s
                   | |   _| || |____| . \\| |____   | |  | |____ _| |_| |\\  | |____  | |____| |____ _| |_| |____| |\\  |  | |  \s
                   |_|  |_____\\_____|_|\\_\\______|  |_|  |______|_____|_| \\_|______|  \\_____|______|_____|______|_| \\_|  |_|  \s  """);
    }

    public boolean loginRegister(){
        int option = InputProtection.chooseOption("Choose a menu: " , "Login","Register","Exit");

        switch (option){
            case 1 -> {
                return login();
            }
            case 2 ->{
                return registerUser();
            }
            default -> {
                return false;
            }
        }
    }


    public boolean registerUser(){
        String nome = InputProtection.readString("Name: ", false);
        String username = InputProtection.readString("Username(no spaces): ", true);
        String password = InputProtection.readString("Password: ", false);

        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(username);
        parameters.add(nome);
        parameters.add(password);

        //Send information to server
        this.client.createDBHelper("INSERT","user" , parameters,-1 ,null);

        if(client.waitToReceiveResultRequest().equals("false")){
            System.out.println("Could not create a new user! Try again!");
            return false;
        }

        System.out.println("New user created! Welcome!");
        return true;
    }
    public boolean login(){

        String username, password;
        System.out.println("\nLogin");
        do {
            username = InputProtection.readString("\tUsername: ", false);

            password = InputProtection.readString("\tPassword: ", false);

            verifyLogin(username, password);
            String out = client.waitToReceiveResultRequest();
            

            if(out.equals("User doesnt exist!")){
                System.out.println(out);
                return false;
            }
            if(out.contains("\nAdmin:1"))
                admin = 1;

            out = out.replaceAll(" ", "");
            String[] splitted = out.split("\n");
            String[] id = splitted[0].split(":");

            client.setClientID(Integer.parseInt(id[1]));
            return true;
        }while (true);
    }



    private void listInformation(){
        System.out.println("List Menu");
        int input = InputProtection.chooseOption(null, "List shows","List reservations","List seats","List shows with empty seats","Show unpaid reservations","Show paid reservations");
        ArrayList<String> empty = new ArrayList<>();
        switch (input){
            case 1 ->{
                int id = InputProtection.readInt("Show ID (-1 for all shows): ");
                this.client.createDBHelper("SELECT", "show", null, id , null);
                System.out.println(client.waitToReceiveResultRequest());
            }
            case 2 ->{
                int id = InputProtection.readInt("Reservation ID (-1 for all reservations): ");
                this.client.createDBHelper("SELECT", "reservation", null, id , null);
                System.out.println(client.waitToReceiveResultRequest());
                //System.out.println(this.data.listReservations(id == -1 ? null : id));
            }
            case 3 ->{
                int id = InputProtection.readInt("Show ID (-1 for all shows): ");
                this.client.createDBHelper("SELECT", "show", null, id , null);
                System.out.println(client.waitToReceiveResultRequest());

                int idShow = InputProtection.readInt("ID of the show: ");
                this.client.createDBHelper("SELECT", "seat", null, idShow , null);
                System.out.println(client.waitToReceiveResultRequest());
            }
            case 4->{
                this.client.createDBHelper(-1, "SELECT", "show",2,null);              //Show with empty seats(one day before)     -> OPTION 2
                System.out.println(client.waitToReceiveResultRequest());
                //System.out.println(this.data.listReservations(id == -1 ? null : id));
            }
            case 5->{
                this.client.createDBHelper(client.getClientID(), "SELECT", "reservation",3,"0");              //List unpaid reservartion     -> OPTION 3 parameter 0
                System.out.println(client.waitToReceiveResultRequest());
                //System.out.println(this.data.listReservations(id == -1 ? null : id));
            }
            case 6->{
                this.client.createDBHelper(client.getClientID(), "SELECT", "reservation",3,"1");              //List paid reservartion     -> OPTION 3 parameter 1
                System.out.println(client.waitToReceiveResultRequest());
                //System.out.println(this.data.listReservations(id == -1 ? null : id));
            }
            default -> {
                System.out.println("Not a valid option! Try again!");
            }
        }
    }

    public void insertData(){
        int input = InputProtection.chooseOption(null, "Insert a reservation", "Insert an user");

        switch (input){
            case 1 ->{
                //Insert Reservation
                String data_hora = InputProtection.readString("Date_Hour: ", false);
                int pago = InputProtection.readInt("Paid?: ");
                int id_utilizador = InputProtection.readInt("User ID: ");
                int id_espetaculo = InputProtection.readInt("Show ID: ");

                ArrayList<String> parameters = new ArrayList<>();
                parameters.add(data_hora);
                parameters.add(Integer.toString(pago));
                parameters.add(Integer.toString(id_utilizador));
                parameters.add(Integer.toString(id_espetaculo));

                this.client.createDBHelper("INSERT", "reservation", parameters, -1 , null);
                System.out.println(client.waitToReceiveResultRequest());

            }
            case 2 ->{
                //Insert user
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

                //Send information to server
                this.client.createDBHelper("INSERT","user" , parameters,-1 ,null);
                System.out.println(client.waitToReceiveResultRequest());
            }
            default -> {
                System.out.println("Not a valid option!");
                insertData();
            }
        }
    }

    private void deleteData() {
        int input = InputProtection.chooseOption(null, "Delete unpaid reservation","Back to main menu");
        //int input = InputProtection.chooseOption(null, "Delete show", "Delete seat", "Delete reservation", "Delete user");
        switch (input){/*
            case 1 -> {
                int id = InputProtection.readInt("Show ID: ");
                /*if (!this.data.deleteShow(id)){
                    System.out.println("Could not delete show");
                }*/
            /*}
            case 2 -> {
                int id = InputProtection.readInt("Seat ID: ");
               /* if (!this.data.deleteSeat(id)){
                    System.out.println("Could not delete seat");
                }*/
            /*}
            case 3 -> {
                int id = InputProtection.readInt("Reservation ID: ");
                /*if (!this.data.deleteReservations(id)){
                    System.out.println("Could not delete reservation");
                }*/
            /*}
            case 4 -> {
                int id = InputProtection.readInt("User ID: ");
                /*if (!this.data.deleteUsers(id)){
                    System.out.println("Could not delete user");
                }*/
           // }
            case 1 -> {
                this.client.createDBHelper(client.getClientID(), "SELECT", "reservation",3,"0");              //List unpaid reservartion     -> OPTION 3 parameter 0
                System.out.println(client.waitToReceiveResultRequest());

                int reservationId = InputProtection.readInt("Reservation ID: ");
                this.client.createDBHelper(reservationId, "DELETE", "reservation",1, Integer.toString(client.getClientID()));              //List unpaid reservartion     -> OPTION 3 parameter 0
                /*if (!this.data.deleteUsers(id)){
                    System.out.println("Could not delete user");
                }*/
            }
            case 2->{
                return;
            }
            default -> {
                System.out.println("Not a valid option");
                deleteData();
            }
        }
    }

    private void changeShowVisibility(){
        HashMap<String, String> newData = new HashMap<>();
        int id = InputProtection.readInt("Show ID: ");
        String parameter = "visivel";
        String newValue = InputProtection.readString("New data(1 or 0): ", true);
        newData.put(parameter, newValue);
        this.client.createDBHelper(id, "UPDATE", "show", newData);
    }


    private void updateData() {
        int input = InputProtection.chooseOption(null, "Update show", "Update seat", "Update reservation", "Update user");

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
                    this.client.createDBHelper(id, "UPDATE", "show", newData);
                }

                /*if (!this.data.updateShows(id, newData)){
                    System.out.println("Could not update show");
                }*/
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

                /*if (!this.data.updateSeats(id, newData)){
                    System.out.println("Could not update seat");
                }*/
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

                /*if (!this.data.updateReservation(id, newData)){
                    System.out.println("Could not update reservation");
                }*/
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

                /*if(!this.data.updateUser(id, newData)){
                    System.out.println("Could not update user");
                }*/
            }
            default -> {
                System.out.println("Not a valid option");
                updateData();
            }
        }
    }

    public void verifyLogin(String username,String password){
        ArrayList<String> aux= new ArrayList<>();
        aux.add(username);
        aux.add(password);
        this.client.createDBHelper("SELECT","user" ,  null,-1 ,aux);
    }

    public void adminMenu(){
        while(true){
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){
            }
            System.out.println("Admin Menu");
            int input = InputProtection.chooseOption("Choose an action:", "Insert show",
                    "Make show visible","Delete non payed show"
                    ,"Exit");

            switch(input){
                case 1 -> this.client.createDBHelper("INSERT", "show", null, -1 , null);
                case 2 -> changeShowVisibility();
                case 4 -> {
                    client.closeClient();
                    return;
                }
                default -> {
                    System.out.println("Not a valid option.");
                }
            }
        }
    }

    public void makeReservation(){
        //show all the available shows
        int id = InputProtection.readInt("Show ID (-1 for all shows): ");
        this.client.createDBHelper("SELECT", "show", null, id , null);
        System.out.println(client.waitToReceiveResultRequest());

        int idShow = InputProtection.readInt("ID of the show: ");
        this.client.createDBHelper("SELECT", "seat", null, idShow , null);
        System.out.println(client.waitToReceiveResultRequest());

        int idSeat = InputProtection.readInt("ID of the seat: ");

        //ADICONAR RESERVA
        Date dataHoraAtual = new Date();
        String dataHora = new SimpleDateFormat("dd:MM:yyyy").format(dataHoraAtual) + "-";
        dataHora += new SimpleDateFormat("HH:mm").format(dataHoraAtual);


        //Preciso de mandar tudo
        //INSERT, reservation_seat, clientID , datas_hora, id_show, lugar escolhido
        ArrayList<String> aux = new ArrayList<>();
        System.out.println("Fazer a reserva do lugar");
        Collections.addAll(aux,Integer.toString(client.getClientID()),dataHora ,Integer.toString(idShow),Integer.toString(idSeat));
        this.client.createDBHelper("INSERT", "reservation_seat", aux, -1, null);
        System.out.println("Depois de fazer a reserva do lugar");
        System.out.println(client.waitToReceiveResultRequest());
    }

    public void payReservation(){                                                                                                   //Workingee
        //show user non paid reservations
        this.client.createDBHelper(client.getClientID(), "SELECT", "reservation",3,"0");              //List unpaid reservartion     -> OPTION 3 parameter 0
        System.out.println(client.waitToReceiveResultRequest());

        int idReservation = InputProtection.readInt("Reservation Id: ");
        HashMap<String,String> aux = new HashMap<>();
        aux.put("pago","1");

        this.client.createDBHelper(idReservation,"UPDATE", "reservation",aux);
        System.out.println(client.waitToReceiveResultRequest());
    }
    public void clientMenu(){
        while(true){
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){
            }
            System.out.print("\nMain Menu");
            /*int input = InputProtection.chooseOption("Choose an action:",  "List shows","Make a reservation",
                    "Insert data","Delete data",
                    "Update data", "List available servers","Exit");*/
            int input = InputProtection.chooseOption("Choose an action:",  "List data", "Make a reservation",
                    "Insert data","Delete data",
                    "Update data","Exit");

            switch (input){
                case 1 ->{
                    listInformation();
                   /* int id = InputProtection.readInt("Show ID (-1 for all shows): ");
                    this.client.createDBHelper("SELECT", "show", null, id,null);
                    System.out.println(client.waitToReceiveResultRequest());*/
                }
                case 2 -> makeReservation();
                case 3 -> insertData();
                case 4 -> deleteData();
                case 5 -> payReservation();
                case 6 -> {
                    client.closeClient();
                    return;
                }
            }
        }
    }

    public void start(){
        System.out.println(art());

        if(!loginRegister()){
            System.out.println("Could not login");
            return;
        }

        switch (admin){
            case 0 -> clientMenu();
            case 1 -> adminMenu();
        }
    }
}
