package Main;
import static spark.Spark.*;
public class Webserver {
	// File: WebServer.java
	

	    public static void start() {
	        port(8080);
	        get("/", (req, res) -> "✅ Nation Automation Web Service Running in Headless Mode");
	    }
	}
