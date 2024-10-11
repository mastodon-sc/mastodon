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
package org.mastodon.mamut.io.loader.adapter;

import java.io.IOException;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import bdv.img.cache.SimpleCacheArrayLoader;
import net.imglib2.img.cell.CellGrid;

public interface N5ReaderToViewerImgLoaderAdapter< T extends N5Reader >
{

    String getDataset();

    T getN5Reader();

    DataType getSetupDataType( final int setupId ) throws IOException;

    double[][] getMipmapResolutions( final int setupId ) throws IOException;

    long[] getDimensions( int setupId, int timepointId, int level );

    int[] getCellDimensions( int setupId, int timepointId, int level );

    String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level );

    SimpleCacheArrayLoader< ? > createCacheArrayLoader( int setupId, int timepointId, int level, CellGrid grid )
            throws IOException;

    N5Metadata getMetadata();

    default int getNumSetups()
    {
        int numSetups = 0;
        while ( true )
        {
            try
            {
                if ( getMipmapResolutions( numSetups ) == null )
                    break;
            }
            catch ( IOException e )
            {
                break;
            }
            numSetups++;
        }
        return numSetups;
    }

    default String getFullPathName( final String pathName )
    {
        return ( getDataset() + pathName ).replaceAll( "/+", "/" );
    }
}
