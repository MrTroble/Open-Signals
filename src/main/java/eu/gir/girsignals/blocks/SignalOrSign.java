package eu.gir.girsignals.blocks;

public class SignalOrSign extends Signal {

    public SignalOrSign(SignalProperties prop) {
        super(prop);
    }

    @Override
    public boolean hasCostumColor() {
        return prop.hasCostumColor;
    }
}