package org.mastodon.mamut.views.grapher;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.grapher.display.DataDisplayFrame;

public interface DataDisplayFrameSupplier< V extends Vertex< E > & HasTimepoint & HasLabel & Ref< V >, E extends Edge< V > & Ref< E > >
{
	DataDisplayFrame< V, E > getFrame();
}
