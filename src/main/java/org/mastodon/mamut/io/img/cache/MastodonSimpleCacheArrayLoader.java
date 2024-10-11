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
package org.mastodon.mamut.io.img.cache;

import java.io.IOException;

import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.DefaultEmptyArrayCreator;
import bdv.img.cache.EmptyArrayCreator;
import bdv.img.cache.SimpleCacheArrayLoader;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileCharArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileDoubleArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileIntArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileLongArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileShortArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileCharArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileDoubleArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileIntArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileLongArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;

public interface MastodonSimpleCacheArrayLoader< A extends DataAccess > extends SimpleCacheArrayLoader< A >
{

    /**
     * Implementing classes must override this if {@code A} is not a standard
     * {@link VolatileArrayDataAccess} type. The default implementation returns
     * {@code null}, which will let
     * {@link CreateInvalidVolatileCell#get(CellGrid, NativeType, boolean)
     * CreateInvalidVolatileCell.get(...)} try to figure out the appropriate
     * {@link DefaultEmptyArrayCreator}.
     * <p>
     * Default access types are
     * </p>
     * <ul>
     * <li>{@link DirtyVolatileByteArray}</li>
     * <li>{@link VolatileByteArray}</li>
     * <li>{@link DirtyVolatileCharArray}</li>
     * <li>{@link VolatileCharArray}</li>
     * <li>{@link DirtyVolatileDoubleArray}</li>
     * <li>{@link VolatileDoubleArray}</li>
     * <li>{@link DirtyVolatileFloatArray}</li>
     * <li>{@link VolatileFloatArray}</li>
     * <li>{@link DirtyVolatileIntArray}</li>
     * <li>{@link VolatileIntArray}</li>
     * <li>{@link DirtyVolatileLongArray}</li>
     * <li>{@link VolatileLongArray}</li>
     * <li>{@link DirtyVolatileShortArray}</li>
     * <li>{@link VolatileShortArray}</li>
     * </ul>
     *
     * @return an {@link EmptyArrayCreator} for {@code A} or null.
     */
    default EmptyArrayCreator< A > getEmptyArrayCreator()
    {
        return null;
    }

    /**
     * Load cell data into memory. This method blocks until data is successfully
     * loaded. If it completes normally, the returned data is always valid. If
     * anything goes wrong, an {@link IOException} is thrown.
     * <p>
     * {@code SimpleCacheArrayLoader} is supposed to load data one specific
     * image. {@code loadArray()} will not get information about which
     * timepoint, resolution level, etc a requested block belongs to.
     * <p>
     * This is in contrast to
     * {@link CacheArrayLoader#loadArray(int, int, int, int[], long[])}, where
     * all information to identify a particular block in a whole dataset is
     * provided.
     *
     * @param gridPosition
     *            the coordinate of the cell in the cell grid.
     * @param cellDimensions
     *            the dimensions of the cell.
     * @param timepoint
     *            the timepoint.
     *
     * @return loaded cell data.
     */
    A loadArrayAtTimepoint( long[] gridPosition, int[] cellDimensions, int timepoint ) throws IOException;
}
