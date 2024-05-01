package pl.edu.agh.gpsdosimeter;

import androidx.annotation.NonNull;

public class BTCb {
    private JRadicom jRadicom;
    private JRadicom.RCCallbacks callbacks;
    public BTCb (@NonNull JRadicom _jradicom, @NonNull JRadicom.RCCallbacks _rccallbacks)
    {
        this.jRadicom = _jradicom;
        this.callbacks = _rccallbacks;
    }

    public void notify (int[] frame)
    {
        jRadicom.decode(frame, callbacks);
    }
}
