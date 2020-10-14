package org.mastodon.views.bvv.scene;

public class Ellipsoids extends CompactingPool< Ellipsoid >
{
	final EllipsoidShapePool shapes;

	final ColorPool colors;

	public Ellipsoids()
	{
		this( 100 );
	}

	public Ellipsoids( final int initialCapacity )
	{
		this( new EllipsoidShapePool( initialCapacity ), new ColorPool( initialCapacity ), initialCapacity );
	}

	private Ellipsoids( final EllipsoidShapePool shapes, final ColorPool colors, final int initialCapacity )
	{
		super( new EllipsoidPool( shapes, colors ), initialCapacity );
		this.shapes = shapes;
		this.colors = colors;
	}
}
