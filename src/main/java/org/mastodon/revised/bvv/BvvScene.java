package org.mastodon.revised.bvv;

import com.jogamp.opengl.GL3;
import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.bvv.scene.InstancedEllipsoid;
import tpietzsch.example2.VolumeViewerPanel.RenderData;
import tpietzsch.example2.VolumeViewerPanel.RenderScene;
import tpietzsch.util.MatrixMath;

public class BvvScene< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
	implements RenderScene
{
	private final BvvGraph< V, E > graph;

	private final SelectionModel< V, E > selection;

	private final HighlightModel< V, E > highlight;

	private final InstancedEllipsoid instancedEllipsoid;

	private final ReusableInstanceArrays< InstancedEllipsoid.InstanceArray > reusableInstanceArrays;

	public BvvScene(
			final BvvGraph< V, E > graph,
			final SelectionModel< V, E > selection,
			final HighlightModel< V, E > highlight )
	{
		this.graph = graph;
		this.selection = selection;
		this.highlight = highlight;
		instancedEllipsoid = new InstancedEllipsoid( 3 );

		final int numInstanceArrays = 10;
		reusableInstanceArrays = new ReusableInstanceArrays<>(
				t -> graph.getEllipsoids().forTimepoint( t ).getModCount(),
				numInstanceArrays,
				instancedEllipsoid::createInstanceArray
		);

		selection.listeners().add( this::selectionChanged );
	}

	@Override
	public void render(
			final GL3 gl,
			final RenderData data )
	{
		final AffineTransform3D worldToScreen = data.getRenderTransformWorldToScreen();
		final int timepoint = data.getTimepoint();
		final double dCam = data.getDCam();
		final double screenWidth = data.getScreenWidth();
		final double screenHeight = data.getScreenHeight();
		final Matrix4f pv = data.getPv();

		final Matrix4f view = MatrixMath.affine( worldToScreen, new Matrix4f() );
		final Matrix4f camview = new Matrix4f()
			.translation( ( float ) ( -( screenWidth - 1 ) / 2 ), ( float ) ( -( screenHeight - 1 ) / 2 ), ( float ) dCam )
			.mul( view );

		final InstancedEllipsoid.InstanceArray instanceArray = reusableInstanceArrays.getForTimepoint( timepoint );
		final EllipsoidInstances< V, E > instances = graph.getEllipsoids().forTimepoint( timepoint );
		final int modCount = instances.getModCount();
		final boolean needShapeUpdate = instanceArray.getModCount() != modCount;
		if ( needShapeUpdate )
		{
			instanceArray.setModCount( modCount );
			instanceArray.updateShapes( gl, instances.buffer().asFloatBuffer() );
		}

		final boolean needColorUpdate = instances.getColorModCount() != colorModCount;
		if ( needColorUpdate )
		{
			instances.setColorModCount( colorModCount );
			final Vector3f defaultColor = new Vector3f( 0.5f, 1.0f, 0.5f );
			final Vector3f selectedColor = new Vector3f( 1.0f, 0.7f, 0.7f );
			instances.updateColors( v -> selection.isSelected( v ) ? selectedColor : defaultColor );
		}

		if ( needShapeUpdate || needColorUpdate )
		{
			instanceArray.updateColors( gl, instances.colorBuffer().asFloatBuffer() );
		}

		final V vref = graph.vertexRef();
		final V vertex = highlight.getHighlightedVertex( vref );
		final int highlightId = instances.indexOf( vertex );
		graph.releaseRef( vref );
		instancedEllipsoid.draw( gl, pv, camview, instanceArray, highlightId );
	}

	private int colorModCount = 1;

	private void selectionChanged()
	{
		++colorModCount;
	}
}
