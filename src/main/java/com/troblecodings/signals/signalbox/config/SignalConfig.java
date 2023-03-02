package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.ChangeConfigParser;
import com.troblecodings.signals.contentpacks.DefaultConfigParser;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;
import com.troblecodings.signals.properties.ConfigProperty;
import com.troblecodings.signals.properties.SignalPair;

import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;

public final class SignalConfig {

    private SignalConfig() {
    }

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.currentinfo.signal;
        if (info.type.equals(PathType.NORMAL)) {
            if (info.nextinfo != null) {
                final Signal nextSignal = info.nextinfo.signal;
                final SignalPair pair = new SignalPair(currentSignal, nextSignal);
                final List<ConfigProperty> values = ChangeConfigParser.CHANGECONFIGS.get(pair);
                if (values != null) {
                    changeIfPresent(values, info);
                } else {
                    loadDefault(info);
                }
            } else {
                loadDefault(info);
            }
        } else if (info.type.equals(PathType.SHUNTING)) {
            final List<ConfigProperty> shuntingValues = OneSignalConfigParser.SHUNTINGCONFIGS
                    .get(currentSignal);
            if (shuntingValues != null) {
                loadWithoutPredicate(shuntingValues, info.currentinfo);
            }
        }
    }

    private static void loadDefault(final ConfigInfo info) {
        final List<ConfigProperty> defaultValues = DefaultConfigParser.DEFAULTCONFIGS
                .get(info.currentinfo.signal);
        if (defaultValues != null) {
            changeIfPresent(defaultValues, info);
        }
    }

    public static void reset(final SignalStateInfo current) {
        final List<ConfigProperty> resetValues = OneSignalConfigParser.RESETCONFIGS
                .get(current.signal);
        if (resetValues != null) {
            loadWithoutPredicate(resetValues, current);
        }
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private static void changeIfPresent(final List<ConfigProperty> values, final ConfigInfo info) {
        final Map<Class, Object> object = new HashMap<>();
        final Map<SEProperty, String> oldProperties = SignalStateHandler
                .getStates(info.currentinfo);
        if (info.nextinfo != null) {
            object.put(Map.class, SignalStateHandler.getStates(info.nextinfo));
        }
        object.put(Integer.class, info.speed);
        final Map<SEProperty, String> propertiesToSet = new HashMap<>();
        values.forEach(property -> {
            if (property.predicate.test(object)) {
                propertiesToSet.putAll(property.values.entrySet().stream()
                        .filter(entry -> oldProperties.containsKey(entry.getKey()))
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                Map.Entry::getValue)));
            }
        });
        if (!propertiesToSet.isEmpty())
            SignalStateHandler.setStates(info.currentinfo, propertiesToSet);
    }

    private static void loadWithoutPredicate(final List<ConfigProperty> values,
            final SignalStateInfo current) {
        if (values != null) {
            final Map<SEProperty, String> oldProperties = SignalStateHandler.getStates(current);
            final Map<SEProperty, String> propertiesToSet = new HashMap<>();
            values.forEach(property -> {
                propertiesToSet.putAll(property.values.entrySet().stream()
                        .filter(entry -> oldProperties.containsKey(entry.getKey()))
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                Map.Entry::getValue)));

            });
            if (!propertiesToSet.isEmpty())
                SignalStateHandler.setStates(current, propertiesToSet);
        }
    }
}
