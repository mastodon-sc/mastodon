package org.mastodon.mamut.io.loader.util;

import java.io.IOException;
import java.util.Map;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import com.amazonaws.SdkClientException;

import bdv.img.cache.SimpleCacheArrayLoader;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.cell.CellGrid;

public class N5OMEZarrCacheArrayLoader< A extends DataAccess > implements SimpleCacheArrayLoader< A >
{
    private final N5Reader n5;

    private final String pathName;

    private final int channel;

    private final int timepoint;

    private final DatasetAttributes attributes;

    private final ZarrArrayCreator< A, ? > zarrArrayCreator;

    private final ZarrAxes zarrAxes;

    public N5OMEZarrCacheArrayLoader( final N5Reader n5, final String pathName, final int channel, final int timepoint,
            final DatasetAttributes attributes, CellGrid grid, ZarrAxes zarrAxes )
    {
        this.n5 = n5;
        this.pathName = pathName; // includes the level
        this.channel = channel;
        this.timepoint = timepoint;
        this.attributes = attributes;
        final DataType dataType = attributes.getDataType();
        this.zarrArrayCreator = new ZarrArrayCreator<>( grid, dataType, zarrAxes );
        this.zarrAxes = zarrAxes;
    }

    @Override
    public A loadArray( final long[] gridPosition, int[] cellDimensions ) throws IOException
    {
        DataBlock< ? > block = null;

        long[] dataBlockIndices = toZarrChunkIndices( gridPosition );

        try
        {
            block = n5.readBlock( pathName, attributes, dataBlockIndices );
        }
        catch ( SdkClientException e )
        {
            System.err.println( e.getMessage() ); // this happens sometimes, not sure yet why...
        }

        if ( block == null )
        {
            return ( A ) zarrArrayCreator.createEmptyArray( gridPosition );
        }
        else
        {
            return zarrArrayCreator.createArray( block, gridPosition );
        }
    }

    private long[] toZarrChunkIndices( long[] gridPosition )
    {

        long[] chunkInZarr = new long[ zarrAxes.getNumDimension() ];

        // fill in the spatial dimensions
        final Map< Integer, Integer > spatialToZarr = zarrAxes.spatialToZarr();
        for ( Map.Entry< Integer, Integer > entry : spatialToZarr.entrySet() )
            chunkInZarr[ entry.getValue() ] = gridPosition[ entry.getKey() ];

        if ( zarrAxes.hasChannels() )
            chunkInZarr[ zarrAxes.channelIndex() ] = channel;

        if ( zarrAxes.hasTimepoints() )
            chunkInZarr[ zarrAxes.timeIndex() ] = timepoint;

        return chunkInZarr;
    }
}
