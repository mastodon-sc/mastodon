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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5KeyValueReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.bdv.N5ViewerCreator;
import org.janelia.saalfeldlab.n5.metadata.N5ViewerMultichannelMetadata;
import org.janelia.saalfeldlab.n5.metadata.imagej.N5ImagePlusMetadata;
import org.janelia.saalfeldlab.n5.universe.N5DatasetDiscoverer;
import org.janelia.saalfeldlab.n5.universe.N5TreeNode;
import org.janelia.saalfeldlab.n5.universe.metadata.MultiscaleMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5CosemMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.mastodon.mamut.io.loader.util.mobie.N5CacheArrayLoader;

import bdv.img.cache.SimpleCacheArrayLoader;
import bdv.img.n5.DataTypeProperties;
import net.imglib2.img.cell.CellGrid;

public class N5KeyValueReaderToViewerImgLoaderAdapter implements N5ReaderToViewerImgLoaderAdapter< N5KeyValueReader >
{

    private final N5KeyValueReader n5;

    private final String dataset;

    private final N5ViewerMultichannelMetadata metadata;

    private final Map< Integer, List< DatasetAttributes > > setupToAttributesList = new HashMap<>();

    public N5KeyValueReaderToViewerImgLoaderAdapter( final N5KeyValueReader n5, final String dataset )
    {
        this.n5 = n5;
        this.dataset = dataset;
        this.metadata = getMetadata( n5 );
        for ( int i = 0; i < metadata.getChildrenMetadata().length; i++ )
        {
            MultiscaleMetadata< ? > setupMetadata = metadata.getChildrenMetadata()[ i ];
            final List< DatasetAttributes > attributesList = Arrays.stream( setupMetadata.getChildrenMetadata() )
                    .map( levelMetadata -> levelMetadata.getAttributes() )
                    .collect( Collectors.toList() );
            setupToAttributesList.put( i, attributesList );
        }
    }

    @Override
    public String getDataset()
    {
        return dataset;
    }

    @Override
    public N5KeyValueReader getN5Reader()
    {
        return n5;
    }

    @Override
    public DataType getSetupDataType( int setupId ) throws IOException
    {
        return setupToAttributesList.get( setupId ).get( 0 ).getDataType();
    }

    @Override
    public double[][] getMipmapResolutions( int setupId ) throws IOException
    {
        final List< DatasetAttributes > attributesList = setupToAttributesList.get( setupId );
        if ( attributesList == null || attributesList.isEmpty() )
            return null;
        double[][] mipmapResolutions = new double[ attributesList.size() ][];
        long[] dimensionsOfLevel0 = attributesList.get( 0 ).getDimensions();
        for ( int level = 0; level < attributesList.size(); level++ )
        {
            long[] dimensions = attributesList.get( level ).getDimensions();
            mipmapResolutions[ level ] = new double[ 3 ];
            for ( int d = 0; d < 2; d++ )
            {
                mipmapResolutions[ level ][ d ] = Math.round( 1.0 * dimensionsOfLevel0[ d ] / dimensions[ d ] );
            }
            mipmapResolutions[ level ][ 2 ] =
                    attributesList.get( level ).getNumDimensions() == 3 ? Math.round( 1.0 * dimensionsOfLevel0[ 2 ] / dimensions[ 2 ] )
                            : 1.0;
        }
        return mipmapResolutions;
    }

    @Override
    public long[] getDimensions( int setupId, int timepointId, int level )
    {
        final DatasetAttributes attributes = setupToAttributesList.get( setupId ).get( level );
        return attributes.getDimensions();
    }

    @Override
    public int[] getCellDimensions( int setupId, int timepointId, int level )
    {
        final DatasetAttributes attributes = setupToAttributesList.get( setupId ).get( level );
        return attributes.getBlockSize();
    }

    @Override
    public String getPathNameFromSetupTimepointLevel( int setupId, int timepointId, int level )
    {
        return String.format( "c%d/s%d", setupId, level );
    }

    @Override
    public SimpleCacheArrayLoader< ? > createCacheArrayLoader( int setupId, int timepointId, int level, CellGrid grid )
            throws IOException
    {
        String pathName = getFullPathName( String.format( "c%d/s%d", setupId, level ) );
        DatasetAttributes attributes = setupToAttributesList.get( setupId ).get( level );
        return new N5CacheArrayLoader<>( n5, pathName, attributes, DataTypeProperties.of( attributes.getDataType() ) );
    }

    private static boolean isSupportedMetadata( final N5Metadata meta )
    {
        if ( meta instanceof N5ImagePlusMetadata ||
                meta instanceof N5CosemMetadata ||
                meta instanceof N5ViewerMultichannelMetadata )
            return true;
        return false;
    }

    private static N5Metadata getMetadataRecursively( final N5TreeNode node )
    {
        N5Metadata meta = node.getMetadata();
        if ( isSupportedMetadata( meta ) )
        {
            return meta;
        }
        for ( final N5TreeNode child : node.childrenList() )
        {
            meta = getMetadataRecursively( child );
            if ( meta != null )
            {
                return meta;
            }
        }
        return null;
    }

    private static N5ViewerMultichannelMetadata getMetadata( final N5Reader n5 )
    {
        N5TreeNode node = null;
        final N5DatasetDiscoverer discoverer = new N5DatasetDiscoverer(
                n5,
                Executors.newCachedThreadPool(),
                Arrays.asList( N5ViewerCreator.n5vParsers ),
                Arrays.asList( N5ViewerCreator.n5vGroupParsers )
        );
        try
        {
            node = discoverer.discoverAndParseRecursive( "" );
        }
        catch ( final IOException e )
        {}
        if ( node == null )
            return null;
        N5Metadata meta = getMetadataRecursively( node );
        if ( isSupportedMetadata( meta ) )
            return ( N5ViewerMultichannelMetadata ) meta;
        else
            throw new N5Exception( "No N5ViewerMultichannelMetadata found" );
    }

    @Override
    public N5ViewerMultichannelMetadata getMetadata()
    {
        return metadata;
    }

}
