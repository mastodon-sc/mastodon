package org.mastodon.elliter;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOverlay;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import net.imglib2.Cursor;
import net.imglib2.RealLocalizable;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.LinAlgHelpers;
import org.mastodon.elliter.EllpsoidIteratorExample.ScreenVertexMath.Ellipse;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;

public class EllpsoidIteratorMinimalExample
{
	public static void main( String[] args )
	{
		// Create ArrayImg to act as source pixel data.
		// We show it as a Source in BDV with a calibration of (X=1, Y=1, Z=2) as a minimal test whether the EllipsoidIterator handles source transforms correctly.
		// The Img is filled with a checkerboard pattern, so that we can see where the voxel raster is in BDV.
		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50 );
		final Cursor< UnsignedByteType > cursor = img.cursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			final int s = cursor.getIntPosition( 0 ) + cursor.getIntPosition( 1 ) + cursor.getIntPosition( 2 );
			cursor.get().set( s % 2 == 0 ? 32 : 64 );
		}
		final BdvStackSource< UnsignedByteType > bdv = BdvFunctions.show( img, "img", Bdv.options().sourceTransform( 1, 1, 2 ) );

		// Now create a model graph with a few test spots for EllipsoidIterator
		final ModelGraph graph = new ModelGraph();
		graph.addVertex().init( 0,
				new double[] { 50, 50, 50 },
				new double[][] {
						{ 210, 100, 0 },
						{ 100, 110, 10 },
						{ 0, 10, 100 }
				} );
		graph.addVertex().init( 0,
				new double[] { 20, 80, 40 },
				new double[][] {
						{ 90, 0, 0 },
						{ 0, 90,  0 },
						{ 0, 0, 500 }
				} );
		graph.addVertex().init( 0,
				new double[] { 40, 10, 40 },
				new double[][] {
						{ 90, -80, 0 },
						{ -80, 90, 0 },
						{ 0, 0, 90 }
				} );

		// We now create an EllipsoidIterable and re-use it for each spot.
		// For each spot, we iterate inside pixels and set them to 255.
		final EllipsoidIterable< UnsignedByteType > ellipsoidIter = new EllipsoidIterable<>( bdv.getSources().get( 0 ).getSpimSource() );
		graph.vertices().forEach( spot ->
		{
			ellipsoidIter.reset( spot );
			ellipsoidIter.forEach( t -> t.set( 255 ) );
		} );
	}
}
