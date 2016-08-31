package org.mastodon.collection;

import java.util.Map;

/**
 * Map-like interface for maps that map possibly reusable references to plain
 * objects.
 *
 * @param <K>
 *            key type.
 * @param <V>
 *            value type.
 *
 * @author Jean-Yves Tinevez
 */
public interface RefObjectMap< K, V > extends Map< K, V >
{
	public K createKeyRef();

	public void releaseKeyRef( final K obj );
}
