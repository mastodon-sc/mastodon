package org.mastodon.views.bvv.scene;

import java.util.Iterator;
import org.mastodon.RefPool;

// TODO move to mastodon-collection?
interface ModifiableRefPool< O > extends RefPool< O >, Iterable< O >
{
	int size();

	void delete( final O obj );

	O create( final O ref );

	void clear();

	// garbage-free iterator() version
	Iterator< O > iterator( final O obj );
}
