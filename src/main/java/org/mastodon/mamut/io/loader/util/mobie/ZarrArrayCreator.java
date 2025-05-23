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
package org.mastodon.mamut.io.loader.util.mobie;

import java.util.Arrays;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;

import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;

public class ZarrArrayCreator< A, T extends NativeType< T > > extends ArrayCreator< A, T >
{
    private final ZarrAxes zarrAxes;

    public ZarrArrayCreator( final CellGrid cellGrid, final DataType dataType, final ZarrAxes zarrAxes )
    {
        super( cellGrid, dataType );
        this.zarrAxes = zarrAxes;
    }

    public A createArray( final DataBlock< ? > dataBlock, final long[] gridPosition )
    {
        long[] cellDims = getCellDims( gridPosition );
        final int n = ( int ) ( cellDims[ 0 ] * cellDims[ 1 ] * cellDims[ 2 ] );

        if ( zarrAxes.getNumDimension() == 2 )
            cellDims = Arrays.stream( cellDims ).limit( 2 ).toArray();

        return VolatileDoubleArray( dataBlock, cellDims, n );
    }

    @Override
    public long[] getCellDims( final long[] gridPosition )
    {
        final long[] cellMin = new long[ Math.max( zarrAxes.getNumDimension(), 3 ) ];
        final int[] cellDims = new int[ Math.max( zarrAxes.getNumDimension(), 3 ) ];

        if ( zarrAxes.hasChannels() )
        {
            cellDims[ zarrAxes.channelIndex() ] = 1;
        }
        if ( zarrAxes.hasTimepoints() )
        {
            cellDims[ zarrAxes.timeIndex() ] = 1;
        }

        cellGrid.getCellDimensions( gridPosition, cellMin, cellDims );
        return Arrays.stream( cellDims ).mapToLong( i -> i ).toArray(); // casting to long for creating ArrayImgs.*
    }
}
