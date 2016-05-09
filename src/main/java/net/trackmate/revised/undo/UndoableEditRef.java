package net.trackmate.revised.undo;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.BYTE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.LONG_SIZE;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.VertexWithFeatures;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.revised.model.ModelGraph_HACK_FIX_ME;
import net.trackmate.revised.model.undo.old.UndoableEdit;

// TODO rename
public class UndoableEditRef<
			V extends VertexWithFeatures< V, E >,
			E extends Edge< V > >
		extends PoolObject< UndoableEditRef< V, E >, ByteMappedElement >
		implements UndoableEdit
{
	private static final int IS_UNDO_POINT_OFFSET = 0;
	private static final int TYPE_INDEX_OFFSET = IS_UNDO_POINT_OFFSET + BOOLEAN_SIZE;
	private static final int DATA_INDEX_OFFSET = TYPE_INDEX_OFFSET + BYTE_SIZE;
	private static final int SIZE_IN_BYTES = DATA_INDEX_OFFSET + LONG_SIZE;

	private final ModelGraph_HACK_FIX_ME< V, E > graph;

	private final UndoSerializer< V, E > serializer;

	private final UndoIdBimap< V > vertexUndoIdBimap;

	private final UndoIdBimap< E > edgeUndoIdBimap;

	private final UndoDataStack dataStack;

	private final UndoFeatureStore< V, E > featureStore;

	private final ArrayList< UndoableEdit > nonRefEdits;

	private final AddRemoveVertexRecord addRemoveVertex;

	private final AddRemoveEdgeRecord addRemoveEdge;

	final AddVertex addVertex;

	final RemoveVertex removeVertex;

	final AddEdge addEdge;

	final RemoveEdge removeEdge;

	protected UndoableEditRef(
			final Pool< UndoableEditRef< V, E >, ByteMappedElement > pool,
			final ModelGraph_HACK_FIX_ME< V, E > graph,
			final GraphFeatures< V, E > graphFeatures,
			final UndoSerializer< V, E > serializer,
			final UndoIdBimap< V > vertexUndoIdBimap,
			final UndoIdBimap< E > edgeUndoIdBimap,
			final UndoDataStack dataStack,
			final UndoFeatureStore< V, E > featureStore,
			final ArrayList< UndoableEdit > nonRefEdits
			)
	{
		super( pool );
		this.graph = graph;
		this.serializer = serializer;
		this.vertexUndoIdBimap = vertexUndoIdBimap;
		this.edgeUndoIdBimap = edgeUndoIdBimap;
		this.dataStack = dataStack;
		this.featureStore = featureStore;
		this.nonRefEdits = nonRefEdits;

		addRemoveVertex = new AddRemoveVertexRecord();
		addRemoveEdge = new AddRemoveEdgeRecord();
		addVertex = new AddVertex();
		removeVertex = new RemoveVertex();
		addEdge = new AddEdge();
		removeEdge = new RemoveEdge();
	}

	@Override
	public void undo()
	{
		getEdit().undo();
	}

	@Override
	public void redo()
	{
		getEdit().redo();
	}

	@Override
	public boolean isUndoPoint()
	{
		return getEdit().isUndoPoint();
	}

	@Override
	public void setUndoPoint( final boolean isUndoPoint )
	{
		getEdit().setUndoPoint( isUndoPoint );
	}

	@Override
	protected void setToUninitializedState()
	{}

	void clear()
	{
		if ( getTypeIndex() == OTHER )
			// THIS IS A HACK THAT ONLY WORKS BECAUSE OF HOW clearFromIndex() IS IMPLEMENTED!
			nonRefEdits.remove( nonRefEdits.size() - 1 );
	}

	private byte getTypeIndex()
	{
		return access.getByte( TYPE_INDEX_OFFSET );
	}

	private void setTypeIndex( final byte id )
	{
		access.putByte( id, TYPE_INDEX_OFFSET );
	}

	private long getDataIndex()
	{
		return access.getLong( DATA_INDEX_OFFSET );
	}

	private void setDataIndex( final long id )
	{
		access.putLong( id, DATA_INDEX_OFFSET );
	}

	private abstract class Edit
	{
		public abstract void redo();

		public abstract void undo();

		public void setUndoPoint( final boolean isUndoPoint )
		{
			access.putBoolean( isUndoPoint, IS_UNDO_POINT_OFFSET );
		}

		public boolean isUndoPoint()
		{
			return access.getBoolean( IS_UNDO_POINT_OFFSET );
		}
	}

	private static final byte ADD_VERTEX = 0;
	private static final byte REMOVE_VERTEX = 1;
	private static final byte ADD_EDGE = 2;
	private static final byte REMOVE_EDGE = 3;
	private static final byte OTHER = 4;

	private Edit getEdit()
	{
		final byte typeIndex = getTypeIndex();
		switch( typeIndex )
		{
			case ADD_VERTEX:
				return addVertex;
			case REMOVE_VERTEX:
				return removeVertex;
			case ADD_EDGE:
				return addEdge;
			case REMOVE_EDGE:
				return removeEdge;
			default:
				return null;
		}
	}

	class AddVertex extends Edit
	{
		public void init( final V vertex )
		{
			addRemoveVertex.record( vertex );
			setTypeIndex( ADD_VERTEX );
		}

		@Override
		public void redo()
		{
			addRemoveVertex.doAddVertex( getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveVertex.doRemoveVertex( getDataIndex() );
		}
	}

	class RemoveVertex extends Edit
	{
		public void init( final V vertex )
		{
			addRemoveVertex.record( vertex );
			setTypeIndex( REMOVE_VERTEX );
		}

		@Override
		public void redo()
		{
			addRemoveVertex.doRemoveVertex( getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveVertex.doAddVertex( getDataIndex() );
		}
	}

	class AddEdge extends Edit
	{
		public void init( final E edge )
		{
			addRemoveEdge.record( edge );
			setTypeIndex( ADD_EDGE );
		}

		@Override
		public void redo()
		{
			addRemoveEdge.doAddEdge( getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveEdge.doRemoveEdge( getDataIndex() );
		}
	}

	class RemoveEdge extends Edit
	{
		public void init( final E edge )
		{
			addRemoveEdge.record( edge );
			setTypeIndex( REMOVE_EDGE );
		}

		@Override
		public void redo()
		{
			addRemoveEdge.doRemoveEdge( getDataIndex() );
		}

		@Override
		public void undo()
		{
			addRemoveEdge.doAddEdge( getDataIndex() );
		}
	}

	class Other extends Edit
	{
		public void init( final UndoableEdit edit )
		{
			setUndoPoint( false );
			setTypeIndex( OTHER );
			setDataIndex( nonRefEdits.size() );
			nonRefEdits.add( edit );
		}

		@Override
		public void redo()
		{
			nonRefEdits.get( ( int ) getDataIndex() ).redo();
		}

		@Override
		public void undo()
		{
			nonRefEdits.get( ( int ) getDataIndex() ).undo();
		}

		@Override
		public void setUndoPoint( final boolean isUndoPoint )
		{
			nonRefEdits.get( ( int ) getDataIndex() ).setUndoPoint( isUndoPoint );
		}

		@Override
		public boolean isUndoPoint()
		{
			return nonRefEdits.get( ( int ) getDataIndex() ).isUndoPoint();
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
		 * Record the data of v.
		 *
		 * <p>
		 * It doesn't matter whether the vertex is added or removed. The
		 * recorded data is the same in both cases.
		 *
		 * @param vertex
		 * @return the dataIndex
		 */
		public void record( final V vertex )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = featureStore.createFeatureUndoId();
			serializer.getBytes( vertex, data );
			featureStore.storeAll( fi, vertex );

			final int dataIndex = dataStack.getNextDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.write( data );

			setUndoPoint( false );
			setDataIndex( dataIndex );
		}

		public void doRemoveVertex( final long dataIndex )
		{
			dataStack.setDataIndex( dataIndex );
			final int vi = dataStack.in.readInt();

			final V ref = graph.vertexRef();
			final V vertex = vertexUndoIdBimap.getObject( vi, ref );
			graph.remove( vertex );
			graph.releaseRef( ref );
		}

		public void doAddVertex( final long dataIndex )
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

	private class AddRemoveEdgeRecord
	{
		private final byte[] data;

		public AddRemoveEdgeRecord()
		{
			data = new byte[ serializer.getEdgeNumBytes() ];;
		}

		/**
		 * Record the data of v.
		 *
		 * <p>
		 * It doesn't matter whether the vertex is added or removed. The
		 * recorded data is the same in both cases.
		 *
		 * @param vertex
		 * @return the dataIndex
		 */
		public void record( final E edge )
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

			setUndoPoint( false );
			setDataIndex( dataIndex );
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




























	public static class UndoableEditList<
				V extends VertexWithFeatures< V, E >,
				E extends Edge< V > >
			extends Pool< UndoableEditRef< V, E >, ByteMappedElement >
	{
		public UndoableEditList(
				final int initialCapacity,
				final ModelGraph_HACK_FIX_ME< V, E > graph,
				final GraphFeatures< V, E > graphFeatures,
				final UndoSerializer< V, E > serializer,
				final UndoIdBimap< V > vertexUndoIdBimap,
				final UndoIdBimap< E > edgeUndoIdBimap )
		{
			this( initialCapacity, new Factory<>(), graph, graphFeatures, serializer, vertexUndoIdBimap, edgeUndoIdBimap );
		}

		/**
		 * Index in {@link #edits} where the next {@link UndoableEdit} is to be
		 * recorded. (This is not simply the end of the list because of Redo ...)
		 */
		private int nextEditIndex;

		public void setUndoPoint()
		{
			final UndoableEditRef< V, E > ref = createRef();
			if ( nextEditIndex > 0 )
				get( nextEditIndex - 1, ref ).setUndoPoint( true );
			releaseRef( ref );
		}

		public void undo()
		{
			final UndoableEditRef< V, E > ref = createRef();
			boolean first = true;
			for ( int i = nextEditIndex - 1; i >= 0; --i )
			{
				final UndoableEdit edit = get( i, ref );
				if ( edit.isUndoPoint() && !first )
					break;
				edit.undo();
				--nextEditIndex;
				first = false;
			}
			releaseRef( ref );
		}

		public void redo()
		{
			final UndoableEditRef< V, E > ref = createRef();
			for ( int i = nextEditIndex; i < size(); ++i )
			{
				final UndoableEdit edit = get( i, ref );
				edit.redo();
				++nextEditIndex;
				if ( edit.isUndoPoint() )
					break;
			}
			releaseRef( ref );
		}

		public void recordAddVertex( final V vertex )
		{
			final UndoableEditRef< V, E > ref = createRef();
			create( ref ).addVertex.init( vertex );
			releaseRef( ref );
		}

		public void recordRemoveVertex( final V vertex )
		{
			final UndoableEditRef< V, E > ref = createRef();
			create( ref ).removeVertex.init( vertex );
			releaseRef( ref );
		}

		public void recordAddEdge( final E edge )
		{
			final UndoableEditRef< V, E > ref = createRef();
			create( ref ).addEdge.init( edge );
			releaseRef( ref );
		}

		public void recordRemoveEdge( final E edge )
		{
			final UndoableEditRef< V, E > ref = createRef();
			create( ref ).removeEdge.init( edge );
			releaseRef( ref );
		}

		public void recordOther( final UndoableEdit undoableEdit )
		{
//			final UndoableEditRef< V, E > ref = createRef();
//			create( ref ).ot
//			releaseRef( ref );
			System.out.println( "NOT IMPLEMENTED YET." );
		}

		private UndoableEditRef< V, E > get( final int index, final UndoableEditRef< V, E > ref )
		{
			super.getByInternalPoolIndex( index, ref );
			return ref;
		}

		@Override
		public UndoableEditRef< V, E > create( final UndoableEditRef< V, E > ref )
		{
			if ( nextEditIndex < size() )
				clearFromIndex( nextEditIndex, ref );
			super.create( ref );
			++nextEditIndex;
			return ref;
		}

		private void clearFromIndex( final int fromIndex, final UndoableEditRef< V, E > ref )
		{
			for ( int i = super.size() - 1; i >= fromIndex; --i )
			{
				getByInternalPoolIndex( i, ref );
				ref.clear();
				deleteByInternalPoolIndex( i );
			}
		}

		@Override
		protected void deleteByInternalPoolIndex( final int index )
		{
			super.deleteByInternalPoolIndex( index );
		}

		private final ModelGraph_HACK_FIX_ME< V, E > graph;

		private final UndoSerializer< V, E > serializer;

		private final UndoIdBimap< V > vertexUndoIdBimap;

		private final UndoIdBimap< E > edgeUndoIdBimap;

		private final UndoDataStack dataStack;

		private final UndoFeatureStore< V, E > featureStore;

		private final ArrayList< UndoableEdit > nonRefEdits;

		private UndoableEditList(
				final int initialCapacity,
				final Factory< V, E > f,
				final ModelGraph_HACK_FIX_ME< V, E > graph,
				final GraphFeatures< V, E > graphFeatures,
				final UndoSerializer< V, E > serializer,
				final UndoIdBimap< V > vertexUndoIdBimap,
				final UndoIdBimap< E > edgeUndoIdBimap )
		{
			super( initialCapacity, f );
			f.pool = this;

			this.graph = graph;
			this.serializer = serializer;
			this.vertexUndoIdBimap = vertexUndoIdBimap;
			this.edgeUndoIdBimap = edgeUndoIdBimap;
			this.dataStack = new UndoDataStack( 1024 * 1024 * 32 );
			this.featureStore = new UndoFeatureStore<>( graphFeatures );
			this.nonRefEdits = new ArrayList<>();

			f.graph = graph;
			f.graphFeatures = graphFeatures;
			f.serializer = serializer;
			f.vertexUndoIdBimap = vertexUndoIdBimap;
			f.edgeUndoIdBimap = edgeUndoIdBimap;
			f.dataStack = dataStack;
			f.featureStore = featureStore;
			f.nonRefEdits = nonRefEdits;

		}

		private static class Factory<
					V extends VertexWithFeatures< V, E >,
					E extends Edge< V > >
				implements PoolObject.Factory< UndoableEditRef< V, E >, ByteMappedElement >
		{
			private UndoableEditList< V, E > pool;

			private ModelGraph_HACK_FIX_ME< V, E > graph;

			private GraphFeatures< V, E > graphFeatures;

			private UndoSerializer< V, E > serializer;

			private UndoIdBimap< V > vertexUndoIdBimap;

			private UndoIdBimap< E > edgeUndoIdBimap;

			private UndoDataStack dataStack;

			private UndoFeatureStore< V, E > featureStore;

			private ArrayList< UndoableEdit > nonRefEdits;

			@Override
			public int getSizeInBytes()
			{
				return UndoableEditRef.SIZE_IN_BYTES;
			}

			@Override
			public UndoableEditRef< V, E > createEmptyRef()
			{
				return new UndoableEditRef< V, E >(
						pool,
						graph,
						graphFeatures,
						serializer,
						vertexUndoIdBimap,
						edgeUndoIdBimap,
						dataStack,
						featureStore,
						nonRefEdits );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
