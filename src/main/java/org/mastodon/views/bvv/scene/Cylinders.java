package org.mastodon.views.bvv.scene;

public class Cylinders extends CompactingPool< Cylinder >
{
	final ShapeTransformPool shapes;

	final ColorPool colors;

	public Cylinders()
	{
		this( 100 );
	}

	public Cylinders( final int initialCapacity )
	{
		this( new ShapeTransformPool( initialCapacity ), new ColorPool( initialCapacity ), initialCapacity );
	}

	private Cylinders( final ShapeTransformPool shapes, final ColorPool colors, final int initialCapacity )
	{
		super( new CylinderPool( shapes, colors ), initialCapacity );
		this.shapes = shapes;
		this.colors = colors;
	}
}
