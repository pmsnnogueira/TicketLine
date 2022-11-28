package pt.isec.pd.ticketline.src.model.client;

import pt.isec.pd.ticketline.src.model.server.DBHelper;
import pt.isec.pd.ticketline.src.ui.ClientUI;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        ClientUI clientUI = null;
        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            clientUI = new ClientUI(client);
        } catch (SocketException e) {
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

    public Client(String serverIP, int serverPort) throws SocketException {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.CIHandle = true;

        this.servers = new ArrayList<>();

        if (!clientInit()) {
            throw new SocketException();
        }
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
            return false;
        }
        String message = "CONNECTION";
        DatagramPacket packetSent = new DatagramPacket(message.getBytes(), message.getBytes().length, ip, serverPort);

        try {
            socket.send(packetSent);
        } catch (IOException e) {
            return false;
        }

        DatagramPacket packetReceived = new DatagramPacket(new byte[256], 256);

        try {
            socket.receive(packetReceived);
        } catch (IOException e) {
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

    public DBHelper addDBHelper(String operation, String table, ArrayList<String> insertParams) {
        DBHelper dbHelper = new DBHelper();
        if (operation.equals("INSERT")) {
            if (table.equals("user")) {
                dbHelper.setOperation(operation);
                dbHelper.setTable(table);
                dbHelper.setInsertParams(insertParams);

                return dbHelper;
            }
        }
        return null;
    }


    public boolean sendInfoToServer(String operation, String table, ArrayList<String> insertParams) {

        for (String str : servers) {
            String[] s = str.split("-");
            try {
//                System.out.println(s[0] + Integer.parseInt(s[1]));
                socket = new Socket(s[0], Integer.parseInt(s[1]));
                OutputStream os = socket.getOutputStream();
                String msg = "CLIENT";
                os.write(msg.getBytes(), 0, msg.length());

                InputStream is = socket.getInputStream();
                byte[] m = new byte[512];
                int nBytes = is.read(m);
                String msgReceived = new String(m, 0, nBytes);

                System.out.println(msgReceived);

                if(!msgReceived.equals("CONFIRMED")){
                    socket.close();
                    return false;
                }

                DBHelper dbHelper = addDBHelper(operation, table, insertParams); //Criar o DBhelper
                if (dbHelper == null){
                    socket.close();
                    return false;
                }

                System.out.println("DBHELPER");

                ObjectOutputStream oos = null;//Write to Server
                try{
                    oos = new ObjectOutputStream(socket.getOutputStream());
                }catch (IOException e){
                    e.printStackTrace();
                }
                System.out.println("OOS");
                oos.writeObject(dbHelper);
                System.out.println("WRITE");

                is.close();
                os.close();
                oos.close();

                socket.close();
                return true;
            } catch (IOException e) {
                continue;
            }
        }
        return false;
    }
}
