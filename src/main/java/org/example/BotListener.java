package org.example;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class BotListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) { // Ignore les messages des bots
            if (event.getMessage().getContentRaw().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong ! 🏓").queue();
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!join")) {
                joinVoiceChannel(event);
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!disconnect")) {
                leaveVoiceChannel(event); // Déconnexion du canal vocal
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!connectMyAcc")) {
                listUsersInVoiceChannel(event); // Liste des utilisateurs dans le canal vocal
                event.getChannel().sendMessage("Connected").queue();
            }
        }
    }

    private void joinVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);

            if (voiceChannel != null) {
                AudioManager audioManager = guild.getAudioManager();

                // Vérifie si le bot est déjà connecté
                if (audioManager.isConnected()) {
                    event.getChannel().sendMessage("Le bot est déjà connecté à un canal vocal.").queue();
                    System.out.println("Le bot est déjà connecté au canal vocal.");
                } else {
                    // Si le bot n'est pas connecté, on tente d'ouvrir la connexion
                    System.out.println("Tentative de connexion au canal vocal.");
                    audioManager.openAudioConnection(voiceChannel);
                    event.getChannel().sendMessage("Connecté au canal vocal !").queue();
                    System.out.println("Le bot a rejoint le canal vocal.");
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

    private void leaveVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            AudioManager audioManager = guild.getAudioManager();

            // Vérifie si le bot est connecté à un canal vocal
            var connectedChannel = audioManager.getConnectedChannel();

            if (connectedChannel != null && audioManager.isConnected()) {
                // Déconnexion propre
                audioManager.closeAudioConnection();
                event.getChannel().sendMessage("Déconnecté du canal vocal : " + connectedChannel.getName()).queue();
                System.out.println("Le bot a quitté le canal vocal : " + connectedChannel.getName());

                // Attente pour éviter des incohérences dues à la synchronisation
                Thread.sleep(500);
            } else {
                // Si le bot n'est pas connecté à un canal vocal
                event.getChannel().sendMessage("Le bot n'est connecté à aucun canal vocal.").queue();
                System.out.println("Le bot n'était pas connecté à un canal vocal.");
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("Erreur lors de la déconnexion du canal vocal.").queue();
            e.printStackTrace();
        }
    }

    private void listUsersInVoiceChannel(MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);

            if (voiceChannel != null) {
                // Récupérer la liste des membres dans ce canal vocal
                var membersInChannel = voiceChannel.getMembers();

                // Si des membres sont présents
                if (!membersInChannel.isEmpty()) {
                    // Créer un tableau JSON pour stocker les données des utilisateurs
                    JSONArray usersData = new JSONArray();

                    // Pour chaque membre, obtenir les informations nécessaires
                    for (var member : membersInChannel) {
                        String userId = member.getId(); // ID de l'utilisateur
                        String avatarUrl = member.getUser().getAvatarUrl(); // URL de l'avatar
                        String username = member.getUser().getName(); // Nom d'utilisateur
                        String discriminator = member.getUser().getDiscriminator(); // #XXXX
                        String nickname = member.getNickname() != null ? member.getNickname() : username; // Surnom (ou nom d'utilisateur par défaut)

                        // Créer un objet JSON pour chaque utilisateur
                        JSONObject userJson = new JSONObject();
                        userJson.put("id", userId);
                        userJson.put("avatarUrl", avatarUrl);
                        userJson.put("username", username + "#" + discriminator); // Nom d'utilisateur complet
                        userJson.put("nickname", nickname); // Pseudonyme ou nom par défaut

                        // Ajouter l'objet utilisateur au tableau
                        usersData.put(userJson);
                    }

                    // Envoyer les données au programme via HTTP
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

    // Méthode pour envoyer les données à ton programme via HTTP
    private void sendDataToProgram(JSONArray usersData) {
        try {
            // URL du serveur HTTP de ton programme
            URL url = new URL("http://localhost:8080/receiveData"); // Remplace par l'URL de ton programme
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configurer la connexion HTTP
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Envoyer les données sous forme de JSON
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = usersData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lire la réponse du serveur
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                StringBuilder response = new StringBuilder();
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


//1111111111
//package org.example;
//
//import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import net.dv8tion.jda.api.managers.AudioManager;
//
//import java.io.*;
//import java.net.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//public class BotListener extends ListenerAdapter {
//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        if (!event.getAuthor().isBot()) { // Ignore les messages des bots
//            if (event.getMessage().getContentRaw().equalsIgnoreCase("!ping")) {
//                event.getChannel().sendMessage("Pong ! 🏓").queue();
//            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!join")) {
//                joinVoiceChannel(event);
//            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!disconnect")) {
//                leaveVoiceChannel(event); // Déconnexion du canal vocal
//            } else if (event.getMessage().getContentRaw().equalsIgnoreCase("!connectMyAcc")) {
//                listUsersInVoiceChannel(event); // Liste des utilisateurs dans le canal vocal
//                event.getChannel().sendMessage("Connected").queue();
//            }
//        }
//    }
//
//    private void joinVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
//            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);
//
//            if (voiceChannel != null) {
//                AudioManager audioManager = guild.getAudioManager();
//                audioManager.openAudioConnection(voiceChannel);
//                event.getChannel().sendMessage("Connecté au canal vocal !").queue();
//                System.out.println("Le bot a rejoint le canal vocal.");
//            } else {
//                event.getChannel().sendMessage("Canal vocal introuvable.").queue();
//                System.out.println("Canal vocal introuvable.");
//            }
//        } catch (Exception e) {
//            event.getChannel().sendMessage("Erreur lors de la connexion au canal vocal.").queue();
//            e.printStackTrace();
//        }
//    }
//
//    private void leaveVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            AudioManager audioManager = guild.getAudioManager();
//
//            // Vérifie si le bot est connecté à un canal vocal
//            var connectedChannel = audioManager.getConnectedChannel();
//
//            if (connectedChannel != null) {
//                // Déconnexion propre
//                audioManager.closeAudioConnection();
//                event.getChannel().sendMessage("Déconnecté du canal vocal : " + connectedChannel.getName()).queue();
//                System.out.println("Le bot a quitté le canal vocal : " + connectedChannel.getName());
//
//                // Attente pour éviter des incohérences dues à la synchronisation
//                Thread.sleep(500);
//            } else {
//                // Si le bot n'est pas connecté à un canal vocal
//                event.getChannel().sendMessage("Le bot n'est connecté à aucun canal vocal.").queue();
//                System.out.println("Le bot n'était pas connecté à un canal vocal.");
//            }
//        } catch (Exception e) {
//            event.getChannel().sendMessage("Erreur lors de la déconnexion du canal vocal.").queue();
//            e.printStackTrace();
//        }
//    }





//    private void leaveVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            AudioManager audioManager = guild.getAudioManager();
//
//            // Vérifie si le bot est connecté à un canal vocal
//            if (audioManager.isConnected()) {
//                audioManager.closeAudioConnection(); // Déconnexion du bot
//                event.getChannel().sendMessage("Déconnecté du canal vocal !").queue();
//                System.out.println("Le bot a quitté le canal vocal.");
//            } else {
//                // Vérifie si le bot est dans un canal vocal via son état vocal
//                var selfMember = guild.getSelfMember(); // Récupère les informations du bot dans la guilde
//                var connectedChannel = selfMember.getVoiceState().getChannel(); // Vérifie s'il est connecté à un canal vocal
//
//                if (connectedChannel != null) {
//                    // Si le bot est dans un canal vocal, le déconnecter de force
//                    audioManager.closeAudioConnection();
//                    event.getChannel().sendMessage("Déconnecté du canal vocal (forcé) !").queue();
//                    System.out.println("Le bot a quitté le canal vocal (forcé).");
//                } else {
//                    // Si le bot n'est connecté nulle part
//                    event.getChannel().sendMessage("Le bot n'est pas connecté à un canal vocal.").queue();
//                    System.out.println("Le bot n'est pas connecté à un canal vocal.");
//                }
//            }
//        } catch (Exception e) {
//            event.getChannel().sendMessage("Erreur lors de la déconnexion du canal vocal.").queue();
//            e.printStackTrace();
//        }
//    }


//2222222222222222222
//    private void listUsersInVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
//            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);
//
//            if (voiceChannel != null) {
//                // Récupérer la liste des membres dans ce canal vocal
//                var membersInChannel = voiceChannel.getMembers();
//
//                // Si des membres sont présents
//                if (!membersInChannel.isEmpty()) {
//                    // Créer un tableau JSON pour stocker les données des utilisateurs
//                    JSONArray usersData = new JSONArray();
//
//                    // Pour chaque membre, obtenir les informations nécessaires
//                    for (var member : membersInChannel) {
//                        String userId = member.getId(); // ID de l'utilisateur
//                        String avatarUrl = member.getUser().getAvatarUrl(); // URL de l'avatar
//                        String username = member.getUser().getName(); // Nom d'utilisateur
//                        String discriminator = member.getUser().getDiscriminator(); // #XXXX
//                        String nickname = member.getNickname() != null ? member.getNickname() : username; // Surnom (ou nom d'utilisateur par défaut)
//
//                        // Créer un objet JSON pour chaque utilisateur
//                        JSONObject userJson = new JSONObject();
//                        userJson.put("id", userId);
//                        userJson.put("avatarUrl", avatarUrl);
//                        userJson.put("username", username + "#" + discriminator); // Nom d'utilisateur complet
//                        userJson.put("nickname", nickname); // Pseudonyme ou nom par défaut
//
//                        // Ajouter l'objet utilisateur au tableau
//                        usersData.put(userJson);
//                    }
//
//                    // Envoyer les données au programme via HTTP
//                    sendDataToProgram(usersData);
//                } else {
//                    System.out.println("Aucun utilisateur connecté au canal vocal.");
//                }
//            } else {
//                System.out.println("Canal vocal introuvable.");
//            }
//        } catch (Exception e) {
//            System.out.println("Erreur lors de la récupération des utilisateurs dans le canal vocal.");
//            e.printStackTrace();
//        }
//    }





//    private void listUsersInVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
//            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);
//
//            if (voiceChannel != null) {
//                // Récupérer la liste des membres dans ce canal vocal
//                var membersInChannel = voiceChannel.getMembers();
//
//                // Si des membres sont présents
//                if (!membersInChannel.isEmpty()) {
//                    // Créer un tableau JSON pour stocker les données des utilisateurs
//                    JSONArray usersData = new JSONArray();
//
//                    // Pour chaque membre, obtenir l'ID et l'avatar
//                    for (var member : membersInChannel) {
//                        String userId = member.getId(); // ID de l'utilisateur
//                        String avatarUrl = member.getUser().getAvatarUrl(); // URL de l'avatar
//
//                        // Créer un objet JSON pour chaque utilisateur
//                        JSONObject userJson = new JSONObject();
//                        userJson.put("id", userId);
//                        userJson.put("avatarUrl", avatarUrl);
//
//                        // Ajouter l'objet utilisateur au tableau
//                        usersData.put(userJson);
//                    }
//
//                    // Envoyer les données au programme via HTTP
//                    sendDataToProgram(usersData);
//                } else {
//                    System.out.println("Aucun utilisateur connecté au canal vocal.");
//                }
//            } else {
//                System.out.println("Canal vocal introuvable.");
//            }
//        } catch (Exception e) {
//            System.out.println("Erreur lors de la récupération des utilisateurs dans le canal vocal.");
//            e.printStackTrace();
//        }
//    }

//    private void listUsersInVoiceChannel(MessageReceivedEvent event) {
//        try {
//            Guild guild = event.getGuild();
//            String voiceChannelId = "1100158192345952378"; // ID du canal vocal
//            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelId);
//
//            if (voiceChannel != null) {
//                // Récupérer la liste des membres dans ce canal vocal
//                var membersInChannel = voiceChannel.getMembers();
//
//                // Si des membres sont présents
//                if (!membersInChannel.isEmpty()) {
//                    StringBuilder response = new StringBuilder("Utilisateurs connectés au canal vocal :\n");
//
//                    // Pour chaque membre, obtenir l'ID et l'avatar
//                    for (var member : membersInChannel) {
//                        String userId = member.getId(); // ID de l'utilisateur
//                        String avatarUrl = member.getUser().getAvatarUrl(); // URL de l'avatar
//
//                        // Ajouter l'ID et l'avatar à la réponse
//                        response.append("ID: ").append(userId)
//                                .append(" | Avatar: ").append(avatarUrl)
//                                .append("\n");
//                    }
//
//                    // Envoyer la liste des utilisateurs au canal texte
//                    event.getChannel().sendMessage(response.toString()).queue();
//                } else {
//                    event.getChannel().sendMessage("Aucun utilisateur connecté au canal vocal.").queue();
//                }
//            } else {
//                event.getChannel().sendMessage("Canal vocal introuvable.").queue();
//            }
//        } catch (Exception e) {
//            event.getChannel().sendMessage("Erreur lors de la récupération des utilisateurs dans le canal vocal.").queue();
//            e.printStackTrace();
//        }
//    }







//333333333333
//    // Méthode pour envoyer les données à ton programme via HTTP
//    private void sendDataToProgram(JSONArray usersData) {
//        try {
//            // URL du serveur HTTP de ton programme
//            URL url = new URL("http://localhost:8080/receiveData"); // Remplace par l'URL de ton programme
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            // Configurer la connexion HTTP
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "application/json");
//
//            // Envoyer les données sous forme de JSON
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = usersData.toString().getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//
//            // Lire la réponse du serveur
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
//                String responseLine;
//                StringBuilder response = new StringBuilder();
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine.trim());
//                }
//                System.out.println("Réponse du serveur: " + response.toString());
//            }
//        } catch (IOException e) {
//            System.out.println("Erreur lors de l'envoi des données au programme.");
//            e.printStackTrace();
//        }
//    }
//}