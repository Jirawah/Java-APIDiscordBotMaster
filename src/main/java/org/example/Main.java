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

            System.out.println("Bot démarré et prêt !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}