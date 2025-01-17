package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import org.json.JSONObject;

public class HttpServerBotCom {
    public static void main(String[] args) throws Exception {
        // Création du serveur HTTP, écoute sur le port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/receive", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Vérification du type de méthode HTTP (GET, POST, etc.)
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Lecture du corps de la requête
                    InputStream inputStream = exchange.getRequestBody();
                    String requestData = new String(inputStream.readAllBytes());

                    // Affichage des données reçues dans la console
                    System.out.println("Données reçues : " + requestData);

                    // Traitement de la donnée reçue (par exemple, ici en format JSON)
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("receivedData", requestData);

                    // Réponse HTTP
                    String response = jsonResponse.toString();

                    // Définir les en-têtes de la réponse
                    exchange.getResponseHeaders().add("Content-Type", "application/json");

                    // Répondre avec le statut HTTP et la donnée
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    // Écrire la réponse dans le flux de sortie
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    // Si ce n'est pas une méthode POST, répondre avec une erreur
                    String response = "Method Not Allowed";
                    exchange.sendResponseHeaders(405, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });

        // Démarrer le serveur HTTP
        System.out.println("Server started at http://localhost:8080");
        server.start();
    }
}