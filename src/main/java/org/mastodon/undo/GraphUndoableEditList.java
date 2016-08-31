package org.mastodon.undo;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.features.Feature;
import org.mastodon.graph.features.FeatureRegistry;
import org.mastodon.graph.features.Features;
import org.mastodon.undo.attributes.Attribute;
import org.mastodon.undo.attributes.Attributes;

import gnu.trove.map.TIntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;

/**
 * TODO: javadoc
 * TODO: move to package model.undo (?)
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class GraphUndoableEditList<
			V extends Vertex< E >,
			E extends Edge< V > >
		extends UndoableEditList
{
	protected final ListenableGraph< V, E > graph;

	protected final GraphUndoSerializer< V, E > serializer;

	protected final UndoIdBimap< V > vertexUndoIdBimap;

	protected final UndoIdBimap< E > edgeUndoIdBimap;

	protected final UndoFeatureStore< V > vertexFeatureStore;

	protected final UndoFeatureStore< E > edgeFeatureStore;

	protected final Attributes< V > vertexAttributes;

	protected final Attributes< E > edgeAttributes;

	public GraphUndoableEditList(
			final int initialCapacity,
			final ListenableGraph< V, E > graph,
			final Features< V > vertexFeatures,
			final Features< E > edgeFeatures,
			final Attributes< V > vertexAttributes,
			final Attributes< E > edgeAttributes,
			final GraphUndoSerializer< V, E > serializer,
			final UndoIdBimap< V > vertexUndoIdBimap,
			final UndoIdBimap< E > edgeUndoIdBimap )
	{
		super( initialCapacity );
		this.graph = graph;
		this.vertexAttributes = vertexAttributes;
		this.edgeAttributes = edgeAttributes;
		this.serializer = serializer;
		this.vertexUndoIdBimap = vertexUndoIdBimap;
		this.edgeUndoIdBimap = edgeUndoIdBimap;
		vertexFeatureStore = new UndoFeatureStore<>( vertexFeatures );
		edgeFeatureStore = new UndoFeatureStore<>( edgeFeatures );

		addVertex = new AddVertexType();
		removeVertex = new RemoveVertexType();
		addEdge = new AddEdgeType();
		removeEdge = new RemoveEdgeType();
		setVertexFeature = new SetFeatureType<>( vertexUndoIdBimap, vertexFeatureStore );
		setEdgeFeature = new SetFeatureType<>( edgeUndoIdBimap, edgeFeatureStore );
		setVertexAttribute = new SetAttributeType<>( vertexUndoIdBimap, vertexAttributes );
		setEdgeAttribute = new SetAttributeType<>( edgeUndoIdBimap, edgeAttributes );
	}

	public void recordAddVertex( final V vertex )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( addVertex ).init( vertex );
		releaseRef( ref );
	}

	public void recordRemoveVertex( final V vertex )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( removeVertex ).init( vertex );
		releaseRef( ref );
	}

	public void recordAddEdge( final E edge )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( addEdge ).init( edge );
		releaseRef( ref );
	}

	public void recordRemoveEdge( final E edge )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( removeEdge ).init( edge );
		releaseRef( ref );
	}

	public void recordSetVertexFeature( final Feature< ?, V, ? > feature, final V vertex )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( setVertexFeature ).init( feature, vertex );
		releaseRef( ref );
	}

	public void recordSetEdgeFeature( final Feature< ?, E, ? > feature, final E edge )
	{
		final UndoableEditRef ref = createRef();
		create( ref ).getEdit( setEdgeFeature ).init( feature, edge );
		releaseRef( ref );
	}

	public void recordSetVertexAttribute( final Attribute< V > attribute, final V vertex )
	{
		final UndoableEditRef ref = createRef();
		boolean createNewEdit = true;
		if ( nextEditIndex > 0 )
		{
			final UndoableEditRef edit = get( nextEditIndex - 1, ref );
			createNewEdit = edit.isUndoPoint() || !setVertexAttribute.matches( edit, attribute, vertex );
		}
		if ( createNewEdit )
			create( ref ).getEdit( setVertexAttribute ).init( attribute, vertex );
		releaseRef( ref );
	}

	public void recordSetEdgeAttribute( final Attribute< E > attribute, final E edge )
	{
		final UndoableEditRef ref = createRef();
		boolean createNewEdit = true;
		if ( nextEditIndex > 0 )
		{
			final UndoableEditRef edit = get( nextEditIndex - 1, ref );
			createNewEdit = edit.isUndoPoint() || !setEdgeAttribute.matches( edit, attribute, edge );
		}
		if ( createNewEdit )
			create( ref ).getEdit( setEdgeAttribute ).init( attribute, edge );
		releaseRef( ref );
	}

	private final AddVertexType addVertex;

	private final RemoveVertexType removeVertex;

	private final AddEdgeType addEdge;

	private final RemoveEdgeType removeEdge;

	private final SetFeatureType< V > setVertexFeature;

	private final SetFeatureType< E > setEdgeFeature;

	private final SetAttributeType< V > setVertexAttribute;

	private final SetAttributeType< E > setEdgeAttribute;

	private class AddVertexType extends UndoableEditTypeImp< AddVertex >
	{
		@Override
		public AddVertex createInstance( final UndoableEditRef ref )
		{
			return new AddVertex( ref, typeIndex() );
		}
	}

	private class AddVertex extends AbstractUndoableEdit
	{
		private final AddRemoveVertexRecord addRemoveVertex;

		AddVertex( final UndoableEditRef ref, final int typeIndex )
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

	private class RemoveVertexType extends UndoableEditTypeImp< RemoveVertex >
	{
		@Override
		public RemoveVertex createInstance( final UndoableEditRef ref )
		{
			return new RemoveVertex( ref, typeIndex() );
		}
	}

	private class RemoveVertex extends AbstractUndoableEdit
	{
		private final AddRemoveVertexRecord addRemoveVertex;

		RemoveVertex( final UndoableEditRef ref, final int typeIndex )
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

		public void initAdd( final V vertex, final UndoableEditRef ref )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = vertexFeatureStore.createFeatureUndoId();

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( fi );
			dataStack.out.skip( data.length );

			ref.setDataIndex( dataIndex );
		}

		public void initRemove( final V vertex, final UndoableEditRef ref )
		{
			final int vi = vertexUndoIdBimap.getId( vertex );
			final int fi = vertexFeatureStore.createFeatureUndoId();
			serializer.getBytes( vertex, data );
			vertexFeatureStore.storeAll( fi, vertex );

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
			vertexFeatureStore.storeAll( fi, vertex );

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
			serializer.notifyVertexAdded( vertex );
			vertexFeatureStore.retrieveAll( fi, vertex );
			graph.releaseRef( ref );

			return dataStack.getReadDataIndex();
		}
	}

	private class AddEdgeType extends UndoableEditTypeImp< AddEdge >
	{
		@Override
		public AddEdge createInstance( final UndoableEditRef ref )
		{
			return new AddEdge( ref, typeIndex() );
		}
	}

	private class AddEdge extends AbstractUndoableEdit
	{
		private final AddRemoveEdgeRecord addRemoveEdge;

		AddEdge( final UndoableEditRef ref, final int typeIndex )
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

	private class RemoveEdgeType extends UndoableEditTypeImp< RemoveEdge >
	{
		@Override
		public RemoveEdge createInstance( final UndoableEditRef ref )
		{
			return new RemoveEdge( ref, typeIndex() );
		}
	}

	private class RemoveEdge extends AbstractUndoableEdit
	{
		private final AddRemoveEdgeRecord addRemoveEdge;

		RemoveEdge( final UndoableEditRef ref, final int typeIndex )
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

		public void initAdd( final E edge, final UndoableEditRef ref )
		{
			final int ei = edgeUndoIdBimap.getId( edge );
			final int fi = edgeFeatureStore.createFeatureUndoId();

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

		public void initRemove( final E edge, final UndoableEditRef ref )
		{
			final V vref = graph.vertexRef();
			final int ei = edgeUndoIdBimap.getId( edge );
			final int fi = edgeFeatureStore.createFeatureUndoId();
			final int si = vertexUndoIdBimap.getId( edge.getSource( vref ) );
			final int sOutIndex = edge.getSourceOutIndex();
			final int ti = vertexUndoIdBimap.getId( edge.getTarget( vref ) );
			final int tInIndex = edge.getTargetInIndex();
			graph.releaseRef( vref );
			serializer.getBytes( edge, data );
			edgeFeatureStore.storeAll( fi, edge );

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
			edgeFeatureStore.storeAll( fi, edge );

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
			serializer.notifyEdgeAdded( edge );
			edgeFeatureStore.retrieveAll( fi, edge );
			graph.releaseRef( eref );
			graph.releaseRef( vref2 );
			graph.releaseRef( vref1 );

			return dataStack.getReadDataIndex();
		}
	}

	private class SetFeatureType< O > extends UndoableEditTypeImp< SetFeature< O > >
	{
		private final UndoIdBimap< O > undoIdBimap;

		private final UndoFeatureStore< O > featureStore;

		public SetFeatureType(
			final UndoIdBimap< O > undoIdBimap,
			final UndoFeatureStore< O > featureStore )
		{
			super();
			this.undoIdBimap = undoIdBimap;
			this.featureStore = featureStore;
		}

		@Override
		public SetFeature< O > createInstance( final UndoableEditRef ref )
		{
			return new SetFeature<>( ref, typeIndex(), undoIdBimap, featureStore );
		}
	}

	private class SetFeature< O > extends AbstractUndoableEdit
	{
		private final UndoIdBimap< O > undoIdBimap;

		private final UndoFeatureStore< O > featureStore;

		SetFeature(
				final UndoableEditRef ref,
				final int typeIndex,
				final UndoIdBimap< O > undoIdBimap,
				final UndoFeatureStore< O > featureStore )
		{
			super( ref, typeIndex );
			this.undoIdBimap = undoIdBimap;
			this.featureStore = featureStore;
		}

		public void init( final Feature< ?, O, ? > feature, final O obj )
		{
			super.init();

			final int vi = undoIdBimap.getId( obj );
			final int fi = featureStore.createFeatureUndoId();
			final int fuid = feature.getUniqueFeatureId();
			featureStore.store( fi, feature, obj );

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

			final O ref = undoIdBimap.createRef();
			final O obj = undoIdBimap.getObject( vi, ref );
			@SuppressWarnings( "unchecked" )
			final Feature< ?, O, ? > feature = ( Feature< ?, O, ? > ) FeatureRegistry.getFeature( fuid );
			featureStore.swap( fi, feature, obj );
			undoIdBimap.releaseRef( ref );

			return dataStack.getReadDataIndex();
		}
	}

	private class SetAttributeType< O > extends UndoableEditTypeImp< SetAttribute< O > >
	{
		private final UndoIdBimap< O > undoIdBimap;

		private final Attributes< O > attributes;

		public SetAttributeType(
				final UndoIdBimap< O > undoIdBimap,
				final Attributes< O > attributes )
		{
			super();
			this.undoIdBimap = undoIdBimap;
			this.attributes = attributes;
		}

		@Override
		public SetAttribute< O > createInstance( final UndoableEditRef ref )
		{
			return new SetAttribute<>( ref, typeIndex(), undoIdBimap, attributes );
		}

		public boolean matches( final UndoableEditRef ref, final Attribute< O > attribute, final O obj )
		{
			return isInstance( ref ) && ref.getEdit( this ).matches( attribute, obj );
		}
	}

	private static class DataArrays
	{
		final byte[] data;

		final byte[] swapdata;

		public DataArrays( final int numBytes )
		{
			data = new byte[ numBytes ];
			swapdata = new byte[ numBytes ];
		}
	}

	private class SetAttribute< O > extends AbstractUndoableEdit
	{
		private final UndoIdBimap< O > undoIdBimap;

		private final Attributes< O > attributes;

		private final TIntObjectMap< DataArrays > perAttributeDataArrays;

		SetAttribute(
				final UndoableEditRef ref,
				final int typeIndex,
				final UndoIdBimap< O > undoIdBimap,
				final Attributes< O > attributes )
		{
			super( ref, typeIndex );
			this.undoIdBimap = undoIdBimap;
			this.attributes = attributes;
			this.perAttributeDataArrays = new TIntObjectArrayMap<>();
//			this.data = new byte[ attributeSerializer.getNumBytes() ];
//			this.swapdata = new byte[ attributeSerializer.getNumBytes() ];
		}

		public void init( final Attribute< O > attribute, final O obj )
		{
			super.init();

			final int vi = undoIdBimap.getId( obj );
			final int auid = attribute.getAttributeId();
			final byte[] data = getDataArrays( attribute ).data;
			attribute.getUndoSerializer().getBytes( obj, data );

			final long dataIndex = dataStack.getWriteDataIndex();
			dataStack.out.writeInt( vi );
			dataStack.out.writeInt( auid );
			dataStack.out.write( data );

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
			final int auid = dataStack.in.readInt();

			final Attribute< O > attribute = attributes.getAttributeById( auid );
			final DataArrays arrays = getDataArrays( attribute );
			final byte[] data = arrays.data;
			final byte[] swapdata = arrays.swapdata;
			dataStack.in.readFully( swapdata );

			final O ref = undoIdBimap.createRef();
			final O obj = undoIdBimap.getObject( vi, ref );
			attribute.getUndoSerializer().getBytes( obj, data );

			dataStack.setWriteDataIndex( dataIndex );
//			dataStack.out.writeInt( vi );
//			dataStack.out.writeInt( auid );
			dataStack.out.skip( 8 );
			dataStack.out.write( data );

			attribute.getUndoSerializer().setBytes( obj, swapdata );
			attribute.getUndoSerializer().notifySet( obj );
			undoIdBimap.releaseRef( ref );

			return dataStack.getReadDataIndex();
		}

		private DataArrays getDataArrays( final Attribute< O > attribute )
		{
			DataArrays arrays = perAttributeDataArrays.get( attribute.getAttributeId() );
			if ( arrays == null )
			{
				arrays = new DataArrays( attribute.getUndoSerializer().getNumBytes() );
				perAttributeDataArrays.put( attribute.getAttributeId(), arrays );
			}
			return arrays;
		}

		boolean matches( final Attribute< O > attribute, final O obj )
		{
			final int vi = undoIdBimap.getId( obj );
			final int auid = attribute.getAttributeId();

			final long dataIndex = ref.getDataIndex();
			dataStack.setReadDataIndex( dataIndex );
			final boolean matches = ( dataStack.in.readInt() == vi )
					&& ( dataStack.in.readInt() == auid );
			dataStack.setReadDataIndex( dataIndex );

			return matches;
		}
	}
}
