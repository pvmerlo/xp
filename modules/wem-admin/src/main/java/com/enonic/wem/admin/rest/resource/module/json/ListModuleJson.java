package com.enonic.wem.admin.rest.resource.module.json;

import java.util.List;

import com.google.common.collect.ImmutableList;

import com.enonic.wem.admin.json.module.ModuleJson;
import com.enonic.wem.api.module.Module;
import com.enonic.wem.api.module.Modules;

public class ListModuleJson
{

    private List<ModuleJson> list;

    public ListModuleJson( Modules modules )
    {
        ImmutableList.Builder<ModuleJson> builder = ImmutableList.builder();
        for ( Module module : modules )
        {
            builder.add( new ModuleJson( module ) );
        }
        this.list = builder.build();
    }

    public int getTotal()
    {
        return this.list.size();
    }

    public List<ModuleJson> getModules()
    {
        return this.list;
    }
}
