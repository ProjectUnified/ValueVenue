package io.github.projectunified.valuevenue.bukkit;

import io.github.projectunified.valuevenue.core.ValueSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are the players of the server.
 * <p>
 * {@code <key>} resolves to the name of the player, and {@code <key>_<variable>} is delegated to
 * {@link #replaceVariable(String, OfflinePlayer)}, which returns null by default. Override it to resolve the player
 * variables of the owner, calling the default for the rest.
 * <p>
 * Whether a player is active is decided by its own filter, which is refreshed by the update of the generator. Which
 * players a key may see is decided by the key filter, which gets both the player and the key, so the source does not
 * have to know what a key is.
 *
 * @param <K> type of the key
 */
public class BukkitPlayerSource<K> implements ValueSource<K, OfflinePlayer> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_player";

    private final String key;
    private final String prefix;
    private final boolean viewOffline;
    private final Predicate<OfflinePlayer> activatedFilter;
    private final BiPredicate<OfflinePlayer, K> playerFilter;

    /**
     * Creates the source with {@link #DEFAULT_KEY}, without a filter.
     *
     * @param viewOffline whether offline players are included
     */
    public BukkitPlayerSource(boolean viewOffline) {
        this(DEFAULT_KEY, viewOffline, null, null);
    }

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param viewOffline  whether offline players are included
     * @param playerFilter filter a player has to pass to be exposed for a key, or null to expose every player
     */
    public BukkitPlayerSource(boolean viewOffline, BiPredicate<OfflinePlayer, K> playerFilter) {
        this(DEFAULT_KEY, viewOffline, null, playerFilter);
    }

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param viewOffline     whether offline players are included
     * @param activatedFilter filter a player has to pass to be active, or null to activate every player
     * @param playerFilter    filter a player has to pass to be exposed for a key, or null to expose every player
     */
    public BukkitPlayerSource(boolean viewOffline, Predicate<OfflinePlayer> activatedFilter, BiPredicate<OfflinePlayer, K> playerFilter) {
        this(DEFAULT_KEY, viewOffline, activatedFilter, playerFilter);
    }

    /**
     * Creates the source.
     *
     * @param key             name of the variable holding the player itself, without the trailing underscore
     * @param viewOffline     whether offline players are included
     * @param activatedFilter filter a player has to pass to be active, or null to activate every player
     * @param playerFilter    filter a player has to pass to be exposed for a key, or null to expose every player
     */
    public BukkitPlayerSource(String key, boolean viewOffline, Predicate<OfflinePlayer> activatedFilter, BiPredicate<OfflinePlayer, K> playerFilter) {
        this.key = key;
        this.prefix = key + "_";
        this.viewOffline = viewOffline;
        this.activatedFilter = activatedFilter;
        this.playerFilter = playerFilter;
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
        return activatedFilter == null || activatedFilter.test(value);
    }

    @Override
    public Stream<OfflinePlayer> getSourceValueStream() {
        return viewOffline
                ? Arrays.stream(Bukkit.getOfflinePlayers())
                : Bukkit.getOnlinePlayers().stream().map(OfflinePlayer.class::cast);
    }

    @Override
    public Stream<OfflinePlayer> getSourceValueStream(K key) {
        return filter(getSourceValueStream(), key);
    }

    /**
     * Applies the filter of the source to the players of the key.
     *
     * @param players the players to filter
     * @param key     the key they are exposed for
     * @return stream of the players the key may see
     */
    protected Stream<OfflinePlayer> filter(Stream<OfflinePlayer> players, K key) {
        return playerFilter == null ? players : players.filter(player -> playerFilter.test(player, key));
    }
}
