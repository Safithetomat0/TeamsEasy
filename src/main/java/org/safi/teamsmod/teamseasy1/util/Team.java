package org.safi.teamsmod.teamseasy1.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.text.TextColor;
import net.minecraft.entity.player.PlayerEntity;

public class Team {
    private String name;
    private TextColor color;
    private PlayerEntity leader;
    private List<PlayerEntity> members;

    public Team(String name, TextColor color, PlayerEntity leader) {
        this.name = name;
        this.color = color;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
    }

    public String getName() {
        return name;
    }

    public TextColor getColor() {
        return color;
    }

    public PlayerEntity getLeader() {
        return leader;
    }

    public List<PlayerEntity> getMembers() {
        return members;
    }

    // Add more methods to manipulate the team data
}
