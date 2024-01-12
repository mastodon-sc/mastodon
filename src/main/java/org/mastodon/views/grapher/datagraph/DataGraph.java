/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.grapher.datagraph;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.RefPool;
import org.mastodon.adapter.RefBimap;
import org.mastodon.app.ViewGraph;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.ref.IntRefArrayMap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractEdgePool.AbstractEdgeLayout;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.graph.ref.AbstractVertexPool.AbstractVertexLayout;
import org.mastodon.graph.ref.GraphImp;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.listeners.Listeners;

public class DataGraph<
		V extends Vertex< E > & HasTimepoint & HasLabel,
		E extends Edge< V > >
		extends GraphImp<
				DataGraph.DataVertexPool,
				DataGraph.DataEdgePool,
				DataVertex, DataEdge, ByteMappedElement >
		implements GraphListener< V, E >, GraphChangeListener, ViewGraph< V, E, DataVertex, DataEdge >
{

	final ListenableReadOnlyGraph< V, E > modelGraph;

	private final ReentrantReadWriteLock lock;

	final GraphIdBimap< V, E > idmap;

	final IntRefMap< DataVertex > idToDataVertex;

	private final IntRefMap< DataEdge > idToDataEdge;

	private V mv;

	final DataVertex tsv;

	private final DataVertex tsv2;

	private final DataEdge tse;

	private final Listeners.List< GraphChangeListener > listeners;

	private final RefBimap< V, DataVertex > vertexMap;

	private final RefBimap< E, DataEdge > edgeMap;

	/**
	 * Creates a new DataGraph with a default initial capacity.
	 *
	 * @param modelGraph
	 *            the model graph to wrap.
	 * @param idmap
	 *            the bidirectional id map of the model graph.
	 */
	public DataGraph(
			final ListenableReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap )
	{
		this( modelGraph, idmap, new ReentrantReadWriteLock() );
	}

	/**
	 * Creates a new DataGraph with a default initial capacity.
	 *
	 * @param modelGraph
	 *            the model graph to wrap.
	 * @param idmap
	 *            the bidirectional id map of the model graph.
	 * @param lock
	 *            read/write locks for the model graph
	 */
	public DataGraph(
			final ListenableReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ReentrantReadWriteLock lock )
	{
		this( modelGraph, idmap, lock, 10000 );
	}

	/**
	 * Creates a new {@link DataGraph} that reproduces the current model graph
	 * structure. It registers as a {@link GraphListener} to keep in sync with
	 * changes the model graph.
	 *
	 * @param modelGraph
	 *            the model graph to wrap.
	 * @param idmap
	 *            the bidirectional id map of the model graph.
	 * @param lock
	 *            read/write locks for the model graph
	 * @param initialCapacity
	 *            the initial capacity for the graph storage.
	 */
	public DataGraph(
			final ListenableReadOnlyGraph< V, E > modelGraph,
			final GraphIdBimap< V, E > idmap,
			final ReentrantReadWriteLock lock,
			final int initialCapacity )
	{
		super( new DataEdgePool(
				initialCapacity,
				new DataVertexPool( initialCapacity,
						new ModelGraphWrapper<>( idmap ) ) ) );
		this.modelGraph = modelGraph;
		this.lock = lock;
		this.idmap = idmap;
		idToDataVertex = new IntRefArrayMap<>( vertexPool );
		idToDataEdge = new IntRefArrayMap<>( edgePool );
		mv = modelGraph.vertexRef();
		tsv = vertexRef();
		tsv2 = vertexRef();
		tse = edgeRef();
		listeners = new Listeners.SynchronizedList<>();
		vertexMap = new DataVertexBimap<>( this );
		edgeMap = new DataEdgeBimap<>( this );

		modelGraph.addGraphListener( this );
		modelGraph.addGraphChangeListener( this );
		lock.writeLock().lock();
		try
		{
			graphRebuilt();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Exposes the {@link RefPool} for the Data vertices of this DataGraph.
	 *
	 * @return the vertex pool.
	 */
	public RefPool< DataVertex > getVertexPool()
	{
		return vertexPool;
	}

	/**
	 * Exposes the {@link RefPool} for the Data edges of this DataGraph.
	 *
	 * @return the edge pool.
	 */
	public RefPool< DataEdge > getEdgePool()
	{
		return edgePool;
	}

	/**
	 * @return the bidirectional id map of the model graph.
	 */
	public GraphIdBimap< V, E > getGraphIdBimap()
	{
		return idmap;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "DataGraph {\n" );
		sb.append( "  vertices = {\n" );
		for ( final DataVertex vertex : vertices() )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  },\n" );
		sb.append( "  edges = {\n" );
		for ( final DataEdge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "},\n" );
		sb.append( "}" );
		return sb.toString();
	}

	/**
	 * Returns the vertex in this DataGraph that corresponds to the model vertex
	 * with the specified id.
	 *
	 * @param modelId
	 *            the id of the vertex in the model graph.
	 * @param ref
	 *            a DataVertex reference.
	 * @return the DataVertex corresponding to the model vertex with the
	 *         specified id.
	 */
	DataVertex getDataVertexForModelId( final int modelId, final DataVertex ref )
	{
		return idToDataVertex.get( modelId, ref );
	}

	DataEdge getDataEdgeForModelId( final int modelId, final DataEdge ref )
	{
		return idToDataEdge.get( modelId, ref );
	}

	/**
	 * Get the list of GraphChangeListeners. This can be used to add (or remove)
	 * a GraphChangeListener that will be notified when this DataGraph changes.
	 *
	 * @return list of GraphChangeListeners
	 */
	public Listeners< GraphChangeListener > graphChangeListeners()
	{
		return listeners;
	}

	public ReentrantReadWriteLock getLock()
	{
		return lock;
	}

	@Override
	public DataVertex addVertex()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DataVertex addVertex( final DataVertex ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DataEdge addEdge( final DataVertex source, final DataVertex target )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DataEdge addEdge( final DataVertex source, final DataVertex target, final DataEdge ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DataEdge insertEdge( final DataVertex source, final int sourceOutIndex, final DataVertex target,
			final int targetInIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DataEdge insertEdge( final DataVertex source, final int sourceOutIndex, final DataVertex target,
			final int targetInIndex, final DataEdge ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove( final DataVertex vertex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove( final DataEdge edge )
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * GraphChangeListener
	 */

	@Override
	public void graphChanged()
	{
		for ( final GraphChangeListener l : listeners.list )
			l.graphChanged();
	}

	/*
	 * GraphListener
	 */

	@Override
	public void graphRebuilt()
	{
		idToDataVertex.clear();
		idToDataEdge.clear();

		for ( final V v : modelGraph.vertices() )
		{
			final int id = idmap.getVertexId( v );
			super.addVertex( tsv ).initModelId( id, v.getTimepoint() );
			idToDataVertex.put( id, tsv );
		}
		for ( final E e : modelGraph.edges() )
		{
			final int id = idmap.getEdgeId( e );
			idToDataVertex.get( idmap.getVertexId( e.getSource( mv ) ), tsv );
			idToDataVertex.get( idmap.getVertexId( e.getTarget( mv ) ), tsv2 );
			super.insertEdge( tsv, e.getSourceOutIndex(), tsv2, e.getTargetInIndex(), tse ).initModelId( id );
			idToDataEdge.put( id, tse );
		}
	}

	@Override
	public void vertexAdded( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		super.addVertex( tsv ).initModelId( id, vertex.getTimepoint() );
		idToDataVertex.put( id, tsv );
	}

	@Override
	public void vertexRemoved( final V vertex )
	{
		final int id = idmap.getVertexId( vertex );
		if ( idToDataVertex.remove( id, tsv ) != null )
			super.remove( tsv );
	}

	@Override
	public void edgeAdded( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		idToDataVertex.get( idmap.getVertexId( edge.getSource( mv ) ), tsv );
		idToDataVertex.get( idmap.getVertexId( edge.getTarget( mv ) ), tsv2 );
		super.insertEdge( tsv, edge.getSourceOutIndex(), tsv2, edge.getTargetInIndex(), tse ).initModelId( id );
		idToDataEdge.put( id, tse );
	}

	@Override
	public void edgeRemoved( final E edge )
	{
		final int id = idmap.getEdgeId( edge );
		if ( idToDataEdge.remove( id, tse ) != null )
			super.remove( tse );
	}

	/**
	 * Get bidirectional mapping between model vertices and view vertices.
	 *
	 * @return bidirectional mapping between model vertices and view vertices.
	 */
	@Override
	public RefBimap< V, DataVertex > getVertexMap()
	{
		return vertexMap;
	}

	/**
	 * Get bidirectional mapping between model edges and view edges.
	 *
	 * @return bidirectional mapping between model edges and view edges.
	 */
	@Override
	public RefBimap< E, DataEdge > getEdgeMap()
	{
		return edgeMap;
	}

	/*
	 * vertex and edge pools
	 */

	static class DataVertexLayout extends AbstractVertexLayout
	{
		final IndexField origVertexIndex = indexField();

		final IntField modelTimepoint = intField();

		final IndexField layoutInEdgeIndex = indexField();

		final DoubleField layoutX = doubleField();

		final DoubleField layoutY = doubleField();

		final IndexField screenVertexIndex = indexField();
	}

	static DataVertexLayout vertexLayout = new DataVertexLayout();

	static class DataVertexPool extends AbstractVertexPool< DataVertex, DataEdge, ByteMappedElement >
	{
		final ModelGraphWrapper< ?, ? > modelGraphWrapper;

		final IndexAttribute< DataVertex > origVertexIndex = new IndexAttribute<>( vertexLayout.origVertexIndex, this );

		final IntAttribute< DataVertex > modelTimepoint = new IntAttribute<>( vertexLayout.modelTimepoint, this );

		final IndexAttribute< DataVertex > layoutInEdgeIndex =
				new IndexAttribute<>( vertexLayout.layoutInEdgeIndex, this );

		final DoubleAttribute< DataVertex > layoutX = new DoubleAttribute<>( vertexLayout.layoutX, this );

		final DoubleAttribute< DataVertex > layoutY = new DoubleAttribute<>( vertexLayout.layoutY, this );

		final IndexAttribute< DataVertex > screenVertexIndex =
				new IndexAttribute<>( vertexLayout.screenVertexIndex, this );

		private DataVertexPool( final int initialCapacity, final ModelGraphWrapper< ?, ? > modelGraphWrapper )
		{
			super( initialCapacity, vertexLayout, DataVertex.class,
					SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
			this.modelGraphWrapper = modelGraphWrapper;
		}

		@Override
		protected DataVertex createEmptyRef()
		{
			return new DataVertex( this );
		}
	}

	static class DataEdgeLayout extends AbstractEdgeLayout
	{
		final IndexField origEdgeIndex = indexField();

		final IndexField screenEdgeIndex = indexField();
	}

	static DataEdgeLayout edgeLayout = new DataEdgeLayout();

	static class DataEdgePool extends AbstractEdgePool< DataEdge, DataVertex, ByteMappedElement >
	{
		final ModelGraphWrapper< ?, ? > modelGraphWrapper;

		final IndexAttribute< DataEdge > origEdgeIndex = new IndexAttribute<>( edgeLayout.origEdgeIndex, this );

		final IndexAttribute< DataEdge > screenEdgeIndex = new IndexAttribute<>( edgeLayout.screenEdgeIndex, this );

		private DataEdgePool( final int initialCapacity, final DataVertexPool vertexPool )
		{
			super( initialCapacity, edgeLayout, DataEdge.class,
					SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
			modelGraphWrapper = vertexPool.modelGraphWrapper;
			vertexPool.linkEdgePool( this );
		}

		@Override
		protected DataEdge createEmptyRef()
		{
			return new DataEdge( this );
		}
	}
}
