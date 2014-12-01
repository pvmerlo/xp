package com.enonic.wem.api.form.inputtype;


import org.apache.commons.lang.StringUtils;

import com.enonic.wem.api.data2.Property;
import com.enonic.wem.api.data2.Value;
import com.enonic.wem.api.form.BreaksRequiredContractException;

final class Phone
    extends InputType
{
    Phone()
    {

    }

    @Override
    public void checkBreaksRequiredContract( final Property property )
    {
        final String stringValue = (String) property.getObject();
        if ( StringUtils.isBlank( stringValue ) )
        {
            throw new BreaksRequiredContractException( property, this );
        }
    }

    @Override
    public Value newValue( final String value )
    {
        return Value.newString( value );
    }

    @Override
    public InputTypeConfig getDefaultConfig()
    {
        return null;
    }

}

