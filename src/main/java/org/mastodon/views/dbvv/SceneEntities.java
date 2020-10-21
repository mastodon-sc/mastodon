package org.mastodon.views.dbvv;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.views.bvv.scene.Cylinders;
import org.mastodon.views.bvv.scene.Ellipsoids;

public class SceneEntities
{
	final DColoredEllipsoids ellipsoids;

	final DColoredCylinders cylinders;

	public SceneEntities( final ModelGraph graph )
	{
		ellipsoids = new DColoredEllipsoids( graph );
		cylinders = new DColoredCylinders( graph );
	}

	public Ellipsoids getEllipsoids()
	{
		return ellipsoids.getEllipsoids();
	}

	public Cylinders getCylinders()
	{
		return cylinders.getCylinders();
	}
}
