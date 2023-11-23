package org.safi.teamsmod.teamseasy1.util;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateTeamCommand {

    private static final long COOLDOWN_TIME = 30000; // 30 seconds cooldown
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<Integer, Team> teams = new HashMap<Integer, net.minecraft.scoreboard.Team>();
    private static final Map<UUID, Integer> playerTeams = new HashMap<>();
    private static int nextTeamId = 1;


    public static void register() {


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Command to create a team
            dispatcher.register(CommandManager
                    .literal("teamcreate")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(CreateTeamCommand::createTeam)
                    .requires(source -> source.hasPermissionLevel(0)));

            /*
            // Command to remove empty teams
            dispatcher.register(CommandManager
                    .literal("teamdisband")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(TeamCommands::removeEmptyTeams)
                    .requires(source -> source.hasPermissionLevel(0)));

            // Command to invite a player to a team
            dispatcher.register(CommandManager
                    .literal("teaminvite")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("player", EntityArgumentType.entity())
                            .requires(source -> source.hasPermissionLevel(0))
                            .executes(context -> {
                                return invitePlayer(context, EntityArgumentType.getEntity(context, "player").getEntityName());
                            }).requires(source -> source.hasPermissionLevel(0))));

            // Command for a player to accept an invitation
            dispatcher.register(CommandManager
                    .literal("teamaccept")
                    .requires(source -> source.hasPermissionLevel(0))
                    .then(CommandManager.argument("team", IntegerArgumentType.integer())
                            .executes(context -> {
                                return acceptInvitation(context, IntegerArgumentType.getInteger(context, "team"));
                            }).requires(source -> source.hasPermissionLevel(0))));

            // Command for a player to leave a team
            dispatcher.register(CommandManager
                    .literal("teamleave")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(TeamCommands::leaveTeam)
                    .requires(source -> source.hasPermissionLevel(0)));

             */
        });
    }

    private static int createTeam(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayer();
        Scoreboard scoreboard = source.getServer().getScoreboard();
        String teamName = String.valueOf(nextTeamId);

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUuid())) {
            long lastTime = cooldowns.get(player.getUuid());
            long elapsedTime = currentTime - lastTime;

            if (elapsedTime < COOLDOWN_TIME) {
                long remainingTime = COOLDOWN_TIME - elapsedTime;
                source.sendError(Text.literal("You must wait " + (remainingTime / 1000) + " seconds before creating another team."));
                return 0;
            }
        }

        Team team = scoreboard.addTeam(teamName);
        // team settings
        team.setFriendlyFireAllowed(false);

        // Make sure team doesn't exist already
        if (scoreboard.getTeam(teamName) != null) {
            nextTeamId++;
            source.sendError(Text.literal("Try again until it works, technical issue."));
            // result
            return 0;
        } else {
            // create function
            scoreboard.addPlayerToTeam(player.getEntityName(), team);
            teams.put(nextTeamId, team);
            playerTeams.put(player.getUuid(), nextTeamId);
            nextTeamId++;

            // Update cooldown
            cooldowns.put(player.getUuid(), currentTime);

            // Set team color for players

            source.sendMessage(Text.literal("Team " + teamName + " created.").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            // result
            return 1;
        }
    }
}