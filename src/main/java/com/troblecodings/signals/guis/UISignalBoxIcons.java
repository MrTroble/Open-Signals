package com.troblecodings.signals.guis;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UISignalBoxIcons {

    private UISignalBoxIcons() {

    }

    public static final ResourceLocation ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/symbols.png");
    public static final ResourceLocation ARROW_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/arrow.png");
    public static final ResourceLocation INCOMING_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/connection_in.png");
    public static final ResourceLocation OUTGOING_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/connection_out.png");
    public static final ResourceLocation SIGNALS =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/signals.png");
    public static final ResourceLocation NE1_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/ne1.png");
    public static final ResourceLocation NE5_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/ne5.png");
    public static final ResourceLocation ZS3_ICON =
            new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/zs3.png");

}
