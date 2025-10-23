package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.ClientThread;

public class ListaClientes {
	private Map<String, ClientThread> clientes; // Mapa sincronizado de clientes con sus hilos
	private int clientesActivos;
	
	// Constructor
	public ListaClientes()
	{
		this.clientes = Collections.synchronizedMap(new HashMap<>());
	}
	
	// Devuelve la cantidad de clientes activos
	public int clientesActivos()
	{
		return clientesActivos;
	}
	
	// Añade un cliente con su hilo al HashMap
	public synchronized void nuevoCliente(String usuario, ClientThread hilo) 
	{
		clientes.put(usuario, hilo); 
		clientesActivos = clientes.size();
	}
	
	// Elimina un cliente del HashMap
	public synchronized void retirarCliente(String usuario) 
	{
		clientes.remove(usuario); 
		clientesActivos = clientes.size();
	}
	
	// Obtiene un cliente especifico
	public ClientThread obtenerCliente(String destinatario) 
	{
		return clientes.get(destinatario);
	}
	
	// Para el for-each (PREGUNTAR)
	public Map<String, ClientThread> obtenerClientes() 
	{
		return clientes;
	}
	
	// Para actualizar lista (PREGUNTAR)
	public List<String> listaClientesActivos()
	{
		return new ArrayList<>(clientes.keySet());
	}
}
