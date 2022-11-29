package pt.isec.pd.ticketline.src.model.client;

import pt.isec.pd.ticketline.src.model.server.DBHelper;
import pt.isec.pd.ticketline.src.model.server.Server;
import pt.isec.pd.ticketline.src.ui.ClientUI;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;

public class Client {

    private static final String SELECT = "SELECT";
    private static final String INSERT = "INSERT";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    private static final String USER = "user";
    private static final String SHOW = "show";
    private static final String SEAT = "seat";
    private static final String RESERVATION = "reservation";

    private static final String CONFIRMED = "CONFIRMED";
    public static void main(String[] args) {
        ClientUI clientUI = null;
        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            clientUI = new ClientUI(client);
        } catch (IOException e) {
            System.out.println("Could not create a client (ERROR:" + e + ")");
            e.printStackTrace();
        }

        assert clientUI != null;
        clientUI.start();
    }

    public String serverIP;
    public int serverPort;
    public boolean CIHandle;
    public ArrayList<String> servers;
    private Socket socket;
    ServerReader sr;
    public boolean srHandle;
    public boolean confirmHandle;

    public Client(String serverIP, int serverPort) throws IOException {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.CIHandle = true;
        this.srHandle = true;
        this.confirmHandle = false;

        this.servers = new ArrayList<>();

        if (!clientInit()) {
            throw new SocketException();
        }


            this.sr = new ServerReader();
            sr.start();



/*        if (!connectToServer()) {
            throw new Exception();
        }*/
    }

    public boolean clientInit() {
        DatagramSocket socket;
        InetAddress ip;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            ip = InetAddress.getByName(serverIP);
        } catch (IOException e) {
            System.out.println("Error");
            return false;
        }
        String message = "CONNECTION";
        DatagramPacket packetSent = new DatagramPacket(message.getBytes(), message.getBytes().length, ip, serverPort);

        try {
            socket.send(packetSent);
        } catch (IOException e) {
            System.out.println("Error");

            return false;
        }

        DatagramPacket packetReceived = new DatagramPacket(new byte[256], 256);

        try {
            socket.receive(packetReceived);
        } catch (IOException e) {
            System.out.println("Error");

            return false;
        }

        String messageReceived = new String(packetReceived.getData(), 0, packetReceived.getLength());

        String[] strings = messageReceived.split("\\|");

        servers.clear();
        servers.addAll(Arrays.asList(strings));
        System.out.println(servers);
        socket.close(); 
        return true;
    }

    public boolean connectToServer() {
        for (String str : servers) {
            String[] s = str.split("-");

            try {
                socket = new Socket(s[0], Integer.parseInt(s[1]));
                OutputStream os = socket.getOutputStream();
                String msg = "CLIENT";
                os.write(msg.getBytes(), 0, msg.length());

                InputStream is = socket.getInputStream();
                byte[] m = new byte[512];
                int nBytes = is.read(m);
                String msgReceived = new String(m, 0, nBytes);
                System.out.println(msgReceived);
                os.close();
                is.close();
                socket.close();

            } catch (IOException e) {
                continue;
            }

            return true;
        }

        return false;
    }

    public DBHelper addDBHelper(String operation, String table, ArrayList<String> insertParams, int id) {
        DBHelper dbHelper = new DBHelper();
        if (operation.equals("INSERT")) {
            if (table.equals("user")) {
                //dbHelper.setClientIp();
                insertUser(dbHelper,insertParams);
                return dbHelper;
            }
            if (table.equals("show")) {
                insertShow(dbHelper);
                return dbHelper;
            }
            if (table.equals("reservation")) {
                insertReservation(dbHelper,insertParams);
                return dbHelper;
            }
        }
        if(operation.equals("SELECT")){
            if(table.equals("user")){
                listUsers(dbHelper , id);
                return dbHelper;
            }
            if(table.equals("show")){
                listShows(dbHelper , id);
                return dbHelper;
            }
            if(table.equals("reservation")){
                listReservations(dbHelper , id);
                return dbHelper;
            }
        }
        return null;
    }

    class ServerReader extends Thread{
        Socket socketSr;
        private boolean update;
        public ServerReader()  {
            update = false;
            srHandle = false;

            String[] s = servers.get(0).split("-");
            try {
                this.socketSr = new Socket(s[0], Integer.parseInt(s[1]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
            while(true) {

                    try {
                        OutputStream os = socketSr.getOutputStream();
                        String msg = "CLIENT";
                        os.write(msg.getBytes(), 0, msg.length());
                        os.close();

                        System.out.println("Antes de Ler");
                        InputStream is = this.socketSr.getInputStream();
                        byte[] m = new byte[512];
                        int nBytes = is.read(m);
                        String msgReceived = new String(m, 0, nBytes);
                        System.out.println("Mensagem lida");
                        System.out.println(msgReceived);

                        if (msgReceived.equals(CONFIRMED))
                            confirmHandle = true;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
    }


    public String sendInfoToServer(String operation, String table, ArrayList<String> insertParams , int id) {

        for (String str : servers) {
            String[] s = str.split("-");
            /*try {
//                System.out.println(s[0] + Integer.parseInt(s[1]));
                socket = new Socket(s[0], Integer.parseInt(s[1]));
                OutputStream os = socket.getOutputStream();
                String msg = "CLIENT";
                os.write(msg.getBytes(), 0, msg.length());
                os.close();*/

                /*InputStream is = socket.getInputStream();
                byte[] m = new byte[512];
                int nBytes = is.read(m);
                String msgReceived = new String(m, 0, nBytes);

                System.out.println(msgReceived);

                if(!msgReceived.equals("CONFIRMED")){
                    socket.close();
                    return "";
                }

                DBHelper dbHelper = addDBHelper(operation, table, insertParams,id); //Criar o DBhelper
                if (dbHelper == null){
                    socket.close();
                    return "";
                }

                System.out.println("DBHELPER");

                ObjectOutputStream oos = null;//Write to Server
                try{
                    oos = new ObjectOutputStream(socket.getOutputStream());
                }catch (IOException e){
                    e.printStackTrace();
                }

                System.out.println("OOS");
                srHandle = true;
                oos.writeObject(dbHelper);
                System.out.println("WRITE");

                is.close();
                os.close();
                oos.close();*/
               /* socket.close();
                srHandle = false;

                return "Lido Alguma coisa";
            } catch (IOException e) {
                continue;
            }*/
        }
        return "";
    }




    public boolean insertUser(DBHelper dbHelper,ArrayList<String> parameters){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(USER);
        dbHelper.setInsertParams(parameters);
        return true;
    }

    public void insertShow(DBHelper dbHelper){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(SHOW);
    }

    public boolean insertReservation(DBHelper dbHelper ,ArrayList<String> parameters){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(RESERVATION);
        dbHelper.setInsertParams(parameters);
        return true;
    }

    public String listUsers(DBHelper dbHelper,Integer userID){
        dbHelper.setId(userID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(USER);
        return "";
    }

    public String listShows(DBHelper dbHelper,Integer showID){
        dbHelper.setId(showID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(SHOW);
        return "";
    }

    public String listReservations(DBHelper dbHelper,Integer reservationID){
        dbHelper.setId(reservationID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(RESERVATION);
        return "";
    }



}
