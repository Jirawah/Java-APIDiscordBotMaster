package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;

public class HttpServerBotCom {
    public static void main(String[] args) throws Exception {
        // Chargement du fichier de configuration
        Properties props = new Properties();
        props.load(new FileInputStream("src/main/resources/config.properties"));

        // Récupération du port et du token depuis le fichier de config (données sensibles)
        int port = Integer.parseInt(props.getProperty("server.port"));
        String authToken = props.getProperty("server.auth.token");

        // Création du serveur HTTP sur le port défini
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Définition du endpoint /receive
        server.createContext("/receive", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Vérification que la requête est bien une POST
                if ("POST".equals(exchange.getRequestMethod())) {

                    // Vérifie le token d'authentification dans l'en-tête "Authorization"
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (!authToken.equals(authHeader)) {
                        String error = "Unauthorized";
                        exchange.sendResponseHeaders(401, error.getBytes().length);
                        exchange.getResponseBody().write(error.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }

                    // Lecture du corps de la requête
                    InputStream inputStream = exchange.getRequestBody();
                    String requestData = new String(inputStream.readAllBytes());

                    // Vérifie que la donnée reçue est un JSON valide
                    try {
                        JSONObject json = new JSONObject(requestData);
                    } catch (Exception e) {
                        String error = "Invalid JSON";
                        exchange.sendResponseHeaders(400, error.getBytes().length);
                        exchange.getResponseBody().write(error.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }

                    // Affichage des données reçues
                    System.out.println("Données reçues : " + requestData);

                    // Réponse à renvoyer
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("receivedData", requestData);
                    String response = jsonResponse.toString();

                    // En-têtes + statut + réponse
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    // Requête non autorisée (non POST)
                    String response = "Method Not Allowed";
                    exchange.sendResponseHeaders(405, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                }
            }
        });

        // Démarrage du serveur HTTP
        System.out.println("Server started at http://localhost:" + port);
        server.start();
    }
}
