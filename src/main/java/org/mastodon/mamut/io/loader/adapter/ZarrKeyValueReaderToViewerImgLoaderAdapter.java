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

import static org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales.MULTI_SCALE_KEY;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.zarr.ZarrKeyValueReader;
import org.mastodon.mamut.io.loader.util.mobie.N5OMEZarrCacheArrayLoader;
import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales;
import org.mastodon.mamut.io.loader.util.mobie.ZarrAxes;

import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.n5.BdvN5Format;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.cell.CellGrid;

public class ZarrKeyValueReaderToViewerImgLoaderAdapter implements N5ReaderToViewerImgLoaderAdapter< ZarrKeyValueReader >
{

    private final ZarrKeyValueReader n5;

    private final String dataset;

    // Used for ZarrKeyValueReader
    private final Map< Integer, String > setupToPathname = new HashMap<>();

    private final Map< Integer, OmeZarrMultiscales > setupToMultiscale = new HashMap<>();

    private final Map< Integer, DatasetAttributes > setupToAttributes = new HashMap<>();

    private final Map< Integer, Integer > setupToChannel = new HashMap<>();

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

    /**
     * @param setupId
     * @param level
     * @return
     */
    private String getPathNameLevel( int setupId, int level )
    {
        return setupToPathname.get( setupId ) + "/" + setupToMultiscale.get( setupId ).datasets[ level ].path;
    }

    public ZarrKeyValueReaderToViewerImgLoaderAdapter( final ZarrKeyValueReader n5, final String dataset )
    {
        this.n5 = n5;
        this.dataset = dataset;
        int setupId = -1;
        final OmeZarrMultiscales[] multiscales = n5.getAttribute( "", MULTI_SCALE_KEY, OmeZarrMultiscales[].class );
        for ( OmeZarrMultiscales multiscale : multiscales )
        {
            final DatasetAttributes attributes = n5.getDatasetAttributes( multiscale.datasets[ 0 ].path );
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

    @Override
    public String getDataset()
    {
        return dataset;
    }

    @Override
    public ZarrKeyValueReader getN5Reader()
    {
        return n5;
    }

    @Override
    public DataType getSetupDataType( int setupId ) throws IOException
    {
        DataType dataType = null;
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

    @Override
    public double[][] getMipmapResolutions( int setupId ) throws IOException
    {
        double[][] mipmapResolutions = null;
        try
        {
            OmeZarrMultiscales multiscale = setupToMultiscale.get( setupId );
            if ( multiscale == null )
            {
                throw new IOException( "Multiscale not found for setup " + setupId );
            }
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
        return mipmapResolutions;
    }

    @Override
    public long[] getDimensions( DatasetAttributes attributes, int setupId )
    {
        return getSpatialDimensions( setupId, attributes ).dimensionsAsLongArray();
    }

    @Override
    public int[] getCellDimensions( DatasetAttributes attributes, int setupId )
    {
        return getBlockSize( setupId, attributes );
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
        return new N5OMEZarrCacheArrayLoader<>( n5, pathName, setupToChannel.get( setupId ), timepointId, attributes, grid,
                setupToMultiscale.get( setupId ).axes );
    }

    @Override
    public String getPathNameFromSetup( int setupId )
    {
        return setupToPathname.get( setupId );
    }

    @Override
    public String getPathNameFromSetupTimepoint( int setupId, int timepointId )
    {
        return "";
    }

    @Override
    public String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level )
    {
        return getPathNameLevel( setupId, level );
    }

}
