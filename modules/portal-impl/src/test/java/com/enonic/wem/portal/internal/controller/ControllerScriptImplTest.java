package com.enonic.wem.portal.internal.controller;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.web.servlet.ServletRequestHolder;

import static org.junit.Assert.*;

public class ControllerScriptImplTest
    extends AbstractControllerTest
{
    @Before
    public void setUp()
    {
        final HttpServletRequest req = Mockito.mock( HttpServletRequest.class );
        ServletRequestHolder.setRequest( req );
    }

    @Test
    public void testExecute()
    {
        this.request.setMethod( "GET" );
        execute( "mymodule:/service/test" );
        assertEquals( PortalResponse.STATUS_OK, this.response.getStatus() );
    }

    @Test
    public void testExecutePostProcess()
    {
        this.request.setMethod( "GET" );
        this.response.setPostProcess( true );

        execute( "mymodule:/service/test" );

        assertEquals( PortalResponse.STATUS_OK, this.response.getStatus() );
        Mockito.verify( this.postProcessor ).processResponse( this.context );
    }

    @Test
    public void testMethodNotSupported()
    {
        this.request.setMethod( "POST" );
        execute( "mymodule:/service/test" );
        assertEquals( PortalResponse.STATUS_METHOD_NOT_ALLOWED, this.response.getStatus() );
    }

    @Test
    public void testGetterAccess()
    {
        this.request.setMethod( "GET" );
        execute( "mymodule:/service/getters" );
        assertEquals( PortalResponse.STATUS_OK, this.response.getStatus() );
        assertEquals( "GET,live", this.response.getBody() );
    }
}
