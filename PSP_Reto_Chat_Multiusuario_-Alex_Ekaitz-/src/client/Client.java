package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private FlowLayout fl_panelSuperior;
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
	private JTextArea areaChat;
	private JScrollPane scroll;
	private FlowLayout fl_panelInferior;
	private JPanel panelInferior;
	private JCheckBox chkPrivado;
	private JLabel labelPara;
	private JComboBox<String> clientes;
	private JTextField txtMensaje;
	private JButton btnEnviar;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client frame = new Client();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Client() {
        setTitle("Chat Multiusuario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(890, 550);

        // Panel superior
        fl_panelSuperior = new FlowLayout(FlowLayout.LEFT);
        fl_panelSuperior.setVgap(10);
        panelSuperior = new JPanel(fl_panelSuperior);
        
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
        
        panelSuperior.add(labelIp);
        panelSuperior.add(txtIp);
        panelSuperior.add(labelPuerto);
        panelSuperior.add(txtPuerto);
        panelSuperior.add(labelUsuario);
        panelSuperior.add(txtUsuario);
        panelSuperior.add(btnConectar);
        panelSuperior.add(btnDesconectar);
        panelSuperior.add(lblEstado);

        getContentPane().add(panelSuperior, BorderLayout.NORTH);

        // Panel central
        areaChat = new JTextArea();
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
        areaChat.setEditable(false);
        scroll = new JScrollPane(areaChat);
        
        getContentPane().add(scroll, BorderLayout.CENTER);

        // Panel inferior
        fl_panelInferior = new FlowLayout(FlowLayout.LEFT);
        fl_panelInferior.setVgap(10);
        panelInferior = new JPanel(fl_panelInferior);
        
        chkPrivado = new JCheckBox("Privado");
        chkPrivado.setFont(new Font("Arial", Font.BOLD, 14));
        chkPrivado.addActionListener(this);
        
        labelPara = new JLabel("Para:");
        labelPara.setFont(new Font("Arial", Font.BOLD, 14));
        
        clientes = new JComboBox<>();
        clientes.setPreferredSize(new Dimension(130, 22));
        clientes.setEnabled(false);
        
        txtMensaje = new JTextField(45);
        txtMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnEnviar = new JButton("Enviar");
        btnEnviar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(this);
        
        panelInferior.add(chkPrivado);
        panelInferior.add(labelPara);
        panelInferior.add(clientes);
        panelInferior.add(txtMensaje);
        panelInferior.add(btnEnviar);

        getContentPane().add(panelInferior, BorderLayout.SOUTH);
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub	
		if (e.getSource() == btnConectar) {
			if (txtIp.getText().isEmpty() || txtPuerto.getText().isEmpty() || txtUsuario.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Rellene los campos IP, Puerto y Usuario", "Atención", JOptionPane.WARNING_MESSAGE);
				return;
			}
		} else if (e.getSource() == chkPrivado) {
			if (chkPrivado.isSelected()) {
				clientes.setEnabled(true);
			} else {
				clientes.setSelectedIndex(-1);
				clientes.setEnabled(false);
			}
		}
	}
	
	public void llenarListaUsuarios() {
		
	}
}
