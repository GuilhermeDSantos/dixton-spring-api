//package net.dixton.springapi.services;
//
//import lombok.RequiredArgsConstructor;
//import net.dixton.dtos.CreatePlayerDto;
//import net.dixton.exceptions.DixtonRuntimeException;
//import net.dixton.exceptions.player.PlayerNotFoundException;
//import net.dixton.exceptions.server.ServerMustBeBukkitException;
//import net.dixton.model.account.Account;
//import net.dixton.model.player.Player;
//import net.dixton.model.player.types.lobby.PlayerMainLobby;
//import net.dixton.model.player.types.rankup.PlayerRankUpMedieval;
//import net.dixton.model.server.Server;
//import net.dixton.model.server.ServerBukkit;
//import net.dixton.model.server.types.rankup.utils.RankUpRank;
//import net.dixton.services.PlayerService;
//import net.dixton.springapi.repositories.player.PlayerMainLobbyRepository;
//import net.dixton.springapi.repositories.player.PlayerRankUpMedievalRepository;
//import net.dixton.springapi.repositories.player.PlayerRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class PlayerServiceImpl implements PlayerService {
//
//    private final ServerServiceImpl serverService;
//    private final AccountServiceImpl accountService;
//    private final PlayerMainLobbyRepository playerMainLobbyRepository;
//    private final PlayerRankUpMedievalRepository playerRankUpMedievalRepository;
//
//    @Override
//    public Player create(CreatePlayerDto createPlayerDto) {
//        Server server = this.serverService.findById(createPlayerDto.serverId());
//
//        if (server.isBukkit()) {
//            Account account = null;
//            account = this.accountService.findByUniqueId(createPlayerDto.playerUniqueId());
//
//            ServerBukkit serverBukkit = (ServerBukkit) server;
//            Player player = null;
//
//            switch (serverBukkit.getType()) {
//                case MAIN_LOBBY -> player = playerMainLobbyRepository.save(new PlayerMainLobby(account, false));
//
//                case GAME -> {
//                    switch (serverBukkit.getGameMode()) {
//                        case RANK_UP -> {
//                            switch (serverBukkit.getName()) {
//                                case "RANK_UP_MEDIEVAL" -> player = playerRankUpMedievalRepository.save(new PlayerRankUpMedieval(account, new RankUpRank(1L)));
//                            }
//                        }
//                    }
//                }
//            }
//
//            return player;
//        }
//
//        throw new DixtonRuntimeException(new ServerMustBeBukkitException());
//    }
//
//    @Override
//    public Player findByUniqueIdAndServer(UUID uniqueId, Long serverId) {
//        Optional<? extends Player> playerOptional = Optional.empty();
//        Server server = this.serverService.findById(serverId);
//
//        if (server.isBukkit()) {
//            ServerBukkit serverBukkit = (ServerBukkit) server;
//
//            switch (serverBukkit.getType()) {
//                case MAIN_LOBBY -> playerOptional = playerMainLobbyRepository.findByAccountUniqueId(uniqueId);
//
//                case GAME -> {
//                    switch (serverBukkit.getGameMode()) {
//                        case RANK_UP -> {
//                            switch (serverBukkit.getName()) {
//                                case "RANK_UP_MEDIEVAL" -> playerOptional = playerRankUpMedievalRepository.findByAccountUniqueId(uniqueId);
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (playerOptional.isPresent()) {
//                return playerOptional.get();
//            }
//
//            throw new DixtonRuntimeException(new PlayerNotFoundException(uniqueId, serverId));
//        }
//
//        throw new DixtonRuntimeException(new ServerMustBeBukkitException());
//    }
//
//    public Player update(Player updatedPlayer) {
//        if (updatedPlayer instanceof PlayerMainLobby updatedMainLobby) {
//            return updatePlayer(playerMainLobbyRepository, updatedMainLobby);
//        } else if (updatedPlayer instanceof PlayerRankUpMedieval updatedRankUpMedieval) {
//            return updatePlayer(playerRankUpMedievalRepository, updatedRankUpMedieval);
//        }
//
//        throw new IllegalArgumentException("Unsupported player type: " + updatedPlayer.getClass().getSimpleName());
//    }
//
//    private <T extends Player> T updatePlayer(PlayerRepository<T> repository, T updatedPlayer) {
//        T existingPlayer = repository.findById(updatedPlayer.getId())
//                .orElseThrow(() -> new PlayerNotFoundException(updatedPlayer.getId()));
//
//        existingPlayer.updateFrom(updatedPlayer);
//
//        return repository.save(existingPlayer);
//    }
//}
