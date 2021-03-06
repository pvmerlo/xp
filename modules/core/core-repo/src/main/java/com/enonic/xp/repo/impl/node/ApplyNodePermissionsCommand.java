package com.enonic.xp.repo.impl.node;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import com.enonic.xp.node.ApplyNodePermissionsParams;
import com.enonic.xp.node.ApplyNodePermissionsResult;
import com.enonic.xp.node.FindNodesByParentParams;
import com.enonic.xp.node.FindNodesByParentResult;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.Nodes;
import com.enonic.xp.repo.impl.search.NodeSearchService;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.acl.Permission;

import static com.enonic.xp.repo.impl.node.NodeConstants.CLOCK;
import static com.enonic.xp.repo.impl.node.NodePermissionsResolver.contextUserHasPermissionOrAdmin;

final class ApplyNodePermissionsCommand
    extends AbstractNodeCommand
{
    private static final Logger LOG = LoggerFactory.getLogger( ApplyNodePermissionsCommand.class );

    private final ApplyNodePermissionsParams params;

    private final PermissionsMergingStrategy mergingStrategy;

    private final ApplyNodePermissionsResult.Builder resultBuilder = ApplyNodePermissionsResult.create();

    private ApplyNodePermissionsCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
        this.mergingStrategy = builder.mergingStrategy;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public ApplyNodePermissionsResult execute()
    {
        final Node node = doGetById( params.getNodeId() );

        if ( node == null )
        {
            return resultBuilder.build();
        }

        applyPermissions( params.getPermissions() != null ? params.getPermissions() : node.getPermissions(), node );

        return resultBuilder.build();
    }

    private void applyPermissionsToChildren( final Node parent )
    {
        final AccessControlList parentPermissions = parent.getPermissions();

        final FindNodesByParentParams findByParentParams = FindNodesByParentParams.create().
            parentPath( parent.path() ).
            size( NodeSearchService.GET_ALL_SIZE_FLAG ).
            build();

        final FindNodesByParentResult result = doFindNodesByParent( findByParentParams );

        final Nodes children = GetNodesByIdsCommand.create( this ).
            ids( result.getNodeIds() ).
            build().
            execute();

        for ( Node child : children )
        {
            applyPermissions( parentPermissions, child );
        }
    }

    private Node applyPermissions( final AccessControlList permissions, final Node node )
    {
        if ( contextUserHasPermissionOrAdmin( Permission.WRITE_PERMISSIONS, node ) )
        {
            final Node childApplied = storePermissions( permissions, node );

            if ( params.getListener() != null )
            {
                params.getListener().permissionsApplied( 1 );
            }

            applyPermissionsToChildren( childApplied );
            resultBuilder.succeedNode( childApplied );

            return childApplied;
        }
        else
        {
            params.getListener().notEnoughRights( 1 );
            resultBuilder.skippedNode( node );

            LOG.info( "Not enough rights for applying permissions to node [" + node.id() + "] " + node.path() );

            return node;
        }
    }

    private Node storePermissions( final AccessControlList permissions, final Node node )
    {
        final Node updatedNode;
        final boolean isParent = node.id().equals( params.getNodeId() );

        if ( params.isOverwriteChildPermissions() || node.inheritsPermissions() || isParent )
        {
            updatedNode = createUpdatedNode( node, permissions, !isParent || params.isInheritPermissions() );
        }
        else
        {
            final AccessControlList mergedPermissions = mergingStrategy.mergePermissions( node.getPermissions(), permissions );
            updatedNode = createUpdatedNode( node, mergedPermissions, false );
        }

        final Node result = StoreNodeCommand.create( this ).
            node( updatedNode ).
            updateMetadataOnly( false ).
            build().
            execute();

        return result;
    }

    private Node createUpdatedNode( final Node persistedNode, final AccessControlList permissions, final boolean inheritsPermissions )
    {
        final Node.Builder updateNodeBuilder = Node.create( persistedNode ).
            timestamp( Instant.now( CLOCK ) ).
            permissions( permissions ).
            inheritPermissions( inheritsPermissions );
        return updateNodeBuilder.build();
    }

    public static class Builder
        extends AbstractNodeCommand.Builder<Builder>
    {
        private ApplyNodePermissionsParams params;

        private PermissionsMergingStrategy mergingStrategy = new DefaultPermissionsMergingStrategy();

        Builder()
        {
            super();
        }

        public Builder params( final ApplyNodePermissionsParams params )
        {
            this.params = params;
            return this;
        }

        public Builder mergingStrategy( final PermissionsMergingStrategy mergingStrategy )
        {
            this.mergingStrategy = mergingStrategy;
            return this;
        }

        public ApplyNodePermissionsCommand build()
        {
            validate();
            return new ApplyNodePermissionsCommand( this );
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( params );
            Preconditions.checkNotNull( mergingStrategy );
        }
    }

}
