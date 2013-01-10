package com.enonic.wem.api.content.type.form.inputtype;


import com.enonic.wem.api.content.data.Data;
import com.enonic.wem.api.content.data.DataSet;
import com.enonic.wem.api.content.datatype.DataTool;
import com.enonic.wem.api.content.datatype.DataTypes;
import com.enonic.wem.api.content.datatype.InvalidValueTypeException;
import com.enonic.wem.api.content.type.form.BreaksRequiredContractException;
import com.enonic.wem.api.content.type.form.InvalidValueException;

public class Image
    extends BaseInputType
{
    public Image()
    {
    }

    @Override
    public void checkValidity( final Data data )
        throws InvalidValueTypeException, InvalidValueException
    {
        DataTool.checkDataType( data, "binary", DataTypes.BLOB );
        DataTool.checkDataType( data, "caption", DataTypes.TEXT );
    }

    @Override
    public void ensureType( final Data data )
    {
        final DataSet dataSet = data.toDataSet();
        DataTypes.BLOB.ensureType( dataSet.getData( "binary" ) );
        DataTypes.TEXT.ensureType( dataSet.getData( "caption" ) );
    }

    @Override
    public void checkBreaksRequiredContract( final Data data )
        throws BreaksRequiredContractException
    {
        final DataSet dataSet = data.toDataSet();
        final Data binary = dataSet.getData( "binary" );
        if ( binary == null )
        {
            throw new BreaksRequiredContractException( data, this );
        }
    }

}

