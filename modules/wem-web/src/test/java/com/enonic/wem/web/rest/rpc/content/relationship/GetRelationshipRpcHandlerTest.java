package com.enonic.wem.web.rest.rpc.content.relationship;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.wem.api.Client;
import com.enonic.wem.api.command.content.relationship.GetRelationships;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.relationship.Relationships;
import com.enonic.wem.api.content.schema.relationship.QualifiedRelationshipTypeName;
import com.enonic.wem.web.json.rpc.JsonRpcHandler;
import com.enonic.wem.web.rest.rpc.AbstractRpcHandlerTest;

import static com.enonic.wem.api.content.relationship.Relationship.newRelationship;
import static org.mockito.Matchers.isA;

public class GetRelationshipRpcHandlerTest
    extends AbstractRpcHandlerTest
{
    private Client client;

    @Override
    protected JsonRpcHandler createHandler()
        throws Exception
    {
        GetRelationshipRpcHandler handler = new GetRelationshipRpcHandler();

        client = Mockito.mock( Client.class );
        handler.setClient( client );

        return handler;
    }

    @Test
    public void get_from_content_with_one_relationship()
        throws Exception
    {
        Relationships relationships = Relationships.from( newRelationship().
            fromContent( ContentId.from( "111" ) ).
            toContent( ContentId.from( "222" ) ).
            type( QualifiedRelationshipTypeName.DEFAULT ).
            build() );
        Mockito.when( client.execute( isA( GetRelationships.class ) ) ).thenReturn( relationships );

        ObjectNode resultJson = objectNode();
        resultJson.put( "success", true );
        resultJson.put( "total", 1 );
        ArrayNode relationshipsArray = resultJson.putArray( "relationships" );
        ObjectNode rel1 = relationshipsArray.addObject();
        rel1.put( "type", "system:default" );
        rel1.put( "fromContent", "111" );
        rel1.put( "toContent", "222" );
        rel1.putNull( "managingData" );
        rel1.putNull( "properties" );

        // exercise & verify
        testSuccess( "getRelationship_param.json", resultJson );
    }

    @Test
    public void get_from_content_with_two_relationships()
        throws Exception
    {
        Relationships relationships = Relationships.from( newRelationship().
            fromContent( ContentId.from( "111" ) ).
            toContent( ContentId.from( "222" ) ).
            type( QualifiedRelationshipTypeName.DEFAULT ).
            build(), newRelationship().
            fromContent( ContentId.from( "111" ) ).
            toContent( ContentId.from( "333" ) ).
            type( QualifiedRelationshipTypeName.DEFAULT ).
            build() );
        Mockito.when( client.execute( isA( GetRelationships.class ) ) ).thenReturn( relationships );

        ObjectNode resultJson = objectNode();
        resultJson.put( "success", true );
        resultJson.put( "total", 2 );
        ArrayNode relationshipsArray = resultJson.putArray( "relationships" );
        ObjectNode rel1 = relationshipsArray.addObject();
        rel1.put( "type", "system:default" );
        rel1.put( "fromContent", "111" );
        rel1.put( "toContent", "222" );
        rel1.putNull( "managingData" );
        rel1.putNull( "properties" );

        ObjectNode rel2 = relationshipsArray.addObject();
        rel2.put( "type", "system:default" );
        rel2.put( "fromContent", "111" );
        rel2.put( "toContent", "333" );
        rel2.putNull( "managingData" );
        rel2.putNull( "properties" );

        // exercise & verify
        testSuccess( "getRelationship_param.json", resultJson );
    }


}
