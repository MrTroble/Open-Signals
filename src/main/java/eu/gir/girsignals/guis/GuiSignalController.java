package eu.gir.girsignals.guis;

import java.io.IOException;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiSignalController extends GuiContainer {

	public GuiSignalController(final SignalControllerTileEntity entity) {
		super(new ContainerSignalController(entity));
	}

	@Override
	public void initGui() {
		
	}

	public static class EnumIntegerable<T extends Enum<T>> implements IIntegerable<T> {

		private Class<T> t;

		public EnumIntegerable(Class<T> t) {
			this.t = t;
		}

		@Override
		public T getObjFromID(int obj) {
			return t.getEnumConstants()[obj];
		}

		@Override
		public int count() {
			return t.getEnumConstants().length;
		}
	}

	public interface ObjGetter<D> {
		D getObjFrom(int x);
	}

	public static class SizeIntegerables<T> implements IIntegerable<T> {

		private final int count;
		private final ObjGetter<T> getter;

		private SizeIntegerables(final int count, final ObjGetter<T> getter) {
			this.count = count;
			this.getter = getter;
		}

		@Override
		public T getObjFromID(int obj) {
			return this.getter.getObjFrom(obj);
		}

		@Override
		public int count() {
			return count;
		}

		public static <T> IIntegerable<T> of(final int count, final ObjGetter<T> get) {
			return new SizeIntegerables<T>(count, get);
		}

	}

	@Override
	public void onGuiClosed() {
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// TODO Auto-generated method stub
		
	}

}
