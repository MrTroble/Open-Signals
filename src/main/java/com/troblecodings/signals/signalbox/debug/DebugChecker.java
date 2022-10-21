package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.GirsignalsMain;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public boolean check() {
        final boolean flag = super.check();
        if (!flag) {
            GirsignalsMain.log.debug("Check failed for " + this.path);
            GirsignalsMain.log.debug(nextNode);
        }
        return flag;
    }

}
