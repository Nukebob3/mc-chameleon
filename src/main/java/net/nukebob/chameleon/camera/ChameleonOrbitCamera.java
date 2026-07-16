package net.nukebob.chameleon.camera;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class ChameleonOrbitCamera extends AbstractClientPlayer {
    private boolean active = false;
    private float yaw = 0f;
    private float pitch = 0f;

    private float distance = 2.0f;

    public ClientInput input;
    private boolean isFreeCam;

    public Player spectateWho;

    private static ChameleonOrbitCamera instance;

    public ChameleonOrbitCamera(int id) {
        super(Minecraft.getInstance().level, new GameProfile(UUID.randomUUID(), "freecam"));

        setId(id);
        setPose(Pose.SWIMMING);
        getAbilities().flying = true;
        input = new KeyboardInput(Minecraft.getInstance().options);

        spectateWho = null;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    public static ChameleonOrbitCamera getInstance() {
        if (Minecraft.getInstance().level==null||Minecraft.getInstance().player==null) return null;
        if (instance==null) instance=new ChameleonOrbitCamera(0);
        return instance;
    }

    public static void recreate() {
        instance = new ChameleonOrbitCamera(-333);
    }

    public void deactivate(Player player) {
        active = false;
        if (player.equals(spectateWho)||spectateWho==null) {
            player.setYRot(yaw);
            player.setXRot(pitch);

            player.yRotO = yaw;
            player.xRotO = pitch;

            player.setYHeadRot(yaw);
            player.yHeadRotO = yaw;

            player.getAttributes().getInstance(Attributes.CAMERA_DISTANCE).setBaseValue(distance * 2);
        }
    }

    public void setSpectatorTarget(Player target) {
        this.spectateWho = target;
        if (target==null) return;
        if (isFreeCam) {
            setPos(target.getEyePosition());
        }
    }

    @Override
    public void tick() {
        this.input.tick();

        if (this.isActive()) {
            if (!this.isInFreeCam()) {
                this.setDeltaMovement(Vec3.ZERO);
            }
        } else {
            super.tick();
        }
    }

    public void move(float deltaTime) {
        float yawRad = (float) Math.toRadians(this.getYaw());
        float pitchRad = (float) Math.toRadians(this.getPitch());

        float cosPitch = Mth.cos(pitchRad);
        float sinPitch = Mth.sin(pitchRad);
        float cosYaw = Mth.cos(yawRad);
        float sinYaw = Mth.sin(yawRad);

        double fwdX = -sinYaw * cosPitch;
        double fwdY = -sinPitch;
        double fwdZ = cosYaw * cosPitch;

        double strafeX = cosYaw;
        double strafeZ = sinYaw;

        double forwardInput = this.input.keyPresses.forward() ? 1 : 0;
        double strafeInput = this.input.keyPresses.left() ? -1 : 0;
        strafeInput += this.input.keyPresses.right() ? 1 : 0;
        forwardInput += this.input.keyPresses.backward() ? -1 : 0;

        double speed = 0.2*deltaTime*(this.input.keyPresses.sprint()?2:1);

        double motionX = (fwdX * forwardInput - strafeX * strafeInput) * speed;
        double motionY = (fwdY * forwardInput) * speed;
        double motionZ = (fwdZ * forwardInput - strafeZ * strafeInput) * speed;

        Minecraft client = Minecraft.getInstance();
        if (client.options.keyJump.isDown()) {
            motionY += speed;
        }
        if (client.options.keyShift.isDown()) {
            motionY -= speed;
        }

        this.setDeltaMovement(motionX, motionY, motionZ);

        this.setPos(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
        this.setOldPos();
    }

    public Vec3 getNonFreeCamPosition() {
        var minecraft = Minecraft.getInstance();
        if (spectateWho==null && minecraft.player == null) {
            return Vec3.ZERO;
        }

        Vec3 pivot = spectateWho==null?minecraft.player.getEyePosition(1.0f):spectateWho.getEyePosition(1.0f);

        float yawRadians = yaw * (float) (Math.PI / 180.0);
        float pitchRadians = pitch * (float) (Math.PI / 180.0);

        double cosPitch = Mth.cos(pitchRadians);

        double offsetX = -Mth.sin(yawRadians) * cosPitch * distance;
        double offsetY = -Mth.sin(pitchRadians) * distance;
        double offsetZ = Mth.cos(yawRadians) * cosPitch * distance;

        return new Vec3(
                pivot.x - offsetX,
                pivot.y - offsetY,
                pivot.z - offsetZ
        );
    }

    public boolean isInFreeCam() {
        return isFreeCam;
    }

    public void setFreeCam(boolean isFreeCam) {
        this.isFreeCam = isFreeCam;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
        super.defineSynchedData(entityData);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    public float getDistance() { return distance; }
    public void setDistance(float value) { distance = Math.min(3f,Math.max(1f, value)); }

    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public void rotate(float dYaw, float dPitch) {
        yaw += dYaw;
        pitch = Math.max(-90f, Math.min(90f, pitch + dPitch));
        if (!isInFreeCam()) {
            this.setPos(getNonFreeCamPosition().x, getNonFreeCamPosition().y, getNonFreeCamPosition().z);
        }
    }

    public void syncToEntityLookDirection(float entityYaw, float entityPitch) {
        yaw = entityYaw;
        pitch = entityPitch;
        this.setPos(getNonFreeCamPosition().x, getNonFreeCamPosition().y, getNonFreeCamPosition().z);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    @Override
    public boolean shouldRender(double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public boolean isSpectator() {
        return true;
    }
}