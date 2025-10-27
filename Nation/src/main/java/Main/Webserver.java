package Main;
import static spark.Spark.*;
public class Webserver {
	// File: WebServer.java
	
	    public static void main(String[] args) {
	        port(8080); // Render/Railway expects this port
	        get("/", (req, res) -> "✅ Server is running!");

	        get("/run", (req, res) -> {
	            try {
	                // Run your existing main automation
	                Main.main(null);
	                return "✅ Automation triggered successfully!";
	            } catch (Exception e) {
	                e.printStackTrace();
	                res.status(500);
	                return "❌ Error: " + e.getMessage();
	            }
	        });
	    }
	}

