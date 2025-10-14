package ejemplos;

import java.util.logging.Logger;
import LogsProyecto.GeneraLog;

public class EjemplosLogs {

	static Logger logger = GeneraLog.getLogger();

	public static void main(String[] args) {

		logger.info("INFORMACION: logger.info( MENSAJE );\n");
		
		logger.config("CONFIGURACION: logger.config( MENSAJE );\n");
		
		logger.fine("FINE: logger.fine( MENSAJE );\n");
		
		logger.finer("FINER: logger.finest( MENSAJE );\n");
		
		logger.finest("FINEST: logger.finest( MENSAJE );\n");
		
		logger.warning("WARNING: logger.finest( MENSAJE );\n");
		
		logger.severe("SEVERE: logger.finest( MENSAJE );\n");

	}

}
