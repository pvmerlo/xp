package com.enonic.wem.core.content;

import com.enonic.wem.api.content.query.ContentQuery;
import com.enonic.wem.api.data.Value;
import com.enonic.wem.api.entity.query.EntityQuery;
import com.enonic.wem.api.query.filter.ValueFilter;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.content.ContentTypeNames;

public class ContentQueryEntityQueryTranslator
{
    public static EntityQuery translate( final ContentQuery contentQuery )
    {
        final EntityQuery.Builder entityQueryBuilder = EntityQuery.newEntityQuery();

        doTranslateEntityQueryProperties( contentQuery, entityQueryBuilder );

        return entityQueryBuilder.build();
    }

    protected static void doTranslateEntityQueryProperties( final ContentQuery contentQuery, final EntityQuery.Builder builder )
    {
        builder.query( contentQuery.getQueryExpr() ).
            from( contentQuery.getFrom() ).
            size( contentQuery.getSize() ).
            addAggregationQueries( contentQuery.getAggregationQueries() ).
            addQueryFilters( contentQuery.getQueryFilters() );

        final ContentTypeNames contentTypeNames = contentQuery.getContentTypes();

        if ( contentTypeNames != null && contentTypeNames.isNotEmpty() )
        {
            final ValueFilter.Builder contentTypeFilterBuilder = ValueFilter.create().
                fieldName( ContentDataSerializer.CONTENT_TYPE_FIELD_NAME ).
                setCache( true );

            for ( final ContentTypeName contentTypeName : contentTypeNames )
            {
                contentTypeFilterBuilder.addValue( Value.newString( contentTypeName.toString() ) );
            }

            builder.addQueryFilter( contentTypeFilterBuilder.build() );
        }
    }

}
