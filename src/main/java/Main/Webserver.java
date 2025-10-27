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
	


	    public static void main(String[] args) {
	        try {
	            int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 10000;
	            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

	            // Serve frontend (index.html)
	            server.createContext("/", exchange -> {
	                if ("GET".equals(exchange.getRequestMethod())) {
	                    File file = new File("src/main/resources/static/index.html");
	                    if (file.exists()) {
	                        byte[] bytes = Files.readAllBytes(file.toPath());
	                        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
	                        exchange.sendResponseHeaders(200, bytes.length);
	                        OutputStream os = exchange.getResponseBody();
	                        os.write(bytes);
	                        os.close();
	                    } else {
	                        byte[] error = "index.html not found".getBytes();
	                        exchange.sendResponseHeaders(404, error.length);
	                        exchange.getResponseBody().write(error);
	                        exchange.getResponseBody().close();
	                    }
	                }
	            });

	            // Handle /run POST request
	            server.createContext("/run", exchange -> {
	                if ("POST".equals(exchange.getRequestMethod())) {
	                    try (InputStream is = exchange.getRequestBody()) {
	                        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
	                        JSONObject json = new JSONObject(body);

	                        String email = json.getString("email");
	                        String password = json.getString("password");
	                        String practice = json.getString("practice");
	                        String type = json.getString("type");

	                        System.out.println("📩 Received Request:");
	                        System.out.println("Email: " + email);
	                        System.out.println("Practice: " + practice);
	                        System.out.println("Type: " + type);

	                        String result;
	                        try {
	                            if (type.equalsIgnoreCase("EOB")) {
	                                Dos automation = new Dos(email, password, practice, null, null);
	                                automation.executeAutomation();
	                            } else if (type.equalsIgnoreCase("ERA")) {
	                                pdf automation = new pdf(email, password, practice, null, null);
	                                automation.executeAutomation();
	                            }
	                            result = "Automation completed successfully for " + practice;
	                        } catch (Exception e) {
	                            e.printStackTrace();
	                            result = "❌ Error: " + e.getMessage();
	                        }

	                        byte[] response = result.getBytes(StandardCharsets.UTF_8);
	                        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
	                        exchange.sendResponseHeaders(200, response.length);
	                        exchange.getResponseBody().write(response);
	                    }
	                } else {
	                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
	                }
	                exchange.close();
	            });

	            server.setExecutor(null);
	            server.start();
	            System.out.println("✅ Server started on port " + port);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
