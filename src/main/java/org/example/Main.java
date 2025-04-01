package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Classe principale qui initialise et lance le bot Discord.
 */
public class Main {
    private static JDA jda;

    public static void main(String[] args) {
        try {
            // Chargement des propriétés de config.properties (données sensibles)
            Properties props = new Properties();
            props.load(new FileInputStream("src/main/resources/config.properties"));

            // Récupération du token du bot
            String token = props.getProperty("discord.bot.token");

            // Initialisation de JDA avec le token et activation des intents nécessaires
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build()
                    .awaitReady();

            // Ajout du listener principal si non enregistré
            addListenerIfAbsent(jda, new BotListener());

            // Hook d'arrêt en cas de fermeture du programme
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdownBot();
            }));

            System.out.println("Bot démarré !");
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement du fichier de configuration.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage du bot.");
            e.printStackTrace();
        }
    }

    /**
     * Ferme la connexion du bot au canal vocal du serveur Discord.
     */
    private static void shutdownBot() {
        try {
            if (jda != null && jda.getStatus().equals(JDA.Status.CONNECTED)) {
                // Déconnexion du vocal
                jda.getGuilds().forEach(guild -> {
                    var audioManager = guild.getAudioManager();
                    if (audioManager.isConnected()) {
                        audioManager.closeAudioConnection();
                        System.out.println("Le bot a quitté le canal vocal.");
                    }
                });

                // Déconnexion complète de l'API Discord
                jda.shutdown();
                System.out.println("Le bot s'est déconnecté.");
            } else {
                System.out.println("Le bot n'était pas correctement initialisé ou n'est pas connecté.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la fermeture du bot.");
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un listener à JDA uniquement s'il n'est pas déjà enregistré.
     *
     * @param jda      L'instance de JDA
     * @param listener Le listener à ajouter
     */
    private static void addListenerIfAbsent(JDA jda, EventListener listener) {
        boolean isAlreadyRegistered = jda.getRegisteredListeners().stream()
                .anyMatch(existingListener -> existingListener.getClass().equals(listener.getClass()));

        if (!isAlreadyRegistered) {
            jda.addEventListener(listener);
            System.out.println("Listener ajouté : " + listener.getClass().getSimpleName());
        } else {
            System.out.println("Listener déjà enregistré : " + listener.getClass().getSimpleName());
        }
    }
}