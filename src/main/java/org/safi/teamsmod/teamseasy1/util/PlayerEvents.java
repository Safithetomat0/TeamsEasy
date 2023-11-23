package org.safi.teamsmod.teamseasy1.util;

import net.fabricmc.fabric.api.event.AutoInvokingEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class PlayerEvents {
    @AutoInvokingEvent
    public void onPlayerJoin(MinecraftServer event) {
        // Get the player who joined
        PlayerEntity player = event.getPlayerManager().getPlayer(event.getName());

        // Get the team of the player
        Team playerTeam = getPlayersTeam(player);

        // Set the display name to include the team name
        assert player != null;
        player.setCustomName(Text.literal(playerTeam.getName() + " " + player.getName()));
    }

    private Team getPlayersTeam(PlayerEntity player) {
        // Implement logic to get the team of the player
        // (You might want to store teams in a map for quick lookup)
        // For now, let's assume the player has a team
        return new Team("TeamName", TextColor.fromRgb(0xFF0000), player);
    }
}
