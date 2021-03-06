/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.service.implementation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.picdrop.exception.ApplicationException;
import com.picdrop.exception.ErrorMessageCode;
import com.picdrop.guice.ApplicationModuleMock;
import com.picdrop.guice.AuthorizationModuleMock;
import com.picdrop.guice.CryptoModule;
import com.picdrop.guice.FileHandlingModuleMock;
import com.picdrop.guice.RepositoryModuleMockNoDB;
import com.picdrop.helper.TestHelper;
import com.picdrop.io.repository.FileRepository;
import com.picdrop.io.writer.FileReader;
import com.picdrop.io.writer.FileWriter;
import com.picdrop.model.FileType;
import com.picdrop.model.RequestContext;
import com.picdrop.model.Share;
import com.picdrop.model.ShareReference;
import com.picdrop.model.resource.Collection;
import com.picdrop.model.resource.FileResource;
import com.picdrop.model.resource.ImageDescriptor;
import com.picdrop.model.resource.ResourceDescriptor;
import com.picdrop.model.user.RegisteredUser;
import com.picdrop.model.user.User;
import com.picdrop.repository.AwareRepository;
import com.picdrop.repository.Repository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.tika.metadata.Metadata;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

/**
 *
 * @author nvonstein
 */
@RunWith(MockitoJUnitRunner.class)
public class FileResourceServiceTest {

    FileResourceService service;

    Repository<String, FileResource> repo;
    AwareRepository<String, Share, User> srepo;
    Repository<String, Collection.CollectionItem> cirepo;
    AwareRepository<String, Collection, User> crepo;

    ApplicationModuleMock appModule = new ApplicationModuleMock();

    @Mock
    FileWriter writer;
    @Mock
    FileReader reader;
    @Mock
    RequestContext ctx;
    @Mock
    FileRepository<String> fr;

    String ID1 = "590497a4cd27f408d6e79d98";
    String ID2 = "123456a4cd37f408d6e79d90";

    public FileResourceServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {

        RepositoryModuleMockNoDB repoModule = new RepositoryModuleMockNoDB();

        Injector inj = Guice.createInjector(appModule,
                new AuthorizationModuleMock(ctx),
                new CryptoModule(),
                repoModule,
                new FileHandlingModuleMock(fr),
                new RequestScopeModule());

        this.crepo = repoModule.getCrepo();
        this.cirepo = repoModule.getCirepo();
        this.repo = repoModule.getRrepo();
        this.srepo = repoModule.getSrepo();

        this.service = inj.getInstance(FileResourceService.class);
    }

    @After
    public void tearDown() {
    }

