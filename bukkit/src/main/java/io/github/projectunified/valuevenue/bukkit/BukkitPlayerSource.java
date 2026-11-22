package io.github.projectunified.valuevenue.bukkit;

import io.github.projectunified.valuevenue.core.ValueSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are the players of the server.
 * <p>
 * {@code <key>} resolves to the name of the player, and {@code <key>_<variable>} is delegated to
 * {@link #replaceVariable(String, OfflinePlayer)}, which returns null by default. Override it to resolve the player
 * variables of the owner, calling the default for the rest.
 */
public class BukkitPlayerSource implements ValueSource<UUID, OfflinePlayer> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_player";

    private final String key;
    private final String prefix;
    private final boolean viewSelf;
    private final boolean viewOffline;
    private final Predicate<OfflinePlayer> filter;

    /**
     * Creates the stream with {@link #DEFAULT_KEY}.
     *
     * @param viewSelf    whether the player viewing the list may see themselves
     * @param viewOffline whether offline players are included
     */
    public BukkitPlayerSource(boolean viewSelf, boolean viewOffline) {
        this(DEFAULT_KEY, viewSelf, viewOffline, null);
    }

    /**
     * Creates the stream with {@link #DEFAULT_KEY} and a filter.
     *
     * @param viewSelf    whether the player viewing the list may see themselves
     * @param viewOffline whether offline players are included
     * @param filter      filter a player has to pass to be exposed, or null to expose every player
     */
    public BukkitPlayerSource(boolean viewSelf, boolean viewOffline, Predicate<OfflinePlayer> filter) {
        this(DEFAULT_KEY, viewSelf, viewOffline, filter);
    }

    /**
     * Creates the stream.
     *
     * @param key         name of the variable holding the player itself, without the trailing underscore
     * @param viewSelf    whether the player viewing the list may see themselves
     * @param viewOffline whether offline players are included
     * @param filter      filter a player has to pass to be exposed, or null to expose every player
     */
    public BukkitPlayerSource(String key, boolean viewSelf, boolean viewOffline, Predicate<OfflinePlayer> filter) {
        this.key = key;
        this.prefix = key + "_";
        this.viewSelf = viewSelf;
        this.viewOffline = viewOffline;
        this.filter = filter;
    }

    @Override
    public Object replace(String input, OfflinePlayer value) {
        if (input.equals(key)) {
            return value.getName();
        }
        if (!input.startsWith(prefix)) {
            return null;
        }
        return replaceVariable(input.substring(prefix.length()), value);
    }

    /**
     * Resolves a player variable, the part of {@code <key>_<variable>} after the prefix.
     *
     * @param variable name of the variable
     * @param value    player the variable is resolved for
     * @return the replacement, or null if the variable is not known
     */
    protected Object replaceVariable(String variable, OfflinePlayer value) {
        return null;
    }

    @Override
    public boolean isSourceValueActivated(OfflinePlayer value) {
        return filter == null || filter.test(value);
    }

    @Override
    public Stream<OfflinePlayer> getSourceValueStream() {
        return viewOffline
                ? Arrays.stream(Bukkit.getOfflinePlayers())
                : Bukkit.getOnlinePlayers().stream().map(OfflinePlayer.class::cast);
    }

    @Override
    public Stream<OfflinePlayer> getSourceValueStream(UUID key) {
        Stream<OfflinePlayer> stream = getSourceValueStream();
        return viewSelf ? stream : stream.filter(player -> !Objects.equals(player.getUniqueId(), key));
    }
}
