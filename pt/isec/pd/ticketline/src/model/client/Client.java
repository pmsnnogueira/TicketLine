package pt.isec.pd.ticketline.src.model.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        try{
            Client client = new Client(args[0], Integer.parseInt(args[1]));
        }catch (Exception e){
            System.out.println("Could not create a client");
        }
    }

    public String serverIP;
    public int serverPort;
    public boolean CIHandle;
    public ArrayList<String> servers;
    private Socket socket;

    public Client(String serverIP, int serverPort) throws Exception {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.CIHandle = true;

        this.servers = new ArrayList<>();

        if(!clientInit()){
            throw new Exception();
        }
        if(!connectToServer()){
            throw new Exception();
        }
    }

    public boolean clientInit(){
        DatagramSocket socket;
        InetAddress ip;
        try{
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            ip = InetAddress.getByName(serverIP);
        }catch (IOException e){
            return false;
        }
        String message = "CONNECTION";
        DatagramPacket packetSent = new DatagramPacket(message.getBytes(), message.getBytes().length, ip, serverPort);

        try{
            socket.send(packetSent);
        }catch (IOException e){
            return false;
        }

        DatagramPacket packetReceived = new DatagramPacket(new byte[256], 256);

        try{
            socket.receive(packetReceived);
        }catch (IOException e){
            return false;
        }

        String messageReceived = new String(packetReceived.getData(), 0, packetReceived.getLength());

        String[] strings = messageReceived.split("\\|");

        servers.clear();
        servers.addAll(Arrays.asList(strings));
        System.out.println(servers);
        return true;
    }

    public boolean connectToServer(){
        for(String str : servers){
            String[] s = str.split("-");

            try{
                socket = new Socket(s[0], Integer.parseInt(s[1]));
                OutputStream os = socket.getOutputStream();
                String msg = "CLIENT";
                os.write(msg.getBytes(), 0, msg.length());

                InputStream is = socket.getInputStream();
                byte[] m = new byte[512];
                int nBytes = is.read(m);
                String msgReceived = new String(m, 0, nBytes);
                System.out.println(msgReceived);

            }catch (IOException e){
                continue;
            }

            return true;
        }

        return false;
    }
}
