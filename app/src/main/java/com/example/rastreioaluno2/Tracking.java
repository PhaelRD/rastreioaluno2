package com.example.rastreioaluno2;

public class Tracking {
    private String id;
    private String trackingName;
    private String schoolAddress;
    private String homeAddress;
    private String transportUid;
    private String studentUid;

    public Tracking() {
        // Construtor padrão necessário para chamadas a DataSnapshot.getValue(Tracking.class)
    }

    public Tracking(String trackingName, String schoolAddress, String homeAddress, String transportUid, String studentUid) {
        this.trackingName = trackingName;
        this.schoolAddress = schoolAddress;
        this.homeAddress = homeAddress;
        this.transportUid = transportUid;
        this.studentUid = studentUid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrackingName() {
        return trackingName;
    }

    public void setTrackingName(String trackingName) {
        this.trackingName = trackingName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getTransportUid() {
        return transportUid;
    }

    public void setTransportUid(String transportUid) {
        this.transportUid = transportUid;
    }

    public String getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }
}
