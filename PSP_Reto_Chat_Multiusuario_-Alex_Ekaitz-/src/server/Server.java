package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Mensaje;

public class Server {

	private final int PUERTO = 1234;
	private int limite = 6;
	private ClientThread hilo;
	private Map<String, ClientThread> clientes = Collections.synchronizedMap(new HashMap<>());

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

				hilo = new ClientThread(clienteSocket, this);
				hilo.start();
			}

		} catch (IOException ex) {
			System.err.println("Error en el servidor: " + ex.getMessage());
		}
	}

	public synchronized void conexion(String usuario, ClientThread hilo) {
		clientes.put(usuario, hilo);
		System.out.println("Usuario conectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes();
	}

	public synchronized void desconexion(String usuario) {
		clientes.remove(usuario);
		System.out.println("Usuario desconectado: " + usuario + " | Activos: " + clientes.size());
		actualizarClientes();
	}

	public synchronized List<String> getClientesActivos() {
		return new ArrayList<>(clientes.keySet());
	}

	public synchronized void actualizarClientes() {
		Mensaje mensaje = new Mensaje(getClientesActivos());
		
		for (ClientThread hilo : clientes.values()) {
			hilo.enviarMensaje(mensaje);
		}
	}

	public int getLimite() {
		return limite;
	}
}