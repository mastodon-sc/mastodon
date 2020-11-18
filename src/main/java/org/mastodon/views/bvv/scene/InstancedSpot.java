package org.mastodon.views.bvv.scene;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.mastodon.views.bvv.scene.HotLoading.ShaderHotLoader;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.shadergen.DefaultShader;
import tpietzsch.shadergen.Shader;
import tpietzsch.shadergen.Uniform1f;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static org.mastodon.views.bvv.scene.InstancedLink.highlight_stuff;

/**
 * Draw instanced ellipsoids.
 * <p>
 * {@code InstancedEllipsoid} sets up vertex buffer and element buffer objects to to draw a tesselated unit sphere.
 * Then {@link #draw} is called with an {@code Ellipsoids} collection to draw as instances of the transformed unit sphere.
 */
public class InstancedSpot
{
	private final int subdivisions;

	private final ShaderHotLoader hotloader;
	private Shader ellipsoidProg;
	private Shader sphereProg;
	private Shader sphereHighlightProg;

	private int sphereVbo;
	private int sphereEbo;
	private int sphereNumElements;

	private final ReusableResources< Ellipsoids, InstanceArray > instanceArrays;

	public InstancedSpot()
	{
		this( 3, 10 );
	}

	public InstancedSpot( final int subdivisions, final int numReusableInstanceArrays )
	{
		this.subdivisions = subdivisions;
		hotloader = new ShaderHotLoader()
				.watch( InstancedSpot.class, "instancedellipsoid.vp" )
				.watch( InstancedSpot.class, "instancedsphere.vp" )
				.watch( InstancedSpot.class, "instancedellipsoid.fp" )
				.watch( InstancedSpot.class, "instancedsphere-highlight.vp" )
				.watch( InstancedSpot.class, "instancedellipsoid-highlight.fp" );
		hotloadShader();
		instanceArrays = new ReusableResources<>( numReusableInstanceArrays, InstanceArray::new );
	}

