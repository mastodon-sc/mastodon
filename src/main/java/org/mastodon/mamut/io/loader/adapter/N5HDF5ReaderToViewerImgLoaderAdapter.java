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

import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.universe.metadata.SpatialMultiscaleMetadata;
import org.mastodon.mamut.io.loader.util.mobie.N5CacheArrayLoader;

import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.n5.BdvN5Format;
import bdv.img.n5.DataTypeProperties;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Exception.N5IOException;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import hdf.hdf5lib.exceptions.HDF5Exception;
import net.imglib2.img.cell.CellGrid;

public class N5HDF5ReaderToViewerImgLoaderAdapter implements N5ReaderToViewerImgLoaderAdapter< N5HDF5Reader >
{

    private final N5HDF5Reader n5;

    private final String dataset;

    private static IHDF5Reader openHdf5Reader( String hdf5Path )
    {
        try
        {
            return HDF5Factory.openForReading( hdf5Path );
        }
        catch ( HDF5Exception e )
        {
            throw new N5IOException( "Cannot open HDF5 Reader", new IOException( e ) );
        }
    }

    public N5HDF5ReaderToViewerImgLoaderAdapter( final N5HDF5Reader n5, final String dataset )
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
    public N5HDF5Reader getN5Reader()
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
            dataType = null;
        }
        return dataType == null ? DataType.UINT16 : dataType;
    }

    @Override
    public double[][] getMipmapResolutions( int setupId ) throws IOException
    {
        final String pathName = getFullPathName( getPathNameFromSetup( setupId ) ) + "/resolutions";
        final IHDF5Reader reader = openHdf5Reader( ( ( N5HDF5Reader ) n5 ).getFilename().toString() );
        final double[][] mipmapResolutions = reader.readDoubleMatrix( pathName );
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
        return String.format( "s%02d", setupId );
    }

    @Override
    public String getPathNameFromSetupTimepoint( int setupId, int timepointId )
    {
        return String.format( "t%05d/s%02d", timepointId, setupId );
    }

    @Override
    public String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level )
    {
        return String.format( "t%05d/s%02d/%d/cells", timepointId, setupId, level );
    }

    @Override
    public SpatialMultiscaleMetadata< ? > getMetadata()
    {
        throw new UnsupportedOperationException( "Not supported" );
    }
}
