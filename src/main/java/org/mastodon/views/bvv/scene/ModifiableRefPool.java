package org.mastodon.views.bvv.scene;

import org.mastodon.RefPool;

// TODO move to mastodon-collection?
public interface ModifiableRefPool< O > extends RefPool< O >
{
	int size();

	void delete( final O obj );

	O create( final O ref );
}
