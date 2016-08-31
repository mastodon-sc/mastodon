package org.mastodon.collection;

import java.util.Set;

/**
 * A {@link Set} that is a {@link RefCollection}.
 *
 * @param <O>
 *            the type of elements maintained by this set.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefSet< O > extends RefCollection< O >, Set< O >
{}
