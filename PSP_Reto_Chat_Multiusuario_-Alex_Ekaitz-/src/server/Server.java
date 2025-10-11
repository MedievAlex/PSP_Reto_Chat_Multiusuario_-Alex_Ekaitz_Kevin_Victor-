package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import client.Client;

public class Server {
    
    private final int PUERTO = 1234;
    private int CONTADOR = 0;
    
    public static void main(String[] args) {
        // TODO code application logic here
        Server server = new Server();
        server.iniciar();
    }
    
    public void iniciar() {
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + PUERTO);

            while (true) {
                Socket cliente = server.accept();
                System.out.println("Nueva conexión entrante...");

                Client hilo = new Client();
                hilo.start();
            }

        } catch (IOException ex) {
            System.err.println("Error en el servidor: " + ex.getMessage());
        }
    }
}