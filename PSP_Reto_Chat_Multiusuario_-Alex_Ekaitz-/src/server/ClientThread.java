package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import logger.GeneraLog;
import model.Mensaje;

public class ClientThread extends Thread 
{
    
	// [ VARIABLES ]
	private Socket socket; // Shocket del cliente
    private Server server; // Servidor al que se accede
    private String usuario; // Nombre de usuario del cliente
    private ObjectOutputStream salida;  
    private ObjectInputStream entrada;  
    private boolean conectado = false; // Estado de conexion

    // [ CONSTRUCTORES ]
    public ClientThread(Socket socket, Server server) 
    {
        this.socket = socket;
        this.server = server;
    }

    // [ INICIAR ]
    @Override
    public void run() 
    {
        try 
        {
        	Mensaje mensaje;
        	
        	salida = new ObjectOutputStream(socket.getOutputStream());
        	entrada = new ObjectInputStream(socket.getInputStream());
        	
            usuario = (String) entrada.readObject(); // Obtiene el nombre de usuario

            if (server.getClientesActivos().size() >= server.getLimite()) // Mensaje de error si el servidor esta lleno
            { 
                salida.writeObject(new Mensaje("ERROR_LLENO"));
                return;
            }

            if (server.getClientesActivos().contains(usuario)) // Mensaje de error si ya hay un cliente con ese usuario
            { 
                salida.writeObject(new Mensaje("ERROR_DUPLICADO"));
                return;
            }
            
            if ("Server".equalsIgnoreCase(usuario)) // Mensaje de error si es un nombre de usuario reservado EJ.: servidor
            { 
                salida.writeObject(new Mensaje("ERROR_RESERVADO"));
                return;
            }
            
            conectado = true;
            salida.writeObject(new Mensaje("OK"));
            server.conexion(usuario, this);

            // MensajeS enviados
            while ((mensaje = (Mensaje) entrada.readObject()) != null) 
            {
            	if ("mensaje_publico".equals(mensaje.getTipo())) // Si el menasaje es publico
            	{
            		server.enviarMensajePublico(mensaje);
            		GeneraLog.getLogger().info("(Público) [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " @" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                } 
            	else if ("mensaje_privado".equals(mensaje.getTipo())) // Si el menasaje es privado
                {
            		server.enviarMensajePrivado(mensaje);
            		GeneraLog.getLogger().info("(Privado) [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "De @" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                }
            	else if ("respuesta_server".equals(mensaje.getTipo()) && "DESCONEXION".equals(mensaje.getContenido())) // Si el menasaje es de desconexion
                {
                    System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] El usuario " + usuario + " se desconectó.");
                    GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] El usuario " + usuario + " se desconectó.");
                    break;
                }
            }
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            System.err.println("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
            GeneraLog.getLogger().severe("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
        } 
        
        finally 
        {
            if (usuario != null && conectado) // Desconecta el usuario
            {
                server.desconexion(usuario);
            }
            try 
            {
                socket.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
                // System.err.println("[ERROR CERRANDO EL SOCKET]: " + e.getMessage());
                GeneraLog.getLogger().warning("[ERROR CERRANDO EL SOCKET]: " + e.getMessage());
            }
        }
    }
    
    // [ METODOS ]
    public void enviarMensaje(Mensaje mensaje) 
    {
    	try 
    	{
			salida.writeObject(mensaje);
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
			// System.err.println("[ERROR ENVIANDO MENSAJE]: " + e.getMessage()); 
			GeneraLog.getLogger().warning("[ERROR ENVIANDO MENSAJE]: " + e.getMessage());
		}
    }
    
	// [ GETTER Y SETTER NECESARIOS ]
    public String getUsuario() 
    {
    	return usuario;
    }
}