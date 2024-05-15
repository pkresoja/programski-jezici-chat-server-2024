package rs.ac.singidunum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private String name;
    private boolean online;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientHandler(Socket socket, String name) {
        this.name = name;
        this.online = true;

        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("Welcome, " + name + "!");
        } catch (IOException ex) {
            online = false;
            System.out.println("Client " + name + " disconnected during the setup process");
        }
    }

    @Override
    public void run() {
        String received;
        while (online) {
            try {
                received = dis.readUTF();
                if (received.equals("/logout")) {
                    disconnect();
                    break;
                }

                // Send message to everyone
                send(name + ": " + received);

            } catch (SocketException se) {
                System.out.println(se.getClass().getName() + ": " + se.getMessage());
                disconnect();
                break;

            } catch (IOException ex) {
                System.out.println("Doslo je dogreske u komunikaciji: " + ex.getMessage());
            }
        }
    }

    private void disconnect() {
        Main.clients.remove(this);
        try {
            dos.close();
            dis.close();
        } catch (IOException ex) {
            System.out.println("Doslo je do greske prilikom zatvaranja konekcije klijenta: " + name);
        }
        online = false;
    }

    private void send(String message) {
        System.out.println(message);
        for (ClientHandler ch : Main.clients) {
            try {
                ch.dos.writeUTF(message);
            } catch (IOException ex) {
                ch.disconnect();
            }
        }
    }
}
