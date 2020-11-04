package org.mastodon.views.bvv.scene;

/**
 * Helper class to create vertices and triangles for a tesselated unit cylinder.
 */
class UnitCylinder
{
	private final float[] vertices;

	private final int[] indices;

	private final int numElements;

	private final int subdivisions;

	public int getNumElements()
	{
		return numElements;
	}

	public float[] getVertices()
	{
		return vertices;
	}

	public int[] getIndices()
	{
		return indices;
	}

	/*
	 * There are {@code subdivision} points on the circles on top and bottom of the cylinder respectively.
	 * These are arranged in counterclockwise order when looking down the z axis onto the xy plane.
	 *
	 * The points have coordinates
	 * p_i^j = [x=cos(i * 2*pi/subdivisions), y=sin(i * 2*pi/subdivisions), z=j] for j in {0,1}.
	 *
	 * In the vertex array, they are ordered as (p_0^0, p_0^1, p_1^0, p_1^1, ...).
	 * So, p_i^j has index 2*i+j.
	 *
	 * Triangles in counterclockwise order are
	 * 		[p_0^0, p_1^0, p_0^1]
	 * 		[p_1^0, p_1^1, p_0^1]
	 *
	 * p_0^1 -- p_1^1
	 *   | \      |
	 *   |  \     |
	 *   |   \    |
	 *   |    \   |
	 *   |     \  |
	 *   |      \ |
	 * p_0^0 -- p_1^0
	 */
	public UnitCylinder( final int subdivisions )
	{
		this.subdivisions = subdivisions;
		final int numVertices = 2 * subdivisions;
		final int numTriangles = 2 * subdivisions;

		// create vertices
		vertices = new float[ numVertices * 3 ];
		int k = 0;
		for ( int i = 0; i < subdivisions; ++i )
			for ( int j = 0; j < 2; ++j )
			{
				vertices[ k++ ] = ( float ) Math.cos( i * 2 * Math.PI / subdivisions );
				vertices[ k++ ] = ( float ) Math.sin( i * 2 * Math.PI / subdivisions );
				vertices[ k++ ] = j;
			}

		// create triangles
		numElements = numTriangles * 3;
		indices = new int[ numElements ];
		k = 0;
		for ( int i = 0; i < subdivisions; ++i )
		{
			indices[ k++ ] = p( i, 0 );
			indices[ k++ ] = p( i + 1, 0 );
			indices[ k++ ] = p( i, 1 );

			indices[ k++ ] = p( i + 1, 0 );
			indices[ k++ ] = p( i + 1, 1 );
			indices[ k++ ] = p( i, 1 );
		}
	}

	private int p( final int i, final int j )
	{
		return 2 * ( i % subdivisions ) + j;
	}
}
