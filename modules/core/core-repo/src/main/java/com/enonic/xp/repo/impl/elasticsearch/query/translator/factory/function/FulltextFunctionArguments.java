package com.enonic.xp.repo.impl.elasticsearch.query.translator.factory.function;

import java.util.List;

import com.enonic.xp.query.expr.ValueExpr;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.resolver.SearchQueryFieldNameResolver;
import com.enonic.xp.repo.impl.index.IndexValueType;
import com.enonic.xp.repo.impl.node.NodeConstants;

import static com.google.common.base.Strings.nullToEmpty;

public class FulltextFunctionArguments
    extends AbstractSimpleQueryStringFunctionArguments
{
    @Override
    protected String resolveAnalyzer( final String value )
    {
        return nullToEmpty( value ).isBlank() ? NodeConstants.DEFAULT_FULLTEXT_SEARCH_ANALYZER : value;
    }

    public FulltextFunctionArguments( final List<ValueExpr> arguments )
    {
        super( arguments );
    }

    @Override
    public String getFunctionName()
    {
        return "fulltext";
    }


    @Override
    public String resolveQueryFieldName( final String baseFieldName )
    {
        return new SearchQueryFieldNameResolver().resolve( baseFieldName, IndexValueType.ANALYZED );
    }
}
