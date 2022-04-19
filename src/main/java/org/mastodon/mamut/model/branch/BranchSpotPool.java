package org.mastodon.mamut.model.branch;

import org.mastodon.RefPool;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.IntAttribute;

public class BranchSpotPool extends AbstractListenableVertexPool< 
		BranchSpot, 
		BranchLink, 
		ByteMappedElement >
{

	public static class BranchVertexLayout extends AbstractVertexLayout
	{
		final IntField linkedVertexID = intField();
	}

	public static final BranchVertexLayout layout = new BranchVertexLayout();

	protected final IntAttribute< BranchSpot > spotID;

	private final RefPool< Spot > vertexPool;

	BranchSpotPool( final int initialCapacity, final RefPool< Spot > vertexPool )
	{
		super( initialCapacity, layout, BranchSpot.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		this.vertexPool = vertexPool;
		this.spotID = new IntAttribute<>( layout.linkedVertexID, this );
	}

	@Override
	protected BranchSpot createEmptyRef()
	{
		return new BranchSpot( this, vertexPool );
	}
}
