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
/*
 * Original code from bigdataviewer-core:
 * https://github.com/bigdataviewer/bigdataviewer-core/blob/c47e2370ae9b9e281444f127cb36cd9ef1497336/src/main/java/bdv/img/n5/N5ImageLoader.java
 * LICENSE is shown below:
 * 
 * #%L
 * BigDataViewer core classes with minimal dependencies.
 * %%
 * Copyright (C) 2012 - 2023 BigDataViewer developers.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.mastodon.mamut.io.loader.util.OmeZarrMultiscales;
import org.mastodon.mamut.io.loader.util.OmeZarrMultiscalesAdapter;
import org.mastodon.mamut.io.loader.util.ZarrAxes;
import org.mastodon.mamut.io.loader.util.ZarrAxesAdapter;

import com.google.gson.GsonBuilder;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.n5.DataTypeProperties;
import bdv.util.ConstantRandomAccessible;
import bdv.util.MipmapTransforms;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import mpicbg.spim.data.sequence.MultiResolutionSetupImgLoader;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;

public class N5UniverseImgLoader implements ViewerImgLoader, MultiResolutionImgLoader
{
    private N5Reader n5;

    private N5Factory factory;

    private final String url;

    public String getUrl()
    {
        return url;
    }

    private final String dataset;

    public String getDataset()
    {
        return dataset;
    }

    private BdvN5UniverseFormat format;

    // TODO: it would be good if this would not be needed
    //       find available setups from the n5
    private final AbstractSequenceDescription< ?, ?, ? > seq;

    /**
     * Maps setup id to {@link SetupImgLoader}.
     */
    private final Map< Integer, SetupImgLoader< ?, ? > > setupImgLoaders = new HashMap<>();

    public N5UniverseImgLoader( final String uri, final String dataset, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( ZarrCompressor.class, ZarrCompressor.jsonAdapter );
        gsonBuilder.registerTypeAdapter( ZarrAxes.class, new ZarrAxesAdapter() );
        gsonBuilder.registerTypeAdapter( OmeZarrMultiscales.class, new OmeZarrMultiscalesAdapter() );
        this.factory = new N5Factory()
                .cacheAttributes( true )
                .hdf5DefaultBlockSize( 64 )
                .zarrDimensionSeparator( "/" )
                .zarrMapN5Attributes( true )
                .gsonBuilder( gsonBuilder );
        this.url = uri;
        this.dataset = dataset.endsWith( "/" ) ? dataset : dataset + "/";
        this.seq = sequenceDescription;
    }

    public N5Reader getN5Reader()
    {
        return n5;
    }

    private volatile boolean isOpen = false;

    private SharedQueue createdSharedQueue;

    private VolatileGlobalCellCache cache;

    private int requestedNumFetcherThreads = -1;

    private SharedQueue requestedSharedQueue;

    @Override
    public synchronized void setNumFetcherThreads( final int n )
    {
        requestedNumFetcherThreads = n;
    }

    @Override
    public void setCreatedSharedQueue( final SharedQueue createdSharedQueue )
    {
        requestedSharedQueue = createdSharedQueue;
    }

    private void open()
    {
        if ( !isOpen )
        {
            synchronized ( this )
            {
                if ( isOpen )
                    return;

                try
                {
                    this.n5 = factory.openReader( url );
                    this.format = new BdvN5UniverseFormat( n5, dataset );
                    int maxNumLevels = 0;
                    final List< ? extends BasicViewSetup > setups = seq.getViewSetupsOrdered();
                    for ( final BasicViewSetup setup : setups )
                    {
                        final int setupId = setup.getId();
                        final SetupImgLoader< ?, ? > setupImgLoader = createSetupImgLoader( setupId );
                        setupImgLoaders.put( setupId, setupImgLoader );
                        maxNumLevels = Math.max( maxNumLevels, setupImgLoader.numMipmapLevels() );
                    }

                    final int numFetcherThreads = requestedNumFetcherThreads >= 0
                            ? requestedNumFetcherThreads
                            : Math.max( 1, Runtime.getRuntime().availableProcessors() );
                    final SharedQueue queue = requestedSharedQueue != null
                            ? requestedSharedQueue
                            : ( createdSharedQueue = new SharedQueue( numFetcherThreads, maxNumLevels ) );
                    cache = new VolatileGlobalCellCache( queue );
                }
                catch ( final IOException e )
                {
                    throw new RuntimeException( e );
                }

                isOpen = true;
            }
        }
    }

    /**
     * Clear the cache. Images that were obtained from
     * this loader before {@link #close()} will stop working. Requesting images
     * after {@link #close()} will cause the n5 to be reopened (with a
     * new cache).
     */
    public void close()
    {
        if ( isOpen )
        {
            synchronized ( this )
            {
                if ( !isOpen )
                    return;

                if ( createdSharedQueue != null )
                    createdSharedQueue.shutdown();
                cache.clearCache();

                createdSharedQueue = null;
                isOpen = false;
            }
        }
    }

    @Override
    public SetupImgLoader< ?, ? > getSetupImgLoader( final int setupId )
    {
        open();
        return setupImgLoaders.get( setupId );
    }

    private < T extends NativeType< T >, V extends Volatile< T > & NativeType< V > > SetupImgLoader< T, V >
            createSetupImgLoader( final int setupId ) throws IOException
    {
        DataType dataType = format.getSetupDataType( setupId );
        return new SetupImgLoader<>( setupId, Cast.unchecked( DataTypeProperties.of( dataType ) ) );
    }

    @Override
    public CacheControl getCacheControl()
    {
        open();
        return cache;
    }

    public class SetupImgLoader< T extends NativeType< T >, V extends Volatile< T > & NativeType< V > >
            extends AbstractViewerSetupImgLoader< T, V >
            implements MultiResolutionSetupImgLoader< T >
    {
        private final int setupId;

        private final double[][] mipmapResolutions;

        private final AffineTransform3D[] mipmapTransforms;

        public SetupImgLoader( final int setupId, final DataTypeProperties< T, V, ?, ? > props ) throws IOException
        {
            this( setupId, props.type(), props.volatileType() );
        }

        public SetupImgLoader( final int setupId, final T type, final V volatileType ) throws IOException
        {
            super( type, volatileType );
            this.setupId = setupId;
            mipmapResolutions = format.getMipmapResolutions( setupId );
            mipmapTransforms = new AffineTransform3D[ mipmapResolutions.length ];
            for ( int level = 0; level < mipmapResolutions.length; level++ )
                mipmapTransforms[ level ] = MipmapTransforms.getMipmapTransformDefault( mipmapResolutions[ level ] );
        }

        @Override
        public RandomAccessibleInterval< V > getVolatileImage( final int timepointId, final int level, final ImgLoaderHint... hints )
        {
            return prepareCachedImage( timepointId, level, LoadingStrategy.BUDGETED, volatileType );
        }

        @Override
        public RandomAccessibleInterval< T > getImage( final int timepointId, final int level, final ImgLoaderHint... hints )
        {
            return prepareCachedImage( timepointId, level, LoadingStrategy.BLOCKING, type );
        }

        @Override
        public Dimensions getImageSize( final int timepointId, final int level )
        {
            try
            {
                final String pathName = getFullPathName( format.getPathName( setupId, timepointId, level ) );
                final DatasetAttributes attributes = n5.getDatasetAttributes( pathName );
                return new FinalDimensions( attributes.getDimensions() );
            }
            catch ( final RuntimeException e )
            {
                return null;
            }
        }

        @Override
        public double[][] getMipmapResolutions()
        {
            return mipmapResolutions;
        }

        @Override
        public AffineTransform3D[] getMipmapTransforms()
        {
            return mipmapTransforms;
        }

        @Override
        public int numMipmapLevels()
        {
            return mipmapResolutions.length;
        }

        @Override
        public VoxelDimensions getVoxelSize( final int timepointId )
        {
            return null;
        }

        /**
         * Create a {@link CellImg} backed by the cache.
         */
        private < K extends NativeType< K > > RandomAccessibleInterval< K > prepareCachedImage( final int timepointId, final int level,
                final LoadingStrategy loadingStrategy, final K type )
        {
            try
            {
                final String pathName = getFullPathName( format.getPathName( setupId, timepointId, level ) );
                final DatasetAttributes attributes = n5.getDatasetAttributes( pathName );
                final long[] dimensions = format.getDimensions( attributes, setupId );
                final int[] cellDimensions = format.getCellDimensions( attributes, setupId );
                final CellGrid grid = new CellGrid( dimensions, cellDimensions );

                final int priority = numMipmapLevels() - 1 - level;
                final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );

                final SimpleCacheArrayLoader< ? > loader = format.createCacheArrayLoader( pathName, setupId, timepointId, grid );
                return cache.createImg( grid, timepointId, setupId, level, cacheHints, loader, type );
            }
            catch ( final IOException | N5Exception e )
            {
                System.err.println( String.format(
                        "image data for timepoint %d setup %d level %d could not be found.",
                        timepointId, setupId, level ) );
                return Views.interval(
                        new ConstantRandomAccessible<>( type.createVariable(), 3 ),
                        new FinalInterval( 1, 1, 1 ) );
            }
        }
    }

    private String getFullPathName( final String pathName )
    {
        return dataset + pathName;
    }
}
