package pt.isec.pd.ticketline.src.model.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ThreadTcpConnection extends Thread
{
    private int portUdp;
    private String databaseDirectory;

    public ThreadTcpConnection(int portUdp, String databaseDirectory)
    {
        this.portUdp = portUdp;
        this.databaseDirectory = databaseDirectory;
    }

    @Override
    public void run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(portUdp);
            // All the client-server logic
        }
        catch(IOException ioe){
            System.out.println("Port isnt available");
            return;
        }
        

    }


    
}
