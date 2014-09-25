package com.enonic.wem.portal.internal.base;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.google.common.base.Charsets;

import com.enonic.wem.api.module.Module;
import com.enonic.wem.api.module.ModuleKey;
import com.enonic.wem.api.module.ModuleService;
import com.enonic.wem.api.resource.ResourceKey;

public abstract class ModuleBaseResourceTest<T extends ModuleBaseResource>
    extends BaseResourceTest<T>
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected Path tmpDir;

    protected ModuleService moduleService;

    @Override
    protected void configure()
        throws Exception
    {
        this.tmpDir = this.temporaryFolder.getRoot().toPath();

        this.moduleService = Mockito.mock( ModuleService.class );
        this.resource.moduleService = moduleService;
    }

    protected final void addResource( final String name, final String key, final String content )
        throws Exception
    {
        final Path filePath = this.tmpDir.resolve( name );
        Files.write( filePath, content.getBytes( Charsets.UTF_8 ) );

        final ResourceKey moduleResourceKey = ResourceKey.from( key );
        final Module module = Mockito.mock( Module.class );
        final URL resourcePathUrl = filePath.toUri().toURL();
        final String path = StringUtils.removeStart( moduleResourceKey.getPath(), "/" );
        Mockito.when( module.getResource( path ) ).thenReturn( resourcePathUrl );
        Mockito.when( this.moduleService.getModule( moduleResourceKey.getModule() ) ).thenReturn( module );
    }

    protected final void addModule( final String moduleName )
        throws Exception
    {
        final Module module = Mockito.mock( Module.class );
        Mockito.when( this.moduleService.getModule( ModuleKey.from( moduleName ) ) ).thenReturn( module );
    }
}
