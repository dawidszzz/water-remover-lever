package com.example.waterremover;

import com.example.waterremover.network.ToggleModPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.Properties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Główna klasa inicjalizująca mod.
 * Po przełączeniu zwykłej dźwigni (LeverBlock) w stan powered = true,
 * uruchamiany jest skan 3D wokół dźwigni, który usuwa wodę.
 *
 * Funkcję można globalnie włączać/wyłączać bindem klawiszowym
 * (patrz: com.example.waterremover.client.WaterRemoverClient),
 * co wysyła pakiet ToggleModPayload do serwera.
 */
public class WaterRemoverMod implements ModInitializer {

    public static final String MOD_ID = "waterremover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Promień skanu w każdej osi (X, Y, Z).
    // UWAGA: koszt skanu rośnie sześciennie - (2*RADIUS+1)^3 bloków.
    // RADIUS=5  -> ~1331 bloków (bezpieczne)
    // RADIUS=8  -> ~4913 bloków (wciąż OK, jednorazowo)
    // RADIUS=16 -> ~35900 bloków (może zauważalnie obciążyć serwer)
    private static final int RADIUS = 8;

    // Globalny stan włączenia moda (wspólny dla całego serwera).
    // Domyślnie aktywny.
    private static volatile boolean enabled = true;

    @Override
    public void onInitialize() {
        LOGGER.info("[WaterRemover] Inicjalizacja moda...");

        // Rejestracja typu pakietu sieciowego
        PayloadTypeRegistry.playC2S().register(ToggleModPayload.ID, ToggleModPayload.CODEC);

        // Odbiornik na serwerze: przełącza flagę enabled po otrzymaniu pakietu
        ServerPlayNetworking.registerGlobalReceiver(ToggleModPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    enabled = !enabled;
                    String status = enabled ? "włączony" : "wyłączony";
                    context.player().sendMessage(Text.literal("[WaterRemover] Mod " + status), true);
                    LOGGER.info("[WaterRemover] Stan przełączony przez {}: {}",
                            context.player().getName().getString(), enabled);
                })
        );

        // Nasłuch interakcji gracza z blokami
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            // Działamy wyłącznie po stronie serwera
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (!enabled) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof LeverBlock) {
                boolean currentlyPowered = state.get(LeverBlock.POWERED);

                // Jeśli dźwignia jest obecnie wyłączona, kliknięcie
                // zaraz przełączy ją na powered = true.
                if (!currentlyPowered) {
                    ServerWorld serverWorld = (ServerWorld) world;

                    // Odkładamy wykonanie na koniec przetwarzania, aby
                    // wanilia zdążyła najpierw zaktualizować stan dźwigni.
                    serverWorld.getServer().execute(() ->
                            clearWaterAround(serverWorld, pos, RADIUS)
                    );
                }
            }

            return ActionResult.PASS;
        });

        LOGGER.info("[WaterRemover] Mod zainicjalizowany pomyślnie.");
    }

    /**
     * Skanuje sześcian o zadanym promieniu wokół pozycji dźwigni
     * i zamienia każdy blok wody (źródło lub płynącą) na powietrze.
     * Usuwa też wodę z bloków "waterlogged" (schody, płyty, ogrodzenia).
     */
    private void clearWaterAround(ServerWorld world, BlockPos center, int radius) {
        int removed = 0;
        Mutable cursor = new Mutable();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {

                    cursor.set(
                            center.getX() + dx,
                            center.getY() + dy,
                            center.getZ() + dz
                    );

                    BlockState targetState = world.getBlockState(cursor);
                    Block targetBlock = targetState.getBlock();

                    // Blocks.WATER obejmuje zarówno źródło, jak i płynącą wodę
                    // (różni je właściwość LEVEL) - proste porównanie wystarcza.
                    if (targetBlock == Blocks.WATER) {
                        world.setBlockState(
                                cursor.toImmutable(),
                                Blocks.AIR.getDefaultState(),
                                Block.NOTIFY_ALL
                        );
                        removed++;
                        continue;
                    }

                    // Obsługa bloków "waterlogged"
                    if (targetState.contains(Properties.WATERLOGGED)
                            && targetState.get(Properties.WATERLOGGED)) {
                        world.setBlockState(
                                cursor.toImmutable(),
                                targetState.with(Properties.WATERLOGGED, false),
                                Block.NOTIFY_ALL
                        );
                        removed++;
                    }
                }
            }
        }

        LOGGER.info("[WaterRemover] Usunięto {} bloków wody wokół pozycji {}.", removed, center);
    }
}
