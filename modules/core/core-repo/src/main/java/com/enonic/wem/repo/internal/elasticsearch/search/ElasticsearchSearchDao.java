package com.enonic.wem.repo.internal.elasticsearch.search;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.wem.repo.internal.branch.search.NodeBranchQuery;
import com.enonic.wem.repo.internal.elasticsearch.ElasticsearchDao;
import com.enonic.wem.repo.internal.elasticsearch.query.ElasticsearchQuery;
import com.enonic.wem.repo.internal.elasticsearch.query.translator.NodeBranchQueryTranslator;
import com.enonic.wem.repo.internal.elasticsearch.query.translator.NodeQueryTranslator;
import com.enonic.wem.repo.internal.elasticsearch.query.translator.NodeVersionDiffQueryTranslator;
import com.enonic.wem.repo.internal.elasticsearch.query.translator.NodeVersionQueryTranslator;
import com.enonic.wem.repo.internal.search.SearchDao;
import com.enonic.wem.repo.internal.search.SearchRequest;
import com.enonic.wem.repo.internal.search.result.SearchHits;
import com.enonic.wem.repo.internal.search.result.SearchResult;
import com.enonic.wem.repo.internal.version.search.NodeVersionDiffQuery;
import com.enonic.wem.repo.internal.version.search.NodeVersionQuery;
import com.enonic.xp.node.NodeQuery;
import com.enonic.xp.node.SearchMode;
import com.enonic.xp.query.Query;

@Component
public class ElasticsearchSearchDao
    implements SearchDao
{
    private ElasticsearchDao elasticsearchDao;

    private final NodeQueryTranslator nodeQueryTranslator = new NodeQueryTranslator();

    private final NodeVersionQueryTranslator nodeVersionQueryTranslator = new NodeVersionQueryTranslator();

    private final NodeVersionDiffQueryTranslator nodeVersionDiffQueryTranslator = new NodeVersionDiffQueryTranslator();

    private final NodeBranchQueryTranslator nodeBranchQueryTranslator = new NodeBranchQueryTranslator();

    @Override
    public SearchResult search( final SearchRequest searchRequest )
    {
        final ElasticsearchQuery esQuery = translateQuery( searchRequest );

        if ( searchRequest.getQuery().getSearchMode().equals( SearchMode.COUNT ) )
        {
            final long count = elasticsearchDao.count( esQuery );

            return SearchResult.create().
                hits( SearchHits.create().
                    totalHits( count ).
                    build() ).
                build();
        }

        return this.elasticsearchDao.search( esQuery );
    }

    private ElasticsearchQuery translateQuery( final SearchRequest searchRequest )
    {
        final Query query = searchRequest.getQuery();

        if ( query instanceof NodeQuery )
        {
            return nodeQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeVersionQuery )
        {
            return nodeVersionQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeVersionDiffQuery )
        {
            return nodeVersionDiffQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeBranchQuery )
        {
            return nodeBranchQueryTranslator.translate( searchRequest );
        }

        throw new UnsupportedOperationException( "Queries of type " + query.getClass() + " not implemented yes" );
    }


    @Reference
    public void setElasticsearchDao( final ElasticsearchDao elasticsearchDao )
    {
        this.elasticsearchDao = elasticsearchDao;
    }
}
