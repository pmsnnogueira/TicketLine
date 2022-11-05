package pt.isec.pd.ticketline.src.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Server {
    // Id Server is used for the create of the new database for eache server
    private static int idServer=0;
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";

    public static void main(String[] args)
    {
        idServer++;
        ArrayList<Socket> servers = new ArrayList<>();
        ArrayList<Socket> clients = new ArrayList<>();
        int numberOfConnections=0;

        try
        {
            int portUdp = Integer.parseInt(args[0]);
            String databaseDirectory = args[1];
            ServerTcp serverTcp = new ServerTcp(portUdp, databaseDirectory);
            serverTcp.start();
        
            try
            {
                MulticastSocket mcs = new MulticastSocket(multicastPort);
                InetAddress ipGroup = InetAddress.getByName(ipMulticast);
                SocketAddress sa = new InetSocketAddress(ipGroup, multicastPort);
                NetworkInterface ni = NetworkInterface.getByIndex(0);
                mcs.joinGroup(sa, ni);

                boolean keepGoing = true;
                while(keepGoing)
                {
                    // Each time a server sends a heartbeat if its not on the list, its added to the servers list
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                    mcs.receive(dp);
                    Socket socket = new Socket(dp.getAddress(), dp.getPort());
                    addServer(servers, socket);
                }

                mcs.leaveGroup(sa, ni);
                mcs.close();
            }
            catch(IOException ioe){
                System.out.println("Port isnt available");
                return;
            }
        }
        catch(NumberFormatException nfe){
            System.out.println("User didnt provide a valid number for the port");
            return;
        }
    }

    public static boolean addServer(ArrayList<Socket> servers, Socket socket)
    {
        if(servers.contains(socket))
            return false;
        servers.add(socket);
        return true;
    }
}
