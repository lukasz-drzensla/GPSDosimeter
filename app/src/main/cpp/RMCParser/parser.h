#ifndef GPSDOSIMETER_PARSER_H
#define GPSDOSIMETER_PARSER_H

#include <string>

namespace gpsfields {

    class GpsField {
    public:
        GpsField ();
        virtual ~GpsField ();
        virtual void from_str (std::string str) {};
        virtual std::string to_string() { return ""; };
    protected:
        bool check_length (std::string str);
        bool set = false;
    };

    class Utc : public GpsField
    {
    public:
        int hh;
        int mm;
        int ss;
        int _ss;
        void from_str (std::string str);
        std::string to_string();
    };

    class Latitide : public GpsField
    {
    public:
        int DD;
        int mm;
        int _mm;
        void from_str (std::string str);
        std::string to_string();
    };

    class Longitude : public GpsField
    {
    public:
        int DDD;
        int mm;
        int _mm;
        void from_str (std::string str);
        std::string to_string();
    };

    class Speeed_Kn : public GpsField
    {
    public:
        int x;
        int _x;
        void from_str (std::string str);
        std::string to_string();
    };
    class Track_True : public GpsField
    {
    public:
        int x;
        int _x;
        void from_str (std::string str);
        std::string to_string();
    };
    class Date : public GpsField
    {
    public:
        int dd;
        int mm;
        int yy;
        void from_str (std::string str);
        std::string to_string();
    };
    class Mag_var : public GpsField
    {
    public:
        int x;
        int _x;
        void from_str (std::string str);
        std::string to_string();
    };
}


/* This class contains data fields from https://docs.novatel.com/OEM7/Content/Logs/GPRMC.htm */
class GPSDataUnpacked
{
public:
    std::string log_header;
    gpsfields::Utc utc;
    char pos_status;
    gpsfields::Latitide lat;
    char lat_dir;
    gpsfields::Longitude lon;
    char lon_dir;
    gpsfields::Speeed_Kn speed_Kn;
    gpsfields::Track_True track_true;
    gpsfields::Date date;
    gpsfields::Mag_var mag_var;
    char var_dir;
    char mode_ind;
    std::string check_sum;
    std::string to_string();
};

enum : unsigned char{
    FIELD_NUM = 13
};

class RMCParser
{
public:
    RMCParser (char _field_separator) : field_separator(_field_separator) {};

    GPSDataUnpacked parse_string (std::string input);

private:
    char field_separator;
};

#endif //GPSDOSIMETER_PARSER_H
