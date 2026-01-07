package cn.xiaozi0721.oldsneak.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to EntityPlayer to implement the old 1.7 sneak animation.
 */
@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends Entity {
    @Unique private long oldSneak$sneakStartTime = 0L;
    @Unique private boolean oldSneak$wasSneaking = false;

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void onGetEyeHeight(CallbackInfoReturnable<Float> cir) {
        EntityPlayer self = (EntityPlayer) (Object) this;

        if (self.isPlayerSleeping()) {
            return;
        }

        float newEyeHeight = oldSneak$getOldSneakEyeHeight(self);
        cir.setReturnValue(newEyeHeight);
    }

    @Unique private float oldSneak$getOldSneakEyeHeight(EntityPlayer player) {
        // 定义动画的持续时间（毫秒）
        final float ANIMATION_DURATION = 120.0F;

        // 默认的站立视线高度
        float eyeHeight = 1.62F;

        // 检查潜行状态是否在这一帧发生了变化
        if (player.isSneaking() != this.oldSneak$wasSneaking) {
            // 如果状态变化了，记录当前时间戳
            this.oldSneak$sneakStartTime = System.currentTimeMillis();
        }
        // 更新上一帧的潜行状态
        this.oldSneak$wasSneaking = player.isSneaking();

        // 获取从潜行状态改变开始到现在所经过的时间
        long timeSinceStateChange = System.currentTimeMillis() - this.oldSneak$sneakStartTime;

        // 计算动画的进度（0.0 到 1.0 之间）
        float progress = Math.min(1.0F, timeSinceStateChange / ANIMATION_DURATION);

        // 根据当前是否在潜行来计算视线高度
        if (player.isSneaking()) {
            // 如果正在潜行，视线高度从 1.62F 平滑过渡到 1.54F
            // (1.62F - 0.08F = 1.54F)
            eyeHeight -= 0.08F * progress;
        } else {
            // 如果没有潜行，视线高度从 1.54F 平滑过渡回 1.62F
            eyeHeight = 1.54F + 0.08F * progress;
        }

        // 返回最终计算出的高度
        return eyeHeight;
    }
}