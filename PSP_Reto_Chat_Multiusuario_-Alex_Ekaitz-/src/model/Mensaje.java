package model;

import java.io.Serializable;
import java.util.List;

public class Mensaje implements Serializable
{
	private static final long serialVersionUID = 1L;

	// [ VARIABLES ]
	private String tipo;
    private List<String> clientes;
    private String contenido;
    private String remitente;
    private String destinatario;

    /**
     * Constructor para respuestas del servidor
     * @param respuesta Respuesta del servidor
     */
    public Mensaje(String respuesta)
    {
        this.tipo = "respuesta_server";
        this.contenido = respuesta;
    }

    /**
     * Constructor para mensajes de lista de clientes
     * @param clientes Lista de clientes conectados
     */
    public Mensaje(List<String> clientes)
    {
        this.tipo = "lista_clientes";
        this.clientes = clientes;
    }

    /**
     * Constructor para mensajes públicos
     * @param contenido Contenido del mensaje
     * @param remitente Quien envía el mensaje
     */
    public Mensaje(String contenido, String remitente)
    {
        this.tipo = "mensaje_publico";
        this.contenido = contenido;
        this.remitente = remitente;
    }

    /**
     * Constructor para mensajes privados
     * @param contenido Contenido del mensaje
     * @param remitente Quien envía el mensaje
     * @param destinatario Quien recibe el mensaje
     */
    public Mensaje(String contenido, String remitente, String destinatario)
    {
        this.tipo = "mensaje_privado";
        this.contenido = contenido;
        this.remitente = remitente;
        this.destinatario = destinatario;
    }

	public String getTipo()
	{
		return tipo;
	}

	public List<String> getClientes()
	{
		return clientes;
	}

	public String getContenido()
	{
		return contenido;
	}

	public String getRemitente()
	{
		return remitente;
	}

	public String getDestinatario()
	{
		return destinatario;
	}
}