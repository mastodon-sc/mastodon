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

import java.lang.reflect.Type;
import java.util.List;

import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales.Dataset;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OmeZarrMultiscalesAdapter implements JsonDeserializer< OmeZarrMultiscales >, JsonSerializer< OmeZarrMultiscales >
{

	@SuppressWarnings( "serial" )
	@Override
    public OmeZarrMultiscales deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
    {
        final JsonObject jsonObject = json.getAsJsonObject();
        final Type zarrAxisListType = new TypeToken< List< ZarrAxis > >()
        {}.getType();
        final List< ZarrAxis > zarrAxisList = context.deserialize( jsonObject.get( "axes" ), zarrAxisListType );
        final ZarrAxes axes = context.deserialize( jsonObject.get( "axes" ), ZarrAxes.class );
        final Type datasetsType = new TypeToken< Dataset[] >()
        {}.getType();
        final Dataset[] datasets = context.deserialize( jsonObject.get( "datasets" ), datasetsType );
        final String version = jsonObject.get( "version" ).getAsString();
        final OmeZarrMultiscales multiscales = new OmeZarrMultiscales();
        multiscales.axes = axes;
        multiscales.zarrAxisList = zarrAxisList;
        multiscales.datasets = datasets;
        multiscales.version = version;
        return multiscales;
    }

    @Override
    public JsonElement serialize( final OmeZarrMultiscales src, final Type typeOfSrc, final JsonSerializationContext context )
    {
        final JsonObject obj = new JsonObject();
        obj.add( "axes", context.serialize( src.zarrAxisList ) );
        obj.add( "datasets", context.serialize( src.datasets ) );
        obj.add( "name", context.serialize( src.name ) );
        obj.add( "type", context.serialize( src.type ) );
        obj.add( "version", context.serialize( src.version ) );
        obj.add( "coordinateTransformations", context.serialize( src.coordinateTransformations ) );
        return obj;
    }
}
