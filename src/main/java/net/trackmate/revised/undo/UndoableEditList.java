package net.trackmate.revised.undo;

import java.util.ArrayList;

import gnu.trove.map.TIntObjectArrayMap;
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

public class UndoableEditList<
			V extends VertexWithFeatures< V, E >,
			E extends Edge< V > >
		extends Pool< UndoableEditRef< V, E >, ByteMappedElement >
{
	protected final ModelGraph_HACK_FIX_ME< V, E > graph;

	protected final UndoSerializer< V, E > serializer;

	protected final UndoIdBimap< V > vertexUndoIdBimap;

	protected final UndoIdBimap< E > edgeUndoIdBimap;

	protected final UndoDataStack dataStack;

	protected final UndoFeatureStore< V, E > featureStore;

	protected final ArrayList< UndoableEdit > nonRefEdits;

	/**
	 * Index in {@link #edits} where the next {@link UndoableEdit} is to be
	 * recorded. (This is not simply the end of the list because of Redo ...)
	 */
	protected int nextEditIndex;

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
		dataStack = new UndoDataStack( 1024 * 1024 * 32 );
		featureStore = new UndoFeatureStore<>( graphFeatures );
		nonRefEdits = new ArrayList<>();
	}

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

	private static class Factory<
				V extends VertexWithFeatures< V, E >,
				E extends Edge< V > >
			implements PoolObject.Factory< UndoableEditRef< V, E >, ByteMappedElement >
	{
		private UndoableEditList< V, E > pool;

		@Override
		public int getSizeInBytes()
		{
			return UndoableEditRef.SIZE_IN_BYTES;
		}

		@Override
		public UndoableEditRef< V, E > createEmptyRef()
		{
			return new UndoableEditRef< V, E >(	pool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}
	};

	/**
	 * An {@link UndoableEdit} that can be cleared.
	 * This is used to remove non-ref edits from {@link UndoableEditList#nonRefEdits}.
	 */
	public interface ClearableUndoableEdit extends UndoableEdit
	{
		public void clear();
	}

	/**
	 * Represents a specific sub-type of {@link ClearableUndoableEdit},
	 * identified by a unique index.
	 *
	 * @param <T>
	 *            the {@link ClearableUndoableEdit} type.
	 */
	public interface UndoableEditType< V extends VertexWithFeatures< V, E >, E extends Edge< V >, T extends ClearableUndoableEdit >
	{
		/**
		 * Get the unique index associated to {@code T}.
		 *
		 * @return the unique index associated to T.
		 */
		public int typeIndex();

		/**
		 * Create a {@link ClearableUndoableEdit} of type {@code T}.
		 *
		 * @param ref
		 *            the {@link UndoableEditRef} that will use the created
		 *            {@code T}.
		 * @return a new {@code T}.
		 */
		public T createInstance( final UndoableEditRef< V, E > ref );
	}

	/**
	 * Abstract base class for {@link ClearableUndoableEdit}s that has an
	 * {@link UndoableEditRef} to which it forwards. {@link #clear()} does
	 * nothing.
	 */
	protected abstract class AbstractClearableUndoableEdit implements ClearableUndoableEdit
	{
		protected final UndoableEditRef< V, E > ref;

		protected final byte typeIndex;

		protected AbstractClearableUndoableEdit( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			this.ref = ref;
			this.typeIndex = ( byte ) typeIndex;
		}

		public void init()
		{
			ref.setIsUndoPointField( false );
			ref.setTypeIndex( typeIndex );
		}

		@Override
		public boolean isUndoPoint()
		{
			return ref.getIsUndoPointField();
		}

		@Override
		public void setUndoPoint( final boolean isUndoPoint )
		{
			ref.setIsUndoPointField( isUndoPoint );
		}

		@Override
		public void clear()
		{}
	}

	private final int idgen = 0;

	private final TIntObjectArrayMap< UndoableEditType< V, E, ? > > undoableEditTypes = new TIntObjectArrayMap<>();

	/**
	 * Abstract base class for the {@link UndoableEditType}s of this {@link UndoableEditList}.
	 *
	 * @param <T>
	 *            the {@link AbstractClearableUndoableEdit} type.
	 */
	protected abstract class UndoableEditTypeImp< T extends AbstractClearableUndoableEdit > implements UndoableEditType< V, E, T >
	{
		private final int typeIndex;

		public UndoableEditTypeImp()
		{
			typeIndex = idgen;
			undoableEditTypes.put( typeIndex, this );
		}

		@Override
		public int typeIndex()
		{
			return typeIndex;
		}

		@Override
		public abstract T createInstance( final UndoableEditRef< V, E > ref );
	}

	UndoableEditType< V, E, ? > getUndoableEditType( final byte typeIndex )
	{
		return undoableEditTypes.get( typeIndex );
	}



	/*
	 * =========================================================================
	 *
	 *                        recording specific edits
	 *
	 * =========================================================================
	 */

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

	public void recordOther( final UndoableEdit undoableEdit )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( other ).init( undoableEdit );
		releaseRef( ref );
	}

	protected final AddVertexType addVertex = new AddVertexType();

	protected final RemoveVertexType removeVertex = new RemoveVertexType();

	protected final AddEdgeType addEdge = new AddEdgeType();

	protected final RemoveEdgeType removeEdge = new RemoveEdgeType();

	protected final OtherType other = new OtherType();

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
//				graph.notifyEdgeAdded( edge ); // TODO: this should exist obviously, analogous to notifyVertexAdded()
			graph.releaseRef( eref );
			graph.releaseRef( vref2 );
			graph.releaseRef( vref1 );
		}
	}

	protected class OtherType extends UndoableEditTypeImp< Other >
	{
		@Override
		public Other createInstance( final UndoableEditRef< V, E > ref )
		{
			return new Other( ref, typeIndex() );
		}
	}

	private class Other extends AbstractClearableUndoableEdit
	{
		Other( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
		}

		public void init( final UndoableEdit edit )
		{
			super.init();
			ref.setDataIndex( nonRefEdits.size() );
			nonRefEdits.add( edit );
		}

		@Override
		public void redo()
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).redo();
		}

		@Override
		public void undo()
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).undo();
		}

		@Override
		public void setUndoPoint( final boolean isUndoPoint )
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).setUndoPoint( isUndoPoint );
		}

		@Override
		public boolean isUndoPoint()
		{
			return nonRefEdits.get( ( int ) ref.getDataIndex() ).isUndoPoint();
		}

		@Override
		public void clear()
		{
			// THIS ONLY WORKS BECAUSE OF HOW clearFromIndex() IS IMPLEMENTED!
			nonRefEdits.remove( nonRefEdits.size() - 1 );
		}
	}
}