	private void hotloadShader()
	{
		if ( hotloader.isModified() || ellipsoidProg == null )
		{
			final Segment ex1vp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid.vp" ).instantiate();
			final Segment ex1fp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid.fp" ).instantiate();
			ellipsoidProg = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );

			final Segment ex2vp = new SegmentTemplate( InstancedSpot.class, "instancedsphere.vp" ).instantiate();
			final Segment ex2fp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid.fp" ).instantiate();
			sphereProg = new DefaultShader( ex2vp.getCode(), ex2fp.getCode() );

			final Segment ex3vp = new SegmentTemplate( InstancedSpot.class, "instancedsphere-highlight.vp" ).instantiate();
			final Segment ex3fp = new SegmentTemplate( InstancedSpot.class, "instancedellipsoid-highlight.fp" ).instantiate();
			sphereHighlightProg = new DefaultShader( ex3vp.getCode(), ex3fp.getCode() );
		}
	}

	private boolean initialized;

	private void init( GL3 gl )
	{
		initialized = true;

		final UnitSphere sphere = new UnitSphere( subdivisions );
		final float[] vertices = sphere.getVertices();
		final int[] indices = sphere.getIndices();
		sphereNumElements = sphere.getNumElements();

		final int[] tmp = new int[ 2 ];
		gl.glGenBuffers( 2, tmp, 0 );
		sphereVbo = tmp[ 0 ];
		sphereEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
	}

	public enum SpotDrawingMode
	{
		ELLIPSOIDS,
		SPHERES
	}

	class InstanceArray extends ReusableResource< Ellipsoids >
	{
		private int instanceCount;
		private int vboShape;
		private int vboColor;
		private int ellipsoidVao;
		private int sphereVao;

		private boolean initialized;

		private void init( GL3 gl )
		{
			initialized = true;

			final int[] tmp = new int[ 2 ];
			gl.glGenBuffers( 2, tmp, 0 );
			vboShape = tmp[ 0 ];
			vboColor = tmp[ 1 ];
			gl.glGenVertexArrays( 2, tmp, 0 );
			ellipsoidVao = tmp[ 0 ];
			sphereVao = tmp[ 1 ];

			gl.glBindVertexArray( ellipsoidVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
			for ( int i = 0; i < 7; ++i )
			{
				gl.glVertexAttribPointer( 1 + i, 3, GL_FLOAT, false, 21 * Float.BYTES, i * 3 * Float.BYTES );
				gl.glEnableVertexAttribArray( 1 + i );
				gl.glVertexAttribDivisor( 1 + i, 1 );
			}
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
			gl.glVertexAttribPointer( 8, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 8 );
			gl.glVertexAttribDivisor( 8, 1 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
			gl.glBindVertexArray( 0 );

			gl.glBindVertexArray( sphereVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
			gl.glVertexAttribPointer( 1, 3, GL_FLOAT, false, 21 * Float.BYTES, 18 * Float.BYTES );
			gl.glEnableVertexAttribArray( 1 );
			gl.glVertexAttribDivisor( 1, 1 );
			gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
			gl.glVertexAttribPointer( 2, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 2 );
			gl.glVertexAttribDivisor( 2, 1 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
			gl.glBindVertexArray( 0 );
		}

		private void update( GL3 gl )
		{
			boolean updateShape = needsUpdate.getAndSet( false );
			boolean updateColor = updateShape;

			final Ellipsoids ellipsoids = this.key();
			updateShape |= ellipsoids.shapes.getAndClearModified();
			updateColor |= ellipsoids.colors.getAndClearModified();

			if ( updateShape )
			{
				final FloatBuffer data = ellipsoids.shapes.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
				instanceCount = ellipsoids.size();
			}

			if ( updateColor )
			{
				final FloatBuffer data = ellipsoids.colors.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
			}
		}

		private void draw( GL3 gl, JoglGpuContext context, SpotDrawingMode mode, boolean onlyHighlights )
		{
			if ( !initialized )
				init( gl );

			update( gl );

			switch ( mode )
			{
			case ELLIPSOIDS:
				ellipsoidProg.use( context );
				gl.glBindVertexArray( ellipsoidVao );
				gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
				gl.glBindVertexArray( 0 );
				break;
			case SPHERES:
				if ( onlyHighlights )
					sphereHighlightProg.use( context );
				else
					sphereProg.use( context );
				gl.glBindVertexArray( sphereVao );
				gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
				gl.glBindVertexArray( 0 );
				break;
			}
		}
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4fc vm, Ellipsoids ellipsoids,
			final int highlightIndex,
			final SpotDrawingMode spotDrawingMode,
			final float spotRadius,
			final boolean onlyHighlights )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );
		hotloadShader();

		final Matrix3f itvm = vm.invert( new Matrix4f() ).transpose().get3x3( new Matrix3f() );

		if ( onlyHighlights )
		{
			sphereHighlightProg.getUniformMatrix4f( "pvm" ).set( pvm );
			sphereHighlightProg.getUniformMatrix4f( "vm" ).set( vm );
			sphereHighlightProg.getUniformMatrix3f( "itvm" ).set( itvm );
			sphereHighlightProg.getUniform1f( "radius" ).set( spotRadius );
			highlight_stuff( pvm, vm, itvm,
					sphereHighlightProg.getUniform1f( "highlight_f" ),
					sphereHighlightProg.getUniform1f( "highlight_k" ) );
			sphereHighlightProg.setUniforms( context );
		}
		else
		{
			ellipsoidProg.getUniformMatrix4f( "pvm" ).set( pvm );
			ellipsoidProg.getUniformMatrix4f( "vm" ).set( vm );
			ellipsoidProg.getUniformMatrix3f( "itvm" ).set( itvm );
			ellipsoidProg.getUniform1i( "highlight" ).set( highlightIndex );
			ellipsoidProg.setUniforms( context );

			sphereProg.getUniformMatrix4f( "pvm" ).set( pvm );
			sphereProg.getUniformMatrix4f( "vm" ).set( vm );
			sphereProg.getUniformMatrix3f( "itvm" ).set( itvm );
			sphereProg.getUniform1i( "highlight" ).set( highlightIndex );
			sphereProg.getUniform1f( "radius" ).set( spotRadius );
			highlight_stuff( pvm, vm, itvm,
					sphereProg.getUniform1f( "highlight_f" ),
					sphereProg.getUniform1f( "highlight_k" ) );
			sphereProg.setUniforms( context );
		}

		instanceArrays.get( ellipsoids ).draw( gl, context, spotDrawingMode, onlyHighlights );
	}

	public static void highlight_stuff( Matrix4fc pvm, Matrix4fc vm, Matrix3fc itvm, final Uniform1f highlight_f, final Uniform1f highlight_k )
	{
		final int viewportWidth = 800; // TODO!

		final Matrix4f ipvm = pvm.invert( new Matrix4f() );
		final float dx = ( float ) ( 2.0 / viewportWidth );

		final Vector4f adx = vm.transform( ipvm.transform( new Vector4f( dx, 0, -1, 1 ) ) );
		final Vector4f cdx = vm.transform( ipvm.transform( new Vector4f( dx, 0,  1, 1 ) ) );
		adx.div( adx.w() );
		cdx.div( cdx.w() );

		final float sNear = adx.x;
		final float sFar = cdx.x;
		final float ac = cdx.z - adx.z;
		final float f = ( sFar - sNear ) / ac;
		final float k = sNear - f * adx.z;

		final float s = ( float ) Math.sqrt( itvm.m00() * itvm.m00() + itvm.m01() * itvm.m01() + itvm.m02() * itvm.m02() );

		highlight_f.set( s * f );
		highlight_k.set( s * k );
	}
}
