/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.json;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.picdrop.guice.names.Config;
import java.util.Properties;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author nvonstein
 */
@Provider
public class JacksonConfigProvider implements ContextResolver<ObjectMapper> {

    @Config
    @Inject
    protected static Properties config;

//    @Inject
//    public JacksonConfigProvider(@Config Properties config) {
//        this.config = config;
//    }
    
    public ObjectMapper createMapper() {
        return createMapper(config.getProperty("service.json.view"));
    }

    public static ObjectMapper createMapper(String view) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setConfig(mapper.getDeserializationConfig().withView(Views.Public.class));
        mapper.setConfig(mapper.getSerializationConfig().withView(Views.Public.class));

        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        switch (view) {
            case "public":
                mapper.setConfig(mapper.getDeserializationConfig().withView(Views.Public.class));
                mapper.setConfig(mapper.getSerializationConfig().withView(Views.Public.class));
                break;
            case "detailed":
                mapper.setConfig(mapper.getDeserializationConfig().withView(Views.Detailed.class));
                mapper.setConfig(mapper.getSerializationConfig().withView(Views.Detailed.class));
                break;
            default:
                mapper.setConfig(mapper.getDeserializationConfig().withView(Views.Public.class));
                mapper.setConfig(mapper.getSerializationConfig().withView(Views.Public.class));
                break;
        }

        return mapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return createMapper();
    }
}
