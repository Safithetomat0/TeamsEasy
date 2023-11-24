package org.safi.teamsmod.teamseasy1.util;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.*;
import java.util.*;

public class CreateTeamCommand {

    private static final long COOLDOWN_TIME = 100; //30000; // 30 seconds cooldown
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<Integer, Team> teams = new HashMap<>();
    private static final Map<UUID, Integer> playerTeams = new HashMap<>();
    private static final Set<String> createdTeamNames = new HashSet<>();
    private static final int MAX_TEAM_ID = 50;
    private static int nextTeamId = 1;
    private static final File DATA_FOLDER = new File("world/data"); // Adjust the path as needed
    private static final File TEAMS_FILE = new File(DATA_FOLDER, "teams.txt");
    private static final Map<String, UUID> teamLeaders = new HashMap<>();


    public static void register() {


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Command to create a team
            dispatcher.register(CommandManager
                    .literal("teamcreate")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(CreateTeamCommand::createTeam)
                    .requires(source -> source.hasPermissionLevel(0)));

            dispatcher.register(CommandManager
                    .literal("teamremoveall")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(CreateTeamCommand::removeAllTeams)
                    .requires(source -> source.hasPermissionLevel(2)));
            dispatcher.register(CommandManager
                    .literal("teamsinfo")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(CreateTeamCommand::readTeams)
                    .requires(source -> source.hasPermissionLevel(0)));

            // Command to disband teams
            dispatcher.register(CommandManager
                    .literal("teamdisband")
                    .requires(source -> source.hasPermissionLevel(0))
                    .executes(CreateTeamCommand::disbandTeam)
                    .requires(source -> source.hasPermissionLevel(0)));
            /**
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
        Random random = new Random();

        if (nextTeamId > MAX_TEAM_ID) {
            player.sendMessage(Text.literal("Error: Unable to find an available team name."), true);
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        assert player != null;

        // Check cooldown
        if (cooldowns.containsKey(player.getUuid())) {
            long lastTime = cooldowns.get(player.getUuid());
            long elapsedTime = currentTime - lastTime;

            if (elapsedTime < COOLDOWN_TIME) {
                long remainingTime = COOLDOWN_TIME - elapsedTime;
                player.sendMessage(Text.literal("You must wait " + (remainingTime / 1000) + " seconds before creating another team."), true);
                return 0;
            }
        }

        // Try to create a team with the current nextTeamId
        String teamName = String.valueOf(nextTeamId);

        // Make sure team doesn't exist already
        populateCreatedTeamNames(context);
        while (createdTeamNames.contains(teamName)) {
            // Team name already exists, navigate to the next team number
            nextTeamId++;
            // Check if nextTeamId exceeds the limit
            if (nextTeamId > MAX_TEAM_ID) {
                nextTeamId = 1;
                player.sendMessage(Text.literal("Error: Unable to find an available team name."), true);
                return 0;
            }
            player.sendMessage(Text.literal("try again"), true);
            teamName = String.valueOf(nextTeamId);
        }

        // Create team
        Team team = scoreboard.addTeam(teamName);
        // team settings
        team.setFriendlyFireAllowed(false);

        // Create function
        scoreboard.addPlayerToTeam(player.getEntityName(), team);
        teams.put(nextTeamId, team);
        playerTeams.put(player.getUuid(), nextTeamId);

        // Update cooldown
        cooldowns.put(player.getUuid(), currentTime);

        // Set team color for players
        Formatting teamColor = Formatting.values()[random.nextInt(Formatting.values().length)];
        Style teamStyle = Style.EMPTY.withColor(teamColor);

        // Set team color
        team.setColor(teamColor);

        // Display the team name in a random color
        Text teamMessage = Text.literal("Team " + teamName + " created.").setStyle(teamStyle);
        player.sendMessage(teamMessage, false);

        // Save the created team name to the file
        createdTeamNames.add(teamName);

        UUID leaderUuid = player.getUuid();  // UUID of the player who created the team
        teamLeaders.put(teamName, leaderUuid);  // Store the leader UUID for the team

        return 1;
    }

    private static int removeAllTeams(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Scoreboard scoreboard = source.getServer().getScoreboard();
        ServerPlayerEntity player = source.getPlayer();

        List<Team> teamsToRemove = new ArrayList<>();
        teamsToRemove.addAll(scoreboard.getTeams());

        for (Team team : teamsToRemove) {
            scoreboard.removeTeam(team);
        }
        nextTeamId = 1;
        assert player != null;
        player.sendMessage(Text.literal("All teams removed!").formatted(Formatting.GREEN), true);
        return 1;
    }

    private static int readTeams(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        try (BufferedReader reader = new BufferedReader(new FileReader(TEAMS_FILE))) {
            String line;
            assert player != null;
            player.sendMessage(Text.literal("Teams in teams.txt:").formatted(Formatting.YELLOW));
            while ((line = reader.readLine()) != null) {
                player.sendMessage(Text.literal(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1;
    }

    private static int disbandTeam(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayer();
        Scoreboard scoreboard = source.getServer().getScoreboard();

        // Ensure that the player is in a team
        if (player.getScoreboardTeam() == null) {
            player.sendMessage(Text.literal("You are not in a team."), true);
            return 0;
        }

        Team playerTeam = scoreboard.getTeam(player.getScoreboardTeam().getName());

        // Get the team name
        String teamName = playerTeam.getName();

        // Check if the player is the leader of the team
        UUID leaderUuid = teamLeaders.get(teamName);
        if (leaderUuid == null || !leaderUuid.equals(player.getUuid())) {
            player.sendMessage(Text.literal("You are not the leader of this team."), true);
            return 0;
        }

        // Remove the team from the scoreboard
        scoreboard.removeTeam(playerTeam);

        // Remove the team name from the set
        createdTeamNames.remove(teamName);

        // Additional clean-up if necessary (remove player from associated data structures)
        teams.remove(teamName);
        playerTeams.remove(player.getUuid());
        teamLeaders.remove(teamName);

        player.sendMessage(Text.literal("Team " + teamName + " disbanded."), false);
        return 1;
    }


    private static void populateCreatedTeamNames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Scoreboard scoreboard = source.getServer().getScoreboard();
        createdTeamNames.clear(); // Clear existing entries

        // Iterate over teams in the scoreboard and add their names to the set
        for (Team team : scoreboard.getTeams()) {
            createdTeamNames.add(team.getName());
        }
    }

}