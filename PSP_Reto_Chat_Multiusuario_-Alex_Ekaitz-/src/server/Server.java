package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    private final int PUERTO = 1234;
    private int limite = 4;

    private List<String> clientes = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        Server server = new Server();
        server.iniciar();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + PUERTO + "...");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante...");

                ClientThread hilo = new ClientThread(clienteSocket, this);
                hilo.start();
            }

        } catch (IOException ex) {
            System.err.println("Error en el servidor: " + ex.getMessage());
        }
    }

    public synchronized void conexion(String usuario) {
        clientes.add(usuario);
        System.out.println("Usuario conectado: " + usuario + " | Activos: " + clientes.size());
    }

    public synchronized void desconexion(String usuario) {
        clientes.remove(usuario);
        System.out.println("Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
    }

    public synchronized List<String> getUsuariosActivos() {
        return new ArrayList<>(clientes);
    }
    
    public int getLimite() {
        return limite;
    }
}