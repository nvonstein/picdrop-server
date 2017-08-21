/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.model.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.picdrop.helper.ObjectMerger;
import com.picdrop.model.FileType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mongodb.morphia.annotations.Embedded;

/**
 *
 * @author i330120
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ImageDescriptor extends ResourceDescriptor {

    @Embedded("thumbnailUris")
    Map<String, String> thumbnailUris = new HashMap<>();
    String orientation; // TODO enum?

    ImageDescriptor() {
        super(FileType.IMAGE_WILDTYPE);
    }

    ImageDescriptor(FileType ft) {
        super(ft);
    }

    public Map<String, String> getThumbnailUris() {
        return thumbnailUris;
    }

    public void setThumbnailUris(Map<String, String> thumbnailUris) {
        this.thumbnailUris = thumbnailUris;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    @JsonIgnore
    public String getThumbnailUri(String key) {
        return this.thumbnailUris.get(key);
    }

    @JsonIgnore
    public void addThumbnailUri(String key, String value) {
        this.thumbnailUris.put(key, value);
    }

    @Override
    public ImageDescriptor merge(ResourceDescriptor update, ObjectMerger merger) throws IOException {
        super.merge(update, merger);
        if (update.isImage()) {
            merger.merge(this, update.to(ImageDescriptor.class));
        }
        return this;
    }

}
