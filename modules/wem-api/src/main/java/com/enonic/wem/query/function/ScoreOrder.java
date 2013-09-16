package com.enonic.wem.query.function;

import com.enonic.wem.query.DynamicOrder;

public final class ScoreOrder
    extends DynamicOrder
{
    public final static String NAME = "score";

    public ScoreOrder( final Direction direction )
    {
        super( direction, NAME );
    }
}
