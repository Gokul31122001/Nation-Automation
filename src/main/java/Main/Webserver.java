package Main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Webserver {
	


	    public static void main(String[] args) throws Exception {
	        int port = 10000; // Render default port
	        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
	        System.out.println("🚀 Server started on port " + port);

	        // Serve the HTML UI
	        server.createContext("/", exchange -> {
	            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
	                serveFile(exchange, "index.html");
	            } else {
	                exchange.sendResponseHeaders(405, -1);
	            }
	        });

	        // Handle automation trigger (from HTML form)
	        server.createContext("/run", exchange -> {
	            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
	                try {
	                    // Read JSON body
	                    InputStream inputStream = exchange.getRequestBody();
	                    String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	                    JSONObject json = new JSONObject(body);

	                    String email = json.getString("email");
	                    String password = json.getString("password");
	                    String practice = json.getString("practice");
	                    String type = json.getString("type");

	                    System.out.println("🧠 Received Automation Request:");
	                    System.out.println("Email: " + email);
	                    System.out.println("Practice: " + practice);
	                    System.out.println("Type: " + type);

	                    // (Here you can trigger your Selenium automation logic)
	                    String result = "Automation started successfully for " + practice + " (" + type + ")";

	                    byte[] response = result.getBytes(StandardCharsets.UTF_8);
	                    exchange.sendResponseHeaders(200, response.length);
	                    exchange.getResponseBody().write(response);
	                    exchange.getResponseBody().close();

	                } catch (Exception e) {
	                    e.printStackTrace();
	                    String error = "Error: " + e.getMessage();
	                    exchange.sendResponseHeaders(500, error.getBytes().length);
	                    exchange.getResponseBody().write(error.getBytes());
	                    exchange.getResponseBody().close();
	                }
	            } else {
	                exchange.sendResponseHeaders(405, -1);
	            }
	        });

	        server.start();
	    }

	    // Utility: serve index.html from resources/static
	    private static void serveFile(HttpExchange exchange, String fileName) throws IOException {
	        File file = new File("src/main/resources/static/" + fileName);
	        if (!file.exists()) {
	            String msg = "❌ File not found: " + fileName;
	            exchange.sendResponseHeaders(404, msg.length());
	            exchange.getResponseBody().write(msg.getBytes());
	            exchange.getResponseBody().close();
	            return;
	        }

	        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
	        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
	        exchange.sendResponseHeaders(200, bytes.length);
	        exchange.getResponseBody().write(bytes);
	        exchange.getResponseBody().close();
	    }
	}
