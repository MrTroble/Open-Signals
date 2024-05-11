package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;

public class DebugChecker extends ConnectionChecker {

    @Override
    public PathwayRequestResult check() {
        final PathwayRequestResult flag = super.check();
        if (!flag.isPass()) {
            OpenSignalsMain.getLogger().debug("Check failed for " + this.path);
        }
        return flag;
    }

}
