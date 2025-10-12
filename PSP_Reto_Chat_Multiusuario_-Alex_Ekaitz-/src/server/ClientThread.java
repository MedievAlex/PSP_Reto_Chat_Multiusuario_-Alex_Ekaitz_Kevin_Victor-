package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket socket;
    private Server server;
    private String usuario;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
        	Object mensaje;

            usuario = (String) entrada.readObject();

            if (server.getUsuariosActivos().size() >= server.getLimite()) {
                salida.writeObject("ERROR_LLENO");
                return;
            }

            if (server.getUsuariosActivos().contains(usuario)) {
                salida.writeObject("ERROR_DUPLICADO");
                return;
            }
            
            server.conexion(usuario);

            salida.writeObject("OK");

            while ((mensaje = entrada.readObject()) != null) {
                System.out.println("[" + usuario + "]: " + mensaje.toString());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Cliente " + usuario + " desconectado o error: " + e.getMessage());
        } finally {
            if (usuario != null) {
                server.desconexion(usuario);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
