package net.trackmate;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;


public class SpotCollection implements Iterable< Spot >
{

	/**
	 * for each spot: x, y, z position
	 */
	private final Img< SpotType > spotData;

	public SpotCollection()
	{
		final ArrayImg< SpotType, ? > img = new ArrayImgFactory< SpotType >().create( new long[] { 10 }, new SpotType() );
		spotData = img;

		final ImgSpot spot = new ImgSpot( 3 );

		final Cursor< SpotType > c = img.cursor();
		double x = 0;
		double y = 0;
		double z = 0;
		while ( c.hasNext() )
		{
			c.fwd();
			spot.update( c );
			spot.setX( x );
			spot.setY( y );
			spot.setZ( z );
			x += 0.1;
			y += 0.2;
			z += 0.4;
		}
	}

	@Override
	public Iterator< Spot > iterator()
	{
		final Cursor< SpotType > c = spotData.cursor();
		final ImgSpot spot = new ImgSpot( 3 );
		return new Iterator< Spot >()
		{
			@Override
			public boolean hasNext()
			{
				return c.hasNext();
			}

			@Override
			public Spot next()
			{
				c.fwd();
				spot.update( c );
				return spot;
			}

			@Override
			public void remove()
			{}
		};
	}

	public static void main( final String[] args )
	{
		final SpotCollection spots = new SpotCollection();

		for ( final Spot spot : spots )
			System.out.println( String.format( "spot id = %d at ( %.1f %.1f %.1f )", spot.getId(), spot.getX(), spot.getY(), spot.getZ() ) );
	}

}
