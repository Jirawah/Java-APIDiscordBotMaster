package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) {
        try {
            var jda = JDABuilder.createDefault("MTMyOTg0MDE4NzE1NjU5ODgxNw.GSk5BI.SQFEkRxb4gm0X9qCOtf6TgZsJaJlIF3-JB-D50")
                    .addEventListeners(new BotListener()) // Enregistrement de l'écouteur
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Activer l'intention MESSAGE_CONTENT
                    .build()
                    .awaitReady();






            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (jda != null) {
                        jda.shutdown(); // Ferme proprement la connexion avec Discord
                        System.out.println("Le bot s'est déconnecté proprement.");
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de la fermeture du bot.");
                    e.printStackTrace();
                }
            }));

            System.out.println("Bot démarré !");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage du bot.");
            e.printStackTrace();
        }
    }
}
//            System.out.println("Bot démarré et prêt !");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}