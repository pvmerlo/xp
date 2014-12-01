package com.enonic.wem.api.content.page;

import org.junit.Test;

import com.enonic.wem.api.content.page.layout.LayoutComponent;
import com.enonic.wem.api.content.page.layout.LayoutDescriptorKey;
import com.enonic.wem.api.content.page.part.PartComponent;
import com.enonic.wem.api.content.page.part.PartDescriptorKey;
import com.enonic.wem.api.data2.PropertyTree;

import static com.enonic.wem.api.content.page.Page.newPage;
import static com.enonic.wem.api.content.page.layout.LayoutComponent.newLayoutComponent;
import static com.enonic.wem.api.content.page.part.PartComponent.newPartComponent;
import static org.junit.Assert.*;

public class ComponentsTest
{

    @Test
    public void page()
    {
        PropertyTree pageConfig = new PropertyTree( new PropertyTree.PredictivePropertyIdProvider() );
        pageConfig.addLong( "pause", 200L );

        Page page = newPage().
            template( PageTemplateKey.from( "pageTemplateName" ) ).
            config( pageConfig ).
            regions( PageRegions.newPageRegions().build() ).
            build();

        assertEquals( "pageTemplateName", page.getTemplate().toString() );
    }

    @Test
    public void part()
    {
        PropertyTree partConfig = new PropertyTree( new PropertyTree.PredictivePropertyIdProvider() );
        partConfig.addLong( "width", 150L );

        PartComponent partComponent = newPartComponent().
            name( "my-part" ).
            descriptor( PartDescriptorKey.from( "mainmodule:partTemplateName" ) ).
            config( partConfig ).
            build();

        assertEquals( "my-part", partComponent.getName().toString() );
        assertEquals( "partTemplateName", partComponent.getDescriptor().getName().toString() );
        assertEquals( "mainmodule", partComponent.getDescriptor().getModuleKey().toString() );
    }

    @Test
    public void layout()
    {
        PropertyTree layoutConfig = new PropertyTree( new PropertyTree.PredictivePropertyIdProvider() );
        layoutConfig.addLong( "columns", 2L );

        LayoutComponent layoutComponent = newLayoutComponent().
            name( "my-template" ).
            descriptor( LayoutDescriptorKey.from( "mainmodule:layoutTemplateName" ) ).
            config( layoutConfig ).
            build();

        assertEquals( "my-template", layoutComponent.getName().toString() );
        assertEquals( "layoutTemplateName", layoutComponent.getDescriptor().getName().toString() );
        assertEquals( "mainmodule", layoutComponent.getDescriptor().getModuleKey().toString() );
    }
}
