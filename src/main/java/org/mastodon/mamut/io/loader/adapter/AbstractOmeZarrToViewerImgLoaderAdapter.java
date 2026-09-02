/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2026 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.universe.N5TreeNode;
import org.janelia.saalfeldlab.n5.universe.metadata.N5GenericSingleScaleMetadataParser;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.OmeNgffMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.ome.ngff.OmeNgffMetadataParser;
import org.mastodon.mamut.io.loader.util.mobie.N5OMEZarrCacheArrayLoader;
import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales;
import org.mastodon.mamut.io.loader.util.mobie.ZarrAxes;

import bdv.img.cache.SimpleCacheArrayLoader;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.cell.CellGrid;

/**
 * Base adapter for OME-Zarr (OME-NGFF) containers, shared by the Zarr v2 and
 * Zarr v3 readers. The two differ only in where the {@code multiscales}
 * metadata lives in the container attributes.
 *
 * @param <T>
 *            the type of the {@link N5Reader} backing the container.
 */
public abstract class AbstractOmeZarrToViewerImgLoaderAdapter< T extends N5Reader > implements N5ReaderToViewerImgLoaderAdapter< T >
{

    protected final T n5;

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
        tmp[ 0 ] = Arrays.stream( chunkSize( attributes ) ).toArray()[ 0 ];
        tmp[ 1 ] = Arrays.stream( chunkSize( attributes ) ).toArray()[ 1 ];
        tmp[ 2 ] = 1;
        return tmp;
    }

    /**
     * https://github.com/mobie/mobie-io/blob/0216a25b2fa6f4e3fd7f9f6976de278b8eaa1b76/src/main/java/org/embl/mobie/io/ome/zarr/loaders/N5OMEZarrImageLoader.java#L520
     * @param setupId
     * @param attributes
     * @return
     */
    /**
     * The size of the unit that is actually fetched and decoded. For a sharded
     * Zarr v3 dataset this is the inner chunk, not the shard: a shard of such a
     * dataset routinely holds hundreds of megabytes, which makes a hopeless
     * cell size for the viewer. For every other dataset the two are the same.
     *
     * @param attributes
     *            the attributes of the dataset.
     * @return the size of a chunk.
     */
    protected static int[] chunkSize( final DatasetAttributes attributes )
    {
        return attributes.getChunkSize();
    }

    private int[] getBlockSize( int setupId, DatasetAttributes attributes )
    {
        ZarrAxes zarrAxes = setupToMultiscale.get( setupId ).axes;
        if ( !zarrAxes.hasZAxis() )
        {
            return fillBlockSize( attributes );
        }
        else
        {
            return Arrays.stream( chunkSize( attributes ) ).limit( 3 ).toArray();
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

    /**
     * @param n5
     *            the reader for the container.
     * @param dataset
     *            the path of the multiscales group within the container.
     * @param multiscalesKey
     *            the attribute path the {@code multiscales} metadata is stored
     *            under, which depends on the OME-NGFF version.
     */
    protected AbstractOmeZarrToViewerImgLoaderAdapter( final T n5, final String dataset, final String multiscalesKey )
    {
        this.n5 = n5;
        this.dataset = dataset;
        int setupId = -1;
        final OmeZarrMultiscales[] multiscales = n5.getAttribute( dataset, multiscalesKey, OmeZarrMultiscales[].class );
        if ( multiscales == null )
        {
            throw new N5Exception( "No OME-Zarr '" + multiscalesKey + "' metadata found in '" + dataset + "'" );
        }
        for ( OmeZarrMultiscales multiscale : multiscales )
        {
            final DatasetAttributes attributes = n5.getDatasetAttributes( dataset + multiscale.datasets[ 0 ].path );
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
    public T getN5Reader()
    {
        return n5;
    }

    @Override
    public DataType getSetupDataType( int setupId ) throws IOException
    {
        // The multiscales group itself carries no data type; take it from the
        // full resolution level.
        final DatasetAttributes attributes = setupToAttributes.get( setupId );
        if ( attributes == null )
        {
            throw new IOException( "Attributes not found for setup " + setupId );
        }
        return attributes.getDataType();
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
                long[] dimensions = n5.getDatasetAttributes( getFullPathName( getPathNameLevel( setupId, level ) ) ).getDimensions();
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

    private DatasetAttributes getDatasetAttributes( int setupId, int level ) throws N5Exception
    {
        return n5.getDatasetAttributes( getFullPathName( getPathNameLevel( setupId, level ) ) );
    }

    @Override
    public long[] getDimensions( int setupId, int timepointId, int level )
    {
        final DatasetAttributes attributes = getDatasetAttributes( setupId, level );
        return getSpatialDimensions( setupId, attributes ).dimensionsAsLongArray();
    }

    @Override
    public int[] getCellDimensions( int setupId, int timepointId, int level )
    {
        final DatasetAttributes attributes = getDatasetAttributes( setupId, level );
        return getBlockSize( setupId, attributes );
    }

    @Override
    public SimpleCacheArrayLoader< ? > createCacheArrayLoader( int setupId, int timepointId, int level, CellGrid grid )
            throws IOException
    {
        final DatasetAttributes attributes;
        try
        {
            attributes = getDatasetAttributes( setupId, level );
        }
        catch ( final N5Exception e )
        {
            throw new IOException( e );
        }
        final String pathName = getFullPathName( getPathNameLevel( setupId, level ) );
        return new N5OMEZarrCacheArrayLoader<>( n5, pathName, setupToChannel.get( setupId ), timepointId, attributes, grid,
                setupToMultiscale.get( setupId ).axes );
    }

    @Override
    public String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level )
    {
        return getPathNameLevel( setupId, level );
    }

    @Override
    public OmeNgffMetadata getMetadata()
    {
        // Walk the dataset only. Walking the whole container from its root
        // lists every group of the container, which is prohibitively expensive
        // when the container is remote.
        final String path = dataset.endsWith( "/" ) ? dataset.substring( 0, dataset.length() - 1 ) : dataset;
        // The discoverer submits one task per group to this executor. An
        // unbounded pool spawns a thread per group, which a large remote
        // container will not survive.
        final ExecutorService executor = Executors.newFixedThreadPool( Math.max( 1, Runtime.getRuntime().availableProcessors() ) );
        try
        {
            final N5DatasetDiscoverer discoverer = new N5DatasetDiscoverer( n5,
                    executor,
                    Collections.singletonList( new N5GenericSingleScaleMetadataParser() ),
                    Collections.singletonList( new OmeNgffMetadataParser() ) );
            final N5TreeNode node = discoverer.discoverAndParseRecursive( path );
            final N5Metadata meta = node == null ? null : node.getMetadata();
            if ( meta instanceof OmeNgffMetadata )
            {
                return ( OmeNgffMetadata ) meta;
            }
            return null;
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            executor.shutdown();
        }
    }

}
