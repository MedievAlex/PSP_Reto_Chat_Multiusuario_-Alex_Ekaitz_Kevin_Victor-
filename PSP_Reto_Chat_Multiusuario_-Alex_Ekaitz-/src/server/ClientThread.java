package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Mensaje;

public class ClientThread extends Thread {
    private Socket socket;
    private Server server;
    private String usuario;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado = false;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
        	Mensaje mensaje;
        	
        	salida = new ObjectOutputStream(socket.getOutputStream());
        	entrada = new ObjectInputStream(socket.getInputStream());
        	
            usuario = (String) entrada.readObject();

            if (server.getClientesActivos().size() >= server.getLimite()) {
                salida.writeObject(new Mensaje("ERROR_LLENO"));
                return;
            }

            if (server.getClientesActivos().contains(usuario)) {
                salida.writeObject(new Mensaje("ERROR_DUPLICADO"));
                return;
            }
            
            conectado = true;
            salida.writeObject(new Mensaje("OK"));
            server.conexion(usuario, this);

            while ((mensaje = (Mensaje) entrada.readObject()) != null) {
            	if ("DESCONEXION".equals(mensaje.getContenido())) {
                    System.out.println("Usuario " + usuario + " se desconectó");
                    break;
                }
                System.out.println("[" + usuario + "]: " + mensaje.toString());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error con el cliente " + usuario + ": " + e.getMessage());
        } finally {
            if (usuario != null && conectado) {
                server.desconexion(usuario);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void enviarMensaje(Mensaje mensaje) {
    	try {
			salida.writeObject(mensaje);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
