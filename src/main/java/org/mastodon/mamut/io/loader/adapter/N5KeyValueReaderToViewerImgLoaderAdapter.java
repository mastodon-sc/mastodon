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
package org.mastodon.mamut.io.loader.adapter;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5KeyValueReader;
import org.mastodon.mamut.io.loader.util.mobie.N5CacheArrayLoader;

import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.n5.BdvN5Format;
import bdv.img.n5.DataTypeProperties;
import net.imglib2.img.cell.CellGrid;

public class N5KeyValueReaderToViewerImgLoaderAdapter implements N5ReaderToViewerImgLoaderAdapter< N5KeyValueReader >
{

    private final N5KeyValueReader n5;

    private final String dataset;

    public N5KeyValueReaderToViewerImgLoaderAdapter( final N5KeyValueReader n5, final String dataset )
    {
        this.n5 = n5;
        this.dataset = dataset;
    }

    @Override
    public String getDataset()
    {
        return dataset;
    }

    @Override
    public N5KeyValueReader getN5Reader()
    {
        return n5;
    }

    @Override
    public DataType getSetupDataType( int setupId ) throws IOException
    {
        DataType dataType = null;
        try
        {
            final String pathName = getFullPathName( getPathNameFromSetup( setupId ) );
            dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
        }
        catch ( final N5Exception e )
        {
            throw new IOException( e );
        }
        return dataType;
    }

    @Override
    public double[][] getMipmapResolutions( int setupId ) throws IOException
    {
        double[][] mipmapResolutions = null;
        try
        {
            final String pathName = getFullPathName( getPathNameFromSetup( setupId ) );
            mipmapResolutions = n5.getAttribute( pathName, BdvN5Format.DOWNSAMPLING_FACTORS_KEY, double[][].class );
        }
        catch ( final N5Exception e )
        {
            throw new IOException( e );
        }
        return mipmapResolutions;
    }

    @Override
    public long[] getDimensions( DatasetAttributes attributes, int setupId )
    {
        return attributes.getDimensions();
    }

    @Override
    public int[] getCellDimensions( DatasetAttributes attributes, int setupId )
    {
        return attributes.getBlockSize();
    }

    @Override
    public SimpleCacheArrayLoader< ? > createCacheArrayLoader( String pathName, int setupId, int timepointId, CellGrid grid )
            throws IOException
    {
        final DatasetAttributes attributes;
        try
        {
            attributes = n5.getDatasetAttributes( pathName );
        }
        catch ( final N5Exception e )
        {
            throw new IOException( e );
        }
        return new N5CacheArrayLoader<>( n5, pathName, attributes, DataTypeProperties.of( attributes.getDataType() ) );
    }

    @Override
    public String getPathNameFromSetup( int setupId )
    {
        return String.format( "setup%d", setupId );
    }

    @Override
    public String getPathNameFromSetupTimepoint( int setupId, int timepointId )
    {
        return String.format( "setup%d/timepoint%d", setupId, timepointId );
    }

    @Override
    public String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level )
    {
        return String.format( "setup%d/timepoint%d/s%d", setupId, timepointId, level );
    }

}
