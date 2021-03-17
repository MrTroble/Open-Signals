package eu.gir.girsignals.tileentitys;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TrackIOTileEntity extends TileEntity implements SimpleComponent {

	@Optional.Method(modid = "opencomputers")
	public void sendSignalReachable(final String signalname, final Object... objects) {
		for (EnumFacing face : EnumFacing.VALUES) {
			final BlockPos newpos = this.pos.offset(face);
			final TileEntity entity = world.getTileEntity(newpos);
			if (entity != null)
				System.out.println(entity);
			if (entity != null && entity instanceof Environment) {
				final Environment env = (Environment) entity;
				final Node node = env.node();
				if(node != null)
					node.sendToReachable("computer.signal", signalname, objects);
			}
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		sendSignalReachable("test");
	}
	
	@Override
	public String getComponentName() {
		return "trackio";
	}

}
