/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.service.implementation;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.picdrop.exception.ApplicationException;
import com.picdrop.exception.ErrorMessageCode;
import static com.picdrop.helper.LogHelper.*;
import com.picdrop.model.RequestContext;
import com.picdrop.model.Share;
import com.picdrop.model.TokenSet;
import com.picdrop.model.resource.Collection;
import com.picdrop.model.resource.FileResource;
import com.picdrop.model.user.RegisteredUser;
import com.picdrop.model.user.User;
import com.picdrop.repository.Repository;
import com.picdrop.security.authentication.Permission;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author i330120
 */
@Path("/app/users")
@Consumes("application/json")
@Produces("application/json")
public class RegisteredUserService {

    Logger log = LogManager.getLogger(this.getClass());

    Repository<String, RegisteredUser> repo;
    Repository<String, TokenSet> tsrepo;
    Repository<String, FileResource> frepo;
    Repository<String, Collection> crepo;
    Repository<String, Share> srepo;
    Repository<String, Collection.CollectionItem> cirepo;

    FileResourceService fileService;

    @Inject
    Provider<RequestContext> contextProv;

    Pattern emailPattern = Pattern.compile("^[^@]+[@][^@]+[.][^@]+$");

    @Inject
    public RegisteredUserService(Repository<String, RegisteredUser> repo,
            Repository<String, TokenSet> tsrepo,
            Repository<String, FileResource> frepo,
            Repository<String, Collection> crepo,
            Repository<String, Collection.CollectionItem> cirepo,
            Repository<String, Share> srepo
    ) {
        this.repo = repo;
        this.tsrepo = tsrepo;
        this.frepo = frepo;
        this.crepo = crepo;
        this.cirepo = cirepo;
        this.srepo = srepo;
        log.trace(SERVICE, "created with ({},{},{},{},{},{})", repo, tsrepo, frepo, crepo, cirepo, srepo);
    }

    @Inject
    public void setEmailPattern(@Named("service.validation.user.email.regex") String pattern) {
        emailPattern = Pattern.compile(pattern);
    }

    @Inject
    public void setFileService(FileResourceService fileService) {
        this.fileService = fileService;
    }

    @POST
    @Path("/")
    public RegisteredUser create(RegisteredUser entity) throws ApplicationException {
        log.entry(entity);
        if (entity == null) {
            throw new ApplicationException()
                    .status(400)
                    .code(ErrorMessageCode.BAD_REQUEST_BODY);
        }
        log.debug(SERVICE, "Validating user attributes");
        if (Strings.isNullOrEmpty(entity.getPhash())) {
            throw new ApplicationException()
                    .code(ErrorMessageCode.BAD_PHASH)
                    .status(400);
        }
        if (Strings.isNullOrEmpty(entity.getEmail())) {
            throw new ApplicationException()
                    .code(ErrorMessageCode.BAD_EMAIL)
                    .status(400);
        } else if (!emailPattern.matcher(entity.getEmail()).matches()) {
            throw new ApplicationException()
                    .code(ErrorMessageCode.BAD_EMAIL)
                    .status(400);
        }
        if (Strings.isNullOrEmpty(entity.getName())) {
            entity.setName("PicdropUser");
        } else if (entity.getName().length() > 256) {
            throw new ApplicationException()
                    .status(400)
                    .code(ErrorMessageCode.BAD_NAME_TOO_LONG);
        }
        if (!Strings.isNullOrEmpty(entity.getLastname()) && entity.getLastname().length() > 256) {
            throw new ApplicationException()
                    .status(400)
                    .code(ErrorMessageCode.BAD_NAME_TOO_LONG);
        }

        entity = repo.save(entity);

        log.info(SERVICE, "User created");
        log.traceExit();
        return entity;
    }

    @GET
    @Path("/me")
    @Permission("read")
    public User getMe() {
        return contextProv.get().getPrincipal();
    }

    @DELETE
    @Path("/me")
    @Permission("write")
    public void deleteMe() {
        log.traceEntry();
        User me = contextProv.get().getPrincipal();
        if (me != null) {
            repo.delete(me.getId());
            log.debug(SERVICE, "Deleting all owned resources");
            try {
                tsrepo.deleteNamed("with.owner", me.getId());
                cirepo.deleteNamed("with.owner", me.getId());
                crepo.deleteNamed("with.owner", me.getId());
                List<FileResource> fres = frepo.queryNamed("with.owner", me.getId());
                for (FileResource fr : fres) {
                    try {
                        this.fileService.processDelete(fr);
                    } catch (ApplicationException ex) {
                        log.warn(SERVICE, "Error during removal of owned file resources: " + ex.getMessage(), ex.getCause());
                    }
                }
                srepo.deleteNamed("with.owner", me.getId());
            } catch (IOException ex) {
                log.warn(SERVICE, "Error during removal of owned resources and tokens", ex);
            }
        }
        log.info(SERVICE, "User deleted");
        log.traceExit();
    }

    @PUT
    @Path("/me")
    @Permission("write")
    public RegisteredUser updateMe(RegisteredUser entity) throws ApplicationException {
        log.entry(entity);
        RegisteredUser me = contextProv.get().getPrincipal().to(RegisteredUser.class);
        if (me == null) {
            throw new ApplicationException()
                    .status(404)
                    .code(ErrorMessageCode.NOT_FOUND)
                    .devMessage("No principal set");
        }

        log.debug(SERVICE, "Performing object merge");
        try {
            me = me.merge(entity);
        } catch (IOException ex) {
            throw new ApplicationException(ex)
                    .status(500)
                    .code(ErrorMessageCode.ERROR_OBJ_MERGE)
                    .devMessage(ex.getMessage());
        }
        me = repo.update(me.getId(), me);

        log.info(SERVICE, "User updated");
        log.traceExit();
        return me;
    }

}
