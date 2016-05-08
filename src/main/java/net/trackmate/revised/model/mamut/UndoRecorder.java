package net.trackmate.revised.model.mamut;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.revised.model.mamut.RemoveLinkUndoableEdit.RemoveLinkUndoableEditPool;
import net.trackmate.revised.model.mamut.RemoveSpotUndoableEdit.RemoveSpotUndoableEditPool;
import net.trackmate.revised.model.undo.old.PolymorphicUndoableEdit;
import net.trackmate.revised.model.undo.old.UndoableEdit;
import net.trackmate.revised.model.undo.old.PolymorphicUndoableEdit.PolymorphicUndoableEditList;
import net.trackmate.revised.undo.UndoIdBimap;

/**
 * TODO: javadoc
 * TODO: figure out, when mappings can be removed from UndoIdBimaps.
 * TODO: generalize and move to package net.trackmate.revised.model.undo once all UndoableEdits are implemented
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class UndoRecorder implements GraphListener< Spot, Link >
{
	private static final int defaultCapacity = 1000;

	/**
	 * Index in {@link #edits} where the next {@link UndoableEdit} is to be recorded.
	 */
	private int nextEditIndex;

	private boolean recording;

	private final RemoveLinkUndoableEditPool removeEdge;

	private final RemoveSpotUndoableEditPool removeVertex;

	private final PolymorphicUndoableEditList edits;

	// TODO the following fields are only here to be able to test with non-Ref UndoableEdits:
	private final ModelGraph graph; // TODO remove
	private final UndoIdBimap< Spot > vertexUndoIdBimap; // TODO remove
	private final UndoIdBimap< Link > edgeUndoIdBimap; // TODO remove

	public UndoRecorder( final ModelGraph graph, final GraphIdBimap< Spot, Link > idmap )
	{
		final UndoIdBimap< Spot > vertexUndoIdBimap = new UndoIdBimap< Spot >( idmap.vertexIdBimap() );
		final UndoIdBimap< Link > edgeUndoIdBimap = new UndoIdBimap< Link >( idmap.edgeIdBimap() );
		removeEdge = new RemoveLinkUndoableEditPool( defaultCapacity, graph, vertexUndoIdBimap, edgeUndoIdBimap );
		removeVertex = new RemoveSpotUndoableEditPool( defaultCapacity, graph, vertexUndoIdBimap );
		edits = new PolymorphicUndoableEditList( defaultCapacity );
		edits.registerPool( removeEdge );
		edits.registerPool( removeVertex );
		nextEditIndex = 0;
		recording = true;
		graph.addGraphListener( this );

		this.graph = graph; // TODO remove
		this.vertexUndoIdBimap = vertexUndoIdBimap; // TODO remove
		this.edgeUndoIdBimap = edgeUndoIdBimap; // TODO remove
	}

	public void setUndoPoint()
	{
		final PolymorphicUndoableEdit ref = edits.createRef();
		if ( nextEditIndex > 0 )
			edits.get( nextEditIndex - 1, ref ).setUndoPoint( true );
		edits.releaseRef( ref );
	}

	public void undo()
	{
		recording = false;

		final PolymorphicUndoableEdit ref = edits.createRef();
		boolean first = true;
		for ( int i = nextEditIndex - 1; i >= 0; --i )
		{
			final UndoableEdit edit = edits.get( i, ref );
			if ( edit.isUndoPoint() && !first )
				break;
			edit.undo();
			--nextEditIndex;
			first = false;
		}
		edits.releaseRef( ref );

		recording = true;
	}

	public void redo()
	{
		recording = false;

		final PolymorphicUndoableEdit ref = edits.createRef();
		for ( int i = nextEditIndex; i < edits.size(); ++i )
		{
			final UndoableEdit edit = edits.get( i, ref );
			edit.redo();
			++nextEditIndex;
			if ( edit.isUndoPoint() )
				break;
		}
		edits.releaseRef( ref );

		recording = true;
	}

	@Override
	public void graphRebuilt()
	{
		System.out.println( "Model.UndoRecorder.graphRebuilt()" );
	}

	@Override
	public void vertexAdded( final Spot vertex )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.vertexAdded()" );
		}
	}

	@Override
	public void vertexRemoved( final Spot vertex )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.vertexRemoved()" );

			final RemoveSpotUndoableEdit ref = removeVertex.createRef();
			final UndoableEdit edit = removeVertex.create( ref ).init( vertex );
//			final UndoableEdit edit = new RemoveVertexUndoableEdit( graph, vertexUndoIdBimap, vertex ); // TODO remove
			record( edit );
			removeVertex.releaseRef( ref );
		}
	}

	@Override
	public void edgeAdded( final Link edge )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.edgeAdded()" );
		}
	}

	@Override
	public void edgeRemoved( final Link edge )
	{
		if ( recording )
		{
			System.out.println( "Model.UndoRecorder.edgeRemoved()" );

			final RemoveLinkUndoableEdit ref = removeEdge.createRef();
			final UndoableEdit edit = removeEdge.create( ref ).init( edge );
//			final UndoableEdit edit = new RemoveEdgeUndoableEdit( graph, vertexUndoIdBimap, edgeUndoIdBimap, edge ); // TODO remove
			record( edit );
			removeEdge.releaseRef( ref );
		}
	}

	private void record( final UndoableEdit edit )
	{
		if ( nextEditIndex < edits.size() )
			edits.clearFromIndex( nextEditIndex );
		final PolymorphicUndoableEdit ref = edits.createRef();
		edits.create( ref ).init( edit );
		edits.releaseRef( ref );
		++nextEditIndex;
	}
}