package net.pl3x.map.claimchunk.task;

import com.cjburkey.claimchunk.chunk.DataChunk;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.MapWorld;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Polygon;
import net.pl3x.map.api.marker.Rectangle;
import net.pl3x.map.claimchunk.configuration.Config;
import net.pl3x.map.claimchunk.data.Claim;
import net.pl3x.map.claimchunk.data.Group;
import net.pl3x.map.claimchunk.hook.ClaimChunkHook;
import net.pl3x.map.claimchunk.util.RectangleMerge;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pl3xMapTask extends BukkitRunnable {
    private final MapWorld world;
    private final SimpleLayerProvider provider;

    private boolean stop;

    public Pl3xMapTask(MapWorld world, SimpleLayerProvider provider) {
        this.world = world;
        this.provider = provider;
    }

    @Override
    public void run() {
        if (stop) {
            cancel();
        }
        updateClaims();
    }

    void updateClaims() {
        provider.clearMarkers(); // TODO track markers instead of clearing them
        DataChunk[] dataChunksArr = ClaimChunkHook.getClaims();
        if (dataChunksArr == null) {
            return;
        }
        List<DataChunk> dataChunks = Arrays.stream(dataChunksArr)
                .filter(claim -> claim.chunk.getWorld().equals(this.world.name()))
                .collect(Collectors.toList());

        // show simple markers (marker per chunk)
        if (Config.SHOW_CHUNKS) {
            dataChunks.forEach(this::drawChunk);
            return;
        }

        // show combined chunks into polygons
        List<Claim> claims = dataChunks.stream()
                .map(dataChunk -> new Claim(
                        dataChunk.chunk.getX(),
                        dataChunk.chunk.getZ(),
                        dataChunk.player
                )).collect(Collectors.toList());
        List<Group> groups = groupClaims(claims);
        for (Group group : groups) {
            drawGroup(group);
        }
    }

    private List<Group> groupClaims(List<Claim> claims) {
        // break groups down by owner
        Map<UUID, List<Claim>> byOwner = new HashMap<>();
        for (Claim claim : claims) {
            List<Claim> list = byOwner.getOrDefault(claim.getOwner(), new ArrayList<>());
            list.add(claim);
            byOwner.put(claim.getOwner(), list);
        }

        // combine touching claims
        Map<UUID, List<Group>> groups = new HashMap<>();
        for (Map.Entry<UUID, List<Claim>> entry : byOwner.entrySet()) {
            UUID owner = entry.getKey();
            List<Claim> list = entry.getValue();
            next1:
            for (Claim claim : list) {
                List<Group> groupList = groups.getOrDefault(owner, new ArrayList<>());
                for (Group group : groupList) {
                    if (group.isTouching(claim)) {
                        group.add(claim);
                        continue next1;
                    }
                }
                groupList.add(new Group(claim, owner));
                groups.put(owner, groupList);
            }
        }

        // combined touching groups
        List<Group> combined = new ArrayList<>();
        for (List<Group> list : groups.values()) {
            next:
            for (Group group : list) {
                for (Group toChk : combined) {
                    if (toChk.isTouching(group)) {
                        toChk.add(group);
                        continue next;
                    }
                }
                combined.add(group);
            }
        }

        return combined;
    }

    private void drawGroup(Group group) {
        Polygon polygon = RectangleMerge.getPoly(group.claims());
        MarkerOptions.Builder options = options(group.owner());
        polygon.markerOptions(options);

        String markerid = "claimchunk_" + world.name() + "_chunk_" + group.id();
        this.provider.addMarker(Key.of(markerid), polygon);
    }

    private void drawChunk(DataChunk claim) {
        int minX = claim.chunk.getX() << 4;
        int maxX = (claim.chunk.getX() + 1) << 4;
        int minZ = claim.chunk.getZ() << 4;
        int maxZ = (claim.chunk.getZ() + 1) << 4;

        Rectangle rect = Marker.rectangle(Point.of(minX, minZ), Point.of(maxX, maxZ));
        MarkerOptions.Builder options = options(claim.player);
        rect.markerOptions(options);

        String markerid = "claimchunk_" + world.name() + "_chunk_" + minX + "_" + minZ;
        this.provider.addMarker(Key.of(markerid), rect);
    }

    private MarkerOptions.Builder options(UUID owner) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
        String ownerName = player.getName() == null ? "unknown" : player.getName();
        return MarkerOptions.builder()
                .strokeColor(Config.STROKE_COLOR)
                .strokeWeight(Config.STROKE_WEIGHT)
                .strokeOpacity(Config.STROKE_OPACITY)
                .fillColor(Config.FILL_COLOR)
                .fillOpacity(Config.FILL_OPACITY)
                .clickTooltip(Config.CLAIM_TOOLTIP
                        .replace("{world}", world.name())
                        .replace("{owner}", ownerName)
                );
    }

    public void disable() {
        cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}

