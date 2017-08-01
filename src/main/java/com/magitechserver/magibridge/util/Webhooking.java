package com.magitechserver.magibridge.util;

import com.magitechserver.magibridge.MagiBridge;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.Webhook;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Frani on 15/07/2017.
 */
public class Webhooking {

    public static void sendWebhookMessage(String hook, String player, String message, String channelID) {

        TextChannel channel = MagiBridge.jda.getTextChannelById(channelID);
        if(!channel.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            MagiBridge.logger.error("The bot does not have the MANAGE WEBHOOKS permission, so it can't create webhooks!");
            MagiBridge.logger.error("Please give it or disable the use-webhooks feature!");
            return;
        }

        Webhook webhook = channel.getGuild()
            .getWebhooks().complete().stream().filter(wh ->
                        wh.getName().equals("MB: " + channel.getName())).findFirst().orElse(null);

        if(webhook == null) {
            webhook = channel.getGuild().getController().createWebhook(channel, "MB: " + channel.getName()).complete();
        }

        String content = message.replaceAll("&([0-9a-fA-FlLkKrR])", "");
        List<String> usersMentioned = new ArrayList<>();
        Arrays.stream(content.split(" ")).filter(word ->
                word.startsWith("@")).forEach(mention ->
                    usersMentioned.add(mention.substring(1)));

        if(!usersMentioned.isEmpty()) {
            for (String user : usersMentioned) {
                List<User> users = MagiBridge.jda.getUsersByName(user, true);
                if(!users.isEmpty()) {
                    content = content.replaceAll("@" + user, "<@" + users.get(0).getId() + ">");
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("content", content);
        json.put("username", hook.replaceAll("&([0-9a-fA-FlLkKrR])", ""));
        json.put("avatar_url", MagiBridge.getConfig().getString("messages", "webhook-picture-url").replace("%player%", player));
        try {
            Unirest.post(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asJsonAsync();
        } catch (Exception e) {
            MagiBridge.logger.error("Error delivering Webhook request: ");
            e.printStackTrace();
        }
    }

}
