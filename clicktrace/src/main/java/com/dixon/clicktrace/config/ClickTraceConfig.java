package com.dixon.clicktrace.config;

/**
 * Created by dixon.xu on 2018/12/18.
 * <p>
 * 对于Java get、set必须有...
 */

public class ClickTraceConfig {

    private String basepath;
    private String[] basepaths;
    private String tracepath;
    private String tracemethod;

    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    public String getBasepath() {
        return basepath;
    }

    public String[] getBasepaths() {
        return basepaths;
    }

    public void basepaths(String... basepaths) {
        this.basepaths = basepaths;
    }

    public String getTracepath() {
        return tracepath;
    }

    public String getTracemethod() {
        return tracemethod;
    }

    public void tracepath(String tracepath, String tracemethod) {
        this.tracepath = tracepath;
        this.tracemethod = tracemethod;
    }
}
