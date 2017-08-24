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
import com.picdrop.guice.FileHandlingModule;
import com.picdrop.guice.RepositoryModuleMockNoDB;
import com.picdrop.helper.EnvHelper;
import com.picdrop.model.RequestContext;
import com.picdrop.model.Share;
import com.picdrop.model.resource.FileResource;
import com.picdrop.model.resource.Resource;
import com.picdrop.model.user.RegisteredUser;
import com.picdrop.model.user.User;
import com.picdrop.repository.AwareRepository;
import com.picdrop.repository.Repository;
import java.io.IOException;
import java.util.Map;
import static org.mockito.Mockito.*;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import static com.picdrop.helper.TestHelper.*;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nvonstein
 */
@RunWith(MockitoJUnitRunner.class)
public class ShareServiceTest {

    ShareService service;

    @Mock
    RequestContext ctx;
    @Mock
    AwareRepository<String, Share, User> repo;

    @Mock
    Repository<String, FileResource> frepo;

    Map<String, String> config = EnvHelper.getPropertiesTest();

    String ID1 = "590497a4cd27f408d6e79d98";
    String ID2 = "123456a4cd37f408d6e79d90";

    public ShareServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {

        Injector inj = Guice.createInjector(new ApplicationModuleMock(),
                new AuthorizationModuleMock(ctx),
                RepositoryModuleMockNoDB
                        .builder()
                        .shareRepo(repo)
                        .build(),
                new FileHandlingModule(),
                new RequestScopeModule());

        this.service = inj.getInstance(ShareService.class);
    }

    @After
    public void tearDown() {
    }

    @Test(expected = ApplicationException.class)
    public void getTestInvalidId() throws ApplicationException {
        Share dut;

        try {
            dut = service.get("");
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 404);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.NOT_FOUND);
            throw ex;
        } finally {
            verify(repo, times(1)).get(any(), eq(null));
            verify(repo, times(0)).get(ID1);
        }
    }

    @Test
    public void getTestValidId() throws ApplicationException {
        Share dut;

        when(repo.get(ID1, null)).thenReturn(new Share(ID1));

        dut = service.get(ID1);

        assertNotNull(dut);
        assertEquals("id differs!", ID1, dut.getId());
        verify(repo, times(1)).get(ID1, null);
        verify(repo, times(0)).get(ID1);
    }

    @Ignore
    @Test
    public void createTestFileResourceValid() throws Exception {
        Share obj = new Share();
        obj.setResource(new FileResource(ID2));

        when(ctx.getPrincipal()).thenReturn(new RegisteredUser(ID2));
        when(frepo.get(ID2)).thenReturn(new FileResource(ID2));
        when(repo.save(any())).thenAnswer(reflectWithId(0, ID1));

        Share dut = service.create(obj);

        assertNotNull("DUT is null", dut);
        assertEquals("Id mismatch", ID1, dut.getId());

        User owner = dut.getOwner();
        assertNotNull("Owner is null", owner);
        assertEquals("Id owner mismatch", ID2, owner.getId());

        Resource r = dut.getResource();
        assertNotNull("Resource is null", r);
        assertTrue("Share id not set on resource", r.getShareIds().contains(ID1));

        verify(repo).save(obj);
        verify(frepo, times(1)).get(ID2);
    }

    @Ignore
    @Test(expected = ApplicationException.class)
    public void createTestInvalidOwner() throws Exception {
        Share dut;
        Share obj = new Share();
        obj.setResource(new FileResource(ID2));

        when(ctx.getPrincipal()).thenReturn(new RegisteredUser(ID2));

        FileResource mock = mock(FileResource.class);
        when(mock.getOwner()).thenReturn(new RegisteredUser(ID1));

        when(frepo.get(ID2)).thenReturn(mock);
        when(repo.save(any())).thenAnswer(reflectWithId(0, ID1));

        try {
            dut = service.create(obj);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 404);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.NOT_FOUND);
            throw ex;
        } finally {
            verify(frepo, times(1)).get(ID2);
        }
    }

    @Test
    public void listTestValid() throws Exception {
        when(repo.list()).thenReturn(Arrays.asList(new Share(ID1)));

        List<Share> dsut = service.list();

        assertNotNull("list() returned null", dsut);
        assertTrue("Invalid length", dsut.size() == 1);

        verify(repo, times(1)).list();

        verify(repo, times(0)).list(any());
    }

    @Test
    public void deleteTestValid() throws Exception {
        Share mock = mock(Share.class);

        when(repo.delete(ID1)).thenReturn(true);
        when(repo.get(ID1)).thenReturn(mock);

        this.service.delete(ID1);

        verify(repo, times(1)).get(ID1);
        verify(repo, times(1)).delete(ID1);

        verify(repo, times(0)).get(any(), any());
        verify(repo, times(0)).delete(any(), any());
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestInvalidId() throws Exception {
        when(repo.get(ID1)).thenReturn(null);

        try {
            this.service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 404);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.NOT_FOUND);
            throw ex;
        } finally {
            verify(repo, times(1)).get(ID1);
            verify(repo, times(0)).delete(ID1);

            verify(repo, times(0)).get(any(), any());
            verify(repo, times(0)).delete(any(), any());
        }
    }

    @Test(expected = ApplicationException.class)
    public void deleteTestErrorOnDelete() throws Exception {
        Share mock = mock(Share.class);

        when(repo.delete(ID1)).thenReturn(false);
        when(repo.get(ID1)).thenReturn(mock);

        try {
            this.service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_DELETE);
            throw ex;
        } finally {
            verify(repo, times(1)).get(ID1);
            verify(repo, times(1)).delete(ID1);

            verify(repo, times(0)).get(any(), any());
            verify(repo, times(0)).delete(any(), any());
        }
    }

    @Test
    public void updateTestValid() throws Exception {
        Share obj1 = new Share(ID1);

        when(repo.get(ID1)).thenReturn(obj1);
        when(repo.update(ID1, obj1)).thenReturn(obj1);

        this.service.update(ID1, obj1);

        verify(repo, times(1)).get(ID1);
        verify(repo, times(1)).update(ID1, obj1);

        verify(repo, times(0)).get(any(), any());
        verify(repo, times(0)).update(any(), any(), any());
    }

    @Test(expected = ApplicationException.class)
    public void updateErrorOnMerge() throws Exception {
        Share mock = mock(Share.class);
        Share obj2 = new Share(ID2);

        when(repo.get(ID1)).thenReturn(mock);
        when(mock.merge(any())).thenThrow(new IOException());

        this.service.update(ID1, obj2);

        try {
            this.service.delete(ID1);
        } catch (ApplicationException ex) {
            assertEquals("Wrong http status code", ex.getStatus(), 500);
            assertEquals("Wrong error code", ex.getCode(), ErrorMessageCode.ERROR_OBJ_MERGE);
            throw ex;
        } finally {
            verify(repo, times(1)).get(ID1);
            verify(repo, times(0)).update(ID1, mock);

            verify(repo, times(0)).get(any(), any());
            verify(repo, times(0)).update(any(), any(), any());
        }
    }
}