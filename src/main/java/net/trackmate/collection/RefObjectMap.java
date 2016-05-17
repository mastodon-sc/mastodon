package net.trackmate.collection;

import java.util.Map;

/**
 * Map-like interface for maps that map possibly reusable references to plain
 * objects.
 *
 * @param <O>
 *            key type.
 * @param <V>
 *            value type.
 *
 * @author Jean-Yves Tinevez
 */
public interface RefObjectMap< O, V > extends Map< O, V >
{
	public O createKeyRef();

	public void releaseKeyRef( final O obj );
}
