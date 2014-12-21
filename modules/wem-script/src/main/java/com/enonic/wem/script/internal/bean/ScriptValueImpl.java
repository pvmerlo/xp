package com.enonic.wem.script.internal.bean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.Undefined;

import com.enonic.wem.api.convert.Converters;
import com.enonic.wem.script.ScriptValue;

public final class ScriptValueImpl
    implements ScriptValue
{
    private final static ScriptValueImpl UNDEFINED = new ScriptValueImpl( null, null );

    private final Object value;

    private final JSObject jsObject;

    private final ScriptMethodInvoker invoker;

    public ScriptValueImpl( final Object value, final ScriptMethodInvoker invoker )
    {
        this.invoker = invoker;

        Object unwrapped = ScriptUtils.unwrap( value );
        if ( unwrapped instanceof Undefined )
        {
            unwrapped = null;
        }

        this.jsObject = ( unwrapped instanceof JSObject ) ? (JSObject) unwrapped : null;
        this.value = ( this.jsObject == null ) ? unwrapped : null;
    }

    @Override
    public boolean isArray()
    {
        return ( this.jsObject != null ) && this.jsObject.isArray();
    }

    @Override
    public boolean isObject()
    {
        return ( this.jsObject != null ) && ( !this.jsObject.isFunction() && !this.jsObject.isArray() );
    }

    @Override
    public boolean isValue()
    {
        return this.value != null;
    }

    @Override
    public boolean isFunction()
    {
        return ( this.jsObject != null ) && this.jsObject.isFunction();
    }

    @Override
    public boolean isUndefined()
    {
        return ( this.value == null ) && ( this.jsObject == null );
    }

    @Override
    public Object getValue()
    {
        return this.value;
    }

    @Override
    public Set<String> getKeys()
    {
        if ( !isObject() )
        {
            return Collections.emptySet();
        }

        return this.jsObject.keySet();
    }

    @Override
    public boolean hasMember( final String key )
    {
        return isObject() && this.jsObject.hasMember( key );
    }

    @Override
    public ScriptValue getMember( final String key )
    {
        if ( !isObject() )
        {
            return UNDEFINED;
        }

        return new ScriptValueImpl( this.jsObject.getMember( key ), this.invoker );
    }

    @Override
    public List<ScriptValue> getArray()
    {
        if ( !isArray() )
        {
            return Collections.emptyList();
        }

        return this.jsObject.values().stream().map( this::newScriptObject ).collect( Collectors.toList() );
    }

    private ScriptValue newScriptObject( final Object value )
    {
        return new ScriptValueImpl( value, this.invoker );
    }

    @Override
    public ScriptValue call( final Object... args )
    {
        if ( !isFunction() )
        {
            return UNDEFINED;
        }

        final Object result = this.invoker.invoke( this.jsObject, args );
        return new ScriptValueImpl( result, this.invoker );
    }

    @Override
    public <T> T getValue( final Class<T> type )
    {
        return Converters.convert( this.value, type );
    }

    @Override
    public <T> List<T> getArray( final Class<T> type )
    {
        if ( !isArray() )
        {
            return Collections.emptyList();
        }

        return this.jsObject.values().stream().map( new Function<Object, T>()
        {
            @Override
            public T apply( final Object o )
            {
                return convertValue( type, o );
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
    }

    private <T> T convertValue( final Class<T> type, final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        return Converters.convert( value, type );
    }

    @Override
    public Map<String, Object> getMap()
    {
        if ( !isObject() )
        {
            return Collections.emptyMap();
        }

        return JsObjectConverter.toMap( this.jsObject );
    }
}
