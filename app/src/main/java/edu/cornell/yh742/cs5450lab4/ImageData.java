package edu.cornell.yh742.cs5450lab4;

/**
 * Created by seanhsu on 11/22/17.
 */

public class ImageData {
    public String url;
    public String description;

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public ImageData(String url, String description){
        this.url = url;
        this.description = description;
    }

    public ImageData(){}
}
