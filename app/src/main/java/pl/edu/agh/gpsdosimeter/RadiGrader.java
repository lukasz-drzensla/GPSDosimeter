package pl.edu.agh.gpsdosimeter;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;

public class RadiGrader {
    private static final int rad_thresholds[] = {10, 20, 30};
    private static final float color_thresholds[] = {HUE_GREEN, HUE_YELLOW, HUE_ORANGE, HUE_RED};
    public static float grade (int radiation)
    {
        int i;
        for (i = 0; i < rad_thresholds.length; i++)
        {
            if (radiation < rad_thresholds[i])
            {
                break;
            }
        }
        return color_thresholds[i];
    }
}
