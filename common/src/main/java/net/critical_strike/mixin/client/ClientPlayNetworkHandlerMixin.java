package net.critical_strike.mixin.client;

import net.critical_strike.CriticalStrikeMod;
import net.critical_strike.client.ParticleHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;

    @Inject(method = "onEntityAnimation", at = @At("TAIL"))
    private void crit_onEntityAnimation_TAIL(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        if (packet.getAnimationId() == CriticalStrikeMod.CRIT_PACKET_CODE) {
            Entity entity = world.getEntityById(packet.getEntityId());
            if (entity != null) {
                ParticleHelper.spawnCritParticles(entity);
            }
        }
    }
}
