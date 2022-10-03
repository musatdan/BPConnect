package com.KoAlco.BPConnect;

public class BT_Device {
    private String m_Name;
    private String m_Address;
    private String m_Level;
    private boolean m_isChecked;

    public BT_Device(String name, String address, String level){
        this.m_isChecked = false;
        this.m_Address = address;
        this.m_Name = name;
        this.m_Level = level;
    }

    public String getName() {
        return this.m_Name;
    }

    public String getAddress() {
        return this.m_Address;
    }

    public void setAddress(String address) {
        this.m_Address = address;
    }

    public void setLevel (String level) { this.m_Level = level; }

    public String getLevel() { return this.m_Level; }

    public void setName(String name){
        this.m_Name = name;
    }

    public boolean isChecked() { return this.m_isChecked; }

    public void setChecked() { this.m_isChecked = true; }

    public void setUnchecked() { this.m_isChecked = false; }
}
