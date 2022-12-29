package pt.isec.pd.ticketline.src.model.server.rmi;

import pt.isec.pd.ticketline.src.model.client.rmi.TicketLineClientRemoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TicketLineServerRemoteInterface extends Remote {
    String REGISTRY_BIND_NAME = "TICKET_LINE_SERVICE_";

    void listActiveServers(TicketLineClientRemoteInterface clientRef) throws RemoteException;
    void addTCPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void removeTCPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void addUDPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void removeUDPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void addLostTCPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void removeLostTCPListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void addLoginListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void removeLoginListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void addLogoutListener(TicketLineClientRemoteInterface listener) throws RemoteException;
    void removeLogoutListener(TicketLineClientRemoteInterface listener) throws RemoteException;
}
