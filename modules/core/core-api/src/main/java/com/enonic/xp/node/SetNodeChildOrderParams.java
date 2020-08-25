package com.enonic.xp.node;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.index.ChildOrder;

@PublicApi
public class SetNodeChildOrderParams
{
    private final NodeId nodeId;

    private final ChildOrder childOrder;

    private final SetNodeChildOrderProcessor processor;

    private SetNodeChildOrderParams( final Builder builder )
    {
        nodeId = builder.nodeId;
        childOrder = builder.childOrder;
        processor = builder.processor;
    }

    public NodeId getNodeId()
    {
        return nodeId;
    }

    public ChildOrder getChildOrder()
    {
        return childOrder;
    }

    public SetNodeChildOrderProcessor getProcessor()
    {
        return processor;
    }

    public static Builder create()
    {
        return new Builder();
    }


    public static final class Builder
    {
        private NodeId nodeId;

        private ChildOrder childOrder;

        private SetNodeChildOrderProcessor processor;

        private Builder()
        {
        }

        public Builder nodeId( NodeId nodeId )
        {
            this.nodeId = nodeId;
            return this;
        }

        public Builder childOrder( ChildOrder childOrder )
        {
            this.childOrder = childOrder;
            return this;
        }

        public Builder processor( SetNodeChildOrderProcessor processor )
        {
            this.processor = processor;
            return this;
        }

        public SetNodeChildOrderParams build()
        {
            return new SetNodeChildOrderParams( this );
        }
    }
}
