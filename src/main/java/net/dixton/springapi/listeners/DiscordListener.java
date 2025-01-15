package net.dixton.springapi.listeners;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.Platform;
import net.dixton.model.account.AccountLink;
import net.dixton.springapi.events.AccountLinkedEvent;
import net.dixton.springapi.events.AccountUnlinkedEvent;
import net.dixton.springapi.services.AccountLinkServiceImpl;
import net.dixton.springapi.services.DiscordService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DiscordListener extends ListenerAdapter {

    private final DiscordService discordService;
    private final AccountLinkServiceImpl accountLinkService;

    @EventListener
    public void onAccountLinked(AccountLinkedEvent event) {
        if (event.getAccountLink().getPlatform() == Platform.DISCORD) {
            this.discordService.setupAccount(event.getAccountLink().getAccount().getId());
        }
    }

    @EventListener
    public void onAccountUnlinked(AccountUnlinkedEvent event) {
        if (event.getAccountLink().getPlatform() == Platform.DISCORD) {
            this.discordService.kickMember(event.getAccountLink().getAccount().getId());
        }
    }

//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        if (event.getAuthor().isBot()) return;
//
//        String message = event.getMessage().getContentRaw();
//
//        if (message.equalsIgnoreCase("!dix")) {
//            event.getChannel().sendMessage("Olá! Eu sou o Dix, seu assistente. Como posso ajudar?").queue();
//        }
//    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        var member = event.getMember();

        AccountLink accountLink = new AccountLink(Platform.DISCORD, event.getUser().getId());
        String code = this.accountLinkService.generateLinkCode(accountLink).code();

        var category = guild.getCategoryById("1325527043798073385");
        if (category == null) {
            System.out.println("Categoria não encontrada com ID: 1325527043798073385");
            return;
        }

        guild.createTextChannel("registro-" + member.getEffectiveName())
                .setParent(category)
                .addPermissionOverride(guild.getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY), EnumSet.of(Permission.MESSAGE_SEND))
                .addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), EnumSet.noneOf(Permission.class))
                .queue(channel -> {
                    channel.sendMessage("Bem-vindo(a), " + event.getMember().getEffectiveName() + "!\n" +
                            "\n" +
                            "Para concluir a sincronização entre sua Conta Dixton e o Discord, utilize o comando no Minecraft:\n" +
                            "```/accountlink discord " + code + "```\n" +
                            ":warning: Atenção:\n" +
                            "    •    Este código é **individual** e só pode ser usado por você. Não compartilhe!\n" +
                            "    •    O código expira em **5 minutos**, então seja rápido para usá-lo! \n" +
                            "\n" +
                            "Estamos ansiosos para te ver no servidor! :rocket:").queue();

                    channel.delete().queueAfter(5, TimeUnit.MINUTES);
                });
    }
}