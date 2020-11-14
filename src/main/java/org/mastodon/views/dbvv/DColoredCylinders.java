package org.mastodon.views.dbvv;

import java.util.function.Function;
import org.joml.Vector3fc;
import org.mastodon.RefPool;
import org.mastodon.collection.RefCollections;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.views.bvv.BvvRenderer;
import org.mastodon.views.bvv.scene.Cylinder;
import org.mastodon.views.bvv.scene.CylinderMath;
import org.mastodon.views.bvv.scene.Cylinders;

public class DColoredCylinders
{
	private final ModelGraph graph;

	private final Cylinders cylinders = new Cylinders();

	private final CylinderMath math;

	/**
	 * Tracks potential modifications to cylinder colors because of {@code selectionChanged()} events, etc.
	 * These events do not immediately trigger a color update, because it would need to go through all timepoints whether or not they are currently painted.
	 * Rather, the {@link BvvRenderer} actively does the update when required.
	 */
	private int colorModCount = -1;

	public DColoredCylinders( final ModelGraph graph )
	{
		this.graph = graph;
		math = new CylinderMath( graph );
	}

	public Cylinders getCylinders()
	{
		return cylinders;
	}

	public void addOrUpdate( final Link edge )
	{
		colorModCount = -1;
		final Cylinder ref = cylinders.createRef();
		final int key = graph.getGraphIdBimap().getEdgeId( edge );
		math.setFromEdge( edge, cylinders.getOrAdd( key, ref ) );
		cylinders.releaseRef( ref );
	}

	public void remove( final Link edge )
	{
		final int key = graph.getGraphIdBimap().getEdgeId( edge );
		cylinders.remove( key );
	}

	/**
	 * Returns the index of the cylinder corresponding to {@code edge}
	 * if {@code edge} is represented in {@code cylinders}.
	 * Otherwise, returns -1.
	 */
	public int indexOf( final Link edge )
	{
		if ( edge == null )
			return -1;

		final Cylinder ref = cylinders.createRef();
		try
		{
			final int key = graph.getGraphIdBimap().getEdgeId( edge );
			final Cylinder cylinder = cylinders.get( key, ref );
			return ( cylinder == null ) ? -1 : cylinder.getInternalPoolIndex();
		}
		finally
		{
			cylinders.releaseRef( ref );
		}
	}

	public void updateColors( final int modCount, final Function< Link, Vector3fc > coloring )
	{
		if ( colorModCount != modCount )
		{
			colorModCount = modCount;
			final RefPool< Link > edgePool = RefCollections.tryGetRefPool( graph.edges() );
			final Link ref = edgePool.createRef();
			for ( Cylinder cylinder : cylinders )
			{
				final Link edge = edgePool.getObjectIfExists( cylinders.keyOf( cylinder ), ref );
				if ( edge != null )
					cylinder.rgb.set( coloring.apply( edge ) );
			}
			edgePool.releaseRef( ref );
		}
	}
}
