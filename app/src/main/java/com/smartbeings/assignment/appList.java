package com.smartbeings.assignment;

import java.util.ArrayList;
import java.util.List;


public class appList {

    List<listings> listingses = new ArrayList<>();
    String success;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public List<listings> getListingses() {
        return listingses;
    }

    public void setListingses(List<listings> listingses) {
        this.listingses = listingses;
    }
}
