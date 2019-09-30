package com.enonic.xp.repository;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.util.AttachedBinary;

@Beta
public interface RepositoryService
{
    Repository createRepository( final CreateRepositoryParams params );

    Repository updateRepository( final UpdateRepositoryParams params );

    Branch createBranch( final CreateBranchParams params );

    Repositories list();

    boolean isInitialized( final RepositoryId id );

    Repository get( final RepositoryId repositoryId );

    RepositoryId deleteRepository( final DeleteRepositoryParams params );

    Branch deleteBranch( final DeleteBranchParams params );

    void invalidateAll();

    void invalidate( final RepositoryId repositoryId );

    ByteSource getAttachment( final AttachedBinary binaryReference );
}
