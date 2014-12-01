package com.enonic.wem.api.form.inputtype;

import org.junit.Test;

import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.data2.Value;

import static junit.framework.Assert.assertEquals;


public class ImageSelectorTest
{
    @Test
    public void newValue()
    {
        Value value = new ImageSelector().newValue( "ABC" );
        assertEquals( ContentId.from( "ABC" ), value.asContentId() );
    }
}
