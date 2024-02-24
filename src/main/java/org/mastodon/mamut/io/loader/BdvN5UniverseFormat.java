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
package org.mastodon.mamut.io.loader;

import static org.mastodon.mamut.io.loader.util.OmeZarrMultiscales.MULTI_SCALE_KEY;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Exception.N5IOException;
import org.janelia.saalfeldlab.n5.N5KeyValueReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.zarr.ZarrKeyValueReader;
import org.mastodon.mamut.io.loader.util.N5CacheArrayLoader;
import org.mastodon.mamut.io.loader.util.N5OMEZarrCacheArrayLoader;
import org.mastodon.mamut.io.loader.util.OmeZarrMultiscales;
import org.mastodon.mamut.io.loader.util.ZarrAxes;

import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.n5.BdvN5Format;
import bdv.img.n5.DataTypeProperties;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import hdf.hdf5lib.exceptions.HDF5Exception;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.cell.CellGrid;

public class BdvN5UniverseFormat
{
    private final N5Reader n5;

    private final String dataset;

    // Used for ZarrKeyValueReader
    private final Map< Integer, String > setupToPathname = new HashMap<>();

    private final Map< Integer, OmeZarrMultiscales > setupToMultiscale = new HashMap<>();

    private final Map< Integer, DatasetAttributes > setupToAttributes = new HashMap<>();

    private final Map< Integer, Integer > setupToChannel = new HashMap<>();

    public BdvN5UniverseFormat( final N5Reader n5, final String dataset )
    {
        this.n5 = n5;
        this.dataset = dataset;
        if ( n5 instanceof ZarrKeyValueReader )
        {
            int setupId = -1;
            OmeZarrMultiscales[] multiscales = n5.getAttribute( "", MULTI_SCALE_KEY, OmeZarrMultiscales[].class );
            for ( OmeZarrMultiscales multiscale : multiscales )
            {
                DatasetAttributes attributes = n5.getDatasetAttributes( multiscale.datasets[ 0 ].path );
                long nC = 1;
                if ( multiscale.axes.hasChannels() )
                {
                    nC = attributes.getDimensions()[ multiscale.axes.channelIndex() ];
                }

                for ( int c = 0; c < nC; c++ )
                {
                    // each channel is one setup
                    setupId++;
                    setupToChannel.put( setupId, c );

                    // all channels have the same multiscale and attributes
                    setupToMultiscale.put( setupId, multiscale );
                    setupToAttributes.put( setupId, attributes );
                    setupToPathname.put( setupId, "" );
                }
            }
        }
    }

    public DataType getSetupDataType( final int setupId ) throws IOException
    {
        DataType dataType = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
            }
            catch ( final N5Exception e )
            {
                dataType = null;
            }
            return dataType == null ? DataType.UINT16 : dataType;
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            try
            {
                final String pathName = getFullPathName( setupToPathname.get( setupId ) );
                dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
            return dataType == null ? DataType.UINT16 : dataType;
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        if ( dataType == null )
        {
            new RuntimeException( "DataType is not available." );
        }
        return dataType;
    }

