/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ZarrAxesAdapter implements JsonDeserializer< ZarrAxes >, JsonSerializer< ZarrAxes >
{

    @Override
    public ZarrAxes deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
    {
        final JsonArray array = json.getAsJsonArray();
        if ( array.size() > 0 )
        {
            final StringBuilder axisString = new StringBuilder( "[" );
            for ( int i = 0; i < array.size(); i++ )
            {
                String element;
                try
                {
                    element = array.get( i ).getAsString();
                }
                catch ( final UnsupportedOperationException e )
                {
                    try
                    {
                        final JsonElement jj = array.get( i );
                        element = jj.getAsJsonObject().get( "name" ).getAsString();
                    }
                    catch ( final Exception exception )
                    {
                        throw new JsonParseException( "" + e );
                    }
                }
                if ( i != 0 )
                {
                    axisString.append( "," );
                }
                axisString.append( "\"" );
                axisString.append( element );
                axisString.append( "\"" );

            }
            axisString.append( "]" );
            return ZarrAxes.decode( axisString.toString() );
        }
        else
        {
            return null;
        }
    }

    @Override
    public JsonElement serialize( final ZarrAxes axes, final Type typeOfSrc, final JsonSerializationContext context )
    {
        final List< String > axisList = axes.getAxesList();
        final JsonArray jsonArray = new JsonArray();
        for ( final String axis : axisList )
        {
            jsonArray.add( axis );
        }
        return jsonArray;
    }
}
