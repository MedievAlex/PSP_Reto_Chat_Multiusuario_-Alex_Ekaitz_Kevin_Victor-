package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Mensaje;

public class ConexionThread extends Thread {
	private String ip;
    private int puerto;
    private String usuario;
    private Client cliente;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado;
    
    public ConexionThread(String ip, int puerto, String usuario, Client cliente) {
        this.ip = ip;
        this.puerto = puerto;
        this.usuario = usuario;
        this.cliente = cliente;
        this.conectado = true;
    }
    
    @Override
    public void run() {
        try {
            Mensaje mensaje;
            
            socket = new Socket(ip, puerto);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            salida.writeObject(usuario);
            mensaje = (Mensaje) entrada.readObject();
            
            if ("OK".equals(mensaje.getContenido())) {
                cliente.conexionExitosa();
                
                while (conectado) {
                    try {
                        mensaje = (Mensaje) entrada.readObject();
                        
                        if ("lista_clientes".equals(mensaje.getTipo())) {
                        	cliente.actualizarClientes(mensaje.getClientes());
                        }
                    } catch (IOException e) {
                        if (conectado) {
                            System.err.println("Error de conexión: " + e.getMessage());
                        }
                        break;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error deserializando mensaje: " + e.getMessage());
                        break;
                    }
                }
            } else if ("ERROR_LLENO".equals(mensaje.getContenido())) {
                cliente.conexionFallida("Servidor lleno");
            } else if ("ERROR_DUPLICADO".equals(mensaje.getContenido())) {
                cliente.conexionFallida("Usuario ya conectado");
            }
            
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Client error: " + ex);
            cliente.conexionFallida("Error de conexión: " + ex.getMessage());
        } finally {
        	cerrarConexion();
        }
    }
    
    public synchronized void desconectar() {
        conectado = false;

        try {
            if (salida != null) {
                salida.writeObject(new Mensaje("DESCONEXION"));
                salida.flush();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error en desconexión: " + e.getMessage());
        }
    }
	
	private void cerrarConexion() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
}