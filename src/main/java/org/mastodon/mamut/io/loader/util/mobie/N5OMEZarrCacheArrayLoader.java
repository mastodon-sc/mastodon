/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Mastodon developers
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

import java.io.IOException;
import java.util.Map;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import com.amazonaws.SdkClientException;

import bdv.img.cache.SimpleCacheArrayLoader;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.cell.CellGrid;

public class N5OMEZarrCacheArrayLoader< A extends DataAccess > implements SimpleCacheArrayLoader< A >
{
    private final N5Reader n5;

    private final String pathName;

    private final int channel;

    private final int timepoint;

    private final DatasetAttributes attributes;

    private final ZarrArrayCreator< A, ? > zarrArrayCreator;

    private final ZarrAxes zarrAxes;

    public N5OMEZarrCacheArrayLoader( final N5Reader n5, final String pathName, final int channel, final int timepoint,
            final DatasetAttributes attributes, final CellGrid grid, final ZarrAxes zarrAxes )
    {
        this.n5 = n5;
        this.pathName = pathName; // includes the level
        this.channel = channel;
        this.timepoint = timepoint;
        this.attributes = attributes;
        final DataType dataType = attributes.getDataType();
        this.zarrArrayCreator = new ZarrArrayCreator<>( grid, dataType, zarrAxes );
        this.zarrAxes = zarrAxes;
    }

    @Override
    public A loadArray( final long[] gridPosition, final int[] cellDimensions ) throws IOException
    {
        DataBlock< ? > block = null;

        final long[] dataBlockIndices = toZarrChunkIndices( gridPosition );

        try
        {
            block = n5.readBlock( pathName, attributes, dataBlockIndices );
        }
        catch ( final SdkClientException e )
        {
            System.err.println( e.getMessage() ); // this happens sometimes, not sure yet why...
        }

        if ( block == null )
        {
            return zarrArrayCreator.createEmptyArray( gridPosition );
        }
        else
        {
            return zarrArrayCreator.createArray( block, gridPosition );
        }
    }

    private long[] toZarrChunkIndices( final long[] gridPosition )
    {

        final long[] chunkInZarr = new long[ zarrAxes.getNumDimension() ];

        // fill in the spatial dimensions
        final Map< Integer, Integer > spatialToZarr = zarrAxes.spatialToZarr();
        for ( final Map.Entry< Integer, Integer > entry : spatialToZarr.entrySet() )
            chunkInZarr[ entry.getValue() ] = gridPosition[ entry.getKey() ];

        if ( zarrAxes.hasChannels() )
            chunkInZarr[ zarrAxes.channelIndex() ] = channel;

        if ( zarrAxes.hasTimepoints() )
            chunkInZarr[ zarrAxes.timeIndex() ] = timepoint;

        return chunkInZarr;
    }
}
