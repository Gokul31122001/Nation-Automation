package Main;
import static spark.Spark.*;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Webserver {
	

	    // ✅ Method you call from Main.main()
	    public static void start() throws IOException {
	        int port = 10000;
	        String envPort = System.getenv("PORT");
	        if (envPort != null) {
	            port = Integer.parseInt(envPort);
	        }

	        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
	        System.out.println("✅ Server started on port " + port);

	        server.createContext("/", (HttpExchange exchange) -> {
	            String response = "🚀 Nation Automation backend is running successfully!";
	            exchange.sendResponseHeaders(200, response.getBytes().length);
	            try (OutputStream os = exchange.getResponseBody()) {
	                os.write(response.getBytes());
	            }
	        });

	        server.createContext("/run", (HttpExchange exchange) -> {
	            String response;
	            try {
	                // You can trigger your automation logic here if needed
	                // Example:
	                // new pdf(email, password, practice, excelFile, pdfFile).executeAutomation();
	                response = "✅ Automation triggered successfully!";
	            } catch (Exception e) {
	                response = "❌ Automation failed: " + e.getMessage();
	                e.printStackTrace();
	            }

	            exchange.sendResponseHeaders(200, response.getBytes().length);
	            try (OutputStream os = exchange.getResponseBody()) {
	                os.write(response.getBytes());
	            }
	        });

	        server.setExecutor(null);
	        server.start();
	    }

	    // Optional — you can also run it directly
	    public static void main(String[] args) throws IOException {
	        start();
	    }
	}
