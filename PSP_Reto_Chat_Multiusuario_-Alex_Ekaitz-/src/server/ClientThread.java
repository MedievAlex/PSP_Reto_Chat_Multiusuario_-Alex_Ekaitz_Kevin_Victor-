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
	private Socket socket; // Socket del cliente
	private Server server; // Instancia de Server para acceder a sus metodos
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
				enviarMensaje(new Mensaje("ERROR_LLENO")); // Se envia el mensaje al ConexionThread

				System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Servidor lleno");
				GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Servidor lleno");

				return;
			}

			if (server.getClientesActivos().contains(usuario)) // Mensaje de error si ya hay un cliente con ese usuario
			{
				enviarMensaje(new Mensaje("ERROR_DUPLICADO")); // Se envia el mensaje al ConexionThread

				System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Usuario duplicado");
				GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Usuario duplicado");

				return;
			}

			if ("Server".equalsIgnoreCase(usuario)) // Mensaje de error si es un nombre de usuario reservado EJ.: Server
			{
				enviarMensaje(new Mensaje("ERROR_RESERVADO")); // Se envia el mensaje al ConexionThread

				System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Nombre reservado");
				GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Conexión rechazada: Nombre reservado");

				return;
			}

			conectado = true;
			enviarMensaje(new Mensaje("OK")); // Se envia el OK al ConexionThread
			server.conexion(usuario, this);

			// Mensajes a enviar
			while ((mensaje = (Mensaje) entrada.readObject()) != null)
			{
				if ("mensaje_publico".equals(mensaje.getTipo())) // Si el mensaje es publico
				{
					server.enviarMensajePublico(mensaje); // Envia el mensaje publico al Server
					GeneraLog.getLogger().info("(Público) [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " @" + usuario + "]: " + mensaje);
				}
				else if ("mensaje_privado".equals(mensaje.getTipo())) // Si el mensaje es privado
				{
					server.enviarMensajePrivado(mensaje); // Envia el mensaje privado al Server
					GeneraLog.getLogger().info("(Privado) [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " @" + usuario + "]: " + mensaje);
				}
				else if ("respuesta_server".equals(mensaje.getTipo()) && "DESCONEXION".equals(mensaje.getContenido())) // Si el mensaje es de desconexion termina el bucle
				{
					System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] El usuario " + usuario + " se desconectó.");
					GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] El usuario " + usuario + " se desconectó.");
					break;
				}
			}
		}
		catch (IOException | ClassNotFoundException e)
		{
			System.err.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
			GeneraLog.getLogger().severe(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CON EL CLIENTE (" + usuario + "): " + e.getMessage());
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
				System.err.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CERRANDO EL SOCKET: " + e.getMessage());
				GeneraLog.getLogger().warning(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR CERRANDO EL SOCKET: " + e.getMessage());
			}
		}
	}

	// [ METODOS ]
	public void enviarMensaje(Mensaje mensaje) // Mensaje a enviar
	{
		try
		{
			salida.writeObject(mensaje); // Se envia el mensaje al ConexionThread
		}
		catch (IOException e)
		{
			System.err.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR ENVIANDO MENSAJE: " + e.getMessage());
			GeneraLog.getLogger().warning(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ERROR ENVIANDO MENSAJE: " + e.getMessage());
		}
	}

	// [ GETTER NECESARIO ]
	public String getUsuario()
	{
		return usuario;
	}
}