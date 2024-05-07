package pl.edu.agh.gpsdosimeter;

import java.io.Serializable;

class Measurement implements Serializable {
    private String gpsData;
    private int radiation;
    private String dateTime;
    private String comment;

    public Measurement(String _gpsData, int _radiation, String _dateTime, String _comment) {
        this.gpsData = _gpsData;
        this.radiation = _radiation;
        this.dateTime = _dateTime;
        this.comment = _comment;
    }

    public String getGPS() {
        return this.gpsData;
    }

    public int getRadiation() {
        return this.radiation;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public String getComment() {
        return this.comment;
    }

    public void setGpsData(String _gpsData)
    {
        this.gpsData = _gpsData;
    }

    public void setRadiation(int _radiation)
    {
        this.radiation = _radiation;
    }

    public void setDateTime(String _dateTime)
    {
        this.dateTime = _dateTime;
    }

    public void setComment(String _comment)
    {
        this.comment = _comment;
    }

}
