package org.mastodon.views.bdv;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import org.scijava.ui.behaviour.util.Behaviours;

public interface BehaviourTransformEventHandlerMamut extends TransformEventHandler< AffineTransform3D >
{
	void install( Behaviours behaviours );
}
