package com.planet_ink.coffee_mud.system.I3.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ListenThread extends Thread {
    private ServerSocket listen;
    private Vector clients;

    public ListenThread(int port) throws java.io.IOException {
        clients = new Vector(10, 2);
        listen = new ServerSocket(port);
        start();
    }

    public void run() {
        while( true ) {
            Socket client;

            try {
                client = listen.accept();
                synchronized( clients ) {
                    clients.addElement(client);
                }
            }
            catch( java.io.IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public Socket nextSocket() {
        Socket client;

        synchronized( clients ) {
            if( clients.size() > 0 ) {
                client = (Socket)clients.elementAt(0);
                clients.removeElementAt(0);
            }
            else {
                client = null;
            }
        }
        return client;
    }
}
