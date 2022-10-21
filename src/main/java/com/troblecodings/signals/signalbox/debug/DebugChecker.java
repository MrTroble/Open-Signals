package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;

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
