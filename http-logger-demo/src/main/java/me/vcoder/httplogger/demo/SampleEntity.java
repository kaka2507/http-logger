package me.vcoder.httplogger.demo;

import javax.validation.constraints.NotEmpty;

/**
 * @author baodn
 * Created on 26 Jun 2018
 */
public class SampleEntity {
    @NotEmpty
    private String id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
