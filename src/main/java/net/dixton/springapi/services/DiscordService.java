package net.dixton.springapi.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dixton.enums.Platform;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.discord.DiscordChannelNotFoundException;
import net.dixton.model.account.Account;
import net.dixton.springapi.listeners.DiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscordService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

    private final JDA jda;
    private final AccountServiceImpl accountService;
    private final AccountLinkServiceImpl accountLinkService;

    private static final String GUILD_ID = "1315072883839598632";

    @PostConstruct
    public void init() {
        jda.addEventListener(new DiscordListener(this, accountLinkService));
    }

    public void setupAccount(Long accountId) {
        Guild guild = jda.getGuildById(GUILD_ID);

        if (guild == null) {
            throw new DixtonRuntimeException(new DiscordChannelNotFoundException());
        }

        Account account = this.accountService.findById(accountId);
        guild.retrieveMemberById(account.getLinkAccounts().get(Platform.DISCORD).getLink())
                .queue(member -> {
                    /* Nickname */
                    guild.modifyNickname(member, account.getNick()).queue();

                    /* Role */
                    Role role = guild.getRoleById(account.getRole().getDiscordId());
                    assert role != null;
                    guild.addRoleToMember(member, role).queue(
                            success -> System.out.println("Cargo atribuído com sucesso!"),
                            error -> System.err.println("Erro ao atribuir o cargo: " + error.getMessage())
                    );

                    if (account.getRole() != net.dixton.enums.Role.MEMBER) {
                        role = guild.getRoleById(net.dixton.enums.Role.MEMBER.getDiscordId());
                        assert role != null;
                        guild.addRoleToMember(member, role).queue(
                                success -> System.out.println("Cargo atribuído com sucesso!"),
                                error -> System.err.println("Erro ao atribuir o cargo: " + error.getMessage())
                        );
                    }
                }, error -> {
                    throw new DixtonRuntimeException(new DiscordChannelNotFoundException());
                });
    }

    public void kickMember(Long accountId) {
        Guild guild = jda.getGuildById(GUILD_ID);

        if (guild == null) {
            throw new DixtonRuntimeException(new DiscordChannelNotFoundException());
        }

        Account account = this.accountService.findById(accountId);
        guild.retrieveMemberById(account.getLinkAccounts().get(Platform.DISCORD).getLink())
                .queue(member -> {
                    guild.kick(member).queue();
                }, error -> {
                    throw new DixtonRuntimeException(new DiscordChannelNotFoundException());
                });
    }

    public void createInvite() {
        TextChannel channel = jda.getTextChannelById("1325527161461018686");
        if (channel == null) {
            System.out.println("not found channel with 1325527161461018686");
        }

        assert channel != null;
        channel.createInvite()
                .setMaxAge(0)  // O convite nunca expira
                .setMaxUses(0) // O convite pode ser usado infinitamente
                .setUnique(false) // Permite reutilizar o mesmo link
                .queue(invite -> {
                    System.out.println("Invite URL: " + invite.getUrl());
                });
    }

    public Guild getGuild() {
        return jda.getGuildById(GUILD_ID);
    }

//
//    public CompletableFuture<DiscordInvite> createInvite(Long accountId) {
//        try {
//            String channelId = "1307444204884201475";
//            Account account = this.accountService.findById(accountId);
//
//            if (account.getLinkAccounts().get(AccountLinkType.DISCORD) != null) {
//                throw new DiscordAccountAlreadyLinkedException(accountId);
//            }
//
//            if (accountInviteMap.containsKey(accountId) && accountInviteMap.get(accountId).isActive() && !accountInviteMap.get(accountId).isUsed()) {
//                return CompletableFuture.completedFuture(accountInviteMap.get(accountId));
//            }
//
//            TextChannel channel = jda.getTextChannelById(channelId);
//            if (channel == null) {
//                throw new DiscordChannelNotFoundException(channelId);
//            }
//
//            CompletableFuture<DiscordInvite> future = new CompletableFuture<>();
//
//            channel.createInvite()
//                    .setMaxAge(300)
//                    .setMaxUses(2)
//                    .setUnique(true)
//                    .queue(invite -> {
//                        DiscordInvite discordInvite = new DiscordInvite(
//                                invite.getUrl(),
//                                0,
//                                account,
//                                Instant.now().plus(Duration.ofMinutes(5))
//                        );
//
//                        inviteMap.put(extractInviteCode(invite.getUrl()), discordInvite);
//                        accountInviteMap.put(accountId, discordInvite);
//
//                        future.complete(discordInvite);
//                    }, future::completeExceptionally);
//
//            return future;
//        } catch (DixtonException exception) {
//            throw new DixtonRuntimeException(exception);
//        }
//    }
//
//    public void deleteInvite(String inviteCode) {
//        DiscordInvite discordInvite = null;
//        try {
//            discordInvite = this.getInvite(inviteCode);
//        } catch (NotFoundException exception) {
//            throw new DixtonRuntimeException(exception);
//        }
//
//        this.accountInviteMap.remove(discordInvite.getAccount().getId());
//        this.inviteMap.remove(inviteCode);
//
//        logger.info("Invite {} has been deleted", inviteCode);
//    }
//
//    public String generate2FACode() {
//        SecureRandom random = new SecureRandom();
//        int code = 100000 + random.nextInt(900000);
//        return String.valueOf(code);
//    }
//
//    public void linkAccount(Account account, String discordId) {
//        AccountLink discordLink = new AccountLink(account, AccountLinkType.DISCORD, discordId);
//        account.getLinkAccounts().put(AccountLinkType.DISCORD, discordLink);
//
//        try {
//            this.accountService.update(account.getId(), account);
//        } catch (AccountNotFoundException e) {
//            throw new DixtonRuntimeException(e);
//        }
//    }
//
//    public DiscordInvite getInvite(String code) throws NotFoundException {
//        if (!inviteMap.containsKey(code)) {
//            throw new NotFoundException("Could not find invite with code " + code);
//        }
//        return inviteMap.get(code);
//    }
//
//    private String extractInviteCode(String url) {
//        Pattern pattern = Pattern.compile("discord\\.gg/([a-zA-Z0-9]+)");
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        throw new IllegalArgumentException("URL inválida");
//    }
}
