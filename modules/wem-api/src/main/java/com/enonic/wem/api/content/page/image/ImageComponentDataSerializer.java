package com.enonic.wem.api.content.page.image;


import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.page.AbstractPageComponentDataSerializer;
import com.enonic.wem.api.data2.PropertySet;

public class ImageComponentDataSerializer
    extends AbstractPageComponentDataSerializer<ImageComponent, ImageComponent>
{
    public void toData( final ImageComponent component, final PropertySet parent )
    {
        final PropertySet asData = parent.addSet( ImageComponent.class.getSimpleName() );
        applyPageComponentToData( component, asData );
        if ( component.getImage() != null )
        {
            asData.addString( "image", component.getImage().toString() );
        }
        if ( component.hasConfig() )
        {
            asData.addSet( "config", component.getConfig().getRoot().copy( asData.getTree() ) );
        }
    }

    public ImageComponent fromData( final PropertySet asData )
    {
        ImageComponent.Builder component = ImageComponent.newImageComponent();
        applyPageComponentFromData( component, asData );
        if ( asData.isNotNull( "image" ) )
        {
            component.image( ContentId.from( asData.getString( "image" ) ) );
        }
        if ( asData.isNotNull( "config" ) )
        {
            component.config( asData.getSet( "config" ).toTree() );
        }
        return component.build();
    }
}
