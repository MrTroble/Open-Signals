package eu.gir.girsignals.signalbox.entrys;

import eu.gir.girsignals.signalbox.EnumGuiMode;

public class PathEntryType<T extends IPathEntry<?>> {

    private final Class<T> entryClass;
    private final String name;

    private PathEntryType(final Class<T> entryClass, final String name) {
        this.entryClass = entryClass;
        this.name = name;
    }

    public static final PathEntryType<BlockposEntry> SIGNAL = new PathEntryType<>(
            BlockposEntry.class, "signal");
    public static final PathEntryType<BlockposEntry> BLOCKING = new PathEntryType<>(
            BlockposEntry.class, "blocking");
    public static final PathEntryType<BlockposEntry> RESETING = new PathEntryType<>(
            BlockposEntry.class, "reseting");
    public static final PathEntryType<BlockposEntry> OUTPUT = new PathEntryType<>(
            BlockposEntry.class, "output");
    public static final PathEntryType<IntegerEntry> SPEED = new PathEntryType<>(IntegerEntry.class,
            "speed");

    @SuppressWarnings("rawtypes")
    public static final PathEntryType<EnumEntry> PATHUSAGE = new PathEntryType<EnumEntry>(
            EnumEntry.class, "pathusage") {
        @Override
        public EnumEntry<EnumGuiMode> newValue() {
            return new EnumEntry<>(EnumGuiMode.class);
        }
    };

    public T newValue() {
        try {
            final T t = this.entryClass.newInstance();
            t.setName(name);
            return t;
        } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
