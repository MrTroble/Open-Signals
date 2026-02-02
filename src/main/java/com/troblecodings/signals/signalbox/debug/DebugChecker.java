package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.signalbox.ConnectionChecker.ConnectionCheckerNormal;

public class DebugChecker extends ConnectionCheckerNormal {

    @Override
    public PathwayRequestMode check() {
        final PathwayRequestMode flag = super.check();
        if (!flag.isPass()) {
            OpenSignalsMain.getLogger().debug("Check failed for " + this.path);
        }
        return flag;
    }

}
