/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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
package org.mastodon.mamut.io.loader.util;

import java.lang.reflect.Type;
import java.util.List;

import org.mastodon.mamut.io.loader.util.OmeZarrMultiscales.Dataset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OmeZarrMultiscalesAdapter implements JsonDeserializer<OmeZarrMultiscales>, JsonSerializer<OmeZarrMultiscales> {

    @Override
    public OmeZarrMultiscales deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonObject = json.getAsJsonObject();
        Type zarrAxisListType = new TypeToken<List<ZarrAxis>>(){}.getType();
        List<ZarrAxis> zarrAxisList = context.deserialize(jsonObject.get("axes"), zarrAxisListType);
        ZarrAxes axes = context.deserialize(jsonObject.get("axes"), ZarrAxes.class);
        Type datasetsType = new TypeToken<Dataset[]>(){}.getType();
        Dataset[] datasets = context.deserialize(jsonObject.get( "datasets"), datasetsType);
        String version = jsonObject.get("version").getAsString();
        OmeZarrMultiscales multiscales = new OmeZarrMultiscales();
        multiscales.axes = axes;
        multiscales.zarrAxisList = zarrAxisList;
        multiscales.datasets = datasets;
        multiscales.version = version;
        return multiscales;
    }

    @Override
    public JsonElement serialize( OmeZarrMultiscales src, Type typeOfSrc, JsonSerializationContext context )
    {
        JsonObject obj = new JsonObject();
        obj.add( "axes", context.serialize( src.zarrAxisList ) );
        obj.add( "datasets", context.serialize( src.datasets ) );
        obj.add( "name", context.serialize( src.name ) );
        obj.add( "type", context.serialize( src.type ) );
        obj.add( "version", context.serialize( src.version ) );
        obj.add( "coordinateTransformations", context.serialize( src.coordinateTransformations ) );
        return obj;
    }
}
