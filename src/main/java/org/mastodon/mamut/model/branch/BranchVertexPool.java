package org.mastodon.mamut.model.branch;

import org.mastodon.RefPool;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.IntAttribute;

public class BranchVertexPool extends AbstractListenableVertexPool< 
		BranchVertex, 
		BranchEdge, 
		ByteMappedElement >
{

	public static class BranchVertexLayout extends AbstractVertexLayout
	{
		final IntField linkedVertexID = intField();
	}

	public static final BranchVertexLayout layout = new BranchVertexLayout();

	protected final IntAttribute< BranchVertex > spotID;

	private final RefPool< Spot > vertexPool;

	BranchVertexPool( final int initialCapacity, final RefPool< Spot > vertexPool )
	{
		super( initialCapacity, layout, BranchVertex.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		this.vertexPool = vertexPool;
		this.spotID = new IntAttribute<>( layout.linkedVertexID, this );
	}

	@Override
	protected BranchVertex createEmptyRef()
	{
		return new BranchVertex( this, vertexPool );
	}
}
