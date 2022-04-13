package org.mastodon.views.bdv.overlay;

import java.util.Iterator;

public interface OverlayBranchGraph< 
	BV extends OverlayVertex< BV, BE >, 
	BE extends OverlayEdge< BE, BV >,
	V extends OverlayVertex< V, E >, 
	E extends OverlayEdge< E, V >  > 
		extends OverlayGraph< BV, BE >
{

	/**
	 * Returns the edge linked to the specified branch edge. The linked edge is
	 * the single outgoing edge of the branch extremity.
	 *
	 * @param be
	 *            the branch edge.
	 * @param ref
	 *            a reference to a linked graph edge used for retrieval.
	 *            Depending on concrete implementation of the linked graph, this
	 *            object can be cleared, ignored or re-used.
	 * @return the linked edge.
	 */
	public E getLinkedEdge( final BE be, final E ref );

	/**
	 * Returns the vertex linked to the specified branch vertex. The linked
	 * vertex is a branch extremity.
	 *
	 * @param bv
	 *            the branch vertex.
	 * @param ref
	 *            a reference to a linked graph vertex used for retrieval.
	 *            Depending on concrete implementation of the linked graph, this
	 *            object can be cleared, ignored or re-used.
	 * @return the linked vertex.
	 */
	public V getLinkedVertex( final BV bv, final V ref );

	/**
	 * Returns the branch edge linked to the specified edge.
	 *
	 * @param edge
	 *            the linked edge.
	 * @param ref
	 *            a reference object to the branch edge used for retrieval.
	 * @return a branch edge.
	 */
	public BE getBranchEdge( final E edge, final BE ref );

	/**
	 * Returns the branch vertex linked to the specified edge if it belongs to a
	 * branch. Returns <code>null</code> if the specified vertex is a branch
	 * extremity.
	 *
	 * @param vertex
	 *            the linked vertex.
	 * @param ref
	 *            a reference object to the branch edge used for retrieval.
	 * @return a branch edge or <code>null</code>.
	 */
	public BE getBranchEdge( final V vertex, final BE ref );

	/**
	 * Returns the branch vertex linked to the specified vertex if it is a
	 * branch extremity. Returns <code>null</code> if the specified vertex
	 * belongs to inside a branch.
	 *
	 * @param vertex
	 *            the linked vertex.
	 * @param ref
	 *            a reference object to the branch vertex used for retrieval.
	 * @return a branch vertex or <code>null</code>.
	 */
	public BV getBranchVertex( final V vertex, BV ref );

	/**
	 * Returns an iterator that iterates in order over the linked vertices in a
	 * branch, specified by its branch edge. The first and last vertex iterated
	 * are the branch nodes.
	 *
	 * @param edge
	 *            the branch edge.
	 * @return a new iterator.
	 */
	public Iterator< V > vertexBranchIterator( BE edge );

	/**
	 * Returns an iterator that iterates in order over the linked edges of a
	 * branch, specified by its branch edge.
	 *
	 * @param edge
	 *            the branch edge.
	 * @return a new iterator.
	 */
	public Iterator< E > edgeBranchIterator( BE edge );
}
