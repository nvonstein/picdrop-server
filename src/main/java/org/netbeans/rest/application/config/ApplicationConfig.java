/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author i330120
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.picdrop.exception.ApplicationExeptionMapper.class);
        resources.add(com.picdrop.exception.FailureExceptionMapper.class);
        resources.add(com.picdrop.exception.IOExceptionMapper.class);
        resources.add(com.picdrop.exception.RuntimeExceptionMapper.class);
        resources.add(com.picdrop.exception.WebApplicationExceptionHandler.class);
        resources.add(com.picdrop.json.JacksonConfigProvider.class);
        resources.add(com.picdrop.service.filter.PermissionAuthenticationFilter.class);
        resources.add(com.picdrop.service.filter.ShareRewriteFilter.class);
        resources.add(com.picdrop.service.implementation.AuthorizationService.class);
        resources.add(com.picdrop.service.implementation.CollectionService.class);
        resources.add(com.picdrop.service.implementation.FileResourceService.class);
        resources.add(com.picdrop.service.implementation.RegisteredUserService.class);
        resources.add(com.picdrop.service.implementation.ShareService.class);
        resources.add(org.jboss.resteasy.plugins.providers.jackson.Jackson2JsonpInterceptor.class);
        resources.add(org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider.class);
        resources.add(org.jboss.resteasy.plugins.providers.jackson.UnrecognizedPropertyExceptionHandler.class);
    }
    
}
