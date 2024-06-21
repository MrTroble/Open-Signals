package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.signalbox.ConnectionChecker.ConnectionCheckerNormal;

public class DebugChecker extends ConnectionCheckerNormal {

    @Override
    public PathwayRequestResult check() {
        final PathwayRequestResult flag = super.check();
        if (!flag.isPass()) {
            OpenSignalsMain.getLogger().debug("Check failed for " + this.path);
        }
        return flag;
    }

}
