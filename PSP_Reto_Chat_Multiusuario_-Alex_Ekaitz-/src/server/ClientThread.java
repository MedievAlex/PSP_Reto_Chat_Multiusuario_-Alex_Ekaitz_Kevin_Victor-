package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
            		GeneraLog.getLogger().info("(Público) [" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                } 
            	else if ("mensaje_privado".equals(mensaje.getTipo())) // Si el menasaje es privado
                {
            		server.enviarMensajePrivado(mensaje);
            		GeneraLog.getLogger().info("(Privado) [" + usuario + "]: " + mensaje);
            		server.setUltimoMensaje(mensaje);
                }
            	else if ("respuesta_server".equals(mensaje.getTipo()) && "DESCONEXION".equals(mensaje.getContenido())) // Si el menasaje es de desconexion
                {
                    System.out.println("Usuario " + usuario + " se desconectó");
                    GeneraLog.getLogger().info("Usuario " + usuario + " se desconectó");
                    break;
                }
            }
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            System.err.println("[ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
            GeneraLog.getLogger().severe("[ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
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
                // System.err.println("[ERROR CERRANDO EL SOCKET]: " + e.getMessage()); (ClientThread) Linea: 104
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
			// System.err.println("[ERROR ENVIANDO MENSAJE]: " + e.getMessage()); (ClientThread) Linea: 121
			GeneraLog.getLogger().warning("[ERROR ENVIANDO MENSAJE]: " + e.getMessage());
		}
    }
    
	// [ GETTER Y SETTER NECESARIOS ]
    public String getUsuario() 
    {
    	return usuario;
    }
}