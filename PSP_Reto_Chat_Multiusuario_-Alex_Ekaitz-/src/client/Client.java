package client;

import javax.swing.*;

import model.Mensaje;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Client extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	// [ VARIABLES ]
	private ConexionThread hilo;
	private JPanel panelSuperior;
	private JLabel labelIp;
	private JTextField txtIp;
	private JLabel labelPuerto;
	private JTextField txtPuerto;
	private JLabel labelUsuario;
	private JTextField txtUsuario;
	private JButton btnConectar;
	private JButton btnDesconectar;
	private JLabel lblEstado;
	private JLabel lblContador;
	private JTextArea areaChat;
	private JScrollPane scroll;
	private JPanel panelInferior;
	private JCheckBox chkPrivado;
	private JLabel labelPara;
	private JComboBox<String> clientesCombo;
	private JTextField txtMensaje;
	private JButton btnEnviar;

	public static void main(String[] args)
	{
		EventQueue.invokeLater(() ->
		{
			try
			{
				Client frame = new Client();
				frame.setVisible(true);
				frame.txtUsuario.requestFocusInWindow(); // Hacer que el campo de usuario esté seleccionado al iniciar la ventana
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	public Client()
	{
		setTitle("Chat Multiusuario");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(980, 550);
		setLocationRelativeTo(null);

		// PANEL SUPERIOR: Izquierdo
		panelSuperior = new JPanel(new BorderLayout());

		JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

		labelIp = new JLabel("IP:");
		labelIp.setFont(new Font("Arial", Font.BOLD, 14));
		txtIp = new JTextField("127.0.0.1", 10);
		txtIp.setFont(new Font("Arial", Font.PLAIN, 14));

		labelPuerto = new JLabel("Puerto:");
		labelPuerto.setFont(new Font("Arial", Font.BOLD, 14));
		txtPuerto = new JTextField("1234", 4);
		txtPuerto.setFont(new Font("Arial", Font.PLAIN, 14));

		labelUsuario = new JLabel("Usuario:");
		labelUsuario.setFont(new Font("Arial", Font.BOLD, 14));
		txtUsuario = new JTextField(8);
		txtUsuario.setFont(new Font("Arial", Font.PLAIN, 14));

		btnConectar = new JButton("Conectar");
		btnConectar.setFont(new Font("Arial", Font.BOLD, 14));
		btnConectar.setPreferredSize(new Dimension(120, 30));
		btnConectar.addActionListener(this);

		btnDesconectar = new JButton("Desconectar");
		btnDesconectar.setFont(new Font("Arial", Font.BOLD, 14));
		btnDesconectar.setPreferredSize(new Dimension(130, 30));
		btnDesconectar.setEnabled(false);
		btnDesconectar.addActionListener(this);

		lblEstado = new JLabel("No conectado");
		lblEstado.setFont(new Font("Arial", Font.BOLD, 14));

		panelIzquierdo.add(labelIp);
		panelIzquierdo.add(txtIp);
		panelIzquierdo.add(labelPuerto);
		panelIzquierdo.add(txtPuerto);
		panelIzquierdo.add(labelUsuario);
		panelIzquierdo.add(txtUsuario);
		panelIzquierdo.add(btnConectar);
		panelIzquierdo.add(btnDesconectar);
		panelIzquierdo.add(lblEstado);

		// PANEL SUPERIOR: Derecho
		JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 17));

		lblContador = new JLabel("");
		lblContador.setFont(new Font("Arial", Font.PLAIN, 14));

		panelDerecho.add(lblContador);

		panelSuperior.add(panelIzquierdo, BorderLayout.WEST);
		panelSuperior.add(panelDerecho, BorderLayout.EAST);

		getContentPane().add(panelSuperior, BorderLayout.NORTH);

		// PANEL CENTRAL
		areaChat = new JTextArea();
		areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
		areaChat.setEditable(false);

		scroll = new JScrollPane(areaChat);

		getContentPane().add(scroll, BorderLayout.CENTER);

		// PANEL INFERIOR
		panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

		chkPrivado = new JCheckBox("Privado");
		chkPrivado.setFont(new Font("Arial", Font.BOLD, 14));
		chkPrivado.addActionListener(this);
		chkPrivado.setEnabled(false);

		labelPara = new JLabel("Para:");
		labelPara.setFont(new Font("Arial", Font.BOLD, 14));

		clientesCombo = new JComboBox<>();
		clientesCombo.setPreferredSize(new Dimension(150, 22));
		clientesCombo.setEnabled(false);

		txtMensaje = new JTextField(50);
		txtMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
		txtMensaje.setEnabled(false);

		btnEnviar = new JButton("Enviar");
		btnEnviar.setFont(new Font("Arial", Font.BOLD, 14));
		btnEnviar.setEnabled(false);
		btnEnviar.addActionListener(this);

		panelInferior.add(chkPrivado);
		panelInferior.add(labelPara);
		panelInferior.add(clientesCombo);
		panelInferior.add(txtMensaje);
		panelInferior.add(btnEnviar);

		getContentPane().add(panelInferior, BorderLayout.SOUTH);
	}

	// ACTION PERFORMER
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnConectar)
		{
			if (txtIp.getText().isEmpty() || txtPuerto.getText().isEmpty() || txtUsuario.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(this, "Rellene los campos IP, Puerto y Usuario", "Atención", JOptionPane.WARNING_MESSAGE);
				return;
			}

			areaChat.setText("");

			hilo = new ConexionThread(txtIp.getText(), Integer.parseInt(txtPuerto.getText()), txtUsuario.getText(), this);
			hilo.start();

		}
		else if (e.getSource() == btnDesconectar)
		{
			desconectarCliente();
		}
		else if (e.getSource() == chkPrivado)
		{
			clientesCombo.setEnabled(chkPrivado.isSelected());

			if (!chkPrivado.isSelected())
			{
				clientesCombo.setSelectedIndex(-1);
			}
		}
		else if (e.getSource() == btnEnviar)
		{
			if (chkPrivado.isSelected() && clientesCombo.getSelectedIndex() == -1)
			{
				JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario", "Atención", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (txtMensaje.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(this, "Debe introducir un mensaje", "Atención", JOptionPane.WARNING_MESSAGE);
				return;
			}

			enviarMensaje();
		}
	}

	// Conexion correcta
	public void conexionExitosa()
	{
		SwingUtilities.invokeLater(() ->
		{
			txtIp.setEditable(false);
			txtPuerto.setEditable(false);
			txtUsuario.setEditable(false);
			btnConectar.setEnabled(false);
			btnDesconectar.setEnabled(true);
			lblEstado.setText("Conectado");
			chkPrivado.setEnabled(true);
			txtMensaje.setEnabled(true);
			btnEnviar.setEnabled(true);
		});
	}

	// Problema de conexion
	public void conexionFallida(String mensajeError)
	{
		SwingUtilities.invokeLater(() ->
		{
			JOptionPane.showMessageDialog(this, mensajeError, "Error de Conexión", JOptionPane.ERROR_MESSAGE);
			txtIp.setEditable(true);
			txtPuerto.setEditable(true);
			txtUsuario.setEditable(true);
			btnConectar.setEnabled(true);
			btnDesconectar.setEnabled(false);
			lblEstado.setText("No conectado");
			lblContador.setText("");
			chkPrivado.setEnabled(false);
			txtMensaje.setEnabled(false);
			btnEnviar.setEnabled(false);
		});
	}

	// Actualiza la lista de clientes conectados en el ComboBox
	public void actualizarClientes(Mensaje mensaje)
	{
		List<String> clientes = new ArrayList<>(mensaje.getClientes());

		SwingUtilities.invokeLater(() ->
		{
			lblContador.setText("Conectados: " + String.valueOf(clientes.size())); // Muestra el número de clientes conectados

			clientes.remove(txtUsuario.getText()); // Elimina al propio usuario para que no aparezca en el combobox

			this.clientesCombo.setModel(new DefaultComboBoxModel<String>(clientes.toArray(new String[0]))); // Actualiza
			this.clientesCombo.setSelectedIndex(-1); // Deselecciona
			
			mostrarMensaje("Público", mensaje.getRemitente(), mensaje.getContenido());
		});
	}

	// Mensajes enviados con el estilo
	public void enviarMensaje()
	{
		Mensaje mensaje;

		if (chkPrivado.isSelected()) // Si el mensaje es privado
		{
			mensaje = new Mensaje(txtMensaje.getText(), txtUsuario.getText(), String.valueOf(clientesCombo.getSelectedItem()));
			mostrarMensaje("Privado", "Yo para @" + mensaje.getDestinatario(), txtMensaje.getText());
		}
		else // Si es público
		{
			mensaje = new Mensaje(txtMensaje.getText(), txtUsuario.getText());
			mostrarMensaje("Público", "Yo", txtMensaje.getText());
		}

		hilo.enviarMensaje(mensaje);

		txtMensaje.setText("");
	}

	// Muestra el mensaje
	public void mostrarMensaje(String tipo, String remitente, String contenido)
	{
		if(tipo.equals("Privado")) // Si el mensaje recibido es privado
		{
			areaChat.append("(" + tipo + ") [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))+" De @" + remitente + "]: " + contenido + "\n\n");
		}
		else // Si el mensaje recibido es público
		{
			areaChat.append("(" + tipo + ") [" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))+" @" + remitente + "]: " + contenido + "\n\n");
		}
		
		areaChat.setCaretPosition(areaChat.getDocument().getLength()); // Fuerza el scroll vertical para mostrar siempre el último mensaje añadido
	}

	// Desconecta el cliente
	public void desconectarCliente()
	{
		hilo.desconectar();
		txtIp.setEditable(true);
		txtPuerto.setEditable(true);
		txtUsuario.setEditable(true);
		btnConectar.setEnabled(true);
		btnDesconectar.setEnabled(false);
		lblEstado.setText("No conectado");
		lblContador.setText("");
		chkPrivado.setEnabled(false);
		this.clientesCombo.removeAllItems();
		txtMensaje.setEnabled(false);
		btnEnviar.setEnabled(false);
	}

}
