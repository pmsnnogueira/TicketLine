package pt.isec.pd.ticketline.src.model.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";

    public static void main(String[] args)
    {
        boolean available = true;
        int databaseVersion = 1;
        int numberOfConnections = 0;
        try
        {
            int portTcp = Integer.parseInt(args[0]);
            //String databaseDirectory = args[1];
            //ThreadTcpConnection serverTcp = new ThreadTcpConnection(portTcp, databaseDirectory);
            //serverTcp.start();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            try
            {
                
                MulticastSocket mcs = new MulticastSocket(multicastPort);
                InetAddress ipGroup = InetAddress.getByName(ipMulticast);
                SocketAddress sa = new InetSocketAddress(ipGroup, multicastPort);
                NetworkInterface ni = NetworkInterface.getByIndex(0);
                mcs.joinGroup(sa, ni);

                scheduler.scheduleAtFixedRate(new ExecuterSendHeartBeat(new HeartBeat(portTcp, available, databaseVersion, numberOfConnections), mcs),
                                            0, 10, TimeUnit.SECONDS);

                boolean keepGoing = true;
                while(keepGoing)
                {
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat heartBeat = (HeartBeat)ois.readObject();
                        System.out.println("Received heartbeat from server -> Port:[" + heartBeat.getPortTcp() + 
                                    "] Available -> [" + heartBeat.getAvailable() + "] Database version -> [" + heartBeat.getdatabaseVersion()
                                    + "] Number of connections -> [" + heartBeat.getnumberOfConnections() + "]");
                    }
                    catch(ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }
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
}