    @Test(expected = ApplicationException.class)
    public void getTestInvalidId() throws ApplicationException {
        FileResource file;

        try {
            file = service.getResource("");
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 404);
            throw ex;
        } finally {
            verify(repo, times(1)).get(any());
        }
    }

    @Test
    public void getTestValidId() throws ApplicationException {
        FileResource file;

        when(repo.get(ID1)).thenReturn(new FileResource(ID1));

        file = service.getResource(ID1);

        assertNotNull(file);
        assertEquals("id differs!", ID1, file.getId());
        verify(repo, times(1)).get(ID1);
    }

    @Test
    public void deleteTestValid() throws IOException, ApplicationException {
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);
        file.addShare(new ShareReference(ID2));

        ImageDescriptor desc = ResourceDescriptor.get(FileType.IMAGE_JPEG)
                .to(ImageDescriptor.class);
        desc.addThumbnailUri("test", "test");
        file.setDescriptor(desc);

        Collection c = new Collection(ID2);
        Collection.CollectionItem ci = new Collection.CollectionItem(ID2);
        c.addItem(ci);
        ci.setParentCollection(c);

        when(repo.get(ID1)).thenReturn(file);
        when(cirepo.queryNamed(eq("citems.with.resource"), any()))
                .thenReturn(Arrays.asList(ci));
        when(crepo.get(ID2)).thenReturn(c);
        when(repo.delete(ID1)).thenReturn(true);
        when(fr.delete(ID1)).thenReturn(true);
        when(srepo.delete(ID2)).thenReturn(true);

        service.delete(ID1);

        verify(repo, times(1)).delete(ID1); // FileResource
        verify(srepo, times(1)).delete(ID2); // Active Shares
        verify(crepo, times(1)).update(eq(ID2), any()); // Update Collection with item
        verify(cirepo, times(1)).delete(ID2); // Collection item for this resource
        verify(fr, times(1)).delete(ID1); // File

        // verify(fp, times(2)).delete("test") // Thumbnails
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestInvalid() throws ApplicationException {
        when(repo.get(ID1)).thenReturn(null);

        try {
            service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 404);
            throw ex;
        } finally {
            verify(repo, times(0)).delete(ID1);
        }
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestErrorOnRepoDeletion() throws IOException, ApplicationException {
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);
        ImageDescriptor desc = ResourceDescriptor.get(FileType.IMAGE_JPEG)
                .to(ImageDescriptor.class);
        desc.addThumbnailUri("test", "test");

        file.setDescriptor(desc);

        when(repo.delete(ID1)).thenReturn(false);
        when(repo.get(ID1)).thenReturn(file);

        try {
            service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_DELETE);
            throw ex;
        } finally {
            verify(repo, times(1)).delete(ID1);
            verify(fr, times(0)).delete(ID1);
        }
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestErrorOnShareRepoDeletion() throws IOException, ApplicationException {
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);
        file.addShare(new ShareReference(ID2));
        ImageDescriptor desc = ResourceDescriptor.get(FileType.IMAGE_JPEG)
                .to(ImageDescriptor.class);
        desc.addThumbnailUri("test", "test");

        file.setDescriptor(desc);

        when(srepo.delete(ID2)).thenReturn(false);
        when(repo.get(ID1)).thenReturn(file);

        try {
            service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_DELETE);
            throw ex;
        } finally {
            verify(srepo, times(1)).delete(ID2);
//            verify(repo, times(0)).delete(ID1); // No hard constraint
            verify(fr, times(0)).delete(ID1);
        }
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestFailedFileDeletion() throws IOException, ApplicationException {
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);
        ImageDescriptor desc = ResourceDescriptor.get(FileType.IMAGE_JPEG)
                .to(ImageDescriptor.class);
        desc.addThumbnailUri("test", "test");

        file.setDescriptor(desc);

        when(repo.get(ID1)).thenReturn(file);
        when(repo.delete(ID1)).thenReturn(true);
        when(fr.delete(ID1)).thenReturn(false);

        try {
            service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_DELETE);
            throw ex;
        } finally {
            verify(repo, times(1)).delete(ID1);
            verify(fr, times(1)).delete(ID1);

            // Verify Roleback
            verify(repo, times(1)).save(file);
        }
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestErrorOnFileDeletion() throws IOException, ApplicationException {
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);
        ImageDescriptor desc = ResourceDescriptor.get(FileType.IMAGE_JPEG)
                .to(ImageDescriptor.class);
        desc.addThumbnailUri("test", "test");

        file.setDescriptor(desc);

        when(repo.get(ID1)).thenReturn(file);
        when(repo.delete(ID1)).thenReturn(true);
        when(fr.delete(ID1)).thenThrow(new IOException("An error occured"));

        try {
            service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_DELETE);
            throw ex;
        } finally {
            verify(repo, times(1)).delete(ID1);
            verify(fr, times(1)).delete(ID1);

            // Verify Roleback
            verify(repo, times(1)).save(file);
        }
    }

    @Test
    public void createTestPngValid() throws IOException, ApplicationException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture.png", "image/png", "somedata".getBytes()));

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("image/png");

        RegisteredUser u = new RegisteredUser(ID1);
        u.setSizeLimit(1000000000);
        when(ctx.getPrincipal()).thenReturn(u);
        when(repo.save(any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                FileResource fr = arg0.getArgument(0);
                fr.setId(ID1);
                return fr;
            }
        });
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });
//        doReturn(FileType.IMAGE_PNG).when(service).parseMimeType(any());

        List<FileResource> files = service.create(req);

        assertEquals("Length is not 1", 1, files.size());
        assertNotNull("Resource is null", files.get(0));
        FileResource file = files.get(0);

        assertNotNull("Descriptor is null", file.getDescriptor());
        assertTrue("Invalid descriptor type", file.getDescriptor() instanceof ImageDescriptor);
        ImageDescriptor desc = file.getDescriptor().to(ImageDescriptor.class);

        assertEquals("FileType incorrect", desc.getType(), FileType.IMAGE_PNG);

        // Thumbnails posponed to webserver
//        assertNotNull("No thumbnails set", desc.getThumbnailUris());
//        assertEquals("Incorrect number of thumbnails", 2, desc.getThumbnailUris().size());
        assertNotNull("Owner not set", file.getOwner());
        assertEquals("Owner Id not match", ID1, files.get(0).getOwner().getId());

        verify(fr, atLeastOnce()).write(any(), any()); // Write of main file
    }

    @Test
    public void createTestJpegValid() throws IOException, ApplicationException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture.jpg", "image/jpeg", "somedata".getBytes()));

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("image/jpeg");

        RegisteredUser u = new RegisteredUser(ID1);
        u.setSizeLimit(1000000000);
        when(ctx.getPrincipal()).thenReturn(u);
        when(repo.save(any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                FileResource fr = arg0.getArgument(0);
                fr.setId(ID1);
                return fr;
            }
        });
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });
//        doReturn(FileType.IMAGE_JPEG).when(service).parseMimeType(any());

        List<FileResource> files = service.create(req);

        assertEquals("Length is not 1", 1, files.size());
        assertNotNull("Resource is null", files.get(0));
        FileResource file = files.get(0);

        assertNotNull("Descriptor is null", file.getDescriptor());
        assertTrue("Invalid descriptor type", file.getDescriptor() instanceof ImageDescriptor);
        ImageDescriptor desc = file.getDescriptor().to(ImageDescriptor.class);

        assertEquals("FileType incorrect", desc.getType(), FileType.IMAGE_JPEG);

        // Thumbnails posponed to webserver
