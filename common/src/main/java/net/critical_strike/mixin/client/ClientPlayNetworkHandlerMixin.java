package net.critical_strike.mixin.client;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.client.ParticleHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void crit_onEntityAnimation_TAIL(ClientboundAnimatePacket packet, CallbackInfo ci) {
        if (packet.getAction() == CriticalStrikeMod.CRIT_PACKET_CODE) {
            Entity entity = level.getEntity(packet.getId());
            if (entity != null) {
                ParticleHelper.spawnCritParticles(entity);
            }
        }
    }
}
