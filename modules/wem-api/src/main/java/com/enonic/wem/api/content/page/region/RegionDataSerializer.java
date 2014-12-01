package com.enonic.wem.api.content.page.region;


import com.enonic.wem.api.content.page.PageComponent;
import com.enonic.wem.api.content.page.PageComponentsDataSerializer;
import com.enonic.wem.api.data2.PropertySet;
import com.enonic.wem.api.support.serializer.AbstractDataSetSerializer;

import static com.enonic.wem.api.content.page.region.Region.newRegion;

public class RegionDataSerializer
    extends AbstractDataSetSerializer<Region, Region>
{
    private static final String NAME = "name";

    private final String propertyName;

    private final PageComponentsDataSerializer componentsSerializer = new PageComponentsDataSerializer();

    public RegionDataSerializer()
    {
        this.propertyName = "region";
    }

    public void toData( final Region region, final PropertySet parent )
    {
        final PropertySet asData = parent.addSet( propertyName );
        asData.addString( "name", region.getName() );

        componentsSerializer.toData( region.getComponents(), asData );
    }

    public Region fromData( final PropertySet asData )
    {
        final Region.Builder region = newRegion();
        region.name( asData.getString( NAME ) );

        for ( PageComponent component : componentsSerializer.fromData( asData.getProperties( "component" ) ) )
        {
            region.add( component );
        }

        return region.build();
    }
}
