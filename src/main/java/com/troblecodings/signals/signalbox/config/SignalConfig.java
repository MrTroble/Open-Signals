package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.ChangeConfigParser;
import com.troblecodings.signals.contentpacks.OneSignalNonPredicateConfigParser;
import com.troblecodings.signals.contentpacks.OneSignalPredicateConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;

public final class SignalConfig {

    private SignalConfig() {
    }

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.currentinfo.signal;
        System.out.println("Changeing Signal " + info.currentinfo + "!");
        System.out.println("Call:");
        for (final StackTraceElement el : Thread.currentThread().getStackTrace()) {
            System.out.println("          " + el.toString());
        }
        if (info.type.equals(PathType.NORMAL)) {
            if (info.nextinfo != null) {
                final Signal nextSignal = info.nextinfo.signal;
                final List<ConfigProperty> values = ChangeConfigParser.CHANGECONFIGS
                        .get(Maps.immutableEntry(currentSignal, nextSignal));
                if (values != null) {
                    changeIfPresent(values, info);
                } else {
                    loadDefault(info);
                }
            } else {
                loadDefault(info);
            }
        } else if (info.type.equals(PathType.SHUNTING)) {
            final List<ConfigProperty> shuntingValues = OneSignalNonPredicateConfigParser.SHUNTINGCONFIGS
                    .get(currentSignal);
            if (shuntingValues != null) {
                loadWithoutPredicate(shuntingValues, info.currentinfo);
            }
        }
    }

    private static void loadDefault(final ConfigInfo info) {
        final List<ConfigProperty> defaultValues = OneSignalPredicateConfigParser.DEFAULTCONFIGS
                .get(info.currentinfo.signal);
        if (defaultValues != null) {
            changeIfPresent(defaultValues, info);
        }
    }

    public static void reset(final ResetInfo info) {
        System.out.println("Resetting Signal " + info.current + "!");
        System.out.println("Call:");
        for (final StackTraceElement el : Thread.currentThread().getStackTrace()) {
            System.out.println("          " + el.toString());
        }
        final List<ConfigProperty> resetValues = OneSignalNonPredicateConfigParser.RESETCONFIGS
                .get(info.current.signal);
        if (resetValues == null)
            return;
        SignalStateHandler.runTaskWhenSignalLoaded(info.current, (stateInfo, oldProperties, _u) -> {
            final Map<Class<?>, Object> object = new HashMap<>();
            object.put(Boolean.class, info.isRepeater);
            object.put(Map.class, oldProperties);

            final Map<SEProperty, String> propertiesToSet = new HashMap<>();
            resetValues.forEach(property -> {
                if (property.test(object)) {
                    propertiesToSet.putAll(property.state.entrySet().stream()
                            .filter(entry -> oldProperties.containsKey(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
            });
            if (!propertiesToSet.isEmpty())
                SignalStateHandler.setStates(info.current, propertiesToSet);
        });
    }

    public static void loadDisable(final ConfigInfo info) {
        final List<ConfigProperty> disableValues = OneSignalPredicateConfigParser.DISABLECONFIGS
                .get(info.currentinfo.signal);
        if (disableValues != null) {
            changeIfPresent(disableValues, info);
        }
    }

    private static void changeIfPresent(final List<ConfigProperty> values, final ConfigInfo info) {
        SignalStateHandler.runTaskWhenSignalLoaded(info.currentinfo,
                (stateInfo, oldProperties, _u) -> {
                    if (info.nextinfo != null) {
                        SignalStateHandler.runTaskWhenSignalLoaded(info.nextinfo,
                                (nextInfo, nextProperties, _u2) -> changeSignals(values, info,
                                        oldProperties, nextProperties));
                    } else {
                        changeSignals(values, info, oldProperties, null);
                    }
                });

    }

    private static void changeSignals(final List<ConfigProperty> values, final ConfigInfo info,
            final Map<SEProperty, String> oldProperties,
            final Map<SEProperty, String> nextProperties) {
        final Map<Class<?>, Object> object = new HashMap<>();
        object.put(Map.class, nextProperties != null ? nextProperties : new HashMap<>());
        object.put(Integer.class, info.speed);
        object.put(String.class, info.zs2Value);
        object.put(Boolean.class, info.isSignalRepeater);
        final Map<SEProperty, String> propertiesToSet = new HashMap<>();
        values.forEach(property -> {
            if (property.test(object)) {
                propertiesToSet.putAll(property.state.entrySet().stream()
                        .filter(entry -> oldProperties.containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        });
        if (!propertiesToSet.isEmpty())
            SignalStateHandler.setStates(info.currentinfo, propertiesToSet);
    }

    private static void loadWithoutPredicate(final List<ConfigProperty> values,
            final SignalStateInfo current) {
        if (values != null) {
            SignalStateHandler.runTaskWhenSignalLoaded(current, (info, oldProperties, _u) -> {
                final Map<SEProperty, String> propertiesToSet = new HashMap<>();
                values.forEach(property -> {
                    propertiesToSet.putAll(property.state.entrySet().stream()
                            .filter(entry -> oldProperties.containsKey(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                });
                if (!propertiesToSet.isEmpty())
                    SignalStateHandler.setStates(current, propertiesToSet);
            });
        }
    }
}
