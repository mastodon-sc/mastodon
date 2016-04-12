package net.trackmate.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import gnu.trove.map.hash.TIntIntHashMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

// TODO: add support for edge attribute serialization
// TODO: serialization needs to reflect respect edge ordering
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
	}

	public static < V extends Vertex< E >, E extends Edge< V > >
			void write(
					final ReadOnlyGraph< V, E > graph,
					final GraphIdBimap< V, E > idmap,
					final Serializer< V, E > io,
					final ObjectOutputStream oos )
			throws IOException
	{
		final int numVertices = graph.vertices().size();
		oos.writeInt( numVertices );

		final Iterator< V > vertexIterator = graph.vertices().iterator();
		final byte[] bytes = new byte[ io.getVertexNumBytes() ];
		final TIntIntHashMap idToFileIndex = new TIntIntHashMap( 2 * numVertices, 0.75f, -1, -1 );
		int i = 0;
		while( vertexIterator.hasNext() )
		{
			final V v = vertexIterator.next();
			io.getBytes( v, bytes );
			oos.write( bytes );

			final int id = idmap.getVertexId( v );
			idToFileIndex.put( id, i );
			++i;
		}

		final int numEdges = graph.edges().size();
		oos.writeInt( numEdges );

		final Iterator< E > edgeIterator = graph.edges().iterator();
		final V v = graph.vertexRef();
		while( edgeIterator.hasNext() )
		{
			final E e = edgeIterator.next();
			final int from = idToFileIndex.get( idmap.getVertexId( e.getSource( v ) ) );
			final int to = idToFileIndex.get( idmap.getVertexId( e.getTarget( v ) ) );
			oos.writeInt( from );
			oos.writeInt( to );
		}

		graph.releaseRef( v );
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

		final byte[] bytes = new byte[ io.getVertexNumBytes() ];
		final TIntIntHashMap fileIndexToId = new TIntIntHashMap( 2 * numVertices, 0.75f, -1, -1 );
		for ( int i = 0; i < numVertices; ++i )
		{
			ois.readFully( bytes );
			graph.addVertex( v1 );
			io.setBytes( v1, bytes );
			fileIndexToId.put( i, idmap.getVertexId( v1 ) );
		}

		final int numEdges = ois.readInt();
		for ( int i = 0; i < numEdges; ++i )
		{
			final int from = fileIndexToId.get( ois.readInt() );
			final int to = fileIndexToId.get( ois.readInt() );
			idmap.getVertex( from, v1 );
			idmap.getVertex( to, v2 );
			graph.addEdge( v1, v2, e );
		}

		graph.releaseRef( v1 );
		graph.releaseRef( v2 );
		graph.releaseRef( e );
	}
}
