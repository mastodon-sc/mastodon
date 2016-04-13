package net.trackmate.revised.model;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.revised.model.undo.AbstractRemoveVertexUndoableEdit;
import net.trackmate.revised.undo.UndoIdBimap;

public abstract class AbstractRemoveAbstractSpot3DUndoableEdit<
		A extends AbstractRemoveAbstractSpot3DUndoableEdit< A, V, E, T >,
		V extends AbstractSpot3D< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends AbstractRemoveVertexUndoableEdit< A, V, E, T >
{
	private static final int X_OFFSET = AbstractRemoveVertexUndoableEdit.SIZE_IN_BYTES;
	private static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
	private static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
	private static final int TP_OFFSET = Z_OFFSET + DOUBLE_SIZE;
	protected static final int SIZE_IN_BYTES = TP_OFFSET + INT_SIZE;

	protected AbstractRemoveAbstractSpot3DUndoableEdit(
			final UndoableEditPool< A > pool,
			final AbstractModelGraph< ?, ?, V, E, ? > modelGraph,
			final UndoIdBimap< V > vertexUndoIdBimap
			)
	{
		super( pool, modelGraph, vertexUndoIdBimap );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public A init( final V vertex )
	{
		super.init( vertex );
		setX( vertex.getX() );
		setY( vertex.getY() );
		setZ( vertex.getZ() );
		setTimepointId( vertex.getTimepointId() );
		return ( A ) this;
	}

	@Override
	protected void setVertexFields( final V vertex )
	{
		vertex.setX( getX() );
		vertex.setY( getY() );
		vertex.setZ( getZ() );
		vertex.setTimepointId( getTimepointId() );
	}

	private double getX()
	{
		return access.getDouble( X_OFFSET );
	}

	private void setX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	private double getY()
	{
		return access.getDouble( Y_OFFSET );
	}

	private void setY( final double y )
	{
		access.putDouble( y, Y_OFFSET );
	}

	private double getZ()
	{
		return access.getDouble( Z_OFFSET );
	}

	private void setZ( final double z )
	{
		access.putDouble( z, Z_OFFSET );
	}

	private int getTimepointId()
	{
		return access.getInt( TP_OFFSET );
	}

	private void setTimepointId( final int tp )
	{
		access.putInt( tp, TP_OFFSET );
	}
}
