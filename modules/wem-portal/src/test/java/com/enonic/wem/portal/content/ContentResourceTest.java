package com.enonic.wem.portal.content;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;

import com.enonic.wem.api.Client;
import com.enonic.wem.api.account.UserKey;
import com.enonic.wem.api.command.content.GetContentByPath;
import com.enonic.wem.api.command.content.page.GetPageDescriptor;
import com.enonic.wem.api.command.content.page.GetPageTemplateByKey;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.page.ComponentDescriptorName;
import com.enonic.wem.api.content.page.Page;
import com.enonic.wem.api.content.page.PageDescriptor;
import com.enonic.wem.api.content.page.PageDescriptorKey;
import com.enonic.wem.api.content.page.PageTemplate;
import com.enonic.wem.api.content.page.PageTemplateKey;
import com.enonic.wem.api.content.page.PageTemplateName;
import com.enonic.wem.api.content.site.SiteTemplateKey;
import com.enonic.wem.api.data.Property;
import com.enonic.wem.api.data.RootDataSet;
import com.enonic.wem.api.data.Value;
import com.enonic.wem.api.module.ModuleKey;
import com.enonic.wem.api.module.ModuleResourceKey;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.content.ContentTypeNames;
import com.enonic.wem.portal.controller.JsControllerFactoryImpl;
import com.enonic.wem.portal.controller.JsHttpRequest;
import com.enonic.wem.portal.postprocess.JavaElExpressionExecutor;
import com.enonic.wem.portal.postprocess.PostProcessor;
import com.enonic.wem.portal.postprocess.PostProcessorFactory;
import com.enonic.wem.portal.postprocess.PostProcessorString;
import com.enonic.wem.portal.script.compiler.ScriptCacheImpl;
import com.enonic.wem.portal.script.compiler.ScriptCompilerImpl;
import com.enonic.wem.portal.script.lib.ContextScriptBean;
import com.enonic.wem.portal.script.lib.SystemScriptBean;
import com.enonic.wem.portal.script.loader.ScriptLoader;
import com.enonic.wem.portal.script.loader.ScriptSourceImpl;
import com.enonic.wem.portal.script.runner.ScriptRunner;
import com.enonic.wem.portal.script.runner.ScriptRunnerFactoryImpl;
import com.enonic.wem.portal.script.runner.ScriptRunnerImpl;
import com.enonic.wem.web.servlet.ServletRequestHolder;
import com.enonic.wem.xml.XmlSerializers;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentResourceTest
{
    public static class Console
    {
        String line;

        public void log( String line )
        {
            this.line = line;
        }
    }

    private Console console = new Console();

    @Test
    public void testScript()
        throws Exception
    {
        final HttpServletRequest httpServletRequest = mock( HttpServletRequest.class );
        when( httpServletRequest.getScheme() ).thenReturn( "http" );
        ServletRequestHolder.setRequest( httpServletRequest );

        final HttpRequestContext requestContext = mock( HttpRequestContext.class );
        when( requestContext.getMethod() ).thenReturn( "method" );

        final ContentResource contentResource = new ContentResource();
        contentResource.contentPath = "content";
        contentResource.client = mock( Client.class );
        contentResource.httpContext = mock( HttpContext.class );

        final Path path = Files.createTempFile( "prefix", "js" );
        final String script = readFromFile( "script-page-component.js" );
        IOUtils.copy( new ByteArrayInputStream( script.getBytes() ), new FileOutputStream( path.toFile() ) );

        final ScriptLoader myScriptLoader = mock( ScriptLoader.class );
        when( myScriptLoader.load( isA( ModuleResourceKey.class ) ) ).thenReturn(
            new ScriptSourceImpl( ModuleResourceKey.from( "mainmodule-1.0.0:/components/landing-page.xml" ), path ) );

        final JsControllerFactoryImpl jsControllerFactory = new JsControllerFactoryImpl();

        final ScriptRunnerFactoryImpl scriptRunnerFactory = new ScriptRunnerFactoryImpl()
        {
            @Override
            public ScriptRunner newRunner()
            {
                final ScriptRunnerImpl scriptRunner = (ScriptRunnerImpl) super.newRunner();
                scriptRunner.setScriptLoader( myScriptLoader );
                scriptRunner.setContextServiceBean( new ContextScriptBean() );
                scriptRunner.setCompiler( new ScriptCompilerImpl( new ScriptCacheImpl() ) );

                final HttpRequestContext httpRequestContext = mock( HttpRequestContext.class );

                final JsHttpRequest jsHttpRequest = new JsHttpRequest( httpRequestContext )
                {
                    @Override
                    public String getPath()
                    {
                        return "/path";
                    }

                    @Override
                    public <T> T getEntity( final Class<T> tClass )
                        throws WebApplicationException
                    {
                        throw new WebApplicationException();
                    }
                };

                scriptRunner.property( "request", jsHttpRequest );
                scriptRunner.property( "console", console );

                return scriptRunner;
            }
        };

        final PostProcessorFactory postProcessorFactory = new PostProcessorFactory()
        {
            @Override
            public PostProcessor newPostProcessor()
            {
                final PostProcessorString postProcessor = new PostProcessorString();
                try
                {
                    postProcessor.setExpressionExecutor( new JavaElExpressionExecutor( false ) );
                }
                catch ( Exception e )
                {
                    throw Throwables.propagate( e );
                }
                return postProcessor;
            }
        };

        final SystemScriptBean systemScriptBean = new SystemScriptBean();
        scriptRunnerFactory.setSystemScriptBean( systemScriptBean );
        scriptRunnerFactory.setContextServiceBeans( mock( Provider.class ) );
        jsControllerFactory.setScriptRunnerFactory( scriptRunnerFactory );
        jsControllerFactory.setPostProcessorFactory( postProcessorFactory );
        contentResource.controllerFactory = jsControllerFactory;

        when( contentResource.httpContext.getRequest() ).thenReturn( requestContext );

        when( contentResource.client.execute( isA( GetPageTemplateByKey.class ) ) ).thenReturn( createPageTemplate() );
        when( contentResource.client.execute( isA( GetContentByPath.class ) ) ).thenReturn(
            createPage( "id", "content", "contenttypename" ) );
        when( contentResource.client.execute( isA( GetPageDescriptor.class ) ) ).thenReturn( createDescriptor() );

        final Response response = contentResource.handleGet();
        assertEquals( 200, response.getStatus() );

        assertEquals( "/path", console.line );
    }

    private Content createPage( final String id, final String name, final String contentTypeName )
    {
        RootDataSet rootDataSet = new RootDataSet();

        Property dataSet = new Property( "property1", new Value.String( "value1" ) );
        rootDataSet.add( dataSet );

        Page page = Page.newPage().
            template( PageTemplateKey.from( "template-1.0.0|mymodule-1.0.0|my-page" ) ).
            config( rootDataSet ).
            build();

        return Content.newContent().
            id( ContentId.from( id ) ).
            path( ContentPath.from( name ) ).
            owner( UserKey.from( "myStore:me" ) ).
            displayName( "My Content" ).
            modifier( UserKey.superUser() ).
            type( ContentTypeName.from( contentTypeName ) ).
            page( page ).
            build();
    }

    private PageTemplate createPageTemplate()
    {
        final SiteTemplateKey siteTemplateKey = SiteTemplateKey.from( "mySiteTemplate-1.0.0" );

        final ModuleKey module = ModuleKey.from( "mymodule-1.0.0" );

        final RootDataSet pageTemplateConfig = new RootDataSet();
        pageTemplateConfig.addProperty( "pause", new Value.Long( 10000 ) );

        final PageTemplate pageTemplate = PageTemplate.newPageTemplate().
            key( PageTemplateKey.from( siteTemplateKey, module, new PageTemplateName( "my-page" ) ) ).
            displayName( "Main page template" ).
            config( pageTemplateConfig ).
            canRender( ContentTypeNames.from( "article", "banner" ) ).
            descriptor( PageDescriptorKey.from( "mainmodule-1.0.0:landing-page" ) ).
            build();

        return pageTemplate;
    }

    private PageDescriptor createDescriptor()
        throws Exception
    {
        final ModuleKey module = ModuleKey.from( "mainmodule-1.0.0" );
        final ComponentDescriptorName name = new ComponentDescriptorName( "mypage" );
        final PageDescriptorKey key = PageDescriptorKey.from( module, name );

        final String xml = readFromFile( "script-page-component.xml" );
        final PageDescriptor.Builder builder = PageDescriptor.newPageDescriptor();

        XmlSerializers.pageDescriptor().parse( xml ).to( builder );

        return builder.
            key( key ).
            displayName( "Landing page" ).
            name( name ).
            build();
    }

    private String readFromFile( final String fileName )
        throws Exception
    {
        final URL url = getClass().getResource( fileName );
        if ( url == null )
        {
            throw new IllegalArgumentException( "Resource file [" + fileName + "] not found" );
        }
        return Resources.toString( url, Charsets.UTF_8 );
    }
}
