package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public boolean check() {
        final boolean flag = super.check();
        if (!flag) {
            OpenSignalsMain.log.debug("Check failed for " + this.path);
            OpenSignalsMain.log.debug(nextNode);
        }
        return flag;
    }

}
