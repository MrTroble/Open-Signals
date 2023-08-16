package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.contentpacks.SoundPropertyParser;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.properties.BooleanProperty;
import com.troblecodings.signals.properties.FloatProperty;
import com.troblecodings.signals.properties.HeightProperty;
import com.troblecodings.signals.properties.SoundProperty;

import net.minecraft.sounds.SoundEvent;

public class SignalPropertiesBuilder {

    private transient Placementtool placementtool = null;
    private String placementToolName = null;
    private int defaultHeight = 1;
    private Map<String, Integer> signalHeights;
    private float customNameRenderHeight = -1;
    private Map<String, Float> renderHeights;
    private float signWidth = 22;
    private boolean autoscale = false;
    private float offsetX = 0;
    private float offsetY = 0;
    private float signScale = 1;
    private Map<String, Boolean> doubleSidedText;
    private int textColor = 0;
    private boolean canLink = true;
    private List<Integer> colors;
    private Map<String, SoundPropertyParser> sounds;
    private Map<String, Object> redstoneOutputs;
    private int defaultItemDamage = 1;

    public SignalProperties build(final FunctionParsingInfo info) {
        if (placementToolName != null) {
            for (int i = 0; i < OSItems.placementtools.size(); i++) {
                final Placementtool tool = OSItems.placementtools.get(i);
                if (tool.getRegistryName().getPath().equalsIgnoreCase(placementToolName)) {
                    placementtool = tool;
                    break;
                }
            }
        }
        if (placementtool == null)
            throw new ContentPackException("There doesn't exists a placementtool with the name '"
                    + placementToolName + "'! Valid Placementtool: " + OSItems.placementtools);

        final List<HeightProperty> signalheights = new ArrayList<>();
        if (signalHeights != null) {
            signalHeights.forEach((property, height) -> {
                try {
                    signalheights
                            .add(new HeightProperty(LogicParser.predicate(property, info), height));
                } catch (final LogicalParserException e) {
                    OpenSignalsMain.getLogger()
                            .error("Something went wrong during the registry of a predicate in "
                                    + info.signalName + "!\nWith statement:" + property);
                    e.printStackTrace();
                }
            });
        }
        final List<FloatProperty> renderheights = new ArrayList<>();
        if (renderHeights != null) {
            renderHeights.forEach((property, height) -> {
                try {
                    renderheights
                            .add(new FloatProperty(LogicParser.predicate(property, info), height));
                } catch (final LogicalParserException e) {
                    OpenSignalsMain.getLogger()
                            .error("Something went wrong during the registry of a predicate in "
                                    + info.signalName + "!\nWith statement:" + property);
                    e.printStackTrace();
                }
            });
        }
        final List<SoundProperty> soundProperties = new ArrayList<>();
        if (sounds != null) {
            for (final Map.Entry<String, SoundPropertyParser> soundProperty : sounds.entrySet()) {
                final SoundPropertyParser soundProp = soundProperty.getValue();
                final SoundEvent sound = OSSounds.SOUNDS.get(soundProp.getName().toLowerCase());
                if (sound == null) {
                    OpenSignalsMain.getLogger().error(
                            "The sound with the name " + soundProp.getName() + " doesn't exists!");
                    continue;
                }
                try {
                    soundProperties.add(new SoundProperty(sound,
                            LogicParser.predicate(soundProperty.getKey(), info),
                            soundProp.getLength()));
                } catch (final LogicalParserException e) {
                    OpenSignalsMain.getLogger()
                            .error("Something went wrong during the registry of a predicate in "
                                    + info.signalName + "!\nWith statement:"
                                    + soundProperty.getKey());
                    e.printStackTrace();
                }
            }
        }
        final List<Object> rsOutputs = new ArrayList<>();
        if (redstoneOutputs != null) {
            for (final Map.Entry<String, Object> outputs : redstoneOutputs.entrySet()) {
                @SuppressWarnings("rawtypes")
                final Predicate predicate = LogicParser.predicate(outputs.getKey(), info);
                final Object obj = outputs.getValue();
                if (obj instanceof Boolean) {
                    rsOutputs.add(new BooleanProperty(predicate, (boolean) obj));
                } else if (obj instanceof String) {
                    final String str = (String) obj;
                    if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true")) {
                        rsOutputs.add(new BooleanProperty(predicate, (boolean) obj));
                        continue;
                    }
                    final SEProperty property = (SEProperty) info.getProperty(str);
                    rsOutputs.add(new ValuePack(property, predicate));
                } else {
                    throw new ContentPackException("[" + obj
                            + "] is not a valid state for RSOutputs! Valid are: String, Boolean");
                }
            }
        }

        final List<BooleanProperty> doubleText = new ArrayList<>();
        if (doubleSidedText != null) {
            doubleSidedText.forEach((property, bool) -> {
                try {
                    doubleText
                            .add(new BooleanProperty(LogicParser.predicate(property, info), bool));
                } catch (final LogicalParserException e) {
                    OpenSignalsMain.getLogger()
                            .error("Something went wrong during the registry of a predicate in "
                                    + info.signalName + "!\nWith statement:" + property);
                    e.printStackTrace();
                }
            });
        }
        this.colors = this.colors == null ? new ArrayList<>() : this.colors;

        return new SignalProperties(placementtool, customNameRenderHeight, defaultHeight,
                ImmutableList.copyOf(signalheights), signWidth, offsetX, offsetY, signScale,
                autoscale, ImmutableList.copyOf(doubleText), textColor, canLink, colors,
                ImmutableList.copyOf(renderheights), ImmutableList.copyOf(soundProperties),
                ImmutableList.copyOf(rsOutputs), defaultItemDamage);
    }
}