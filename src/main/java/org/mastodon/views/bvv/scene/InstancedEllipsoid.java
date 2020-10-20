package org.mastodon.views.bvv.scene;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.mastodon.views.bvv.scene.HotLoading.ShaderHotLoader;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.shadergen.DefaultShader;
import tpietzsch.shadergen.Shader;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

/**
 * Draw instanced ellipsoids.
 * <p>
 * {@code InstancedEllipsoid} sets up vertex buffer and element buffer objects to to draw a tesselated unit sphere.
 * Then {@link #draw} is called with an {@code Ellipsoids} collection to draw as instances of the transformed unit sphere.
 */
public class InstancedEllipsoid
{
	private final int subdivisions;

	private final ShaderHotLoader hotloader;
	private Shader prog;

	private int sphereVbo;
	private int sphereEbo;
	private int sphereNumElements;

	private final ReusableResources< Ellipsoids, InstanceArray > instanceArrays;

	public InstancedEllipsoid()
	{
		this( 3, 10 );
	}

	public InstancedEllipsoid( final int subdivisions, final int numReusableInstanceArrays )
	{
		this.subdivisions = subdivisions;
		hotloader = new ShaderHotLoader()
				.watch( InstancedEllipsoid.class, "instancedellipsoid.vp" )
				.watch( InstancedEllipsoid.class, "instancedellipsoid.fp" );
		hotloadShader();
		instanceArrays = new ReusableResources<>( numReusableInstanceArrays, InstanceArray::new );
	}

	private void hotloadShader()
	{
		if ( hotloader.isModified() || prog == null )
		{
			final Segment ex1vp = new SegmentTemplate( InstancedEllipsoid.class, "instancedellipsoid.vp" ).instantiate();
			final Segment ex1fp = new SegmentTemplate( InstancedEllipsoid.class, "instancedellipsoid.fp" ).instantiate();
			prog = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );
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

	class InstanceArray extends ReusableResource< Ellipsoids >
	{
		private int instanceCount;
		private int vboShape;
		private int vboColor;
		private int vao;

		private boolean initialized;

		private void init( GL3 gl )
		{
			initialized = true;

			final int[] tmp = new int[ 2 ];
			gl.glGenBuffers( 2, tmp, 0 );
			vboShape = tmp[ 0 ];
			vboColor = tmp[ 1 ];
			gl.glGenVertexArrays( 1, tmp, 0 );
			vao = tmp[ 0 ];

			gl.glBindVertexArray( vao );
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

		private void draw( GL3 gl )
		{
			if ( !initialized )
				init( gl );

			update( gl );

			gl.glBindVertexArray( vao );
			gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
			gl.glBindVertexArray( 0 );
		}
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4f vm, Ellipsoids ellipsoids, final int highlightIndex )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );
		hotloadShader();

		final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();
		prog.getUniformMatrix4f( "pvm" ).set( pvm );
		prog.getUniformMatrix4f( "vm" ).set( vm );
		prog.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
		prog.getUniform1i( "highlight" ).set( highlightIndex );
		prog.getUniform3f( "color" ).set( 0.5f, 1.0f, 0.5f );
		prog.setUniforms( context );
		prog.use( context );

		instanceArrays.get( ellipsoids ).draw( gl );
	}
}
