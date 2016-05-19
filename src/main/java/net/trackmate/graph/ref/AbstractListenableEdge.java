package net.trackmate.graph.ref;

import net.trackmate.pool.MappedElement;

/**
 * TODO: javadoc
 *
 * @param <V>
 * @param <E>
 * @param <T>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractListenableEdge< E extends AbstractListenableEdge< E, V, T >, V extends AbstractVertex< V, ?, ? >, T extends MappedElement >
		extends AbstractEdge< E, V, T >
{
	protected AbstractListenableEdge( final AbstractEdgePool< E, V, T > pool )
	{
		super( pool );
	}

	NotifyPostInit< ?, E > notifyPostInit;

	/**
	 * Deriving classes need to have {@code init(...)} methods, which should
	 * call this as the final step.
	 */
	@SuppressWarnings( "unchecked" )
	protected void initDone()
	{
		notifyPostInit.notifyEdgeAdded( ( E ) this );
	}
}
