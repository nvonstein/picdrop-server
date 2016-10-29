/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.guice.provider;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author i330120
 */
public class FileItemInputStreamProvider implements InputStreamProvider {

    FileItem fi;

    @AssistedInject
    public FileItemInputStreamProvider(@Assisted FileItem fi) {
        this.fi = fi;
    }

    @Override
    public InputStream get() throws IOException {
        return this.fi.getInputStream();
    }

}