package org.example;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 * Classe listener du bot Discord.
 * Gère les événements liés aux messages texte et permet d'exécuter plusieurs actions :
 * - Répondre à une commande de test (!ping)
 * - Se connecter et se déconnecter d'un canal vocal (!join / !disconnect)
 * - Lister les membres connectés à un canal vocal (!connectMyAcc)
 * - Envoyer les informations des utilisateurs connectés à un serveur HTTP local
 */

public class BotListener extends ListenerAdapter {

    /**
     * Déconnecte le bot d’un canal vocal si connecté.
     */
    private void leaveVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            AudioManager audioManager = guild.getAudioManager();

            var connectedChannel = audioManager.getConnectedChannel();

            if (connectedChannel != null && audioManager.isConnected()) {
                // Déconnexion propre du canal vocal
                audioManager.closeAudioConnection();
                event.getChannel().sendMessage("Déconnecté du canal vocal : " + connectedChannel.getName()).queue();
                System.out.println("Déconnecté de : " + connectedChannel.getName());

                // Petite pause pour s'assurer de la synchro
                Thread.sleep(500);
            } else {
                event.getChannel().sendMessage("Le bot n'est connecté à aucun canal vocal.").queue();
                System.out.println("Aucun canal connecté.");
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("Erreur lors de la déconnexion du canal vocal.").queue();
            e.printStackTrace();
        }
    }

    /**
     * Événement déclenché lorsqu'un message est reçu sur un serveur.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) { // Ignore les messages d'autres bots
            String message = event.getMessage().getContentRaw();

            if (message.equalsIgnoreCase("!ping")) {
                // Le bot répond simplement "Pong !" pour vérifier qu'il fonctionne
                event.getChannel().sendMessage("Pong ! 🏓").queue();
            } else if (message.equalsIgnoreCase("!join")) {
                // Il rejoint un canal vocal spécifique
                joinVoiceChannel(event);
            } else if (message.equalsIgnoreCase("!disconnect")) {
                // Il quitte le canal vocal
                leaveVoiceChannel(event);
            } else if (message.equalsIgnoreCase("!connectMyAcc")) {
                // Liste les utilisateurs dans le vocal et les envoie à un backend
                listUsersInVoiceChannel(event);
                event.getChannel().sendMessage("Connected").queue();
            }
        }
    }

    /**
     * Connecte le bot à un canal vocal en récupérant l'ID depuis un fichier de config.
     */
    private void joinVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();

            // Chargement du fichier de configuration
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            // Récupération de l'ID du canal vocal
            String voiceChannelId = props.getProperty("discord.voice.channel.id");
            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);

            if (voiceChannel != null) {
                AudioManager audioManager = guild.getAudioManager();

                // Vérifie si le bot est déjà connecté
                if (audioManager.isConnected()) {
                    event.getChannel().sendMessage("Le bot est déjà connecté à un canal vocal.").queue();
                    System.out.println("Déjà connecté.");
                } else {
                    // Connexion au canal vocal
                    audioManager.openAudioConnection(voiceChannel);
                    event.getChannel().sendMessage("Connecté au canal vocal !").queue();
                    System.out.println("Connexion réussie.");
                }
            } else {
                event.getChannel().sendMessage("Canal vocal introuvable.").queue();
                System.out.println("Canal vocal introuvable.");
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("Erreur lors de la connexion au canal vocal.").queue();
            e.printStackTrace();
        }
    }

    /**
     * Liste les utilisateurs présents dans le canal vocal et envoie leurs infos en JSON via HTTP POST.
     */
    private void listUsersInVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();

            // Chargement du fichier de configuration (données sensibles)
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            // Récupération de l'ID du canal vocal
            String voiceChannelId = props.getProperty("discord.voice.channel.id");
            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);

            if (voiceChannel != null) {
                var membersInChannel = voiceChannel.getMembers();

                if (!membersInChannel.isEmpty()) {
                    JSONArray usersData = new JSONArray();

                    for (var member : membersInChannel) {
                        String userId = member.getId(); // ID de l'utilisateur
                        String avatarUrl = member.getUser().getAvatarUrl(); // URL avatar
                        String username = member.getUser().getName(); // Nom Discord
                        String discriminator = member.getUser().getDiscriminator(); // #1234
                        String nickname = member.getNickname() != null ? member.getNickname() : username; // Pseudo serveur

                        // Création objet JSON utilisateur
                        JSONObject userJson = new JSONObject();
                        userJson.put("id", userId);
                        userJson.put("avatarUrl", avatarUrl);
                        userJson.put("username", username + "#" + discriminator);
                        userJson.put("nickname", nickname);

                        usersData.put(userJson); // Ajout au tableau final
                    }

                    // Envoi des données au backend
                    sendDataToProgram(usersData);
                } else {
                    System.out.println("Aucun utilisateur connecté au canal vocal.");
                }
            } else {
                System.out.println("Canal vocal introuvable.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des utilisateurs dans le canal vocal.");
            e.printStackTrace();
        }
    }

    /**
     * Envoie les données JSON au backend (API REST) via une requête POST HTTP.
     */
    private void sendDataToProgram(JSONArray usersData) {
        try {
            // Adresse du serveur backend
            URL url = new URL("http://localhost:8080/receiveData");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configuration de la requête POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Envoi du corps JSON
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = usersData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lecture de la réponse du backend
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Réponse du serveur: " + response.toString());
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi des données au programme.");
            e.printStackTrace();
        }
    }
}
