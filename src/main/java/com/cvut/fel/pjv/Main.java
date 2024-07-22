package com.cvut.fel.pjv;

import com.cvut.fel.pjv.Controllers.GameController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class responsible for starting the Arimaa game application.
 * It initializes the game controller, handles command-line arguments for
 * logging configuration, and starts the game.
 */
public class Main {
    private static Logger logger;

    /**
     * The entry point of the Arimaa game application.
     * It initializes the game controller, configures logging based on command-line
     * arguments, and starts the game.
     * 
     * @param args Command-line arguments. If provided, the first argument specifies
     *             the logging level (DEBUG, INFO, WARN, ERROR).
     */
    public static void main(String[] args) {
        String logLevel = args.length == 1 ? args[0] : "INFO"; // Default to INFO if no parameter provided
        boolean logs = false; // Parameter that defines if the logs are turned on

        if (args.length == 1) { // if yes, we create logger
            logger = LoggerFactory.getLogger(Main.class);
            logs = true;
            configureLogging(logLevel);
        }

        if (logs) {
            logger.info("Game started with logging {} enabled");
        }

        // launch of the game
        GameController game = new GameController(logs);
        game.start();
    }

    /**
     * Configures the logging level based on the provided log level string.
     * 
     * @param logLevel The logging level string provided as a command-line argument.
     */
    private static void configureLogging(String logLevel) {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // switch to define what level of logging was chosen

        switch (logLevel.toUpperCase()) {
            case "DEBUG":
                rootLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
                break;
            case "INFO":
                rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
                break;
            case "WARN":
                rootLogger.setLevel(ch.qos.logback.classic.Level.WARN);
                break;
            case "ERROR":
                rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
                break;
            default:
                rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);
                break;
        }
    }
}
