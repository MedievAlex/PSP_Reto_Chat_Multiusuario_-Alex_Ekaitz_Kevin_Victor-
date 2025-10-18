package server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import logger.GeneraLog;
import model.Mensaje;

public class MonitorThread extends Thread {
	private int clientesConectados;
	private long inicioServidor;  // Te faltaba esta variable
	private Mensaje ultimoMensaje;  // Y esta también
	private int tiempoMostrar;

	// [ CONSTRUCTORES ]
	public MonitorThread(int tiempoMostrar, long inicioServidor)
	{
		this.tiempoMostrar = tiempoMostrar;
		this.inicioServidor = inicioServidor;
		this.clientesConectados = 0;
	}

	// [ METODOS ]
	@Override
	public void run() {
		while (true) {
			try
			{
				Thread.sleep(tiempoMostrar * 1000); // Espera el tiempo configurado

				actividadServer(); // Ejecuta el monitoreo
			}
			catch (InterruptedException e)
			{
				System.err.println("[ERROR EN EL MONITOREO]: " + e.getMessage());
				GeneraLog.getLogger().severe("[ERROR EN EL MONITOREO]: " + e.getMessage());
			}
		}
	}

	public void actividadServer() // Mensaje de informacion sobre el estado del servidor
	{
		long tiempoActivo = System.currentTimeMillis() - inicioServidor;
		String log = " [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ESTADO DEL SERVIDOR:\n Clientes conectados: " + clientesConectados
					+ " | Tiempo activo: " + (tiempoActivo / 1000) + "s"
					+ " | Último mensaje: " + (ultimoMensaje != null ? "(" + ("mensaje_publico".equals(ultimoMensaje.getTipo()) ? "Público" : "Privado") + ") "
					+ "[" + ultimoMensaje.getRemitente() + "]: " + ultimoMensaje.getContenido() : "Ninguno");

		System.out.println(log); // Muestra la actividad

		GeneraLog.getLogger().info(log); // Escribe la actividad en el logger
	}

	// [ GETTER Y SETTER NECESARIOS ]
	public void setClientesConectados(int clientesConectados) {
		this.clientesConectados = clientesConectados;
	}

	public void setUltimoMensaje(Mensaje mensaje) {
		this.ultimoMensaje = mensaje;
	}
}