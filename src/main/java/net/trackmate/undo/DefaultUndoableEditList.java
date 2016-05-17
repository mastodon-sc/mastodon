package net.trackmate.undo;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.FeatureRegistry;
import net.trackmate.graph.zzgraphinterfaces.GraphFeatures;
import net.trackmate.graph.zzgraphinterfaces.VertexFeature;
import net.trackmate.graph.zzgraphinterfaces.VertexWithFeatures;
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

	public void recordSetFeature( final VertexFeature< ?, V, ? > feature, final V vertex )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( setFeature ).init( feature, vertex );
		releaseRef( ref );
	}

	protected final AddVertexType addVertex = new AddVertexType();

	protected final RemoveVertexType removeVertex = new RemoveVertexType();

	protected final AddEdgeType addEdge = new AddEdgeType();

	protected final RemoveEdgeType removeEdge = new RemoveEdgeType();

	protected final SetFeatureType setFeature = new SetFeatureType();

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
			addRemoveVertex.initAdd( vertex, ref );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = addRemoveVertex.doAddVertex( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			addRemoveVertex.doRemoveVertex( d0 );
			dataStack.setWriteDataIndex( d0 );
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
			addRemoveVertex.initRemove( vertex, ref );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = addRemoveVertex.doRemoveVertex( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			addRemoveVertex.doAddVertex( d0 );
			dataStack.setWriteDataIndex( d0 );
		}
	}

	private class AddRemoveVertexRecord
	{
		private final byte[] data;

		public AddRemoveVertexRecord()
		{
			data = new byte[ serializer.getVertexNumBytes() ];
		}

		public void initAdd( final V vertex, final UndoableEditRef< V, E > ref )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = featureStore.createFeatureUndoId();

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.skip( data.length );

			ref.setDataIndex( dataIndex );
		}

		public void initRemove( final V vertex, final UndoableEditRef< V, E > ref )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = featureStore.createFeatureUndoId();
			serializer.getBytes( vertex, data );
			featureStore.storeAll( fi, vertex );

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.write( data );

			ref.setDataIndex( dataIndex );
		}

		public long doRemoveVertex( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();
			final int fi = dataStack.in.readInt();

			final V vref = graph.vertexRef();
			final V vertex = vertexUndoIdBimap.getObject( vi, vref );
			serializer.getBytes( vertex, data );
			featureStore.storeAll( fi, vertex );

			dataStack.setWriteDataIndex( dataIndex );
//			dataStack.out.writeInt( vi );
//			dataStack.out.writeInt( fi );
			dataStack.out.skip( 8 );
			dataStack.out.write( data );

			graph.remove( vertex );
			graph.releaseRef( vref );

			return dataStack.getWriteDataIndex();
		}

		public long doAddVertex( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
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

			return dataStack.getReadDataIndex();
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
			addRemoveEdge.initAdd( edge, ref );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = addRemoveEdge.doAddEdge( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			addRemoveEdge.doRemoveEdge( d0 );
			dataStack.setWriteDataIndex( d0 );
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
			addRemoveEdge.initRemove( edge, ref );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = addRemoveEdge.doRemoveEdge( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			addRemoveEdge.doAddEdge( d0 );
			dataStack.setWriteDataIndex( d0 );
		}
	}

	private class AddRemoveEdgeRecord
	{
		private final byte[] data;

		public AddRemoveEdgeRecord()
		{
			data = new byte[ serializer.getEdgeNumBytes() ];;
		}

		public void initAdd( final E edge, final UndoableEditRef< V, E > ref )
		{
			final int ei = edgeUndoIdBimap.getId( edge );
			final int fi = featureStore.createFeatureUndoId();

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( ei );
			dataStack.out.writeInt( fi );
//			dataStack.out.writeInt( si );
//			dataStack.out.writeInt( sOutIndex );
//			dataStack.out.writeInt( ti );
//			dataStack.out.writeInt( tInIndex );
			dataStack.out.skip( 16 + data.length );

			ref.setDataIndex( dataIndex );
		}

		public void initRemove( final E edge, final UndoableEditRef< V, E > ref )
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

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( ei );
			dataStack.out.writeInt( fi );
			dataStack.out.writeInt( si );
			dataStack.out.writeInt( sOutIndex );
			dataStack.out.writeInt( ti );
			dataStack.out.writeInt( tInIndex );
			dataStack.out.write( data );

			ref.setDataIndex( dataIndex );
		}

		public long doRemoveEdge( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
			final int ei = dataStack.in.readInt();
			final int fi = dataStack.in.readInt();

			final E ref = graph.edgeRef();
			final E edge = edgeUndoIdBimap.getObject( ei, ref );

			final V vref = graph.vertexRef();
			final int si = vertexUndoIdBimap.getId( edge.getSource( vref ) );
			final int sOutIndex = edge.getSourceOutIndex();
			final int ti = vertexUndoIdBimap.getId( edge.getTarget( vref ) );
			final int tInIndex = edge.getTargetInIndex();
			graph.releaseRef( vref );
			serializer.getBytes( edge, data );
			featureStore.storeAll( fi, edge );

			dataStack.setWriteDataIndex( dataIndex );
//			dataStack.out.writeInt( ei );
//			dataStack.out.writeInt( fi );
			dataStack.out.skip( 8 );
			dataStack.out.writeInt( si );
			dataStack.out.writeInt( sOutIndex );
			dataStack.out.writeInt( ti );
			dataStack.out.writeInt( tInIndex );
			dataStack.out.write( data );

			graph.remove( edge );
			graph.releaseRef( ref );

			return dataStack.getWriteDataIndex();
		}

		public long doAddEdge( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
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

			return dataStack.getReadDataIndex();
		}
	}

	protected class SetFeatureType extends UndoableEditTypeImp< SetFeature >
	{
		@Override
		public SetFeature createInstance( final UndoableEditRef< V, E > ref )
		{
			return new SetFeature( ref, typeIndex() );
		}
	}

	private class SetFeature extends AbstractClearableUndoableEdit
	{
		SetFeature( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
		}

		public void init( final VertexFeature< ?, V, ? > feature, final V vertex )
		{
			super.init();

			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = featureStore.createFeatureUndoId();
			final int fuid = feature.getUniqueFeatureId();
			featureStore.store( fi, feature, vertex );

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.writeInt( fuid );

			ref.setDataIndex( dataIndex );
		}

		@Override
		public void redo()
		{
			final long d0 = ref.getDataIndex();
			final long d1 = swap( d0 );
			dataStack.setWriteDataIndex( d1 );
		}

		@Override
		public void undo()
		{
			final long d0 = ref.getDataIndex();
			swap( d0 );
			dataStack.setWriteDataIndex( d0 );
		}

		private long swap( final long dataIndex )
		{
			dataStack.setReadDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();
			final int fi = dataStack.in.readInt();
			final int fuid = dataStack.in.readInt();

			final V ref = graph.vertexRef();
			final V vertex = vertexUndoIdBimap.getObject( vi, ref );
			@SuppressWarnings( "unchecked" )
			final VertexFeature< ?, V, ? > feature = ( VertexFeature< ?, V, ? > ) FeatureRegistry.getVertexFeature( fuid );
			featureStore.swap( fi, feature, vertex );
			graph.releaseRef( ref );

			return dataStack.getReadDataIndex();
		}
	}
}
