package logger;

import java.io.IOException;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GeneraLog 
{
	// [ VARIABLES ]
    private static Logger logger = null;
    private static FileHandler fileHandler = null;

    // [ CONSTRUCTORES ]
    // Constructor privado para evitar la instanciación externa
    private GeneraLog() 
    {
        // Inicializa el logger solo si no se ha inicializado previamente
        if (logger == null) 
        {
        	File carpeta = new File("./logs"); // Ruta de la carpeta

            if (!carpeta.exists()) 
            {
            	carpeta.mkdir();
            }
                
            try 
            {
                // Configura el logger
                logger = Logger.getLogger(GeneraLog.class.getName());
                // A false evita la salida en consola
                logger.setUseParentHandlers(false);
                fileHandler = new FileHandler("./logs/ProyectLogs.log", true); // Ruta de la carpeta + nombre archivo .log [true = append mode]

                SimpleFormatter formatter = new SimpleFormatter();
                fileHandler.setFormatter(formatter);
                logger.addHandler(fileHandler);
                logger.setLevel(Level.ALL); // Establece el nivel de log (puedes ajustarlo según tu necesidad)
            } 
            catch (IOException e) 
            {
                System.err.println("[ERROR AL CONFIGURAR EL LOGGER]: " + e.getMessage());
            }
        }
    }

    // [ METODOS ]
    // Método para obtener la instancia del logger
    public static Logger getLogger() 
    {
        if (logger == null)
        {
            new GeneraLog(); // Si aún no se ha creado, inicialízalo
        }
        return logger;
    }

    // Método para cerrar el fileHandler
    public static void closeLogger() 
    {
        if (fileHandler != null)
        {
            fileHandler.close();
        }
    }
}