package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logger.GeneraLog;
import model.Mensaje;

public class Server
{

	// [ VARIABLES ]
	private final int PUERTO = 1234; // Puerto elegido
	private int limite = 2; // Limite de clientes simultaneos
	private int tiempoMostrar = 30; // Tiempo entre mensajes
	private ClientThread hilo; // Hilo para cada cliente
	private Map<String, ClientThread> clientes = Collections.synchronizedMap(new HashMap<>()); // Mapa sincronizado de clientes con sus hilos
	private Map<String, List<Mensaje>> mensajes = Collections.synchronizedMap(new HashMap<>()); // Mapa sincronizado de clientes con sus mensajes
	private long inicioServidor; // Momento de inicio
	private MonitorThread monitor; // Hilo de monitoreo

	// [ MAIN ]
	public static void main(String[] args)
	{
		Server server = new Server();
		server.iniciar();
	}

	// [ METODOS ]
	public void iniciar()
	{
		// Cierra automaticamente el recurso al finalizar (Try with resources)
		try (ServerSocket serverSocket = new ServerSocket(PUERTO))
		{
			System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Servidor iniciado. Esperando conexiones en el puerto " + PUERTO + "...");
			inicioServidor = System.currentTimeMillis(); // Registra el momento de inicio del servidor

			// Crear e iniciar el hilo de monitoreo
			monitor = new MonitorThread(tiempoMostrar, inicioServidor);
			monitor.start();

			// Mantiene el servidor abierto
			while (true)
			{
				Socket clienteSocket = serverSocket.accept(); // Shocket para el cliente entrante
				System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Nueva conexión entrante...");

				hilo = new ClientThread(clienteSocket, this); // Crea un hilo por el cliente entrante
				hilo.start(); // Inicia el hilo
			}
		}
		catch (IOException ex) // Gestiona los posibles errores de los shocket del servidor y cliente
		{
			System.err.println("[ERROR EN EL SERVIDOR]: " + ex.getMessage());
			GeneraLog.getLogger().severe("[ERROR EN EL SERVIDOR]: " + ex.getMessage());
		}
	}

	public void conexion(String usuario, ClientThread hilo)
	{
		synchronized(clientes) { 
			clientes.put(usuario, hilo); // Añade un cliente con su hilo al HashMap

			// Actualizar contador en el monitor
			if (monitor != null)
			{			
				monitor.setClientesConectados(clientes.size());
			}
		}
		System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Usuario conectado: " + usuario + " | Activos: " + clientes.size());
		GeneraLog.getLogger().info("Usuario conectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes(true, usuario);	
	}

	public void desconexion(String usuario)
	{
		synchronized(clientes) { 
			clientes.remove(usuario); // Elimina el cliente del HashMap

			// Actualizar contador en el monitor
			if (monitor != null)
			{
				monitor.setClientesConectados(clientes.size());
			}
		}
		System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
		GeneraLog.getLogger().info("Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes(false, usuario);
	}

	public List<String> getClientesActivos()
	{
		return new ArrayList<>(clientes.keySet());
	}

	public synchronized void enviarMensajePublico(Mensaje mensaje)
	{
		List<Mensaje> lista = mensajes.getOrDefault(mensaje.getRemitente(), new ArrayList<>()); // Obtiene la lista de mensajes y si no existe la crea

		lista.add(mensaje); // Añade el mensaje a la lista
		mensajes.put(mensaje.getRemitente(), lista); // Añade o reemplaza el usuario

		for (ClientThread hilo : clientes.values()) // Envia el mensaje a todos los clientes conectados menos al remitente
		{
			if (!hilo.getUsuario().equals(mensaje.getRemitente()))
			{
				hilo.enviarMensaje(mensaje); // Llama al metodo de ClientThread
			}
		}

		if (monitor != null && !"Server".equals(mensaje.getRemitente()) && !"lista_clientes".equals(mensaje.getTipo()))
		{
			monitor.setUltimoMensaje(mensaje);

			System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] MENSAJE: "
						+ "(Público) [@" + mensaje.getRemitente() + "]: " + mensaje.getContenido());

			GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] MENSAJE: "
						+ "(Público) [@" + mensaje.getRemitente() + "]: " + mensaje.getContenido());
		}
	}

	public synchronized void enviarMensajePrivado(Mensaje mensaje)
	{
		ClientThread destinatario = clientes.get(mensaje.getDestinatario());
		List<Mensaje> lista = mensajes.getOrDefault(mensaje.getRemitente(), new ArrayList<>()); // Obtiene la lista de mensajes y si no existe la crea

		lista.add(mensaje); // Añade el mensaje a la lista existente o nueva
		mensajes.put(mensaje.getRemitente(), lista); // Añade o reemplaza el usuario

		if (destinatario != null) // Envia el mensaje al destinatario si está conectado
		{
			destinatario.enviarMensaje(mensaje);
		}

		if (monitor != null)
		{
			monitor.setUltimoMensaje(mensaje);
		}

		System.out.println(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] MENSAJE: "
					+ "(Privado) [De @" + mensaje.getRemitente() + " para @" + mensaje.getDestinatario() + "]: " + mensaje.getContenido());

		GeneraLog.getLogger().info(" [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] MENSAJE: "
				+ "(Privado) [De @" + mensaje.getRemitente() + " para @" + mensaje.getDestinatario() + "]: " + mensaje.getContenido());
	}

	public void actualizarClientes(boolean conectado, String usuario)
	{
		enviarMensajePublico(new Mensaje(getClientesActivos()));

		enviarMensajePublico(new Mensaje("Cliente " + usuario + (conectado ? " conectado." : " desconectado."), "Server"));
	}

	// [ GETTER NECESARIO ]
	public int getLimite()
	{
		return limite;
	}
}