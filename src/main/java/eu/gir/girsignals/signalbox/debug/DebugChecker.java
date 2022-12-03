package eu.gir.girsignals.signalbox.debug;

import eu.gir.girsignals.GIRSignalsMain;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public boolean check() {
        final boolean flag = super.check();
        if (!flag) {
            GIRSignalsMain.log.debug("Check failed for " + this.path);
            GIRSignalsMain.log.debug(nextNode);
        }
        return flag;
    }

}
