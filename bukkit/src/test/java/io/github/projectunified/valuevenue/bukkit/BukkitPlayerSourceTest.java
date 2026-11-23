package io.github.projectunified.valuevenue.bukkit;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPlayerSourceTest {
    private final OfflinePlayer player = player("Player");

    @Test
    void resolvesThePlayerName() {
        BukkitPlayerSource<String> source = new BukkitPlayerSource<>(false);

        assertEquals("Player", source.replace(BukkitPlayerSource.DEFAULT_KEY, player));
        assertNull(source.replace("something_else", player));
    }

    @Test
    void usesTheGivenKey() {
        BukkitPlayerSource<String> source = new BukkitPlayerSource<>("player", false, null, null);

        assertEquals("Player", source.replace("player", player));
        assertNull(source.replace("current_player", player));
    }

    @Test
    void delegatesThePlayerVariablesToTheOverride() {
        BukkitPlayerSource<String> source = new BukkitPlayerSource<String>("player", false, null, null) {
            @Override
            protected Object replaceVariable(String variable, OfflinePlayer value) {
                return variable.equals("world") ? "world" : super.replaceVariable(variable, value);
            }
        };

        assertEquals("world", source.replace("player_world", player));
        assertNull(source.replace("player_other", player));
        assertEquals("Player", source.replace("player", player));
    }

    @Test
    void activatesThePlayersOfTheActivationFilter() {
        OfflinePlayer alex = player("Alex");
        BukkitPlayerSource<String> source = new BukkitPlayerSource<>(false, value -> value.getName().equals("Alex"), null);

        assertTrue(source.isSourceValueActivated(alex));
        assertFalse(new BukkitPlayerSource<String>(false, value -> value.getName().equals("Steve"), null)
                .isSourceValueActivated(alex));
        assertTrue(new BukkitPlayerSource<String>(false).isSourceValueActivated(alex));
    }

    @Test
    void filtersThePlayersOfTheKey() {
        OfflinePlayer alex = player("Alex");
        BukkitPlayerSource<String> source = new BukkitPlayerSource<>(false, (value, key) -> !value.getName().equals(key));

        assertEquals(Collections.emptyList(), players(source, alex, "Alex"));
        assertEquals(Collections.singletonList(alex), players(source, alex, "Steve"));
    }

    @Test
    void exposesEveryPlayerWithoutAFilter() {
        OfflinePlayer alex = player("Alex");
        BukkitPlayerSource<String> source = new BukkitPlayerSource<>(false);

        assertEquals(Collections.singletonList(alex), players(source, alex, "Alex"));
    }

    private static java.util.List<OfflinePlayer> players(BukkitPlayerSource<String> source, OfflinePlayer value, String key) {
        return source.filter(Stream.of(value), key).collect(Collectors.toList());
    }

    private static OfflinePlayer player(String name) {
        return (OfflinePlayer) Proxy.newProxyInstance(
                OfflinePlayer.class.getClassLoader(),
                new Class<?>[]{OfflinePlayer.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getName":
                        case "toString":
                            return name;
                        case "getUniqueId":
                            return UUID.fromString("00000000-0000-0000-0000-000000000001");
                        case "hashCode":
                            return name.hashCode();
                        case "equals":
                            return proxy == args[0];
                        default:
                            return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
                    }
                }
        );
    }
}
