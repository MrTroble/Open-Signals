package eu.gir.girsignals.signalbox.debug;

import eu.gir.girsignals.SignalsMain;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public boolean check() {
        final boolean flag = super.check();
        if (!flag) {
            SignalsMain.log.debug("Check failed for " + this.path);
            SignalsMain.log.debug(nextNode);
        }
        return flag;
    }

}
