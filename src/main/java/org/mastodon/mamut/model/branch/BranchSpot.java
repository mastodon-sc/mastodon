package org.mastodon.mamut.model.branch;
import org.mastodon.RefPool;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.attributes.IntAttributeValue;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;

public class BranchSpot extends AbstractListenableVertex< BranchSpot, BranchLink, BranchSpotPool, ByteMappedElement >
		implements HasTimepoint, HasLabel, RealLocalizable
{

	private final RefPool< Spot > vertexBimap;

	private final IntAttributeValue spotID;

	protected BranchSpot( final BranchSpotPool vertexPool, final RefPool< Spot > vertexBimap )
	{
		super( vertexPool );
		this.vertexBimap = vertexBimap;
		this.spotID = pool.spotID.createAttributeValue( this );
	}

	@Override
	public String toString()
	{
		final Spot ref = vertexBimap.createRef();
		final String str = vertexBimap.getObject( getLinkedVertexId(), ref ).toString();
		vertexBimap.releaseRef( ref );
		return "bv(" + getInternalPoolIndex() + ") -> " + str;
	}

	protected int getLinkedVertexId()
	{
		return spotID.get();
	}

	protected void setLinkedVertexId( final int id )
	{
		spotID.set( id );
	}

	public BranchSpot init( final Spot spot )
	{
		setLinkedVertexId( vertexBimap.getId( spot ) );
		initDone();
		return this;
	}

	@Override
	protected void setToUninitializedState() throws IllegalStateException
	{
		super.setToUninitializedState();
		setLinkedVertexId( -1 );
	}

	@Override
	public int numDimensions()
	{
		final Spot ref = vertexBimap.createRef();
		final int n = vertexBimap.getObject( getLinkedVertexId(), ref ).numDimensions();
		vertexBimap.releaseRef( ref );
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLinkedVertexId(), ref ).localize( position );
		vertexBimap.releaseRef( ref );
	}

	@Override
	public void localize( final double[] position )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLinkedVertexId(), ref ).localize( position );
		vertexBimap.releaseRef( ref );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		final Spot ref = vertexBimap.createRef();
		final float x = vertexBimap.getObject( getLinkedVertexId(), ref ).getFloatPosition( d );
		vertexBimap.releaseRef( ref );
		return x;
	}

	@Override
	public double getDoublePosition( final int d )
	{
		final Spot ref = vertexBimap.createRef();
		final double x = vertexBimap.getObject( getLinkedVertexId(), ref ).getDoublePosition( d );
		vertexBimap.releaseRef( ref );
		return x;
	}

	@Override
	public int getTimepoint()
	{
		final Spot ref = vertexBimap.createRef();
		final int t = vertexBimap.getObject( getLinkedVertexId(), ref ).getTimepoint();
		vertexBimap.releaseRef( ref );
		return t;
	}

	@Override
	public String getLabel()
	{
		final Spot ref = vertexBimap.createRef();
		final String label = vertexBimap.getObject( getLinkedVertexId(), ref ).getLabel();
		vertexBimap.releaseRef( ref );
		return label;
	}

	@Override
	public void setLabel( final String label )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLinkedVertexId(), ref ).setLabel( label );
		vertexBimap.releaseRef( ref );
	}
}
