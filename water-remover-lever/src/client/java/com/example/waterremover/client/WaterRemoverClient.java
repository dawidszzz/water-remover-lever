package com.example.waterremover.client;

import com.example.waterremover.network.ToggleModPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Entrypoint klienta. Rejestruje bind klawiszowy, który wysyła
 * do serwera pakiet przełączający włączenie/wyłączenie moda.
 */
public class WaterRemoverClient implements ClientModInitializer {

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.waterremover.toggle",      // klucz tłumaczenia nazwy bindu
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,                 // domyślny klawisz - R
                "key.categories.waterremover"    // kategoria w opcjach sterowania
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.getNetworkHandler() != null) {
                    ClientPlayNetworking.send(new ToggleModPayload());
                }
            }
        });
    }
}
