/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.model.resource;

import com.fasterxml.jackson.annotation.JsonView;
import com.picdrop.json.Views;
import java.io.IOException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

/**
 *
 * @author i330120
 */
@Entity("files")
public class FileResource extends Resource {

    protected String extension;
    protected String fileId;
    protected long size;

    @Embedded
    ResourceDescriptor descriptor;

    public FileResource() {
        super();
    }

    public FileResource(String _id) {
        super(_id);

    }

    public FileResource(ObjectId _id) {
        super(_id);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        String[] tmp = name.split("\\.");
        if (tmp.length > 1) {
            this.setExtension(tmp[tmp.length - 1]);
        }
    }

    @JsonView(value = Views.Public.class)
    public long getSize() {
        return size;
    }

    @JsonView(value = Views.Ignore.class)
    public void setSize(long size) {
        this.size = size;
    }

    @JsonView(value = Views.Public.class)
    public String getExtension() {
        return extension;
    }

    @JsonView(value = Views.Public.class)
    public void setExtension(String extension) {
        this.extension = extension;
    }

    @JsonView(value = Views.Public.class)
    public ResourceDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonView(value = Views.Ignore.class)
    public void setDescriptor(ResourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @JsonView(value = Views.Public.class)
    public String getFileId() {
        return fileId;
    }

    @JsonView(value = Views.Ignore.class)
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public FileResource merge(Resource update) throws IOException {
        if (update == null) {
            return this;
        }

        super.merge(update);
        if (update instanceof FileResource) {
            FileResource nupdate = (FileResource) update;
            if (nupdate.descriptor != null) {
                this.descriptor.merge(nupdate.descriptor);
            }
            if (nupdate.extension != null) {
                this.extension = nupdate.extension;
            }
        }
        return this;
    }

    @Override
    public boolean isFileResource() {
        return true;
    }

    @Override
    public FileResourceReference refer() {
        return new FileResourceReference(this.getId());
    }

    @Override
    public String toResourceString() {
        return String.format("/resources/%s", this.getId());
    }

}
