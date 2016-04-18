package net.trackmate.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

// TODO: add support for featuremaps

/**
 * Write/read a {@link Graph} to/from an ObjectStream. For each {@link Graph}
 * class, the {@link RawGraphIO.Serializer} interface needs to be implemented
 * that serializes vertex and edge attributes to/from a byte array.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RawGraphIO
{
	/**
	 * Provides serialization of vertices and edges to a byte array, for a specific {@link Graph} class.
	 *
	 * @param <V>
	 * @param <E>
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static interface Serializer< V extends Vertex< E >, E extends Edge< V > >
	{
		public int getVertexNumBytes();

		public void getBytes( final V vertex, final byte[] bytes );

		public void setBytes( final V vertex, final byte[] bytes );

		public int getEdgeNumBytes();

		public void getBytes( final E edge, final byte[] bytes );

		public void setBytes( final E edge, final byte[] bytes );
	}

	public static final class FileIdMap< V extends Vertex< E >, E extends Edge< V > >
	{
		private final TIntIntMap vertexIdToFileIndex;

		private final TIntIntMap edgeIdToFileIndex;

		private final GraphIdBimap< V, E > idmap;

		public FileIdMap(
				final TIntIntMap vertexIdToFileIndex,
				final TIntIntMap edgeIdToFileIndex,
				final GraphIdBimap< V, E > idmap )
		{
			this.vertexIdToFileIndex = vertexIdToFileIndex;
			this.edgeIdToFileIndex = edgeIdToFileIndex;
			this.idmap = idmap;
		}

		public int getVertexId( final V v )
		{
			return vertexIdToFileIndex.get( idmap.getVertexId( v ) );
		}

		public int getEdgeId( final E e )
		{
			return edgeIdToFileIndex.get( idmap.getEdgeId( e ) );
		}
	}

	public static < V extends Vertex< E >, E extends Edge< V > >
			FileIdMap< V, E > write(
					final ReadOnlyGraph< V, E > graph,
					final GraphIdBimap< V, E > idmap,
					final Serializer< V, E > io,
					final ObjectOutputStream oos )
			throws IOException
	{
		final int numVertices = graph.vertices().size();
		oos.writeInt( numVertices );

		final byte[] vbytes = new byte[ io.getVertexNumBytes() ];
		final boolean writeVertexBytes = io.getVertexNumBytes() > 0;
		final TIntIntHashMap vertexIdToFileIndex = new TIntIntHashMap( 2 * numVertices, 0.75f, -1, -1 );
		int i = 0;
		for ( final V v : graph.vertices() )
		{
			if ( writeVertexBytes )
			{
				io.getBytes( v, vbytes );
				oos.write( vbytes );
			}

			final int id = idmap.getVertexId( v );
			vertexIdToFileIndex.put( id, i );
			++i;
		}

		final int numEdges = graph.edges().size();
		oos.writeInt( numEdges );

		final byte[] ebytes = new byte[ io.getEdgeNumBytes() ];
		final boolean writeEdgeBytes = io.getEdgeNumBytes() > 0;
		final V v = graph.vertexRef();
		final TIntIntHashMap edgeIdToFileIndex = new TIntIntHashMap( 2 * numVertices, 0.75f, -1, -1 );
		i = 0;
		for( final E e : graph.edges() )
		{
			final int from = vertexIdToFileIndex.get( idmap.getVertexId( e.getSource( v ) ) );
			final int to = vertexIdToFileIndex.get( idmap.getVertexId( e.getTarget( v ) ) );
			final int sourceOutIndex = e.getSourceOutIndex();
			final int targetInIndex = e.getTargetInIndex();
			oos.writeInt( from );
			oos.writeInt( to );
			oos.writeInt( sourceOutIndex );
			oos.writeInt( targetInIndex );

			if ( writeEdgeBytes )
			{
				io.getBytes( e, ebytes );
				oos.write( ebytes );
			}

			final int id = idmap.getEdgeId( e );
			edgeIdToFileIndex.put( id, i );
			++i;
		}
		graph.releaseRef( v );

		return new FileIdMap<>( vertexIdToFileIndex, edgeIdToFileIndex, idmap );
	}

	public static < V extends Vertex< E >, E extends Edge< V > >
			void read(
					final Graph< V, E > graph,
					final GraphIdBimap< V, E > idmap,
					final Serializer< V, E > io,
					final ObjectInputStream ois )
			throws IOException
	{
		final int numVertices = ois.readInt();
		final V v1 = graph.vertexRef();
		final V v2 = graph.vertexRef();
		final E e = graph.edgeRef();

		final byte[] vbytes = new byte[ io.getVertexNumBytes() ];
		final boolean readVertexBytes = io.getVertexNumBytes() > 0;
		final TIntIntHashMap fileIndexToId = new TIntIntHashMap( 2 * numVertices, 0.75f, -1, -1 );
		for ( int i = 0; i < numVertices; ++i )
		{
			graph.addVertex( v1 );
			if ( readVertexBytes )
			{
				ois.readFully( vbytes );
				io.setBytes( v1, vbytes );
			}
			fileIndexToId.put( i, idmap.getVertexId( v1 ) );
		}

		final int numEdges = ois.readInt();
		final byte[] ebytes = new byte[ io.getEdgeNumBytes() ];
		final boolean readEdgeBytes = io.getEdgeNumBytes() > 0;
		for ( int i = 0; i < numEdges; ++i )
		{
			final int from = fileIndexToId.get( ois.readInt() );
			final int to = fileIndexToId.get( ois.readInt() );
			final int sourceOutIndex = ois.readInt();
			final int targetInIndex = ois.readInt();
			idmap.getVertex( from, v1 );
			idmap.getVertex( to, v2 );
			graph.insertEdge( v1, sourceOutIndex, v2, targetInIndex, e );
			if ( readEdgeBytes )
			{
				ois.readFully( ebytes );
				io.setBytes( e, ebytes );
			}
		}

		graph.releaseRef( v1 );
		graph.releaseRef( v2 );
		graph.releaseRef( e );
	}
}
