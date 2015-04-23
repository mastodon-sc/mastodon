package net.trackmate.graph.collection;

import java.util.Collection;

/**
 * TODO
 * 
 * @param <O>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface RefCollection< O > extends Collection< O >
{
	/**
	 * TODO
	 * 
	 * @return
	 */
	public O createRef();

	/**
	 * TODO
	 * 
	 * @param obj
	 */
	public void releaseRef( final O obj );
}