//        assertNotNull("No thumbnails set", desc.getThumbnailUris());
//        assertEquals("Incorrect number of thumbnails", 2, desc.getThumbnailUris().size());
        assertNotNull("Owner not set", file.getOwner());
        assertEquals("Owner Id not match", ID1, files.get(0).getOwner().getId());

        verify(fr, atLeastOnce()).write(any(), any()); // Write of main file
    }

    @Test
    public void createTestTiffValid() throws IOException, ApplicationException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture.tiff", "image/tiff", "somedata".getBytes()));

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("image/tiff");

        RegisteredUser u = new RegisteredUser(ID1);
        u.setSizeLimit(1000000000);
        when(ctx.getPrincipal()).thenReturn(u);
        when(repo.save(any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                FileResource fr = arg0.getArgument(0);
                fr.setId(ID1);
                return fr;
            }
        });
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });
//        doReturn(FileType.IMAGE_TIFF).when(service).parseMimeType(any());

        List<FileResource> files = service.create(req);

        assertEquals("Length is not 1", 1, files.size());
        assertNotNull("Resource is null", files.get(0));
        FileResource file = files.get(0);

        assertNotNull("Descriptor is null", file.getDescriptor());
        assertTrue("Invalid descriptor type", file.getDescriptor() instanceof ImageDescriptor);
        ImageDescriptor desc = file.getDescriptor().to(ImageDescriptor.class);

        assertEquals("FileType incorrect", desc.getType(), FileType.IMAGE_TIFF);

        // Thumbnails posponed to webserver
//        assertNotNull("No thumbnails set", desc.getThumbnailUris());
//        assertEquals("Incorrect number of thumbnails", 2, desc.getThumbnailUris().size());
        assertNotNull("Owner not set", file.getOwner());
        assertEquals("Owner Id not match", ID1, files.get(0).getOwner().getId());

        verify(fr, atLeastOnce()).write(any(), any()); // Write of main file
    }

    @Ignore("Currently not testing uploaded data about conformity")
//    @Test(expected = ApplicationException.class)
    public void createTestJpegInvalidFileContent() throws IOException, ApplicationException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture", "image/jpeg", "somedata".getBytes()));

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("text/plain");

        when(ctx.getPrincipal()).thenReturn(new RegisteredUser(ID1));
        when(repo.save(any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                FileResource fr = arg0.getArgument(0);
                fr.setId(ID1);
                return fr;
            }
        });
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });

        try {
            List<FileResource> files = service.create(req);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 400);
            throw ex;
        } finally {
            verify(writer, times(0)).write(any(), any()); // Write of main file
        }
    }

    @Test()
    public void createTestErrorOnFileSave() throws IOException, ApplicationException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture", "image/jpeg", "somedata".getBytes()));

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("image/jpeg");

        RegisteredUser u = new RegisteredUser(ID1);
        u.setSizeLimit(1000000000);
        when(ctx.getPrincipal()).thenReturn(u);
        when(fr.write(any(), any())).thenThrow(new IOException("Some error occured!"));
//        doReturn(FileType.IMAGE_JPEG).when(service).parseMimeType(any());

        try {
            List<FileResource> files = service.create(req);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertTrue("Invalid supressed exception", ex.getCause() instanceof IOException);
            return;
        } finally {
            verify(repo, times(0)).save(any());
        }
        fail("No exception");
    }

    @Test
    public void updateFileTestValidFile() throws ApplicationException, IOException {
        HttpServletRequest req = TestHelper.generateFileRequest(new MockMultipartFile("file", "picture", "image/jpeg", "somedata".getBytes()));
        FileResource file = new FileResource(ID1);
        file.setFileId(ID1);

        when(appModule.getTika().detect(any(InputStream.class), any(Metadata.class))).thenReturn("image/jpeg");

        when(repo.get(ID1)).thenReturn(file);
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });
        when(fr.write(eq(ID1), any())).thenReturn(ID2);

        file = service.updateFile(ID1, req);

        assertNotNull("Resource is null", file);
        assertEquals("File was not updated", ID2, file.getFileId());
    }

    // TODO modify test to only test error proning. Move Merger test to sep. file
    @Test
    public void updateTestValidName() throws Exception {
        FileResource file = new FileResource(ID1);
        file.setName("some name");
        file.setFileId(ID1);

        FileResource fileMod = new FileResource(ID1);
        fileMod.setName(ID2);

        when(repo.get(ID1)).thenReturn(file);
        when(repo.update(eq(ID1), any())).thenAnswer(new Answer<FileResource>() {
            @Override
            public FileResource answer(InvocationOnMock arg0) throws Throwable {
                return arg0.getArgument(1);
            }
        });

        file = service.update(ID1, fileMod);

        assertNotNull("File is null", file);
        assertNotNull("Name is null", file.getName());
        assertEquals("Names differ", ID2, file.getName());
        assertEquals("File id overwritten", ID1, file.getFileId());
    }
}
