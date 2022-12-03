package eu.gir.girsignals.enums;

public enum ChangeableStage {
    APISTAGE, GUISTAGE, APISTAGE_NONE_CONFIG(/* Not configurable in UI */),
    AUTOMATICSTAGE(/* Special stage, does nothing */);
}