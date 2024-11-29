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
package org.mastodon.mamut.io.loader.util.mobie;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.mastodon.mamut.io.img.cache.MastodonSimpleCacheArrayLoader;

import bdv.img.n5.DataTypeProperties;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;

public class N5CacheArrayLoader< T, A extends DataAccess > implements MastodonSimpleCacheArrayLoader< A >
{
    private final N5Reader n5;

    private final String pathName;

    private final DatasetAttributes attributes;

    private final IntFunction< T > createPrimitiveArray;

    private final Function< T, A > createVolatileArrayAccess;

    public N5CacheArrayLoader( final N5Reader n5, final String pathName, final DatasetAttributes attributes,
            final DataTypeProperties< ?, ?, T, A > dataTypeProperties )
    {
        this( n5, pathName, attributes, dataTypeProperties.createPrimitiveArray(), dataTypeProperties.createVolatileArrayAccess() );
    }

    public N5CacheArrayLoader( final N5Reader n5, final String pathName, final DatasetAttributes attributes,
            final IntFunction< T > createPrimitiveArray,
            final Function< T, A > createVolatileArrayAccess )
    {
        this.n5 = n5;
        this.pathName = pathName;
        this.attributes = attributes;
        this.createPrimitiveArray = createPrimitiveArray;
        this.createVolatileArrayAccess = createVolatileArrayAccess;
    }

    @Override
    public A loadArray( final long[] gridPosition, final int[] cellDimensions ) throws IOException
    {
        return loadArrayAtTimepoint( gridPosition, cellDimensions, -1 );
    }

    @Override
    public A loadArrayAtTimepoint( final long[] gridPosition, final int[] cellDimensions, final int timepoint ) throws IOException
    {
        final DataBlock< T > dataBlock;
        try
        {
            dataBlock = Cast.unchecked( n5.readBlock( pathName, attributes, gridPosition ) );
        }
        catch ( final N5Exception e )
        {
            throw new IOException( e );
        }
        if ( dataBlock != null && Arrays.equals( dataBlock.getSize(), cellDimensions ) )
        {
            return createVolatileArrayAccess.apply( dataBlock.getData() );
        }
        else
        {
            final T data = createPrimitiveArray.apply( ( int ) Intervals.numElements( cellDimensions ) );
            if ( dataBlock != null )
            {
                final T src = dataBlock.getData();
                final int[] srcDims = dataBlock.getSize();
                final int[] dstDims = Arrays.copyOf( cellDimensions, srcDims.length );
                if ( cellDimensions.length < dstDims.length )
                {
                    Arrays.fill( dstDims, cellDimensions.length, dstDims.length, 1 );
                }
                final int[] srcPos = new int[ srcDims.length ];
                final int[] dstPos = new int[ srcDims.length ];
                final int[] size = new int[ srcDims.length ];
                if ( srcDims.length == 4 && timepoint >= 0 )
                {
                    srcPos[ srcDims.length - 1 ] = timepoint % srcDims[ srcDims.length - 1 ];
                }
                Arrays.setAll( size, d -> Math.min( srcDims[ d ], dstDims[ d ] ) );
                ndArrayCopy( src, srcDims, srcPos, data, dstDims, dstPos, size );
            }
            return createVolatileArrayAccess.apply( data );
        }
    }

    /**
     * Like `System.arrayCopy()` but for flattened nD arrays.
     *
     * @param src
     * 		the (flattened) source array.
     * @param srcSize
     * 		dimensions of the source array.
     * @param srcPos
     * 		starting position in the source array.
     * @param dest
     * 		the (flattened destination array.
     * @param destSize
     * 		dimensions of the source array.
     * @param destPos
     * 		starting position in the destination data.
     * @param size
     * 		the number of array elements to be copied.
     */
    // TODO: This will be moved to a new imglib2-blk artifact later. Re-use it from there when that happens.
    private static < T > void ndArrayCopy(
            final T src, final int[] srcSize, final int[] srcPos,
            final T dest, final int[] destSize, final int[] destPos,
            final int[] size )
    {
        final int n = srcSize.length;
        int srcStride = 1;
        int destStride = 1;
        int srcOffset = 0;
        int destOffset = 0;
        for ( int d = 0; d < n; ++d )
        {
            srcOffset += srcStride * srcPos[ d ];
            srcStride *= srcSize[ d ];
            destOffset += destStride * destPos[ d ];
            destStride *= destSize[ d ];
        }
        ndArrayCopy( n - 1, src, srcSize, srcOffset, dest, destSize, destOffset, size );
    }

    private static < T > void ndArrayCopy(
            final int d,
            final T src, final int[] srcSize, final int srcPos,
            final T dest, final int[] destSize, final int destPos,
            final int[] size )
    {
        if ( d == 0 )
            System.arraycopy( src, srcPos, dest, destPos, size[ d ] );
        else
        {
            int srcStride = 1;
            int destStride = 1;
            for ( int dd = 0; dd < d; ++dd )
            {
                srcStride *= srcSize[ dd ];
                destStride *= destSize[ dd ];
            }

            final int w = size[ d ];
            for ( int x = 0; x < w; ++x )
            {
                ndArrayCopy( d - 1,
                        src, srcSize, srcPos + x * srcStride,
                        dest, destSize, destPos + x * destStride,
                        size );
            }
        }
    }
}
