package com.enonic.wem.repo.internal.entity;

import org.junit.Test;

import com.enonic.wem.api.security.PrincipalKey;
import com.enonic.wem.api.security.User;
import com.enonic.wem.api.security.UserStoreKey;
import com.enonic.wem.api.security.acl.AccessControlEntry;
import com.enonic.wem.api.security.acl.AccessControlList;
import com.enonic.wem.api.security.auth.AuthenticationInfo;

import static com.enonic.wem.api.security.acl.Permission.CREATE;
import static com.enonic.wem.api.security.acl.Permission.MODIFY;
import static com.enonic.wem.api.security.acl.Permission.PUBLISH;
import static com.enonic.wem.api.security.acl.Permission.READ;
import static org.junit.Assert.*;

public class NodePermissionsResolverTest
{

    private static final UserStoreKey USER_STORE_KEY = new UserStoreKey( "us" );

    private static final PrincipalKey USER_A = PrincipalKey.ofGroup( USER_STORE_KEY, "userA" );

    private static final PrincipalKey GROUP_B = PrincipalKey.ofGroup( USER_STORE_KEY, "groupB" );

    private static final PrincipalKey ROLE_C = PrincipalKey.ofRole( "roleC" );

    @Test
    public void hasPermissionEmptyACL()
        throws Exception
    {
        final AuthenticationInfo authInfo = AuthenticationInfo.create().
            user( User.ANONYMOUS ).
            principals( PrincipalKey.ofAnonymous() ).
            build();

        final AccessControlList nodePermissions = AccessControlList.create().
            add( AccessControlEntry.create().principal( USER_A ).allow( READ ).build() ).
            add( AccessControlEntry.create().principal( GROUP_B ).allow( CREATE ).build() ).
            add( AccessControlEntry.create().principal( ROLE_C ).allow( MODIFY ).build() ).
            build();

        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, READ, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, CREATE, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, MODIFY, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, PUBLISH, nodePermissions ) );
    }

    @Test
    public void hasPermissionAll()
        throws Exception
    {
        final AuthenticationInfo authInfo = AuthenticationInfo.create().
            user( User.create().key( USER_A ).login( "usera" ).build() ).
            principals( USER_A, GROUP_B, ROLE_C ).
            build();

        final AccessControlList nodePermissions = AccessControlList.create().
            add( AccessControlEntry.create().principal( USER_A ).allow( READ ).build() ).
            add( AccessControlEntry.create().principal( GROUP_B ).allow( CREATE ).build() ).
            add( AccessControlEntry.create().principal( ROLE_C ).allow( READ, MODIFY, CREATE ).build() ).
            build();

        assertTrue( NodePermissionsResolver.userHasPermission( authInfo, READ, nodePermissions ) );
        assertTrue( NodePermissionsResolver.userHasPermission( authInfo, CREATE, nodePermissions ) );
        assertTrue( NodePermissionsResolver.userHasPermission( authInfo, MODIFY, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, PUBLISH, nodePermissions ) );
    }

    @Test
    public void hasPermissionSome()
        throws Exception
    {
        final AuthenticationInfo authInfo = AuthenticationInfo.create().
            user( User.create().key( USER_A ).login( "usera" ).build() ).
            principals( USER_A, ROLE_C ).
            build();

        final AccessControlList nodePermissions = AccessControlList.create().
            add( AccessControlEntry.create().principal( USER_A ).allow( READ ).build() ).
            add( AccessControlEntry.create().principal( GROUP_B ).allow( CREATE ).build() ).
            add( AccessControlEntry.create().principal( ROLE_C ).allow( MODIFY ).build() ).
            build();

        assertTrue( NodePermissionsResolver.userHasPermission( authInfo, READ, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, CREATE, nodePermissions ) );
        assertTrue( NodePermissionsResolver.userHasPermission( authInfo, MODIFY, nodePermissions ) );
        assertFalse( NodePermissionsResolver.userHasPermission( authInfo, PUBLISH, nodePermissions ) );
    }
}