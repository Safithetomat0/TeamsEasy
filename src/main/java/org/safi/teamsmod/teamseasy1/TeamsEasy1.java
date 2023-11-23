package org.safi.teamsmod.teamseasy1;

import net.fabricmc.api.ModInitializer;
import org.safi.teamsmod.teamseasy1.util.CreateTeamCommand;

public class TeamsEasy1 implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        CreateTeamCommand.register();
    }
}
