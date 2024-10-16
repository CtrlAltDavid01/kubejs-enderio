package com.almostreliable.kubeio.enderio;

import com.enderio.api.conduit.ConduitMenuData;
import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.TieredConduit;
import com.enderio.api.conduit.ticker.ConduitTicker;
import com.enderio.api.misc.RedstoneControl;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitData;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class CustomEnergyConduitType extends TieredConduit<EnergyConduitData> {

    private static final ConduitMenuData MENU_DATA = new ConduitMenuData.Simple(
        false,
        false,
        false,
        false,
        false,
        true
    );

    private final CustomEnergyConduitTicker ticker;

    public CustomEnergyConduitType(ResourceLocation tierName, int transferRate) {
        super(
            new ResourceLocation("forge:energy"),
            tierName,
            transferRate
        );
        this.ticker = new CustomEnergyConduitTicker(transferRate);
    }

    @Override
    public ConduitTicker<EnergyConduitData> getTicker() {
        return ticker;
    }

    @Override
    public ConduitMenuData getMenuData() {
        return MENU_DATA;
    }

    @Override
    public EnergyConduitData createConduitData(Level level, BlockPos pos) {
        return new EnergyConduitData();
    }

    @Override
    public ConduitConnectionData getDefaultConnection(Level level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
        if (blockEntity == null) return super.getDefaultConnection(level, pos, direction);

        LazyOptional<IEnergyStorage> cap = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
        if (cap.isPresent()) {
            IEnergyStorage storage = cap.orElseThrow(() -> new RuntimeException("present capability was not found"));

            if (!storage.canReceive() && !storage.canExtract()) {
                return new ConduitConnectionData(false, true, RedstoneControl.ALWAYS_ACTIVE);
            }

            return new ConduitConnectionData(storage.canReceive(), storage.canExtract(), RedstoneControl.ALWAYS_ACTIVE);
        }

        return super.getDefaultConnection(level, pos, direction);
    }

    @Override
    public boolean canBeReplacedBy(ConduitType<?> other) {
        // allow replacing with simple energy conduit as it's infinite
        return other instanceof EnergyConduitType || super.canBeReplacedBy(other);
    }

    @Override
    public boolean canBeInSameBlock(ConduitType<?> other) {
        // don't allow simple energy conduit to be in the same block as custom energy conduits
        return !(other instanceof EnergyConduitType) && super.canBeInSameBlock(other);
    }
}
