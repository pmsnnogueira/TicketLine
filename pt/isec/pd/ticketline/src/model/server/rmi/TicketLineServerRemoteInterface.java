package pt.isec.pd.ticketline.src.model.server.rmi;

import pt.isec.pd.ticketline.src.model.client.rmi.TicketLineClientRemoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TicketLineServerRemoteInterface extends Remote {
    String REGISTRY_BIND_NAME = "TICKET_LINE_SERVICE_";

    void listActiveServers(TicketLineClientRemoteInterface clientRef) throws RemoteException;
}
