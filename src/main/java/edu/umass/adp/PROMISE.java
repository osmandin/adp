package edu.umass.adp;

import java.util.List;

/**
* PROMISE object represents a project
 * */
public class PROMISE {

    private String projectName;

    private String version;

    private String url;

    private String sourcePath;

    private List<double[]> rawFeatures;

    private List<double[]> paddedFeatures;

    public PROMISE(String projectName, String version, String url) {
        this.projectName = projectName;
        this.version = version;
        this.url = url;
    }

    public PROMISE(String projectName, String version, String url, String sourcePath) {
        this.projectName = projectName;
        this.version = version;
        this.url = url;
        this.sourcePath = sourcePath;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<double[]> getRawFeatures() {
        return rawFeatures;
    }

    public void setRawFeatures(List<double[]> rawFeatures) {
        this.rawFeatures = rawFeatures;
    }

    public List<double[]> getPaddedFeatures() {
        return paddedFeatures;
    }

    public void setPaddedFeatures(List<double[]> paddedFeatures) {
        this.paddedFeatures = paddedFeatures;
    }
}
