package top.earthstudio.nextgenbedwars.api.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import top.earthstudio.nextgenbedwars.api.team.Team;
import top.earthstudio.nextgenbedwars.api.team.TeamManager;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public final class FindNearestPlayer { // ATTENTION: This was built by LLM.
    private static final int CELL_SHIFT = 5;
    private static final int SPATIAL_THRESHOLD = 256;

    private final int sliceInterval;
    private int tickCursor = 0;

    private Player[] players = new Player[32];
    private Team[] teams = new Team[32];
    private double[] xs = new double[32];
    private double[] ys = new double[32];
    private double[] zs = new double[32];
    private int count = 0;

    private final Long2ObjectOpenHashMap<IntArrayList> grid = new Long2ObjectOpenHashMap<>();

    public FindNearestPlayer(int sliceIntervalTicks) {
        this.sliceInterval = Math.max(1, sliceIntervalTicks);
    }

    public record Result(Player target, double distanceSquared) {}

    public void tick(List<Player> worldPlayers, TeamManager teamManager, BiConsumer<Player, Result> onFound) {
        snapshot(worldPlayers, teamManager);
        if (count < 2) {
            tickCursor = (tickCursor + 1) % sliceInterval;
            return;
        }

        boolean useGrid = count > SPATIAL_THRESHOLD;
        if (useGrid && (tickCursor == 0 || grid.isEmpty())) {
            rebuildGrid();
        }

        for (int i = tickCursor; i < count; i += sliceInterval) {
            Result res = useGrid ? findNearestGrid(i) : findNearestFlat(i);
            if (res != null) {
                onFound.accept(players[i], res);
            }
        }

        tickCursor = (tickCursor + 1) % sliceInterval;
        if (tickCursor == 0) {
            Arrays.fill(players, 0, count, null);
            Arrays.fill(teams, 0, count, null);
        }
    }

    private void snapshot(List<Player> worldPlayers, TeamManager teamManager) {
        int size = worldPlayers.size();
        if (size > players.length) {
            int newCap = Math.max(size, players.length << 1);
            players = new Player[newCap];
            teams = new Team[newCap];
            xs = new double[newCap];
            ys = new double[newCap];
            zs = new double[newCap];
        }

        int idx = 0;
        for (int i = 0; i < size; i++) {
            Player p = worldPlayers.get(i);
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            Team team = teamManager.get(p);
            if (team == null) continue;

            players[idx] = p;
            teams[idx] = team;
            xs[idx] = p.getX();
            ys[idx] = p.getY();
            zs[idx] = p.getZ();
            idx++;
        }
        count = idx;
    }

    private Result findNearestFlat(int selfIdx) {
        Team myTeam = teams[selfIdx];
        double sx = xs[selfIdx], sy = ys[selfIdx], sz = zs[selfIdx];

        int bestIdx = -1;
        double minDistSq = Double.MAX_VALUE;

        for (int i = 0; i < count; i++) {
            if (i == selfIdx || teams[i].equals(myTeam)) continue;
            double dx = xs[i] - sx;
            double dy = ys[i] - sy;
            double dz = zs[i] - sz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                bestIdx = i;
            }
        }

        return bestIdx == -1 ? null : new Result(players[bestIdx], minDistSq);
    }

    private void rebuildGrid() {
        grid.values().forEach(IntArrayList::clear);
        for (int i = 0; i < count; i++) {
            int cx = ((int) Math.floor(xs[i])) >> CELL_SHIFT;
            int cz = ((int) Math.floor(zs[i])) >> CELL_SHIFT;
            grid.computeIfAbsent(cellKey(cx, cz), k -> new IntArrayList(4)).add(i);
        }
    }

    private Result findNearestGrid(int selfIdx) {
        Team myTeam = teams[selfIdx];
        double sx = xs[selfIdx], sy = ys[selfIdx], sz = zs[selfIdx];
        int originCx = ((int) Math.floor(sx)) >> CELL_SHIFT;
        int originCz = ((int) Math.floor(sz)) >> CELL_SHIFT;

        int bestIdx = -1;
        double minDistSq = Double.MAX_VALUE;
        int maxRing = Integer.MAX_VALUE;

        for (int r = 0; r <= maxRing && r <= 32; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    IntArrayList bucket = grid.get(cellKey(originCx + dx, originCz + dz));
                    if (bucket == null || bucket.isEmpty()) continue;

                    for (int k = 0, bSize = bucket.size(); k < bSize; k++) {
                        int targetIdx = bucket.getInt(k);
                        if (targetIdx == selfIdx || teams[targetIdx].equals(myTeam)) continue;
                        double ddx = xs[targetIdx] - sx;
                        double ddy = ys[targetIdx] - sy;
                        double ddz = zs[targetIdx] - sz;
                        double distSq = ddx * ddx + ddy * ddy + ddz * ddz;
                        if (distSq < minDistSq) {
                            minDistSq = distSq;
                            bestIdx = targetIdx;
                        }
                    }
                }
            }
            if (bestIdx != -1) {
                int neededRing = (int) Math.ceil(Math.sqrt(minDistSq) / (1 << CELL_SHIFT)) + 1;
                maxRing = Math.min(maxRing, neededRing);
            }
        }

        return bestIdx != -1 ? new Result(players[bestIdx], minDistSq) : findNearestFlat(selfIdx);
    }

    private static long cellKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    public static Result findNearest(Player self, List<Player> worldPlayers, TeamManager teamManager) {
        Team myTeam = teamManager.get(self);
        if (myTeam == null) return null;

        double sx = self.getX();
        double sy = self.getY();
        double sz = self.getZ();

        Player nearest = null;
        double minDistSq = Double.MAX_VALUE;

        for (Player target : worldPlayers) {
            if (target.getUniqueId().equals(self.getUniqueId())) continue;
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            Team targetTeam = teamManager.get(target);
            if (targetTeam == null || targetTeam.equals(myTeam)) continue;

            double dx = target.getX() - sx;
            double dy = target.getY() - sy;
            double dz = target.getZ() - sz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = target;
            }
        }

        return nearest == null ? null : new Result(nearest, minDistSq);
    }
}