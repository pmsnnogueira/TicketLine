package pt.isec.pd.ticketline.src.model.server.heartbeat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ExecutorSendHeartBeat implements Runnable{
    private HeartBeat heartBeat;
    private MulticastSocket ms;

    public ExecutorSendHeartBeat(HeartBeat heartBeat, MulticastSocket ms)
    {
        this.heartBeat = heartBeat;
        this.ms = ms;
    }

    @Override
    public void run() {
        try
        {
            this.heartBeat.setTimeCreated();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(heartBeat);
            byte[] buffer = baos.toByteArray();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("239.39.39.39"), 4004);
            ms.send(dp);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}