/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.io.loader.util.mobie;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ZarrAxis
{
    private transient final int index;

    private final String name;

    private final String type;

    private String unit;

    public ZarrAxis( final int index, final String name, final String type, final String unit )
    {
        this.index = index;
        this.name = name;
        this.type = type;
        this.unit = unit;
    }

    public ZarrAxis( final int index, final String name, final String type )
    {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public static JsonElement convertToJson( final List< ZarrAxis > zarrAxes )
    {
        final StringBuilder axes = new StringBuilder();
        axes.append( "[" );
        for ( final ZarrAxis axis : zarrAxes )
        {
            axes.append( "\"" ).append( axis.getName() ).append( "\"" );
            if ( axis.getIndex() < zarrAxes.size() - 1 )
            {
                axes.append( "," );
            }
        }
        axes.append( "]" );
        final Gson gson = new Gson();
        return gson.fromJson( axes.toString(), JsonElement.class );
    }

    public int getIndex()
    {
        return index;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit( final String unit )
    {
        this.unit = unit;
    }
}
