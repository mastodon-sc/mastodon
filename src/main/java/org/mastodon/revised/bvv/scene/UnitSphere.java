package org.mastodon.revised.bvv.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class UnitSphere
{
	private final int numElements;

	private final float[] vertices;

	private final int[] indices;

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

	public UnitSphere( final int subdivisions )
	{
		// Six equidistant points lying on the unit sphere
		final MyVertex XPLUS = vertex(  1,  0,  0 );	 //  X
		final MyVertex XMIN  = vertex( -1,  0,  0 );	 // -X
		final MyVertex YPLUS = vertex(  0,  1,  0 );	 //  Y
		final MyVertex YMIN  = vertex(  0, -1,  0 );	 // -Y
		final MyVertex ZPLUS = vertex(  0,  0,  1 );	 //  Z
		final MyVertex ZMIN  = vertex(  0,  0, -1 );	 // -Z

		// Vertices of a unit octahedron
		final ArrayList< MyTriangle > triangles = new ArrayList<>();
		triangles.addAll( Arrays.asList(
				new MyTriangle( XPLUS, YPLUS, ZPLUS ),
				new MyTriangle( YPLUS, XMIN , ZPLUS ),
				new MyTriangle( XMIN , YMIN , ZPLUS ),
				new MyTriangle( YMIN , XPLUS, ZPLUS ),
				new MyTriangle( XPLUS, ZMIN , YPLUS ),
				new MyTriangle( YPLUS, ZMIN , XMIN  ),
				new MyTriangle( XMIN , ZMIN , YMIN  ),
				new MyTriangle( YMIN , ZMIN , XPLUS ) ) );

		/* Subdivide each starting triangle (maxlevels - 1) times */
		for ( int level = 0; level < subdivisions; level++ )
		{
			/* Subdivide each polygon in the old approximation and normalize
			 *  the new points thus generated to lie on the surface of the unit
			 *  sphere.
			 * Each input triangle with vertices labelled [0,1,2] as shown
			 *  below will be turned into four new triangles:
			 *
			 *                      Make new points
			 *                          a = (0+1)/2
			 *                          b = (1+2)/2
			 *                          c = (2+0)/2
			 *        2
			 *       /\             Normalize a, b, c
			 *      /  \
			 *    c/____\ b         Construct new triangles
			 *    /\    /\              [0,a,c]
			 *   /  \  /  \             [a,1,b]
			 *  /____\/____\            [c,b,2]
			 * 0      a     1           [a,b,c]
			 */
			final ArrayList< MyTriangle > previous = new ArrayList<>( triangles );
			triangles.clear();
			for ( MyTriangle previoust : previous )
			{
				final MyVertex v0 = previoust.v0;
				final MyVertex v1 = previoust.v1;
				final MyVertex v2 = previoust.v2;

				MyVertex a = normalizedMidpoint( v0, v1 );
				MyVertex b = normalizedMidpoint( v1, v2 );
				MyVertex c = normalizedMidpoint( v2, v0 );

				triangles.add( new MyTriangle( v0, a, c ) );
				triangles.add( new MyTriangle( a, v1, b ) );
				triangles.add( new MyTriangle( c, b, v2 ) );
				triangles.add( new MyTriangle( a, b, c ) );
			}
		}

		vertices = new float[ myvertices.size() * 3 ];
		int i = 0;
		for ( MyVertex vertex : myvertices )
		{
			vertices[ i++ ] = vertex.x;
			vertices[ i++ ] = vertex.y;
			vertices[ i++ ] = vertex.z;
		}

		numElements = triangles.size() * 3;
		indices = new int[ numElements ];
		i = 0;
		for ( MyTriangle triangle : triangles )
		{
			indices[ i++ ] = triangle.v0.id;
			indices[ i++ ] = triangle.v1.id;
			indices[ i++ ] = triangle.v2.id;
		}

	}

	private static class MyVertex
	{
		private final float x;
		private final float y;
		private final float z;
		private final int id;
		public MyVertex( final float x, final float y, final float z, final int id )
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.id = id;
		}
	}

	private static class MyTriangle
	{
		private final MyVertex v0;
		private final MyVertex v1;
		private final MyVertex v2;
		public MyTriangle( final MyVertex v0, final MyVertex v1, final MyVertex v2 )
		{
			this.v0 = v0;
			this.v1 = v1;
			this.v2 = v2;
		}
	}

	private static class Edge
	{
		private final MyVertex v0;
		private final MyVertex v1;

		public Edge( final MyVertex v0, final MyVertex v1 )
		{
			this.v0 = v0;
			this.v1 = v1;
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( !( o instanceof Edge ) )
				return false;
			final Edge edge = ( Edge ) o;
			if ( v0.id == edge.v0.id && v1.id == edge.v1.id )
				return true;
			if ( v1.id == edge.v0.id && v0.id == edge.v1.id )
				return true;
			return false;
		}

		@Override
		public int hashCode()
		{
			return Math.min( v0.id, v1.id ) * 31 + Math.max( v0.id, v1.id );
		}
	}

	private int id = 0;
	private final HashMap< Edge, MyVertex > midpoints = new HashMap<>();
	private final ArrayList< MyVertex > myvertices = new ArrayList<>();

	private MyVertex normalizedMidpoint( MyVertex a, MyVertex b )
	{
		return midpoints.computeIfAbsent( new Edge( a, b ), edge -> {
			double x = a.x + b.x;
			double y = a.y + b.y;
			double z = a.z + b.z;
			double s = 1.0 / Math.sqrt( x * x + y * y + z * z );
			return vertex( ( float ) ( x * s ), ( float ) ( y * s ), ( float ) ( z * s ) );
		} );
	}

	private MyVertex vertex( final float x, final float y, final float z )
	{
		final MyVertex v = new MyVertex( x, y, z, id++ );
		myvertices.add( v );
		return v;
	}
}
