package eu.gir.girsignals.signalbox.debug;

import eu.gir.girsignals.signalbox.SignalBoxFactory;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugFactory extends SignalBoxFactory {

    private final boolean enableConnectionChecker = true;

    @Override
    public ConnectionChecker getConnectionChecker() {
        if (enableConnectionChecker)
            return new DebugChecker();
        return super.getConnectionChecker();
    }

}
