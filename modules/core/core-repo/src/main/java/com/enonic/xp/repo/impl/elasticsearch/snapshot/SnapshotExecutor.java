package com.enonic.xp.repo.impl.elasticsearch.snapshot;


import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.common.settings.Settings;

import com.enonic.xp.node.SnapshotResult;
import com.enonic.xp.repo.impl.repository.IndexNameResolver;

public class SnapshotExecutor
    extends AbstractSnapshotExecutor
{
    private SnapshotExecutor( final Builder builder )
    {
        super( builder );
    }

    public SnapshotResult execute()
    {
        final CreateSnapshotRequestBuilder createRequest =
            new CreateSnapshotRequestBuilder( this.client.admin().cluster(), CreateSnapshotAction.INSTANCE ).
                setIndices( IndexNameResolver.resolveIndexNames( repositories ).toArray( String[]::new ) ).
                setIncludeGlobalState( false ).
                setWaitForCompletion( true ).
                setRepository( this.snapshotRepositoryName ).
                setSnapshot( snapshotName ).
                setSettings( Settings.settingsBuilder().put( "ignore_unavailable", true ) );

        final CreateSnapshotResponse createSnapshotResponse =
            this.client.admin().cluster().createSnapshot( createRequest.request() ).actionGet();

        return SnapshotResultFactory.create( createSnapshotResponse );
    }

    public static Builder create()
    {
        return new Builder();
    }


    public static final class Builder
        extends AbstractSnapshotExecutor.Builder<Builder>
    {
        private Builder()
        {
        }

        public SnapshotExecutor build()
        {
            return new SnapshotExecutor( this );
        }
    }
}
