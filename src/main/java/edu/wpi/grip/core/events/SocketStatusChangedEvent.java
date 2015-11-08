package edu.wpi.grip.core.events;

import edu.wpi.grip.core.Socket;

public class SocketStatusChangedEvent {
    private final Socket socket;

    public SocketStatusChangedEvent(Socket socket){
        this.socket = socket;
    }

    public Socket getSocket(){
        return this.socket;
    }

}
