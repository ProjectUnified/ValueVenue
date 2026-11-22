package io.github.projectunified.valuevenue.bukkit;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPlayerStreamTest {
    private final OfflinePlayer player = player("Player");

    @Test
    void resolvesThePlayerName() {
        BukkitPlayerStream stream = new BukkitPlayerStream(true, false);

        assertEquals("Player", stream.replace(BukkitPlayerStream.DEFAULT_KEY, player));
        assertNull(stream.replace("something_else", player));
    }

    @Test
    void usesTheGivenKey() {
        BukkitPlayerStream stream = new BukkitPlayerStream("player", true, false, null);

        assertEquals("Player", stream.replace("player", player));
        assertNull(stream.replace("current_player", player));
    }

    @Test
    void delegatesThePlayerVariablesToTheOverride() {
        BukkitPlayerStream stream = new BukkitPlayerStream("player", true, false, null) {
            @Override
            protected Object replaceVariable(String variable, OfflinePlayer value) {
                return variable.equals("world") ? "world" : super.replaceVariable(variable, value);
            }
        };

        assertEquals("world", stream.replace("player_world", player));
        assertNull(stream.replace("player_other", player));
        assertEquals("Player", stream.replace("player", player));
    }

    @Test
    void filtersThePlayers() {
        OfflinePlayer alex = player("Alex");
        Predicate<OfflinePlayer> onlyAlex = player -> player.getName().equals("Alex");

        assertTrue(new BukkitPlayerStream(true, false, onlyAlex).isSourceValueActivated(alex));
        assertFalse(new BukkitPlayerStream(true, false, player -> player.getName().equals("Steve")).isSourceValueActivated(alex));
        assertTrue(new BukkitPlayerStream(true, false).isSourceValueActivated(alex));
    }

    private static OfflinePlayer player(String name) {
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return name;
                    }
                    if (method.getName().equals("getUniqueId")) {
                        return UUID.fromString("00000000-0000-0000-0000-000000000001");
                    }
                    return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
                }
        );
    }
}
