package com.akshaychavan.flashx.pojo;

/**
 * Created by Akshay Chavan on 02,April,2021
 */
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Definition {

    @SerializedName("definition")
    @Expose
    private String definition;
    @SerializedName("synonyms")
    @Expose
    private List<String> synonyms = null;
    @SerializedName("example")
    @Expose
    private String example;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

}