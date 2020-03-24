package com.localcc.baritonechunkminer.mixins;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.localcc.baritonechunkminer.Helpers.AdditionHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMessageMixin {
    @Shadow @Final GameProfile profile;

    @SuppressWarnings("unused")
    @Inject(at = @At("HEAD"), method = "onChatMessage", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void init(ChatMessageS2CPacket packet, CallbackInfo info) {

        if(packet.getMessage().getString().contains(">")) {
            String msg_content = packet.getMessage().getString().split(">")[1].substring(1);
            JsonParser parser = new JsonParser();

            JsonElement parsed = parser.parse(msg_content);
            if(parsed.isJsonObject()) {
                JsonObject args = parsed.getAsJsonObject();
                if(!(args.has("x") && args.has("y") && args.has("z") && args.has(profile.getName()) &&
                        args.has("subchunk_size") && args.has("chunk_size")) && args.has("end_y")) {
                    return;
                }
                int s_x, s_y, s_z;
                int end_y;
                int order;
                int subchunk_size;
                int chunk_size;
                boolean visualize = false;
                boolean instant_start = true;
                try {
                    s_x = args.get("x").getAsInt(); s_y = args.get("y").getAsInt(); s_z = args.get("z").getAsInt();
                    subchunk_size = args.get("subchunk_size").getAsInt();
                    chunk_size = args.get("chunk_size").getAsInt();
                    order = args.get(profile.getName()).getAsInt();
                    end_y = args.get("end_y").getAsInt();
                }catch(NumberFormatException e) {
                    return;
                }
                if(args.has("visualize")) {
                    visualize = args.get("visualize").getAsBoolean();
                }
                if(args.has("instant_start")) {
                    instant_start = args.get("instant_start").getAsBoolean();
                }

                int s_x_addition, s_z_addition;
                s_z_addition = s_x_addition = 0;

                int c_order = 0;
                //TODO: change this cycle to math formula
                while (c_order != order) {
                    c_order++;

                    s_x_addition += subchunk_size;
                    if (s_x_addition > (chunk_size / subchunk_size - 1) * subchunk_size) {
                        s_z_addition += subchunk_size;
                        s_x_addition = 0;
                    }
                }
                s_x = AdditionHelper.add(s_x, s_x_addition + 1);
                s_z = AdditionHelper.add(s_z, s_z_addition + 1);

                if(visualize) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getSelectionManager().addSelection(new BetterBlockPos(s_x, s_y, s_z),
                            new BetterBlockPos(AdditionHelper.add(s_x, subchunk_size - 1), end_y, AdditionHelper.add(s_z, subchunk_size - 1)));
                }
                if (instant_start) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(
                            new BlockPos(s_x, s_y, s_z),
                            new BlockPos(AdditionHelper.add(s_x, subchunk_size - 1), end_y, AdditionHelper.add(s_z, subchunk_size - 1)));
                }
            }
        }
    }
}
