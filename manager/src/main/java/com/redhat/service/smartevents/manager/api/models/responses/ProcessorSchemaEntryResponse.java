package com.redhat.service.smartevents.manager.api.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessorSchemaEntryResponse {
    @JsonProperty("kind")
    private final String kind = "ProcessorSchemaEntry";

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    @JsonProperty("href")
    private String href;

    public ProcessorSchemaEntryResponse() {
    }

    public ProcessorSchemaEntryResponse(String id, String name, String description, String type, String href) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.href = href;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
