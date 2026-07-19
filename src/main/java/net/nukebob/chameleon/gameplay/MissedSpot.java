package net.nukebob.chameleon.gameplay;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MissedSpot {
    public static void missedSpotUpdate(MinecraftServer server) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            if (TeamControl.isChameleon(player.getTeam())) {
                int pointsEarned = checkMissedSpotForHider(player, server);
                if (pointsEarned>0) {
                    Game.addMissedSpotPoints(player.getUUID(), pointsEarned);
                }
            }
        }
    }

    private static int checkMissedSpotForHider(ServerPlayer player, MinecraftServer server) {
        int pointsEarned = 0;
        ServerLevel level = player.level();
        Vec3 hider = player.getEyePosition().add(player.position()).scale(0.5);

        for (ServerPlayer gamePlayer : PlayerLookup.all(server)) {
            if (!TeamControl.isHunter(gamePlayer.getTeam())) continue;
            Vec3 hunter = gamePlayer.getEyePosition();
            Vec3 hiderToHunter = hunter.subtract(hider);
            double distance = hiderToHunter.length();
            if (distance>32||distance<0.5) continue;

            HitResult result = Raycast.raycast(level, player, false, hider, hiderToHunter.normalize(), 32);
            if (result instanceof BlockHitResult) continue;
            if (result instanceof EntityHitResult entityHitResult) {
                if (!entityHitResult.getEntity().getUUID().equals(gamePlayer.getUUID())) continue;
            }

            Vec3 hunterLook = gamePlayer.getViewVector(1.0f).normalize();

            double dotProduct = -hunterLook.dot(hiderToHunter.normalize());

            if (dotProduct > 0.65) {
                pointsEarned += (int) ((dotProduct*30-2*distance));
            }
        }
        return pointsEarned;
    }

    public record MissedSpotEntry(int ranking, String playerName, boolean alive, int score) {
        public static final StreamCodec<FriendlyByteBuf, MissedSpotEntry> STREAM_CODEC = StreamCodec.of(
                (buf, entry) -> {
                    buf.writeVarInt(entry.ranking);
                    buf.writeUtf(entry.playerName);
                    buf.writeBoolean(entry.alive);
                    buf.writeVarInt(entry.score);
                },
                buf -> new MissedSpotEntry(
                        buf.readVarInt(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readVarInt()
                )
        );
    }
}
