package net.trackmate.revised.trackscheme.display;

import net.trackmate.graph.IntPoolObjectMap;
import net.trackmate.revised.trackscheme.display.ScreenVertex.Transition;

public class ScreenEntitiesInterpolation
{
	private final ScreenEntities start;

	private final ScreenEntities end;

	private final IntPoolObjectMap< ScreenVertex > idToStartVertex;

	private final IntPoolObjectMap< ScreenVertex > idToEndVertex;

	public ScreenEntitiesInterpolation( final ScreenEntities start, final ScreenEntities end )
	{
		this.start = start;
		this.end = end;

		idToStartVertex = new IntPoolObjectMap< ScreenVertex >( start.getVertexPool(), start.getVertices().size() );
		for ( final ScreenVertex v : start.getVertices() )
			idToStartVertex.put( v.getTrackSchemeVertexId(), v );

		idToEndVertex = new IntPoolObjectMap< ScreenVertex >( end.getVertexPool(), end.getVertices().size() );
		for ( final ScreenVertex v : end.getVertices() )
			idToEndVertex.put( v.getTrackSchemeVertexId(), v );
	}

	public void interpolate( final double currentRatio, final ScreenEntities current )
	{
		final double accelRatio = Math.sin( Math.PI * Math.sin( Math.PI * currentRatio / 2 ) / 2 );

		// Interpolate vertices
		// ====================
		// Each interpolated vertex either moves, appears, or disappears.
		final ScreenVertex vCurrent = current.getVertexPool().createRef();
		final ScreenVertex vStart = start.getVertexPool().createRef();
		final ScreenVertex vEnd = end.getVertexPool().createRef();
		for ( final ScreenVertex v : start.getVertices() )
		{
			final int vId = v.getTrackSchemeVertexId();
			if ( vId < 0 )
				continue;

			current.getVertices().add( current.getVertexPool().create( vCurrent ) );
			if ( idToEndVertex.get( vId, vEnd ) != null )
				interpolate( v, vEnd, accelRatio, vCurrent );
			else
				disappear( v, accelRatio, vCurrent );
		}
		for ( final ScreenVertex v : end.getVertices() )
		{
			if ( idToStartVertex.get( v.getTrackSchemeVertexId(), vStart ) == null )
			{
				current.getVertices().add( current.getVertexPool().create( vCurrent ) );
				appear( v, accelRatio, vCurrent );
			}
		}

		// Interpolate edges
		// =================
		// For now, only edges between non-disappearing interpolated vertices
		// are added.
		final ScreenEdge eCurrent = current.getEdgePool().createRef();
		for ( final ScreenEdge e : end.getEdges() )
		{
			final int sourceIndex = end.getVertices().get( e.getSourceScreenVertexIndex(), vEnd ).getInterpolatedScreenVertexIndex();
			final int targetIndex = end.getVertices().get( e.getTargetScreenVertexIndex(), vEnd ).getInterpolatedScreenVertexIndex();
			current.getEdges().add( current.getEdgePool().create( eCurrent ) );
			eCurrent.init(
					e.getTrackSchemeEdgeId(),
					sourceIndex,
					targetIndex,
					e.isSelected() );
		}

		// Interpolate dense vertex ranges
		// ===============================
		// For now, simply use the dense ranges of the interpolation target.
		current.getVertexRanges().addAll( end.getVertexRanges() );

		// clean up
		current.getVertexPool().releaseRef( vCurrent );
		start.getVertexPool().releaseRef( vStart );
		end.getVertexPool().releaseRef( vEnd );
		current.getEdgePool().releaseRef( eCurrent );
	}

	private void interpolate( final ScreenVertex vStart, final ScreenVertex vEnd, final double ratio, final ScreenVertex vCurrent )
	{
		vCurrent.setTrackSchemeVertexId( vEnd.getTrackSchemeVertexId() );
		vCurrent.setSelected( vEnd.isSelected() );
		vCurrent.setGhost( vEnd.isGhost() );
		vCurrent.setVertexDist( vEnd.getVertexDist() );
		vCurrent.setX( ratio * vEnd.getX() + ( 1 - ratio ) * vStart.getX() );
		vCurrent.setY( ratio * vEnd.getY() + ( 1 - ratio ) * vStart.getY() );
		vCurrent.setTransition( Transition.NONE );
		vEnd.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
	}

	private void disappear( final ScreenVertex vStart, final double ratio, final ScreenVertex vCurrent )
	{
		vCurrent.setTrackSchemeVertexId( -1 );
		vCurrent.setSelected( vStart.isSelected() );
		vCurrent.setGhost( vStart.isGhost() );
		vCurrent.setVertexDist( vStart.getVertexDist() );
		vCurrent.setX( vStart.getX() );
		vCurrent.setY( vStart.getY() );
		vCurrent.setTransition( Transition.DISAPPEAR );
		vCurrent.setInterpolationCompletionRatio( ratio );
	}

	private void appear( final ScreenVertex vEnd, final double ratio, final ScreenVertex vCurrent )
	{
		vCurrent.setTrackSchemeVertexId( vEnd.getTrackSchemeVertexId() );
		vCurrent.setSelected( vEnd.isSelected() );
		vCurrent.setGhost( vEnd.isGhost() );
		vCurrent.setVertexDist( vEnd.getVertexDist() );
		vCurrent.setX( vEnd.getX() );
		vCurrent.setY( vEnd.getY() );
		vCurrent.setTransition( Transition.APPEAR );
		vCurrent.setInterpolationCompletionRatio( ratio );
		vEnd.setInterpolatedScreenVertexIndex( vCurrent.getInternalPoolIndex() );
	}
}
