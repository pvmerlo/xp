package com.enonic.wem.repo.internal.entity.json;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.enonic.wem.api.blob.BlobKey;
import com.enonic.wem.api.data2.PropertyTree;
import com.enonic.wem.api.index.ChildOrder;
import com.enonic.wem.api.index.IndexConfig;
import com.enonic.wem.api.index.IndexPath;
import com.enonic.wem.api.index.PatternIndexConfigDocument;
import com.enonic.wem.api.node.Attachment;
import com.enonic.wem.api.node.Attachments;
import com.enonic.wem.api.node.Node;
import com.enonic.wem.api.node.NodeId;
import com.enonic.wem.api.node.NodeName;
import com.enonic.wem.api.node.NodePath;
import com.enonic.wem.api.query.expr.FieldOrderExpr;
import com.enonic.wem.api.query.expr.OrderExpr;
import com.enonic.wem.api.security.PrincipalKey;
import com.enonic.wem.api.security.UserStoreKey;
import com.enonic.wem.api.security.acl.AccessControlEntry;
import com.enonic.wem.api.security.acl.AccessControlList;
import com.enonic.wem.api.security.acl.Permission;

import static org.junit.Assert.*;

public class NodeJsonSerializerTest
{
    private final NodeJsonSerializer serializer;

    public NodeJsonSerializerTest()
    {
        this.serializer = NodeJsonSerializer.create( true );
    }

    @Test
    public void serialize_deserialize()
        throws Exception
    {
        PropertyTree nodeData = new PropertyTree( new PropertyTree.PredictivePropertyIdProvider() );
        nodeData.setDouble( "a.b.c", 2.0 );
        nodeData.setLocalDate( "b", LocalDate.of( 2013, 1, 2 ) );
        nodeData.setString( "c", "runar" );
        nodeData.setLocalDateTime( "d", LocalDateTime.of( 2013, 1, 2, 3, 4, 5, 0 ) );

        final AccessControlEntry entry1 = AccessControlEntry.create().
            principal( PrincipalKey.ofAnonymous() ).
            allow( Permission.READ ).
            deny( Permission.DELETE ).
            build();
        final AccessControlEntry entry2 = AccessControlEntry.create().
            principal( PrincipalKey.ofUser( UserStoreKey.system(), "user1" ) ).
            allow( Permission.MODIFY ).
            deny( Permission.PUBLISH ).
            build();
        AccessControlList acl = AccessControlList.create().add( entry1 ).add( entry2 ).build();
        AccessControlList effectiveAcl = acl.getEffective( AccessControlList.empty() );

        Node node = Node.newNode().
            id( NodeId.from( "myId" ) ).
            parent( NodePath.ROOT ).
            name( NodeName.from( "my-name" ) ).
            createdTime( LocalDateTime.of( 2013, 1, 2, 3, 4, 5, 0 ).toInstant( ZoneOffset.UTC ) ).
            creator( PrincipalKey.from( "user:test:creator" ) ).
            modifier( PrincipalKey.from( "user:test:modifier" ) ).
            modifiedTime( LocalDateTime.of( 2013, 1, 2, 3, 4, 5, 0 ).toInstant( ZoneOffset.UTC ) ).
            indexConfigDocument( PatternIndexConfigDocument.create().
                analyzer( "myAnalyzer" ).
                defaultConfig( IndexConfig.MINIMAL ).
                add( "myPath", IndexConfig.FULLTEXT ).
                build() ).
            data( nodeData ).
            attachments( Attachments.from( Attachment.
                newAttachment().
                name( "attachment" ).
                blobKey( new BlobKey( "1234" ) ).
                mimeType( "mimetype" ).
                build() ) ).
            childOrder( ChildOrder.create().
                add( FieldOrderExpr.create( IndexPath.from( "modifiedTime" ), OrderExpr.Direction.ASC ) ).
                add( FieldOrderExpr.create( IndexPath.from( "displayName" ), OrderExpr.Direction.DESC ) ).
                build() ).
            accessControlList( acl ).
            effectiveAcl( effectiveAcl ).
            build();

        final String expectedStr = readJson( "serialized-node.json" );

        final String serializedNode = this.serializer.toString( node );
        System.out.println( expectedStr );
        assertEquals( expectedStr, serializedNode );

        final Node deSerializedNode = this.serializer.toNode( expectedStr );
        assertEquals( node, deSerializedNode );
    }

    private String readJson( final String name )
        throws Exception
    {
        final URL url = getClass().getResource( name );
        final JsonNode node = this.serializer.mapper.readTree( url );
        return this.serializer.mapper.writeValueAsString( node );
    }
}
