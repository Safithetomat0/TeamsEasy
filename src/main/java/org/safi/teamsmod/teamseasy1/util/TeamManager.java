package org.safi.teamsmod.teamseasy1.util;

import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TextColor;

import java.util.HashMap;
import java.util.Map;

public class TeamManager {
    private static Map<PlayerEntity, Team> playerTeams = new HashMap<>();

    public static void invitePlayer(PlayerEntity inviter, PlayerEntity invitee) {
        // Implement logic to send invitation
        // For simplicity, let's assume the invitation is accepted
        acceptInvitation(invitee);
    }

    public static void acceptInvitation(PlayerEntity player) {
        // Implement logic to accept invitation
        // For simplicity, let's assume the player joins the team
        Team team = new Team("TeamName", TextColor.fromRgb(0xFF0000), player);
        playerTeams.put(player, team);
    }

    public static void declineInvitation(PlayerEntity player) {
        // Implement logic to decline invitation
        // For simplicity, let's assume nothing happens
    }

    public static void leaveTeam(PlayerEntity player) {
        // Implement logic to leave team
        // For simplicity, let's assume the player leaves the team
        playerTeams.remove(player);
    }

    public static void disbandTeam(PlayerEntity leader) {
        // Implement logic to disband team
        // For simplicity, let's assume the team is disbanded
        Team team = playerTeams.get(leader);
        if (team != null) {
            playerTeams.remove(leader);
        }
    }

    public static void checkEmptyTeams() {
        // Implement logic to check and delete empty teams
        // For simplicity, let's assume all teams are checked and deleted
        playerTeams.entrySet().removeIf(entry -> entry.getValue().getMembers().isEmpty());
    }
}
