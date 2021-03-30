package eu.gir.girsignals.tileentitys;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import li.cil.oc.api.API;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TrackIOTileEntity extends TileEntity implements SimpleComponent {

	@Optional.Method(modid = "opencomputers")
	public void sendSignalReachable(final Object... objects) {
		assert objects != null && objects.length > 0 && objects[0] instanceof String;
		foreach((env, node) -> node.sendToReachable("computer.signal", objects));
	}

	private void foreach(final BiConsumer<Environment, Node> nodeconsumer) {
		for (EnumFacing face : EnumFacing.VALUES) {
			final BlockPos newpos = this.pos.offset(face);
			final TileEntity entity = world.getTileEntity(newpos);
			if (entity != null && entity instanceof Environment) {
				final Environment env = (Environment) entity;
				final Node node = env.node();
				if (node != null)
					nodeconsumer.accept(env, node);
			}
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if(world.isRemote)
			return;
		for (EnumFacing face : EnumFacing.VALUES) {
			BlockPos npos = pos.offset(face);
			EnumFacing opp = face.getOpposite();
			DriverBlock driver = API.driver.driverFor(world, npos, opp);
			if(driver != null)
				System.out.println(driver);
			if(driver == null)
				continue;
			ManagedEnvironment managed = driver.createEnvironment(world, npos, opp);
			System.out.println(managed);
			foreach((env, node) -> {
				managed.node().connect(node);
				node.connect(managed.node());
			});
		}
		sendSignalReachable("test3");
	}

	@Override
	public String getComponentName() {
		return "trackio";
	}

}
