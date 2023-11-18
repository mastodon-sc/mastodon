import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5KeyValueWriter;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.universe.N5Factory;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class N5Tutorial
{
    public static void main( final String[] args ) throws InterruptedException, ExecutionException
    {
        final String n5Url = "https://janelia-cosem.s3.amazonaws.com/jrc_hela-2/jrc_hela-2.n5";
        final String n5Group = "/volumes/raw";
        final String n5Dataset = n5Group + "/s4";
        final N5Reader n5 = new N5Factory()
                .cacheAttributes( true )
                .hdf5DefaultBlockSize( 64 )
                .zarrDimensionSeparator( "/" )
                .zarrMapN5Attributes( true )
                .openReader( n5Url );

        final DatasetAttributes attributes = n5.getDatasetAttributes( n5Dataset );

        final RandomAccessibleInterval< UnsignedShortType > rai = N5Utils.open( n5, n5Dataset );

        String downloadsDir = System.getProperty( "user.home" ) + "/Downloads/";

        final ExecutorService exec = Executors.newFixedThreadPool( 10 );
        try
        {
            try ( final N5Writer n5Out = new N5Factory().openFSWriter( downloadsDir + "/test.n5" ) )
            {
                N5Utils.save( rai, n5Out, n5Dataset, attributes.getBlockSize(), attributes.getCompression(), exec );
            }

            /* save this dataset into a filesystem Zarr container */
            try ( final N5Writer zarrOut = new N5Factory().openZarrWriter( downloadsDir + "/test.zarr" ) )
            {
                N5Utils.save( rai, zarrOut, n5Dataset, attributes.getBlockSize(), attributes.getCompression(), exec );
            }

            /* save this dataset into an HDF5 file, parallelization does not help here */
            try ( final N5Writer hdf5Out = new N5Factory().openHDF5Writer( downloadsDir + "/test.hdf5" ) )
            {
                N5Utils.save( rai, hdf5Out, n5Dataset, attributes.getBlockSize(), attributes.getCompression() );
            }

        }
        finally
        {
            exec.shutdown();
        }

    }
}
