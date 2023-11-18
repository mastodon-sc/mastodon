package org.mastodon.mamut.io.loader;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.N5Exception;
import org.janelia.saalfeldlab.n5.N5Exception.N5IOException;
import org.janelia.saalfeldlab.n5.N5KeyValueReader;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.hdf5.N5HDF5Reader;
import org.janelia.saalfeldlab.n5.zarr.ZarrKeyValueReader;

import bdv.img.n5.BdvN5Format;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import hdf.hdf5lib.exceptions.HDF5Exception;

public class BdvN5UniverseFormat
{
    private final N5Reader n5;

    private final String dataset;

    public BdvN5UniverseFormat( final N5Reader n5, final String dataset )
    {
        this.n5 = n5;
        this.dataset = dataset;
    }

    public DataType getSetupDataType( final int setupId ) throws IOException
    {
        DataType dataType = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
            }
            catch ( final N5Exception e )
            {
                dataType = null;
            }
            return dataType == null ? DataType.UINT16 : dataType;
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            // TODO: implement here
            new UnsupportedOperationException( "ZarrKeyValueReader is not supported yet." );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                dataType = n5.getAttribute( pathName, BdvN5Format.DATA_TYPE_KEY, DataType.class );
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        if ( dataType == null )
        {
            new RuntimeException( "DataType is not available." );
        }
        return dataType;
    }

    public double[][] getMipmapResolutions( final int setupId ) throws IOException
    {
        double[][] mipmapResolutions = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            final String pathName = getFullPathName( getPathName( setupId ) ) + "/resolutions";
            IHDF5Reader reader = openHdf5Reader( ( ( N5HDF5Reader ) n5 ).getFilename().toString() );
            mipmapResolutions = reader.readDoubleMatrix( pathName );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            new UnsupportedOperationException( "Unimplemented method 'getPathName'" );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            try
            {
                final String pathName = getFullPathName( getPathName( setupId ) );
                mipmapResolutions = n5.getAttribute( pathName, BdvN5Format.DOWNSAMPLING_FACTORS_KEY, double[][].class );
            }
            catch ( final N5Exception e )
            {
                throw new IOException( e );
            }
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        if ( mipmapResolutions == null )
        {
            new RuntimeException( "DataType is not available." );
        }
        return mipmapResolutions;
    }

    public String getPathName( final int setupId )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "s%02d", setupId );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            pathName = String.format( "%d", setupId );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d", setupId );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;
    }

    public String getPathName( final int setupId, final int timepointId )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "t%05d/s%02d", timepointId, setupId );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            pathName = String.format( "%d/%d", setupId, timepointId );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d/timepoint%d", setupId, timepointId );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;
    }

    public String getPathName( final int setupId, final int timepointId, final int level )
    {
        String pathName = null;
        if ( n5 instanceof N5HDF5Reader )
        {
            pathName = String.format( "t%05d/s%02d/%d/cells", timepointId, setupId, level );
        }
        else if ( n5 instanceof ZarrKeyValueReader )
        {
            new UnsupportedOperationException( "Unimplemented method 'getPathName'" );
        }
        else if ( n5 instanceof N5KeyValueReader )
        {
            pathName = String.format( "setup%d/timepoint%d/s%d", setupId, timepointId, level );
        }
        else
        {
            new UnsupportedOperationException( "Unsupported format'" );
        }
        return pathName;

    }

    private String getFullPathName( final String pathName )
    {
        return dataset + pathName;
    }

    private static IHDF5Reader openHdf5Reader( String hdf5Path )
    {
        try
        {
            return HDF5Factory.openForReading( hdf5Path );
        }
        catch ( HDF5Exception e )
        {
            throw new N5IOException( "Cannot open HDF5 Reader", new IOException( e ) );
        }
    }

}
