import static org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales.MULTI_SCALE_KEY;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.zarr.ZarrKeyValueReader;
import org.mastodon.mamut.io.loader.util.mobie.OmeZarrMultiscales;
import org.janelia.saalfeldlab.n5.universe.N5Factory;

public class N5Zarr
{
    public static void main( final String[] args ) throws InterruptedException, ExecutionException
    {
        String n5Url = "https://uk1s3.embassy.ebi.ac.uk/idr/zarr/v0.4/idr0062A/6001240.zarr";//"https://ssbd.riken.jp/100118-dcacbb41/v0.4/test2-N2-030303-01.ome.zarr";
        final String n5Dataset = "/";
        final N5Reader n5 = new N5Factory()
                .cacheAttributes( true )
                .hdf5DefaultBlockSize( 64 )
                .zarrDimensionSeparator( "/" )
                .zarrMapN5Attributes( true )
                .openReader( n5Url );
        ( ( ZarrKeyValueReader ) n5 ).getAttributesFromContainer( n5Dataset, ".zgroup" );
        OmeZarrMultiscales[] multiscales = n5.getAttribute( n5Dataset, MULTI_SCALE_KEY, OmeZarrMultiscales[].class );
        System.out.println( Arrays.toString( multiscales ) );
    }
}
