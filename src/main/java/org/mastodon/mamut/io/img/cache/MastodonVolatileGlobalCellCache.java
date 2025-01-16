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
package org.mastodon.mamut.io.img.cache;

import java.util.concurrent.Callable;

import bdv.cache.CacheControl;
import bdv.cache.SharedQueue;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.EmptyArrayCreator;
import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.img.cache.VolatileGlobalCellCache;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.LoaderCache;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.ref.SoftRefLoaderCache;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.util.KeyBimap;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;

public class MastodonVolatileGlobalCellCache implements CacheControl
{
    /**
     * Key for a cell identified by timepoint, setup, level, and index
     * (flattened spatial coordinate).
     */
    public static class Key
    {
        private final int timepoint;

        private final int setup;

        private final int level;

        private final long index;

        /**
         * Create a Key for the specified cell.
         *
         * @param timepoint
         *            timepoint coordinate of the cell
         * @param setup
         *            setup coordinate of the cell
         * @param level
         *            level coordinate of the cell
         * @param index
         *            index of the cell (flattened spatial coordinate of the
         *            cell)
         */
        public Key( final int timepoint, final int setup, final int level, final long index )
        {
            this.timepoint = timepoint;
            this.setup = setup;
            this.level = level;
            this.index = index;

            int value = Long.hashCode( index );
            value = 31 * value + level;
            value = 31 * value + setup;
            value = 31 * value + timepoint;
            hashcode = value;
        }

        @Override
        public boolean equals( final Object other )
        {
            if ( this == other )
                return true;
            if ( !( other instanceof VolatileGlobalCellCache.Key ) )
                return false;
            final Key that = ( Key ) other;
            return ( this.index == that.index ) && ( this.timepoint == that.timepoint ) && ( this.setup == that.setup )
                    && ( this.level == that.level );
        }

        final int hashcode;

        @Override
        public int hashCode()
        {
            return hashcode;
        }
    }

    private final BlockingFetchQueues< Callable< ? > > queue;

    protected final LoaderCache< Key, Cell< ? > > backingCache;

    /**
     * Create a new global cache with a new fetch queue served by the specified
     * number of fetcher threads.
     *
     * @param maxNumLevels
     *            the highest occurring mipmap level plus 1.
     * @param numFetcherThreads
     *            how many threads should be created to load data.
     */
    public MastodonVolatileGlobalCellCache( final int maxNumLevels, final int numFetcherThreads )
    {
        queue = new SharedQueue( numFetcherThreads, maxNumLevels );
        backingCache = new SoftRefLoaderCache<>();
    }

    /**
     * Create a new global cache with the specified fetch queue. (It is the
     * callers responsibility to create fetcher threads that serve the queue.)
     *
     * @param queue
     *            queue to which asynchronous data loading jobs are submitted
     */
    public MastodonVolatileGlobalCellCache( final BlockingFetchQueues< Callable< ? > > queue )
    {
        this.queue = queue;
        backingCache = new SoftRefLoaderCache<>();
    }

    /**
     * Prepare the cache for providing data for the "next frame",
     * by moving pending cell request to the prefetch queue
     * ({@link BlockingFetchQueues#clearToPrefetch()}).
     */
    @Override
    public void prepareNextFrame()
    {
        queue.clearToPrefetch();
    }

    /**
     * Remove all references to loaded data.
     * <p>
     * Note that there may be pending cell requests which will re-populate the cache
     * unless the fetch queue is cleared as well.
     */
    public void clearCache()
    {
        backingCache.invalidateAll();
    }

    /**
     * Create a {@link VolatileCachedCellImg} backed by this {@link VolatileGlobalCellCache},
     * using the provided {@link CacheArrayLoader} to load data.
     *
     * @param grid
     * @param timepoint
     * @param setup
     * @param level
     * @param cacheHints
     * @param cacheArrayLoader
     * @param type
     * @return
     */
    public < T extends NativeType< T >, A extends DataAccess > VolatileCachedCellImg< T, A > createImg(
            final CellGrid grid,
            final int timepoint,
            final int setup,
            final int level,
            final CacheHints cacheHints,
            final CacheArrayLoader< A > cacheArrayLoader,
            final T type )
    {
        final CacheLoader< Long, Cell< ? > > loader = key -> {
            final int n = grid.numDimensions();
            final long[] cellMin = new long[ n ];
            final int[] cellDims = new int[ n ];
            grid.getCellDimensions( key, cellMin, cellDims );
            return new Cell<>(
                    cellDims,
                    cellMin,
                    cacheArrayLoader.loadArray( timepoint, setup, level, cellDims, cellMin ) );
        };
        return createImg( grid, timepoint, setup, level, cacheHints, loader, cacheArrayLoader.getEmptyArrayCreator(), type );
    }

    /**
     * Create a {@link VolatileCachedCellImg} backed by this {@link VolatileGlobalCellCache},
     * using the provided {@link SimpleCacheArrayLoader} to load data.
     *
     * @param grid
     * @param timepoint
     * @param setup
     * @param level
     * @param cacheHints
     * @param cacheArrayLoader
     * @param type
     * @return
     */
    public < T extends NativeType< T >, A extends DataAccess > VolatileCachedCellImg< T, A > createImg(
            final CellGrid grid,
            final int timepoint,
            final int setup,
            final int level,
            final CacheHints cacheHints,
            final SimpleCacheArrayLoader< A > cacheArrayLoader,
            final T type )
    {
        final CacheLoader< Long, Cell< ? > > loader = key -> {
            final int n = grid.numDimensions();
            final long[] cellMin = new long[ n ];
            final int[] cellDims = new int[ n ];
            final long[] cellGridPosition = new long[ n ];
            grid.getCellDimensions( key, cellMin, cellDims );
            grid.getCellGridPositionFlat( key, cellGridPosition );
            return new Cell<>( cellDims, cellMin, cacheArrayLoader.loadArray( cellGridPosition, cellDims ) );
        };
        return createImg( grid, timepoint, setup, level, cacheHints, loader, cacheArrayLoader.getEmptyArrayCreator(), type );
    }

    public < T extends NativeType< T >, A extends DataAccess > VolatileCachedCellImg< T, A > createImg(
            final CellGrid grid,
            final int timepoint,
            final int setup,
            final int level,
            final CacheHints cacheHints,
            final CacheLoader< Long, Cell< ? > > loader,
            final EmptyArrayCreator< A > emptyArrayCreator, // optional, can be null
            final T type )
    {
        final KeyBimap< Long, Key > bimap = KeyBimap.build(
                index -> new Key( timepoint, setup, level, index ),
                key -> ( key.timepoint == timepoint && key.setup == setup && key.level == level )
                        ? key.index
                        : null );

        final Cache< Long, Cell< ? > > cache = backingCache
                .mapKeys( bimap )
                .withLoader( loader );

        final CreateInvalidVolatileCell< ? > createInvalid = ( emptyArrayCreator == null )
                ? CreateInvalidVolatileCell.get( grid, type, false )
                : new CreateInvalidVolatileCell<>( grid, type.getEntitiesPerPixel(), emptyArrayCreator );

        final VolatileCache< Long, Cell< ? > > vcache = new WeakRefVolatileCache<>( cache, queue, createInvalid );

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		final VolatileCachedCellImg< T, A > img = new VolatileCachedCellImg<>( grid, type, cacheHints, ( VolatileCache ) vcache );

        return img;
    }
}
