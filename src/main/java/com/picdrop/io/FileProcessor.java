/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.io;

import com.picdrop.guice.provider.InputStreamProvider;
import java.io.IOException;

/**
 *
 * @author i330120
 */
public interface FileProcessor<T> {

    T process(T entity, InputStreamProvider in) throws IOException;
}