package org.mastodon.views.dbvv;

import net.imglib2.realtransform.AffineTransform3D;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.mastodon.views.bvv.scene.InstancedSpot.SpotDrawingMode;
import tpietzsch.util.MatrixMath;

import static org.mastodon.views.bvv.scene.InstancedSpot.SpotDrawingMode.ELLIPSOIDS;
import static org.mastodon.views.bvv.scene.InstancedSpot.SpotDrawingMode.SPHERES;

public class SceneRenderData
{
	private int timepoint;
	private final AffineTransform3D worldToScreen;
	private final Matrix4f view;
	private final Matrix4f camview;
	private final Matrix4f pv;
	private double dCam;
	private double dClipNear;
	private double dClipFar;
	private double screenWidth;
	private double screenHeight;
	private SpotDrawingMode spotDrawingMode = SPHERES;

	/**
	 * @param timepoint timepoint index
	 */
	public SceneRenderData(
			final int timepoint,
			final AffineTransform3D worldToScreen,
			final double dCam,
			final double dClipNear,
			final double dClipFar,
			final double screenWidth,
			final double screenHeight )
	{
		this.timepoint = timepoint;
		this.worldToScreen = worldToScreen;
		this.dCam = dCam;
		this.dClipNear = dClipNear;
		this.dClipFar = dClipFar;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		view = MatrixMath.affine( worldToScreen, new Matrix4f() );
		camview = MatrixMath.screen( dCam, screenWidth, screenHeight, new Matrix4f() ).mul( view );
		pv = MatrixMath.perspective( dCam, dClipNear, dClipFar, screenWidth, screenHeight, 0, new Matrix4f() ).mul( camview );
	}

	public SceneRenderData()
	{
		worldToScreen = new AffineTransform3D();
		view = new Matrix4f();
		camview = new Matrix4f();
		pv = new Matrix4f();
	}

	public synchronized SceneRenderData copy()
	{
		final SceneRenderData copy = new SceneRenderData();
		copy.set( this );
		return copy;
	}

	public synchronized void set( final SceneRenderData other )
	{
		this.timepoint = other.timepoint;
		this.worldToScreen.set( other.worldToScreen );
		this.view.set( other.view );
		this.camview.set( other.camview );
		this.pv.set( other.pv );
		this.dCam = other.dCam;
		this.dClipNear = other.dClipNear;
		this.dClipFar = other.dClipFar;
		this.screenWidth = other.screenWidth;
		this.screenHeight = other.screenHeight;
		this.spotDrawingMode = other.spotDrawingMode;
	}

	public int getTimepoint()
	{
		return timepoint;
	}

	public AffineTransform3D getWorldToScreen()
	{
		return worldToScreen;
	}

	public Matrix4fc getView()
	{
		return view;
	}

	public Matrix4fc getCamview()
	{
		return camview;
	}

	public Matrix4fc getPv()
	{
		return pv;
	}

	public double getDCam()
	{
		return dCam;
	}

	public double getDClipNear()
	{
		return dClipNear;
	}

	public double getDClipFar()
	{
		return dClipFar;
	}

	public double getScreenWidth()
	{
		return screenWidth;
	}

	public double getScreenHeight()
	{
		return screenHeight;
	}

	public SpotDrawingMode getSpotDrawingMode()
	{
		return spotDrawingMode;
	}
}
