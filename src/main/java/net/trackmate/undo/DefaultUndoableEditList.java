package net.trackmate.undo;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.VertexWithFeatures;
import net.trackmate.revised.model.ModelGraph_HACK_FIX_ME;

// TODO: move to model.undo ?
public class DefaultUndoableEditList<
			V extends VertexWithFeatures< V, E >,
			E extends Edge< V > >
		extends UndoableEditList< V, E >
{
	protected final ModelGraph_HACK_FIX_ME< V, E > graph;

	protected final UndoSerializer< V, E > serializer;

	protected final UndoIdBimap< V > vertexUndoIdBimap;

	protected final UndoIdBimap< E > edgeUndoIdBimap;

	protected final UndoFeatureStore< V, E > featureStore;

	public DefaultUndoableEditList(
			final int initialCapacity,
			final ModelGraph_HACK_FIX_ME< V, E > graph,
			final GraphFeatures< V, E > graphFeatures,
			final UndoSerializer< V, E > serializer,
			final UndoIdBimap< V > vertexUndoIdBimap,
			final UndoIdBimap< E > edgeUndoIdBimap )
	{
		super( initialCapacity );
		this.graph = graph;
		this.serializer = serializer;
		this.vertexUndoIdBimap = vertexUndoIdBimap;
		this.edgeUndoIdBimap = edgeUndoIdBimap;
		featureStore = new UndoFeatureStore<>( graphFeatures );
	}

	public void recordAddVertex( final V vertex )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( addVertex ).init( vertex );
		releaseRef( ref );
	}

	public void recordRemoveVertex( final V vertex )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( removeVertex ).init( vertex );
		releaseRef( ref );
	}

	public void recordAddEdge( final E edge )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( addEdge ).init( edge );
		releaseRef( ref );
	}

	public void recordRemoveEdge( final E edge )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( removeEdge ).init( edge );
		releaseRef( ref );
	}

	protected final AddVertexType addVertex = new AddVertexType();

	protected final RemoveVertexType removeVertex = new RemoveVertexType();

	protected final AddEdgeType addEdge = new AddEdgeType();

	protected final RemoveEdgeType removeEdge = new RemoveEdgeType();

	protected class AddVertexType extends UndoableEditTypeImp< AddVertex >
	{
		@Override
		public AddVertex createInstance( final UndoableEditRef< V, E > ref )
		{
			return new AddVertex( ref, typeIndex() );
		}
	}

	private class AddVertex extends AbstractClearableUndoableEdit
	{
		private final AddRemoveVertexRecord addRemoveVertex;

		AddVertex( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
			this.addRemoveVertex = new AddRemoveVertexRecord();
		}

		public void init( final V vertex )
		{
			super.init();
			addRemoveVertex.record( vertex, ref );
		}

		@Override
		public void redo()
		{
			addRemoveVertex.doAddVertex( ref.getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveVertex.doRemoveVertex( ref.getDataIndex() );
		}
	}

	protected class RemoveVertexType extends UndoableEditTypeImp< RemoveVertex >
	{
		@Override
		public RemoveVertex createInstance( final UndoableEditRef< V, E > ref )
		{
			return new RemoveVertex( ref, typeIndex() );
		}
	}

	private class RemoveVertex extends AbstractClearableUndoableEdit
	{
		private final AddRemoveVertexRecord addRemoveVertex;

		RemoveVertex( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
			this.addRemoveVertex = new AddRemoveVertexRecord();
		}

		public void init( final V vertex )
		{
			super.init();
			addRemoveVertex.record( vertex, ref );
		}

		@Override
		public void redo()
		{
			addRemoveVertex.doRemoveVertex( ref.getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveVertex.doAddVertex( ref.getDataIndex() );
		}
	}

	private class AddRemoveVertexRecord
	{
		private final byte[] data;

		public AddRemoveVertexRecord()
		{
			data = new byte[ serializer.getVertexNumBytes() ];;
		}

		/**
		 * Record the data of vertex.
		 *
		 * <p>
		 * It doesn't matter whether the vertex is added or removed. The
		 * recorded data is the same in both cases.
		 *
		 * @param vertex
		 * @param ref
		 */
		public synchronized void record( final V vertex, final UndoableEditRef< V, E > ref )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = featureStore.createFeatureUndoId();
			serializer.getBytes( vertex, data );
			featureStore.storeAll( fi, vertex );

			final int dataIndex = dataStack.getNextDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.write( data );

			ref.setDataIndex( dataIndex );
		}

		public synchronized void doRemoveVertex( final long dataIndex )
		{
			dataStack.setDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();

			final V ref = graph.vertexRef();
			final V vertex = vertexUndoIdBimap.getObject( vi, ref );
			graph.remove( vertex );
			graph.releaseRef( ref );
		}

		public synchronized void doAddVertex( final long dataIndex )
		{
			dataStack.setDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();
			final int fi = dataStack.in.readInt();
			dataStack.in.readFully( data );

			final V ref = graph.vertexRef();
			final V vertex = graph.addVertex( ref );
			vertexUndoIdBimap.put( vi, vertex );
			serializer.setBytes( vertex, data );
			featureStore.retrieveAll( fi, vertex );
			graph.notifyVertexAdded( vertex );
			graph.releaseRef( ref );
		}
	}

	protected class AddEdgeType extends UndoableEditTypeImp< AddEdge >
	{
		@Override
		public AddEdge createInstance( final UndoableEditRef< V, E > ref )
		{
			return new AddEdge( ref, typeIndex() );
		}
	}

	private class AddEdge extends AbstractClearableUndoableEdit
	{
		private final AddRemoveEdgeRecord addRemoveEdge;

		AddEdge( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
			this.addRemoveEdge = new AddRemoveEdgeRecord();
		}

		public void init( final E edge )
		{
			super.init();
			addRemoveEdge.record( edge, ref );
		}

		@Override
		public void redo()
		{
			addRemoveEdge.doAddEdge( ref.getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveEdge.doRemoveEdge( ref.getDataIndex() );
		}
	}

	protected class RemoveEdgeType extends UndoableEditTypeImp< RemoveEdge >
	{
		@Override
		public RemoveEdge createInstance( final UndoableEditRef< V, E > ref )
		{
			return new RemoveEdge( ref, typeIndex() );
		}
	}

	private class RemoveEdge extends AbstractClearableUndoableEdit
	{
		private final AddRemoveEdgeRecord addRemoveEdge;

		RemoveEdge( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
			this.addRemoveEdge = new AddRemoveEdgeRecord();
		}

		public void init( final E edge )
		{
			super.init();
			addRemoveEdge.record( edge, ref );
		}

		@Override
		public void redo()
		{
			addRemoveEdge.doRemoveEdge( ref.getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveEdge.doAddEdge( ref.getDataIndex() );
		}
	}

	private class AddRemoveEdgeRecord
	{
		private final byte[] data;

		public AddRemoveEdgeRecord()
		{
			data = new byte[ serializer.getEdgeNumBytes() ];;
		}

		/**
		 * Record the data of edge.
		 *
		 * <p>
		 * It doesn't matter whether the edge is added or removed. The
		 * recorded data is the same in both cases.
		 *
		 * @param edge
		 * @param ref
		 */
		public void record( final E edge, final UndoableEditRef< V, E > ref )
		{
			final V vref = graph.vertexRef();
			final int ei = edgeUndoIdBimap.getId( edge );
			final int fi = featureStore.createFeatureUndoId();
			final int si = vertexUndoIdBimap.getId( edge.getSource( vref ) );
			final int sOutIndex = edge.getSourceOutIndex();
			final int ti = vertexUndoIdBimap.getId( edge.getTarget( vref ) );
			final int tInIndex = edge.getTargetInIndex();
			graph.releaseRef( vref );
			serializer.getBytes( edge, data );
			featureStore.storeAll( fi, edge );

			final int dataIndex = dataStack.getNextDataIndex();
			dataStack.out.writeInt( ei );
			dataStack.out.writeInt( fi );
			dataStack.out.writeInt( si );
			dataStack.out.writeInt( sOutIndex );
			dataStack.out.writeInt( ti );
			dataStack.out.writeInt( tInIndex );
			dataStack.out.write( data );

			ref.setUndoPoint( false );
			ref.setDataIndex( dataIndex );
		}

		public void doRemoveEdge( final long dataIndex )
		{
			dataStack.setDataIndex( dataIndex );
			final int ei = dataStack.in.readInt();

			final E ref = graph.edgeRef();
			final E edge = edgeUndoIdBimap.getObject( ei, ref );
			graph.remove( edge );
			graph.releaseRef( ref );
		}

		public void doAddEdge( final long dataIndex )
		{
			dataStack.setDataIndex( dataIndex );
			final int ei = dataStack.in.readInt();
			final int fi = dataStack.in.readInt();
			final int si = dataStack.in.readInt();
			final int sOutIndex = dataStack.in.readInt();
			final int ti = dataStack.in.readInt();
			final int tInIndex = dataStack.in.readInt();
			dataStack.in.readFully( data );

			final V vref1 = graph.vertexRef();
			final V vref2 = graph.vertexRef();
			final E eref = graph.edgeRef();
			final V source = vertexUndoIdBimap.getObject( si, vref1 );
			final V target = vertexUndoIdBimap.getObject( ti, vref2 );
			final E edge = graph.insertEdge( source, sOutIndex, target, tInIndex, eref );
			edgeUndoIdBimap.put( ei, edge );
			serializer.setBytes( edge, data );
			featureStore.retrieveAll( fi, edge );
//			graph.notifyEdgeAdded( edge ); // TODO: this should exist obviously, analogous to notifyVertexAdded()
			graph.releaseRef( eref );
			graph.releaseRef( vref2 );
			graph.releaseRef( vref1 );
		}
	}
}
