import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.janelia.saalfeldlab.n5.zarr.ZarrCompressor;
import org.mastodon.mamut.io.loader.N5UniverseImgLoader;
import org.mastodon.mamut.io.loader.adapter.N5ReaderToViewerImgLoaderAdapter;
import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales;
import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscalesAdapter;
import org.mastodon.mamut.io.loader.util.mobie.ZarrAxes;
import org.mastodon.mamut.io.loader.util.mobie.ZarrAxesAdapter;

import com.google.gson.GsonBuilder;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class N5Tutorial
{
    public static void main( final String[] args ) throws InterruptedException, ExecutionException
    {
        final String n5Url = "http://localhost:9000/mastodon/mitosis_small.ome.zarr";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( ZarrCompressor.class, ZarrCompressor.jsonAdapter );
        gsonBuilder.registerTypeAdapter( ZarrAxes.class, new ZarrAxesAdapter() );
        gsonBuilder.registerTypeAdapter( OmeZarrMultiscales.class, new OmeZarrMultiscalesAdapter() );
        final N5Reader n5 = new N5Factory()
                .cacheAttributes( true )
                .hdf5DefaultBlockSize( 64 )
                .zarrDimensionSeparator( "/" )
                .zarrMapN5Attributes( true )
                .gsonBuilder( gsonBuilder )
                .openReader( n5Url );

        final String n5Dataset = "/";
        final N5ReaderToViewerImgLoaderAdapter< ? extends N5Reader > adapter = N5UniverseImgLoader.getAdapter( n5, n5Dataset );
        System.out.println( adapter.getNumSetups() );
        final String pathName = adapter.getFullPathName( adapter.getPathNameFromSetupTimepointLevel( 0, 0, 0 ) );
        final DatasetAttributes attributes = n5.getDatasetAttributes( pathName );

        final RandomAccessibleInterval< UnsignedShortType > rai = N5Utils.open( n5, pathName );

        String downloadsDir = System.getProperty( "user.home" ) + "/Downloads/";

        final ExecutorService exec = Executors.newFixedThreadPool( 10 );
        try
        {
            try ( final N5Writer n5Out = new N5Factory().openWriter( downloadsDir + "/test.n5" ) )
            {
                N5Utils.save( rai, n5Out, n5Dataset, attributes.getBlockSize(), attributes.getCompression(), exec );
            }

            /* save this dataset into a filesystem Zarr container */
            try ( final N5Writer zarrOut = new N5Factory().openWriter( downloadsDir + "/test.zarr" ) )
            {
                N5Utils.save( rai, zarrOut, n5Dataset, attributes.getBlockSize(), attributes.getCompression(), exec );
            }

            /* save this dataset into an HDF5 file, parallelization does not help here */
            try ( final N5Writer hdf5Out = new N5Factory().openWriter( downloadsDir + "/test.hdf5" ) )
            {
                N5Utils.save( rai, hdf5Out, n5Dataset.equals( "/" ) ? "/0" : n5Dataset, attributes.getBlockSize(),
                        attributes.getCompression() );
            }

        }
        finally
        {
            exec.shutdown();
        }

    }
}
