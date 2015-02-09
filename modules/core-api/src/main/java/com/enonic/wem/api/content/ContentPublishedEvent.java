package com.enonic.wem.api.content;

import com.google.common.base.MoreObjects;

import com.enonic.wem.api.event.Event;

public final class ContentPublishedEvent
    implements Event
{
    private final ContentId contentId;

    public ContentPublishedEvent( final ContentId contentId )
    {
        this.contentId = contentId;
    }

    public ContentId getContentId()
    {
        return contentId;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).
            add( "contentId", this.contentId ).
            omitNullValues().
            toString();
    }

}
