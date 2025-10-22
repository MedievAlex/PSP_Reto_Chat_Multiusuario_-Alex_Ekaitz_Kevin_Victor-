package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Mensaje;

public class ConexionThread extends Thread
{

	// [ VARIABLES ]
	private String ip;
	private int puerto;
	private String usuario;
	private Client cliente;
	private Socket socket;
	private ObjectOutputStream salida;
	private ObjectInputStream entrada;
	private boolean conectado;

	// [ CONSTRUCTORES ]
	public ConexionThread(String ip, int puerto, String usuario, Client cliente)
	{
		this.ip = ip;
		this.puerto = puerto;
		this.usuario = usuario;
		this.cliente = cliente;
		this.conectado = true;
	}

	@Override
	public void run()
	{
		try
		{
			Mensaje mensaje;

			socket = new Socket(ip, puerto);
			salida = new ObjectOutputStream(socket.getOutputStream());
			entrada = new ObjectInputStream(socket.getInputStream());

			salida.writeObject(usuario);
			mensaje = (Mensaje) entrada.readObject();

			if ("OK".equals(mensaje.getContenido())) // Si recive el OK del ClientThread
			{
				cliente.conexionExitosa();

				while (conectado)
				{
					try
					{
						mensaje = (Mensaje) entrada.readObject(); // El mensaje recibido

						if ("lista_clientes".equals(mensaje.getTipo())) // Si el mensaje recibido es tipo actualizar clientes
						{
							cliente.actualizarClientes(mensaje); // Metodo en cliente
						}
						else if ("mensaje_publico".equals(mensaje.getTipo())) // Si el mensaje recibido es tipo mensaje publico
						{
							cliente.mostrarMensaje("Público", mensaje.getRemitente(), mensaje.getContenido()); // Metodo en cliente
						}
						else if ("mensaje_privado".equals(mensaje.getTipo())) // Si el mensaje recibido es tipo mensaje privado
						{
							cliente.mostrarMensaje("Privado", mensaje.getRemitente(), mensaje.getContenido()); // Metodo en cliente
						}
					}
					catch (IOException e)
					{
						if (conectado)
						{
							cliente.conexionFallida("Error de conexión: " + e.getMessage());
						}
						break;
					}
					catch (ClassNotFoundException e)
					{
						cliente.conexionFallida("Error de comunicación: " + e.getMessage());
						break;
					}
				}
			}
			else if ("ERROR_LLENO".equals(mensaje.getContenido()))
			{
				cliente.conexionFallida("Servidor lleno");
			}
			else if ("ERROR_DUPLICADO".equals(mensaje.getContenido()))
			{
				cliente.conexionFallida("Usuario ya conectado");
			}
			else if ("ERROR_RESERVADO".equals(mensaje.getContenido()))
			{
				cliente.conexionFallida("Nombre de usuario inválido");
			}

		}
		catch (IOException | ClassNotFoundException ex)
		{
			cliente.conexionFallida("Error de conexión: " + ex.getMessage());
		}
		finally
		{
			cerrarConexion();
		}
	}

	public void enviarMensaje(Mensaje mensaje)
	{
		try
		{
			salida.writeObject(mensaje); // Envia el mensaje al ClientThread
		}
		catch (IOException e)
		{
			cliente.conexionFallida("Error enviando mensaje: " + e.getMessage());
		}
	}

	public void desconectar()
	{
		conectado = false;

		try
		{
			if (salida != null)
			{
				enviarMensaje(new Mensaje("DESCONEXION"));
				salida.flush();
			}
			if (socket != null)
			{
				socket.close();
			}
		}
		catch (IOException e)
		{

		}
	}

	private void cerrarConexion()
	{
		try
		{
			if (entrada != null)
			{
				entrada.close();
			}
			if (salida != null)
			{
				salida.close();
			}
			if (socket != null)
			{
				socket.close();
			}
		}
		catch (IOException e)
		{
		}
	}
}