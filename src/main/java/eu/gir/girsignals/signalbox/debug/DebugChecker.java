package eu.gir.girsignals.signalbox.debug;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public boolean check() {
        final boolean flag = super.check();
        if (!flag) {
            GirsignalsMain.log.debug("Check failed for " + this.path);
        }
        return flag;
    }

}
