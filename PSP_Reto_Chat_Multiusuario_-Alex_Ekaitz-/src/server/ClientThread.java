package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import logger.GeneraLog;
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
            
            if ("Server".equalsIgnoreCase(usuario)) {
                salida.writeObject(new Mensaje("ERROR_RESERVADO"));
                return;
            }
            
            conectado = true;
            salida.writeObject(new Mensaje("OK"));
            server.conexion(usuario, this);

            while ((mensaje = (Mensaje) entrada.readObject()) != null) {
            	if ("mensaje_publico".equals(mensaje.getTipo())) {
            		server.enviarMensajePublico(mensaje);
            		GeneraLog.getLogger().info("(Público) [" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                } else if ("mensaje_privado".equals(mensaje.getTipo())) {
            		server.enviarMensajePrivado(mensaje);
            		GeneraLog.getLogger().info("(Privado) [" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                }else if ("respuesta_server".equals(mensaje.getTipo()) && "DESCONEXION".equals(mensaje.getContenido())) {
                    System.out.println("Usuario " + usuario + " se desconectó");
                    GeneraLog.getLogger().info("Usuario " + usuario + " se desconectó");
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error con el cliente " + usuario + ": " + e.getMessage());
            GeneraLog.getLogger().severe("Error con el cliente " + usuario + ": " + e.getMessage());
        } finally {
            if (usuario != null && conectado) {
                server.desconexion(usuario);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                GeneraLog.getLogger().warning("Error cerrando socket: " + e.getMessage());
            }
        }
    }
    
    public void enviarMensaje(Mensaje mensaje) {
    	try {
			salida.writeObject(mensaje);
		} catch (IOException e) {
			e.printStackTrace();
			GeneraLog.getLogger().warning("Error enviando mensaje: " + e.getMessage());
		}
    }
    
    public String getUsuario() {
    	return usuario;
    }
}