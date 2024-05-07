package pl.edu.agh.gpsdosimeter;

public class RMCParser {
    static {
        System.loadLibrary("gpsdosimeter");
    }

    class GPSDataUnpacked{
        private double lat, lon;
        public GPSDataUnpacked(double _lat, double _lon)
        {
            this.lat = _lat;
            this.lon = _lon;
        }

        public double getLat()
        {
            return this.lat;
        }

        public double getLon()
        {
            return this.lon;
        }
    }

    public GPSDataUnpacked parse(String input)
    {
        parse_raw(input);
        double lat = getLat();
        double lon = getLon();
        return new GPSDataUnpacked(lat, lon);
    }

    private native void parse_raw(String raw_str);
    private native double getLat();
    private native double getLon();
}