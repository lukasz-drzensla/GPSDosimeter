#include "parser.h"

#include <string>
#include <memory>
#include <algorithm>

std::string lpad (std::string input, const size_t num, char c);

std::string lpad (std::string input, const size_t num, char c)
{
    std::string str = input;
    if (num > str.size())
    {
        str.insert(0, num - str.size(), c);
    }
    return str;
}

class FieldParser
{
public:
    FieldParser(std::string _input, char _separator) : separator(_separator)
    {
        input = _input + ",";
    };
    ~FieldParser(){};
    bool next(std::string *output)
    {
        return get_next_field(input, separator, output);
    }

private:
    std::string input;
    char separator;
    bool get_next_field(std::string &_input, char _separator, std::string *_output)
    {
        std::string::size_type pos = _input.find(_separator);
        if (std::string::npos != pos)
        {
            *_output = _input.substr(0, pos);
            _input.erase(_input.begin(), std::find_if(_input.begin(), _input.end(), [_separator](char ch)
            { return (ch == _separator); }));
            _input.erase(_input.begin(), _input.begin() + 1);
            return true;
        }
        *_output = _input;
        return false;
    }
};


gpsfields::GpsField::GpsField () {}
gpsfields::GpsField::~GpsField () {}
bool gpsfields::GpsField::check_length (std::string str)
{
    if (0 == str.size())
    {
        set = false;
        return false;
    }
    set = true;
    return true;
}

void gpsfields::Utc::from_str (std::string str)
{
    if (!check_length(str))
        return;
    std::string temp = "";
    temp += str[0];
    temp += str[1];
    hh = std::stoi(temp);
    temp = "";
    temp += str[2];
    temp += str[3];
    mm = std::stoi(temp);
    temp = "";
    temp += str[4];
    temp += str[5];
    ss = std::stoi(temp);
    temp = "";
    temp += str.substr(7, str.size()-1);
    _ss = std::stoi(temp);
}

std::string gpsfields::Utc::to_string ()
{
    if (!set)
    {
        return "";
    }
    return lpad(std::to_string(hh), 2, '0') + lpad(std::to_string(mm), 2, '0') + lpad(std::to_string(ss), 2, '0') + "." + lpad(std::to_string(_ss), 2, '0');
}

void gpsfields::Latitide::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string temp = "";
    temp += str[0];
    temp += str[1];
    DD = std::stoi(temp);
    temp = "";
    temp += str[2];
    temp += str[3];
    mm = std::stoi(temp);
    temp = "";
    temp += str.substr(5, str.size()-1);
    _mm = std::stoi(temp);
}

std::string gpsfields::Latitide::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(DD), 2, '0') + lpad(std::to_string(mm), 2, '0') + "." + lpad(std::to_string(_mm), 2, '0');
}

void gpsfields::Longitude::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string temp = "";
    temp += str[0];
    temp += str[1];
    temp += str[2];
    DDD = std::stoi(temp);
    temp = "";
    temp += str[3];
    temp += str[4];
    mm = std::stoi(temp);
    temp = "";
    temp += str.substr(6, str.size()-1);
    _mm = std::stoi(temp);
}

std::string gpsfields::Longitude::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(DDD), 3, '0') + lpad(std::to_string(mm), 2, '0') + "." + lpad(std::to_string(_mm), 2, '0');
}

void gpsfields::Speeed_Kn::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string::size_type pos = str.find('.');
    std::string temp = str.substr(0, pos);
    x = std::stoi(temp);
    temp = str.substr(pos+1, str.size()-1);
    _x = std::stoi(temp);
}

std::string gpsfields::Speeed_Kn::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(x), 1, '0') + "." + lpad(std::to_string(_x), 2, '0');
}

void gpsfields::Track_True::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string::size_type pos = str.find('.');
    std::string temp = str.substr(0, pos);
    x = std::stoi(temp);
    temp = str.substr(pos+1, str.size()-1);
    _x = std::stoi(temp);
}

std::string gpsfields::Track_True::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(x), 2, '0') + lpad(std::to_string(_x), 2, '0');
}

void gpsfields::Date::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string temp = "";
    temp += str[0];
    temp += str[1];
    dd = std::stoi(temp);
    temp = "";
    temp += str[2];
    temp += str[3];
    mm = std::stoi(temp);
    temp = "";
    temp += str.substr(4, str.size()-1);
    yy = std::stoi(temp);
}

std::string gpsfields::Date::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(dd), 2, '0') + lpad(std::to_string(mm), 2, '0') + lpad(std::to_string(yy), 2, '0');
}

void gpsfields::Mag_var::from_str(std::string str)
{
    if (!check_length(str))
        return;
    std::string::size_type pos = str.find('.');
    std::string temp = str.substr(0, pos);
    x = std::stoi(temp);
    temp = str.substr(pos+1, str.size()-1);
    _x = std::stoi(temp);
}

std::string gpsfields::Mag_var::to_string ()
{
    if (!set)
    {
        return "";
    }
    return "" + lpad(std::to_string(x), 2, '0') + lpad(std::to_string(_x), 2, '0');
}

std::string GPSDataUnpacked::to_string()
{
    return "" + log_header + ',' + utc.to_string() + "," + pos_status + "," + lat.to_string() + "," + lat_dir + "," + lon.to_string() + "," + lon_dir + "," + speed_Kn.to_string() + "," + track_true.to_string() + "," + date.to_string() + "," + mag_var.to_string() + "," + var_dir + "," + mode_ind + check_sum;
}

GPSDataUnpacked RMCParser::parse_string (std::string input)
{
    GPSDataUnpacked result;
    std::string rmc_fields [FIELD_NUM];

    FieldParser fParser(input, field_separator);
    std::string field = "";
    int i = -1;
    while (fParser.next(&field) && i < FIELD_NUM)
    {
        rmc_fields[++i] = field;
    }

    result.log_header = rmc_fields[0];
    result.utc.from_str(rmc_fields[1]);
    result.pos_status = rmc_fields[2][0];
    result.lat.from_str (rmc_fields[3]);
    result.lat_dir = rmc_fields[4][0];
    result.lon.from_str (rmc_fields[5]);
    result.lon_dir = rmc_fields[6][0];
    result.speed_Kn.from_str(rmc_fields[7]);
    result.track_true.from_str(rmc_fields[8]);
    result.date.from_str(rmc_fields[9]);
    result.mag_var.from_str(rmc_fields[10]);
    result.var_dir = rmc_fields[11][0];
    result.mode_ind = rmc_fields[12][0];
    result.check_sum = rmc_fields[12].substr(1, rmc_fields[12].size()-1);
    return result;
}