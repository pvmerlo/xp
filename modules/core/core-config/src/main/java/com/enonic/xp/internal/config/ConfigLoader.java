package com.enonic.xp.internal.config;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleContext;

import com.enonic.xp.config.ConfigBuilder;
import com.enonic.xp.config.ConfigInterpolator;
import com.enonic.xp.config.Configuration;

final class ConfigLoader
{
    private ConfigInterpolator interpolator;

    ConfigLoader( final BundleContext context )
    {
        this.interpolator = new ConfigInterpolator();
        this.interpolator.bundleContext( context );
        this.interpolator.environment( System.getenv() );
        this.interpolator.systemProperties( System.getProperties() );
    }

    Hashtable<String, Object> load( final File file )
        throws Exception
    {
        final Properties props = new Properties();
        props.load( new FileReader( file ) );

        final ConfigBuilder builder = ConfigBuilder.create();
        builder.addAll( props );

        final Configuration config = this.interpolator.interpolate( builder.build() );
        final Hashtable<String, Object> result = new Hashtable<>();
        result.putAll( config.asMap() );
        return result;
    }
}
