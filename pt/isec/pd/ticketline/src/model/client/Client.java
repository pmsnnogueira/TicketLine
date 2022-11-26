package pt.isec.pd.ticketline.src.model.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {

    }

    public String serverIP;
    public int serverPort;
    public boolean CIHandle;
    public ArrayList<String> servers;

    public Client(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.CIHandle = true;


    }

    public boolean ClientInit(){
        DatagramSocket socket;
        InetAddress ip;
        try{
            socket = new DatagramSocket();
            ip = InetAddress.getByName(serverIP);
        }catch (IOException e){
            return false;
        }
        String message = "Connect";
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
        servers.addAll(Arrays.asList(strings));
        return true;
    }
}
