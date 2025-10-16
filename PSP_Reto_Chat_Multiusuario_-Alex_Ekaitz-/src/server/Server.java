package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import logger.GeneraLog;
import model.Mensaje;

public class Server {

	private final int PUERTO = 1234; // Puerto elegido
	private int limite = 6; // Limite de clientes simultaneos
	private int tiempoMostrar = 30; // Tiempo entre mensajes
	private ClientThread hilo; // Hilo para cada cliente
	private Map<String, ClientThread> clientes = Collections.synchronizedMap(new HashMap<>()); // Mapa sincronizado de clientes con sus hilos
	private long inicioServidor; // Momento de inicio
	private Mensaje ultimoMensaje; // Registro de ultimo mensaje enviado

	public static void main(String[] args) {
		Server server = new Server();
		server.iniciar();
	}

	public void iniciar() {
		try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
			System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + PUERTO + "...");
			inicioServidor = System.currentTimeMillis();
			
			Timer timer = new Timer();
			
			timer.scheduleAtFixedRate(new TimerTask() {
			    public void run() {
			        actividadServer();
			    }
			}, 0, tiempoMostrar * 1000);
			
			while (true) {
				Socket clienteSocket = serverSocket.accept();
				System.out.println("Nueva conexión entrante...");

				hilo = new ClientThread(clienteSocket, this);
				hilo.start();
			}

		} catch (IOException ex) {
			System.err.println("Error en el servidor: " + ex.getMessage());
			GeneraLog.getLogger().severe("Error en servidor: " + ex.getMessage());
		}
	}

	public synchronized void conexion(String usuario, ClientThread hilo) {
		clientes.put(usuario, hilo);
		System.out.println("Usuario conectado: " + usuario + " | Activos: " + clientes.size());
		GeneraLog.getLogger().info("Usuario conectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes(true, usuario);
	}

	public synchronized void desconexion(String usuario) {
		clientes.remove(usuario);
		System.out.println("Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
		GeneraLog.getLogger().info("Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes(false, usuario);
	}

	public synchronized List<String> getClientesActivos() {
		return new ArrayList<>(clientes.keySet());
	}
	
	public synchronized void enviarMensajePublico(Mensaje mensaje) {
	    for (ClientThread hilo : clientes.values()) {
	    	if (!hilo.getUsuario().equals(mensaje.getRemitente())) {
	            hilo.enviarMensaje(mensaje);
	        }
	    }
	}
	
	public synchronized void enviarMensajePrivado(Mensaje mensaje) {
		ClientThread destinatario = clientes.get(mensaje.getDestinatario());
		
		if (destinatario != null) {
			destinatario.enviarMensaje(mensaje);
		}
	}

	public synchronized void actualizarClientes(boolean conectado, String usuario) {
		enviarMensajePublico(new Mensaje(getClientesActivos()));

		enviarMensajePublico(new Mensaje("Cliente " + usuario + (conectado ? " conectado." : " desconectado."), "Server"));
	}
	
	public void actividadServer() {
		int clientesConectados = clientes.size();
		long tiempoActivo = System.currentTimeMillis() - inicioServidor;
		String log = "Estado Servidor | Clientes conectados: " + clientesConectados + " | Tiempo activo: " + (tiempoActivo / 1000) + "s | Último mensaje: " + (ultimoMensaje != null ? "(" + ("mensaje_publico".equals(ultimoMensaje.getTipo()) ? "Público" : "Privado") + ") [" + ultimoMensaje.getRemitente() + "]: " + ultimoMensaje.getContenido() : "Ninguno");
		
		System.out.println(log);
		
		GeneraLog.getLogger().info(log);
	}
	
	public synchronized void setUltimoMensaje(Mensaje mensaje) {
	    this.ultimoMensaje = mensaje;
	}

	public int getLimite() {
		return limite;
	}
}