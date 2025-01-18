package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    private static JDA jda;

    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault("MTMyOTg0MDE4NzE1NjU5ODgxNw.GSk5BI.SQFEkRxb4gm0X9qCOtf6TgZsJaJlIF3-JB-D50")
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build()
                    .awaitReady();

            addListenerIfAbsent(jda, new BotListener());

            // Ajout du hook de shutdown pour fermer proprement à la fin
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdownBot();
            }));

            System.out.println("Bot démarré !");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage du bot.");
            e.printStackTrace();
        }
    }

    private static void shutdownBot() {
        try {
            // Vérification de l'instance 'jda' avant de procéder au shutdown
            if (jda != null && jda.getStatus().equals(JDA.Status.CONNECTED)) {
                // Fermeture des connexions vocales d'abord
                jda.getGuilds().forEach(guild -> {
                    var audioManager = guild.getAudioManager();
                    if (audioManager.isConnected()) {  // Vérifie si le bot est connecté à un canal vocal
                        audioManager.closeAudioConnection();  // Déconnecte proprement
                        System.out.println("Le bot a quitté le canal vocal.");
                    }
                });

                // Après avoir quitté tous les canaux vocaux, on ferme la connexion avec Discord
                jda.shutdown();  // Ferme proprement la connexion avec Discord
                System.out.println("Le bot s'est déconnecté proprement.");
            } else {
                System.out.println("Le bot n'était pas correctement initialisé ou n'est pas connecté.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la fermeture du bot.");
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si un listener est déjà présent, et l'ajoute seulement s'il est absent.
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
//111111111111111
//package org.example;
//
//import net.dv8tion.jda.api.JDABuilder;
//import net.dv8tion.jda.api.JDA;
//import net.dv8tion.jda.api.hooks.EventListener;
//import net.dv8tion.jda.api.requests.GatewayIntent;
//
//public class Main {
//    private static JDA jda;
//
//    public static void main(String[] args) {
//        try {
//            jda = JDABuilder.createDefault("TON_TOKEN_DISCORD")
//                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
//                    .build()
//                    .awaitReady();
//
//            addListenerIfAbsent(jda, new BotListener());
//
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                shutdownBot();
//            }));
//
//            System.out.println("Bot démarré !");
//        } catch (Exception e) {
//            System.out.println("Erreur lors du démarrage du bot.");
//            e.printStackTrace();
//        }
//    }
//
//    private static void shutdownBot() {
//        try {
//            if (jda != null) {
//                jda.getGuilds().forEach(guild -> {
//                    var audioManager = guild.getAudioManager();
//                    if (audioManager.isConnected()) {  // Vérifie si le bot est connecté à un canal vocal
//                        audioManager.closeAudioConnection();  // Déconnecte proprement
//                        System.out.println("Le bot a quitté le canal vocal.");
//                    }
//                });
//                jda.shutdown();  // Ferme proprement la connexion avec Discord
//                System.out.println("Le bot s'est déconnecté proprement.");
//            }
//        } catch (Exception e) {
//            System.out.println("Erreur lors de la fermeture du bot.");
//            e.printStackTrace();
//        }
//    }



//public class Main {
//    public static void main(String[] args) {
//        try {
//            var jda = JDABuilder.createDefault("MTMyOTg0MDE4NzE1NjU5ODgxNw.GSk5BI.SQFEkRxb4gm0X9qCOtf6TgZsJaJlIF3-JB-D50")
//                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Activer l'intention MESSAGE_CONTENT
//                    .build()
//                    .awaitReady();
//
//            // Vérifie et ajoute le listener
//            addListenerIfAbsent(jda, new BotListener());
//
//            // Ajout d'un hook pour fermer proprement la connexion
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    if (jda != null) {
//                        jda.shutdown(); // Ferme proprement la connexion avec Discord
//                        System.out.println("Le bot s'est déconnecté proprement.");
//                    }
//                } catch (Exception e) {
//                    System.out.println("Erreur lors de la fermeture du bot.");
//                    e.printStackTrace();
//                }
//            }));
//
//            System.out.println("Bot démarré !");
//        } catch (Exception e) {
//            System.out.println("Erreur lors du démarrage du bot.");
//            e.printStackTrace();
//        }
//    }

//222222222222222
//    /**
//     * Vérifie si un listener est déjà présent, et l'ajoute seulement s'il est absent.
//     *
//     * @param jda      L'instance de JDA
//     * @param listener Le listener à ajouter
//     */
//    private static void addListenerIfAbsent(JDA jda, EventListener listener) {
//        boolean isAlreadyRegistered = jda.getRegisteredListeners().stream()
//                .anyMatch(existingListener -> existingListener.getClass().equals(listener.getClass()));
//
//        if (!isAlreadyRegistered) {
//            jda.addEventListener(listener);
//            System.out.println("Listener ajouté : " + listener.getClass().getSimpleName());
//        } else {
//            System.out.println("Listener déjà enregistré : " + listener.getClass().getSimpleName());
//        }
//    }
//}









//package org.example;
//
//import net.dv8tion.jda.api.JDABuilder;
//import net.dv8tion.jda.api.requests.GatewayIntent;
//


// First and second try
//public class Main {
//    public static void main(String[] args) {
//        try {
//            var jda = JDABuilder.createDefault("MTMyOTg0MDE4NzE1NjU5ODgxNw.GSk5BI.SQFEkRxb4gm0X9qCOtf6TgZsJaJlIF3-JB-D50")
//                    .addEventListeners(new BotListener()) // Enregistrement de l'écouteur
//                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Activer l'intention MESSAGE_CONTENT
//                    .build()
//                    .awaitReady();
//


// Second try
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    if (jda != null) {
//                        jda.shutdown(); // Ferme proprement la connexion avec Discord
//                        System.out.println("Le bot s'est déconnecté proprement.");
//                    }
//                } catch (Exception e) {
//                    System.out.println("Erreur lors de la fermeture du bot.");
//                    e.printStackTrace();
//                }
//            }));
//
//            System.out.println("Bot démarré !");
//        } catch (Exception e) {
//            System.out.println("Erreur lors du démarrage du bot.");
//            e.printStackTrace();
//        }
//    }
//}


// First try
//            System.out.println("Bot démarré et prêt !");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}