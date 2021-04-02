package com.akshaychavan.flashx.pojo;

/**
 * Created by Akshay Chavan on 02,April,2021
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CardPojo {

    @SerializedName("Word")
    @Expose
    private String word;
    @SerializedName("Class")
    @Expose
    private String _class;
    @SerializedName("Meaning")
    @Expose
    private String meaning;
    @SerializedName("Example")
    @Expose
    private String example;
    @SerializedName("Mnemonic")
    @Expose
    private String mnemonic;
    @SerializedName("Image URL")
    @Expose
    private String imageURL;

    public String getIsMastered() {
        return isMastered;
    }

    public void setIsMastered(String isMastered) {
        this.isMastered = isMastered;
    }

    @SerializedName("IsMastered")
    @Expose
    private String isMastered;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getClass_() {
        return _class;
    }

    public void setClass_(String _class) {
        this._class = _class;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
