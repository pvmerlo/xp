package com.enonic.xp.core.impl.app;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationKeys;
import com.enonic.xp.app.ApplicationNotFoundException;
import com.enonic.xp.app.Applications;
import com.enonic.xp.core.impl.app.event.ApplicationClusterEvents;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.event.Event;
import com.enonic.xp.event.EventPublisher;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.Nodes;

import static org.junit.Assert.*;

public class ApplicationServiceImplTest
    extends BundleBasedTest
{
    private ApplicationServiceImpl service;

    private final ApplicationRepoServiceImpl repoService = Mockito.mock( ApplicationRepoServiceImpl.class );

    private EventPublisher eventPublisher;

    @Before
    public void initService()
    {
        this.service = new ApplicationServiceImpl();
        this.service.setRepoService( this.repoService );
        this.eventPublisher = Mockito.mock( EventPublisher.class );
        this.service.setEventPublisher( this.eventPublisher );
    }

    @Test
    public void get_application()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Bundle bundle = deployBundle( "app1", true );

        final Application result = this.service.getInstalledApplication( ApplicationKey.from( "app1" ) );
        assertNotNull( result );
        assertSame( bundle, result.getBundle() );
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void get_application_not_found()
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        this.service.getInstalledApplication( ApplicationKey.from( "app1" ) );
    }

    @Test
    public void get_all_applications()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        deployBundle( "app1", true );
        deployBundle( "app2", true );
        deployBundle( "app3", false );

        final Applications result = this.service.getInstalledApplications();
        assertNotNull( result );
        assertEquals( 2, result.getSize() );
    }

    @Test
    public void get_application_keys()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        deployBundle( "app1", true );
        deployBundle( "app2", true );
        deployBundle( "app3", false );

        final ApplicationKeys result = this.service.getInstalledApplicationKeys();
        assertNotNull( result );
        assertEquals( 2, result.getSize() );
        assertTrue( result.contains( ApplicationKey.from( "app1" ) ) );
        assertTrue( result.contains( ApplicationKey.from( "app2" ) ) );
    }

    @Test
    public void start_application()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Bundle bundle = deployBundle( "app1", true );

        assertEquals( Bundle.INSTALLED, bundle.getState() );
        this.service.startApplication( ApplicationKey.from( "app1" ), false );
        assertEquals( Bundle.ACTIVE, bundle.getState() );
    }

    @Test
    public void stop_application()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Bundle bundle = deployBundle( "app1", true );
        bundle.start();

        assertEquals( Bundle.ACTIVE, bundle.getState() );
        this.service.stopApplication( ApplicationKey.from( "app1" ), false );
        assertEquals( Bundle.RESOLVED, bundle.getState() );
    }

    @Test
    public void install_cluster()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Node applicationNode = Node.create().
            id( NodeId.from( "myNode" ) ).
            parentPath( NodePath.ROOT ).
            name( "myNode" ).
            build();

        mockRepoCreateNode( applicationNode );
        mockRepoGetNode( applicationNode );

        final ByteSource byteSource = createBundleSource();

        final Application application = this.service.installClusterApplication( byteSource );

        assertNotNull( application );
        assertEquals( "my-bundle", application.getKey().getName() );
        assertFalse( this.service.isLocalApplication( application.getKey() ) );

        Mockito.verify( this.eventPublisher, Mockito.times( 1 ) ).publish(
            Mockito.argThat( new ApplicationEventMatcher( ApplicationClusterEvents.installed( applicationNode ) ) ) );

        Mockito.verify( this.eventPublisher, Mockito.times( 1 ) ).publish(
            Mockito.argThat( new ApplicationEventMatcher( ApplicationClusterEvents.started( application.getKey() ) ) ) );
    }

    private ByteSource createBundleSource()
        throws IOException
    {
        final InputStream in = newBundle( "my-bundle", true ).
            build();

        return ByteSource.wrap( ByteStreams.toByteArray( in ) );
    }

    private void mockRepoGetNode( final Node applicationNode )
    {
        Mockito.when( this.repoService.getApplicationNode( "my-bundle" ) ).
            thenReturn( applicationNode );
    }

    @Test
    public void install_local()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Node applicationNode = Node.create().
            id( NodeId.from( "myNode" ) ).
            parentPath( NodePath.ROOT ).
            name( "myNode" ).
            build();

        mockRepoCreateNode( applicationNode );
        mockRepoGetNode( applicationNode );

        final ByteSource byteSource = createBundleSource();

        final Application application = this.service.installLocalApplication( byteSource );

        assertNotNull( application );
        assertEquals( "my-bundle", application.getKey().getName() );
        assertTrue( this.service.isLocalApplication( application.getKey() ) );

        Mockito.verify( this.eventPublisher, Mockito.never() ).publish(
            Mockito.argThat( new ApplicationEventMatcher( ApplicationClusterEvents.installed( applicationNode ) ) ) );

        Mockito.verify( this.eventPublisher, Mockito.never() ).publish(
            Mockito.argThat( new ApplicationEventMatcher( ApplicationClusterEvents.started( application.getKey() ) ) ) );
    }

    @Test
    public void update_installed_application()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Node node = Node.create().
            id( NodeId.from( "myNode" ) ).
            parentPath( NodePath.ROOT ).
            name( "myNode" ).
            build();

        mockRepoCreateNode( node );

        Mockito.when( this.repoService.updateApplicationNode( Mockito.isA( Application.class ), Mockito.isA( ByteSource.class ) ) ).
            thenReturn( node );

        mockRepoGetNode( node );

        final Application originalApplication =
            this.service.installClusterApplication( ByteSource.wrap( ByteStreams.toByteArray( newBundle( "my-bundle", true, "1.0.0" ).
                build() ) ) );

        mockRepoGetNode( node );

        final Application updatedApplication =
            this.service.installClusterApplication( ByteSource.wrap( ByteStreams.toByteArray( newBundle( "my-bundle", true, "1.0.1" ).
                build() ) ) );

        assertEquals( "1.0.0", originalApplication.getVersion().toString() );
        assertEquals( "1.0.1", updatedApplication.getVersion().toString() );
        assertFalse( this.service.isLocalApplication( updatedApplication.getKey() ) );
    }

    private void mockRepoCreateNode( final Node node )
    {
        Mockito.when( this.repoService.createApplicationNode( Mockito.isA( Application.class ), Mockito.isA( ByteSource.class ) ) ).
            thenReturn( node );
    }

    @Test
    public void update_installed_local_application()
        throws Exception
    {
        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.empty() );

        this.service.activate( getBundleContext() );

        final Node node = Node.create().
            id( NodeId.from( "myNode" ) ).
            parentPath( NodePath.ROOT ).
            name( "myNode" ).
            build();

        mockRepoCreateNode( node );

        Mockito.when( this.repoService.updateApplicationNode( Mockito.isA( Application.class ), Mockito.isA( ByteSource.class ) ) ).
            thenReturn( node );

        mockRepoGetNode( node );

        final Application originalApplication =
            this.service.installLocalApplication( ByteSource.wrap( ByteStreams.toByteArray( newBundle( "my-bundle", true, "1.0.0" ).
                build() ) ) );

        final Application updatedApplication =
            this.service.installLocalApplication( ByteSource.wrap( ByteStreams.toByteArray( newBundle( "my-bundle", true, "1.0.1" ).
                build() ) ) );

        assertEquals( "1.0.0", originalApplication.getVersion().toString() );
        assertEquals( "1.0.1", updatedApplication.getVersion().toString() );
        assertTrue( this.service.isLocalApplication( updatedApplication.getKey() ) );
    }


    @Test
    public void install_applications_stored_in_repo()
        throws Exception
    {
        final PropertyTree data = new PropertyTree();
        data.setBoolean( ApplicationPropertyNames.STARTED, true );

        final Node node = Node.create().
            id( NodeId.from( "myNodeId" ) ).
            name( "myBundle" ).
            parentPath( ApplicationRepoServiceImpl.APPLICATION_PATH ).
            data( data ).
            build();

        Mockito.when( this.repoService.getApplications() ).
            thenReturn( Nodes.create().
                add( node ).
                build() );

        Mockito.when( this.repoService.getApplicationSource( node.id() ) ).
            thenReturn( ByteSource.wrap( ByteStreams.toByteArray( newBundle( "my-bundle", true, "1.0.1" ).
                build() ) ) );

        this.service.activate( getBundleContext() );

        Mockito.verify( this.repoService, Mockito.times( 1 ) ).getApplications();
        Mockito.verify( this.repoService, Mockito.times( 1 ) ).getApplicationSource( node.id() );
        Mockito.verify( this.eventPublisher, Mockito.never() ).publish( Mockito.isA( Event.class ) );
    }

    private Bundle deployBundle( final String key, final boolean isApp )
        throws Exception
    {
        final InputStream in = newBundle( key, isApp ).
            build();

        return deploy( key, in );
    }

    private class ApplicationEventMatcher
        extends ArgumentMatcher<Event>
    {
        Event thisObject;

        public ApplicationEventMatcher( Event thisObject )
        {
            this.thisObject = thisObject;
        }

        @Override
        public boolean matches( Object argument )
        {
            if ( argument == null || thisObject.getClass() != argument.getClass() )
            {
                return false;
            }

            final Event event = (Event) argument;

            return thisObject.getType().equals( event.getType() ) && this.thisObject.getData().equals( event.getData() );
        }
    }

}
