package pl.edu.agh.gpsdosimeter;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;

public class RadiGrader {
    private final int rad_thresholds[] = {5, 10, 15, 20, 25, 30, 35, 40, 45};
    private final float color_step = (HUE_GREEN - HUE_RED)/rad_thresholds.length;
    private float color_thresholds[] = new float [rad_thresholds.length];

    public RadiGrader()
    {
        float f = HUE_GREEN;
        for (int i = 0; i < rad_thresholds.length; i++)
        {
            color_thresholds[i] = f;
            f -= color_step;
        }
    }

    public float grade (int radiation)
    {
        int i;
        for (i = 0; i < rad_thresholds.length - 1; i++)
        {
            if (radiation < rad_thresholds[i])
            {
                break;
            }
        }
        return color_thresholds[i];
    }
}
