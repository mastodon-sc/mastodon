package org.mastodon.revised.trackscheme;

import java.util.ArrayList;

import org.mastodon.RefPool;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.IntRefArrayMap;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.graph.ref.GraphImp;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.revised.trackscheme.wrap.ModelGraphProperties;
import org.mastodon.spatial.HasTimepoint;

/**
 * A specialized graph to be used in the TrackScheme application.
 * <p>
 * A {@link TrackSchemeGraph} wraps a model graph, where the graph is laid out
 * in a hierarchical-temporal way.
 * <p>
 * The {@link TrackSchemeGraph} duplicates the structure of the the model graph
 * using {@link TrackSchemeVertex} and {@link TrackSchemeEdge} objects. The
 * structure is kept in sync with the model graph by registering as a
 * {@link GraphListener}.
 * <p>
 * The vertices and edges of the {@link TrackSchemeGraph} expose properties
 * related to graph {@link LineageTreeLayout layout} (such as layout
 * coordinates) and painting (such as whether the vertex is selected). Some of
 * these (layout coordinates) are stored in the {@link TrackSchemeGraph}
 * entities, while others (label, selected state) are backed by the model
 * entities.
 * <p>
 * A mapping between vertices of the model graph and {@link TrackSchemeVertex
 * vertices} of the {@link TrackSchemeGraph} is established through unique IDs
 * assigned to model vertices. For this, we require a {@link GraphIdBimap
 * bidirectional map} from model graph entities to unique IDs. The
 * {@link TrackSchemeGraph} then maintains a map from unique model IDs to
 * {@link TrackSchemeVertex} allowing to go from a model vertex to the
 * corresponding {@link TrackSchemeVertex}. Vice versa, allowing to go from a
 * {@link TrackSchemeVertex} to the corresponding model vertex, each
 * {@link TrackSchemeVertex} stores the unique ID of its corresponding model
 * vertex.
 * <p>
 * Through the model-ID bimap, {@link TrackSchemeGraph} is decoupled from the
 * model graph implementation. The model graph might be stored as pool objects
 * or plain objects, or it might be a wrapper around a graph stored in a
 * database. The only requirements on the model graph is that its vertices
 * implement {@link HasTimepoint}. Other properties (such as vertex labels or
 * selection states) are accessed through {@link ModelGraphProperties} that know
 * how to retrieve/compute them for a given model vertex/edge ID. We provide a
 * default implementation of {@link DefaultModelGraphProperties} that should be
 * applicable for almost all model graphs.
 * <p>
 * {@link TrackSchemeGraph} registers as a {@link GraphChangeListener} with the
 * model graph and forwards {@link GraphChangeListener#graphChanged()
 * graphChanged} events such that interested clients can register with the
 * {@link TrackSchemeGraph} and do not have to know the model graph.
 *
 * @param <V>
 *            the type of the vertices of the wrapped model graph.
 * @param <E>
 *            the type of the edges of the wrapped model graph.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeGraph<
		V extends Vertex< E >,
		E extends Edge< V > >
	extends GraphImp<
				TrackSchemeGraph.TrackSchemeVertexPool,
				TrackSchemeGraph.TrackSchemeEdgePool,
				TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	implements GraphListener< V, E >, GraphChangeListener
{
	private final ListenableReadOnlyGraph< V, E > modelGraph;

	private final GraphIdBimap< V, E > idmap;

	private final IntRefMap< TrackSchemeVertex > idToTrackSchemeVertex;

	private final IntRefMap< TrackSchemeEdge > idToTrackSchemeEdge;

	private final RefSet< TrackSchemeVertex > roots;

	private V mv;

	private final TrackSchemeVertex tsv;

	private final TrackSchemeVertex tsv2;

	private final TrackSchemeEdge tse;

	private final ArrayList< GraphChangeListener > listeners;

	/**
	 * Creates a new TrackSchemeGraph with a default initial capacity.
	 *
	 * @param modelGraph
	 *            the model graph to wrap.
	 * @param idmap
	 *            the bidirectional id map of the model graph.
	 * @param modelGraphProperties
	 *            the properties of the model graph.
	 */
	public TrackSchemeGraph(
			final ListenableReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ModelGraphProperties< V, E > modelGraphProperties )
	{
		this( modelGraph, idmap, modelGraphProperties, 10000 );
	}

	/**
	 * Creates a new {@link TrackSchemeGraph} that reproduces the current model graph structure.
	 * It registers as a {@link GraphListener} to keep in sync with changes the model graph.
	 *
	 * @param modelGraph
	 *            the model graph to wrap.
	 * @param idmap
	 *            the bidirectional id map of the model graph.
	 * @param modelGraphProperties
	 *            an accessor for properties of the model graph.
	 * @param initialCapacity
	 *            the initial capacity for the graph storage.
	 */
	public TrackSchemeGraph(
			final ListenableReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ModelGraphProperties< V, E > modelGraphProperties,
			final int initialCapacity )
	{
		super( new TrackSchemeEdgePool(
				initialCapacity,
				new TrackSchemeVertexPool(
						new ModelGraphWrapper<>( idmap, modelGraphProperties ),
						initialCapacity ) ) );
		this.modelGraph = modelGraph;
		this.idmap = idmap;
		idToTrackSchemeVertex =	new IntRefArrayMap<>( vertexPool );
		idToTrackSchemeEdge = new IntRefArrayMap<>( edgePool );
		roots = new RefSetImp<>( vertexPool );
		mv = modelGraph.vertexRef();
		tsv = vertexRef();
		tsv2 = vertexRef();
		tse = edgeRef();
		listeners = new ArrayList<>();
		modelGraph.addGraphListener( this );
		modelGraph.addGraphChangeListener( this );
		graphRebuilt();
	}

	/**
	 * Exposes the {@link RefPool} for the TrackScheme vertices of this
	 * TrackSchemeGraph.
	 *
	 * @return the vertex pool.
	 */
	public RefPool< TrackSchemeVertex > getVertexPool()
	{
		return vertexPool;
	}

	/**
	 * Exposes the {@link RefPool} for the TrackScheme edges of this
	 * TrackSchemeGraph.
	 *
	 * @return the edge pool.
	 */
	public RefPool< TrackSchemeEdge > getEdgePool()
	{
		return edgePool;
	}

	/**
	 * Returns the roots of this graph.
	 * <p>
	 * Roots are defined as the vertices that have no incoming edges. Therefore
	 * a single connected-component of the graph may have several roots.
	 * <p>
	 * To be properly used in TrackScheme, it is best to adopt the convention
	 * where all edges are directed along time: They should depart from the
	 * earliest vertex in time, and point to the latest vertex in time. Then,
	 * the roots of the graph corresponds to vertices that appear as time
	 * increases.
	 *
	 * @return the roots of the graph, as a set of vertices.
	 */
	public RefSet< TrackSchemeVertex > getRoots()
	{
		return roots;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "TrackSchemeGraph {\n" );
		sb.append( "  vertices = {\n" );
		for ( final TrackSchemeVertex vertex : vertices() )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  },\n" );
		sb.append( "  edges = {\n" );
		for ( final TrackSchemeEdge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "},\n" );
		sb.append( "  roots = {\n" );
		for ( final TrackSchemeVertex vertex : roots )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  },\n" );
		sb.append( "}" );
		return sb.toString();
	}

	/**
	 * Returns the vertex in this TrackSchemeGraph that corresponds to the model
	 * vertex with the specified id.
	 *
	 * @param modelId
	 *            the id of the vertex in the model graph.
	 * @param ref
	 *            a TrackSchemeVertex reference.
	 * @return the TrackSchemeVertex corresponding to the model vertex with the
	 *         specified id.
	 */
	TrackSchemeVertex getTrackSchemeVertexForModelId( final int modelId, final TrackSchemeVertex ref )
	{
		return idToTrackSchemeVertex.get( modelId, ref );
	}

	TrackSchemeEdge getTrackSchemeEdgeForModelId( final int modelId, final TrackSchemeEdge ref )
	{
		return idToTrackSchemeEdge.get( modelId, ref );
	}

	/**
	 * Adds a GraphChangeListener that will be notified when this
	 * TrackSchemeGraph changes.
	 *
	 * @param listener
	 *            the {@link GraphChangeListener} to register.
	 * @return {@code true} if the listener was added to the list of
	 *         listeners. {@code false} if the listener was already
	 *         registered prior to this call.
	 */
	public synchronized boolean addGraphChangeListener( final GraphChangeListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified GraphChangeListener from the list of listeners to
	 * be notified when this TrackSchemeGraph changes.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the list of
	 *         listeners and was successfully removed from it.
	 */
	public synchronized boolean removeGraphChangeListener( final GraphChangeListener listener )
	{
		return listeners.remove( listener );
	}

	/*
	 * GraphChangeListener
	 */

	@Override
	public void graphChanged()
	{
		for ( final GraphChangeListener l : listeners )
			l.graphChanged();
	}

	/*
	 * GraphListener
	 */

	@Override
	public void graphRebuilt()
	{
		idToTrackSchemeVertex.clear();
		idToTrackSchemeEdge.clear();
		roots.clear();

		for ( final V v : modelGraph.vertices() )
		{
			final int id = idmap.getVertexId( v );
			addVertex( tsv ).init( id );
			idToTrackSchemeVertex.put( id, tsv );
			if ( v.incomingEdges().isEmpty() )
				roots.add( tsv );
		}
		for ( final E e : modelGraph.edges() )
		{
			final int id = idmap.getEdgeId( e );
			idToTrackSchemeVertex.get( idmap.getVertexId( e.getSource( mv ) ), tsv );
			idToTrackSchemeVertex.get( idmap.getVertexId( e.getTarget( mv ) ), tsv2 );
			insertEdge( tsv, e.getSourceOutIndex(), tsv2, e.getTargetInIndex(), tse ).init( id );
			idToTrackSchemeEdge.put( id, tse );
		}
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		addVertex( tsv ).init( id );
		idToTrackSchemeVertex.put( id, tsv );
		roots.add( tsv );
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		if ( idToTrackSchemeVertex.remove( id, tsv ) != null )
		{
			if ( tsv.incomingEdges().isEmpty() )
				roots.remove( tsv );
			this.remove( tsv );
		}
	}

	@Override
	public void edgeAdded( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		idToTrackSchemeVertex.get( idmap.getVertexId( edge.getSource( mv ) ), tsv );
		idToTrackSchemeVertex.get( idmap.getVertexId( edge.getTarget( mv ) ), tsv2 );
		if ( tsv2.incomingEdges().isEmpty() )
			roots.remove( tsv2 );
		insertEdge( tsv, edge.getSourceOutIndex(), tsv2, edge.getTargetInIndex(), tse ).init( id );
		idToTrackSchemeEdge.put( id, tse );
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		if ( idToTrackSchemeEdge.remove( id, tse ) != null )
		{
			if ( tse.getTarget( tsv ).incomingEdges().size() == 1 )
				roots.add( tsv );
			this.remove( tse );
		}
	}

//	@Override // TODO: should be implemented for some listener interface, or REMOVE? (vertices never change timepoint?)
	public void vertexTimepointChanged( final V vertex )
	{
		idToTrackSchemeVertex.get( idmap.getVertexId( vertex ), tsv ).updateTimepointFromModel();
	}

	/*
	 * vertex and edge pools
	 */

	static class TrackSchemeVertexPool extends AbstractVertexPool< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	{
		private final ModelGraphWrapper< ?, ? > modelGraphWrapper;

		private TrackSchemeVertexPool( final ModelGraphWrapper< ?, ? > modelGraphWrapper, final int initialCapacity )
		{
			this( initialCapacity, new VertexFactory( modelGraphWrapper ) );
		}

		private TrackSchemeVertexPool( final int initialCapacity, final VertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
			this.modelGraphWrapper = f.modelGraphWrapper;
		}

		private static class VertexFactory implements PoolObject.Factory< TrackSchemeVertex, ByteMappedElement >
		{
			private TrackSchemeVertexPool vertexPool;

			private final ModelGraphWrapper< ?, ? > modelGraphWrapper;

			private VertexFactory( final ModelGraphWrapper< ?, ? > modelGraphWrapper )
			{
				this.modelGraphWrapper = modelGraphWrapper;
			}

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeVertex.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeVertex createEmptyRef()
			{
				final TrackSchemeVertex vertex = new TrackSchemeVertex( vertexPool );
				vertex.modelVertex = modelGraphWrapper.createVertexWrapper( vertex );
				return vertex;
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}

			@Override
			public Class< TrackSchemeVertex > getRefClass()
			{
				return TrackSchemeVertex.class;
			}
		}
	}

	static class TrackSchemeEdgePool extends AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
	{
		private TrackSchemeEdgePool( final int initialCapacity, final TrackSchemeVertexPool vertexPool )
		{
			this( initialCapacity, new EdgeFactory( vertexPool.modelGraphWrapper ), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		private TrackSchemeEdgePool( final int initialCapacity, final EdgeFactory f, final TrackSchemeVertexPool vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.edgePool = this;
		}

		private static class EdgeFactory implements PoolObject.Factory< TrackSchemeEdge, ByteMappedElement >
		{
			private TrackSchemeEdgePool edgePool;

			private final ModelGraphWrapper< ?, ? > modelGraphWrapper;

			private EdgeFactory( final ModelGraphWrapper< ?, ? > modelGraphWrapper )
			{
				this.modelGraphWrapper = modelGraphWrapper;
			}

			@Override
			public int getSizeInBytes()
			{
				return TrackSchemeEdge.SIZE_IN_BYTES;
			}

			@Override
			public TrackSchemeEdge createEmptyRef()
			{
				final TrackSchemeEdge edge = new TrackSchemeEdge( edgePool );
				edge.modelEdge = modelGraphWrapper.createEdgeWrapper( edge );
				return edge;
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}

			@Override
			public Class< TrackSchemeEdge > getRefClass()
			{
				return TrackSchemeEdge.class;
			}
		};
	}
}
