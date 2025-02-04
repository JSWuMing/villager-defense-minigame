package me.theguyhere.villagerdefense.plugin.events;

import me.theguyhere.villagerdefense.plugin.game.models.arenas.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignGUIEvent extends Event implements Cancellable {
    private final Arena arena;
    private final Player player;
    private final String[] lines;
    private boolean isCancelled;
    private static final HandlerList HANDLERS = new HandlerList();

    public SignGUIEvent(Arena arena, Player player, String... args) {
        this.arena = arena;
        this.player = player;
        this.lines = args;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public String[] getLines() {
        return lines;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
