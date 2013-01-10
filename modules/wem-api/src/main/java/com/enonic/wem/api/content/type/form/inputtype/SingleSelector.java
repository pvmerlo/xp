package com.enonic.wem.api.content.type.form.inputtype;


import org.apache.commons.lang.StringUtils;

import com.enonic.wem.api.content.data.Data;
import com.enonic.wem.api.content.datatype.DataTypes;
import com.enonic.wem.api.content.datatype.InvalidValueTypeException;
import com.enonic.wem.api.content.type.form.BreaksRequiredContractException;
import com.enonic.wem.api.content.type.form.InvalidValueException;

public class SingleSelector
    extends BaseInputType
{
    public SingleSelector()
    {
        super( SingleSelectorConfig.class );
    }

    public AbstractInputTypeConfigJsonSerializer getInputTypeConfigJsonGenerator()
    {
        return SingleSelectorConfigJsonSerializer.DEFAULT;
    }

    @Override
    public AbstractInputTypeConfigXmlSerializer getInputTypeConfigXmlGenerator()
    {
        return SingleSelectorConfigXmlSerializer.DEFAULT;
    }

    @Override
    public void checkValidity( final Data data )
        throws InvalidValueTypeException, InvalidValueException
    {
        DataTypes.TEXT.checkValidity( data );
    }


    @Override
    public void ensureType( final Data data )
    {
        DataTypes.TEXT.ensureType( data );
    }

    @Override
    public void checkBreaksRequiredContract( final Data data )
        throws BreaksRequiredContractException
    {
        final String stringValue = (String) data.getObject();
        if ( StringUtils.isBlank( stringValue ) )
        {
            throw new BreaksRequiredContractException( data, this );
        }
    }

}
