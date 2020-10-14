package org.mastodon.views.bvv.scene;

import org.mastodon.Ref;

// TODO move to mastodon-collection?
interface ModifiableRef< O extends ModifiableRef< O > > extends Ref< O >
{
	void set( final O obj );
}