    public double[][] getMipmapResolutions( final int setupId ) throws IOException
    {
        double[][] mipmapResolutions = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            final String pathName = getFullPathName( getPathName( setupId ) ) + "/resolutions";
            IHDF5Reader reader = openHdf5Reader( ( ( N5HDF5Reader ) n5 ).getFilename().toString() );
            mipmapResolutions = reader.readDoubleMatrix( pathName );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            try
            {
                OmeZarrMultiscales multiscale = setupToMultiscale.get( setupId );
                mipmapResolutions = new double[ multiscale.datasets.length ][];

                long[] dimensionsOfLevel0 = setupToAttributes.get( setupId ).getDimensions();
                mipmapResolutions[ 0 ] = new double[] { 1.0, 1.0, 1.0 };

                for ( int level = 1; level < mipmapResolutions.length; level++ )
                {
                    long[] dimensions = n5.getDatasetAttributes( getPathNameLevel( setupId, level ) ).getDimensions();
                    mipmapResolutions[ level ] = new double[ 3 ];
                    for ( int d = 0; d < 2; d++ )
                    {
                        mipmapResolutions[ level ][ d ] = Math.round( 1.0 * dimensionsOfLevel0[ d ] / dimensions[ d ] );
                    }
                    mipmapResolutions[ level ][ 2 ] =
                            multiscale.axes.hasZAxis() ? Math.round( 1.0 * dimensionsOfLevel0[ 2 ] / dimensions[ 2 ] ) : 1.0;
                }
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                mipmapResolutions = n5.getAttribute( pathName, BdvN5Format.DOWNSAMPLING_FACTORS_KEY, double[][].class );
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        if ( mipmapResolutions == null )
        {
            new RuntimeException( "DataType is not available." );
        }
        return mipmapResolutions;
    }

    public String getPathName( final int setupId )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "s%02d", setupId );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            pathName = setupToPathname.get( setupId );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d", setupId );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;
    }

    /**
     * https://github.com/mobie/mobie-io/blob/0216a25b2fa6f4e3fd7f9f6976de278b8eaa1b76/src/main/java/org/embl/mobie/io/ome/zarr/loaders/N5OMEZarrImageLoader.java#L422
     * @param setupId
     * @param attributes
     * @return
     */
    private Dimensions getSpatialDimensions( int setupId, DatasetAttributes attributes )
    {
        final long[] spatialDimensions = new long[ 3 ];
        long[] attributeDimensions = attributes.getDimensions();
        Arrays.fill( spatialDimensions, 1 );
        ZarrAxes zarrAxes = setupToMultiscale.get( setupId ).axes;
        final Map< Integer, Integer > spatialToZarr = zarrAxes.spatialToZarr();
        for ( Map.Entry< Integer, Integer > entry : spatialToZarr.entrySet() )
        {
            spatialDimensions[ entry.getKey() ] = attributeDimensions[ entry.getValue() ];
        }
        return new FinalDimensions( spatialDimensions );
    }

    public long[] getDimensions( DatasetAttributes attributes, int setupId )
    {
        long[] dimensions = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            dimensions = attributes.getDimensions();
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            dimensions = getSpatialDimensions( setupId, attributes ).dimensionsAsLongArray();
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            dimensions = attributes.getDimensions();
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return dimensions;
    }

    /**
     * https://github.com/mobie/mobie-io/blob/0216a25b2fa6f4e3fd7f9f6976de278b8eaa1b76/src/main/java/org/embl/mobie/io/ome/zarr/loaders/N5OMEZarrImageLoader.java#L529
     * @param attributes
     * @return
     */
    private int[] fillBlockSize( DatasetAttributes attributes )
    {
        int[] tmp = new int[ 3 ];
        tmp[ 0 ] = Arrays.stream( attributes.getBlockSize() ).toArray()[ 0 ];
        tmp[ 1 ] = Arrays.stream( attributes.getBlockSize() ).toArray()[ 1 ];
        tmp[ 2 ] = 1;
        return tmp;
    }

    /**
     * https://github.com/mobie/mobie-io/blob/0216a25b2fa6f4e3fd7f9f6976de278b8eaa1b76/src/main/java/org/embl/mobie/io/ome/zarr/loaders/N5OMEZarrImageLoader.java#L520
     * @param setupId
     * @param attributes
     * @return
     */
    // TODO: Add description
    private int[] getBlockSize( int setupId, DatasetAttributes attributes )
    {
        ZarrAxes zarrAxes = setupToMultiscale.get( setupId ).axes;
        if ( !zarrAxes.hasZAxis() )
        {
            return fillBlockSize( attributes );
        }
        else
        {
            return Arrays.stream( attributes.getBlockSize() ).limit( 3 ).toArray();
        }
    }

    public int[] getCellDimensions( DatasetAttributes attributes, int setupId )
    {
        int[] cellDimensions = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            cellDimensions = attributes.getBlockSize();
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            cellDimensions = getBlockSize( setupId, attributes );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            cellDimensions = attributes.getBlockSize();
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return cellDimensions;
    }

    public SimpleCacheArrayLoader< ? > createCacheArrayLoader( final String pathName, int setupId, int timepointId, CellGrid grid )
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

        SimpleCacheArrayLoader< ? > loader = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            loader = new N5CacheArrayLoader<>( n5, pathName, attributes, DataTypeProperties.of( attributes.getDataType() ) );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            loader = new N5OMEZarrCacheArrayLoader<>( n5, pathName, setupToChannel.get( setupId ), timepointId, attributes, grid,
                    setupToMultiscale.get( setupId ).axes );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            loader = new N5CacheArrayLoader<>( n5, pathName, attributes, DataTypeProperties.of( attributes.getDataType() ) );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return loader;
    }

    public String getPathNameLevel( int setupId, int level )
    {
        return setupToPathname.get( setupId ) + "/" + setupToMultiscale.get( setupId ).datasets[ level ].path;
    }

    public String getPathName( final int setupId, final int timepointId )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "t%05d/s%02d", timepointId, setupId );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            pathName = "";
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d/timepoint%d", setupId, timepointId );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;
    }

    public String getPathName( final int setupId, final int timepointId, final int level )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "t%05d/s%02d/%d/cells", timepointId, setupId, level );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            pathName = getPathNameLevel( setupId, level );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d/timepoint%d/s%d", setupId, timepointId, level );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;

    }

    private String getFullPathName( final String pathName )
    {
        return dataset + pathName;
    }

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

}
