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
 * Draw instanced cylinders.
 * TODO
 * <p>
 * {@code InstancedCylinder} sets up vertex buffer and element buffer objects to to draw a unit cylinder.
 * Then {@link #draw} is called with a {@code Cylinders} collection to draw a set of cylindrical frustums as instances of the transformed unit cylinder.
 */
public class InstancedLink
{
	private final int cylinderSubdivisions;
	private final int sphereSubdivisions;

	private final ShaderHotLoader hotloader;
	private Shader cylinderProg;
	private Shader sphereProg;

	private int cylinderVbo;
	private int cylinderEbo;
	private int cylinderNumElements;

	private int sphereVbo;
	private int sphereEbo;
	private int sphereNumElements;

	private final ReusableResources< Cylinders, InstanceArray > instanceArrays;

	public InstancedLink()
	{
		this( 36, 3, 20 );
	}

	public InstancedLink( final int cylinderSubdivisions, final int sphereSubdivisions, final int numReusableInstanceArrays  )
	{
		this.cylinderSubdivisions = cylinderSubdivisions;
		this.sphereSubdivisions = sphereSubdivisions;
		hotloader = new ShaderHotLoader()
				.watch( InstancedLink.class, "instancedcylinder.vp" )
				.watch( InstancedLink.class, "instancedsphere.vp" )
				.watch( InstancedLink.class, "instancedellipsoid.fp" );
		hotloadShader();
		instanceArrays = new ReusableResources<>( numReusableInstanceArrays, InstanceArray::new );
	}

	private void hotloadShader()
	{
		if ( hotloader.isModified() || cylinderProg == null )
		{
			final Segment ex1vp = new SegmentTemplate( InstancedLink.class, "instancedcylinder.vp" ).instantiate();
			final Segment ex1fp = new SegmentTemplate( InstancedLink.class, "instancedellipsoid.fp" ).instantiate();
			cylinderProg = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );

			final Segment ex2vp = new SegmentTemplate( InstancedLink.class, "instancedsphere.vp" ).instantiate();
			final Segment ex2fp = new SegmentTemplate( InstancedLink.class, "instancedellipsoid.fp" ).instantiate();
			sphereProg = new DefaultShader( ex2vp.getCode(), ex2fp.getCode() );
		}
	}

	private boolean initialized;

	private void init( GL3 gl )
	{
		initialized = true;

		final UnitCylinder cylinder = new UnitCylinder( cylinderSubdivisions );
		final float[] cylinderVertices = cylinder.getVertices();
		final int[] cylinderIndices = cylinder.getIndices();
		cylinderNumElements = cylinder.getNumElements();

		final int[] tmp = new int[ 2 ];
		gl.glGenBuffers( 2, tmp, 0 );
		cylinderVbo = tmp[ 0 ];
		cylinderEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, cylinderVertices.length * Float.BYTES, FloatBuffer.wrap( cylinderVertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, cylinderIndices.length * Integer.BYTES, IntBuffer.wrap( cylinderIndices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		final UnitSphere sphere = new UnitSphere( sphereSubdivisions );
		final float[] sphereVertices = sphere.getVertices();
		final int[] sphereIndices = sphere.getIndices();
		sphereNumElements = sphere.getNumElements();

		gl.glGenBuffers( 2, tmp, 0 );
		sphereVbo = tmp[ 0 ];
		sphereEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, sphereVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, sphereVertices.length * Float.BYTES, FloatBuffer.wrap( sphereVertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, sphereEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, sphereIndices.length * Integer.BYTES, IntBuffer.wrap( sphereIndices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
	}

	class InstanceArray extends ReusableResource< Cylinders >
	{
		private int instanceCount;
		private int vboShape;
		private int vboColor;
		private int cylinderVao;
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
			cylinderVao = tmp[ 0 ];
			sphereVao = tmp[ 1 ];

			gl.glBindVertexArray( cylinderVao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
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
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
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

			final Cylinders cylinders = this.key();
			updateShape |= cylinders.shapes.getAndClearModified();
			updateColor |= cylinders.colors.getAndClearModified();

			if ( updateShape )
			{
				final FloatBuffer data = cylinders.shapes.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboShape );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
				instanceCount = cylinders.size();
			}

			if ( updateColor )
			{
				final FloatBuffer data = cylinders.colors.buffer().asFloatBuffer();
				final int size = data.limit();
				gl.glBindBuffer( GL_ARRAY_BUFFER, vboColor );
				gl.glBufferData( GL_ARRAY_BUFFER, size * Float.BYTES, data, GL_DYNAMIC_DRAW );
				gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
			}
		}

		private void draw( GL3 gl, JoglGpuContext context )
		{
			if ( !initialized )
				init( gl );

			update( gl );

			cylinderProg.use( context );
			gl.glBindVertexArray( cylinderVao );
			gl.glDrawElementsInstanced( GL_TRIANGLES, cylinderNumElements, GL_UNSIGNED_INT, 0, instanceCount );
			gl.glBindVertexArray( 0 );

			sphereProg.use( context );
			gl.glBindVertexArray( sphereVao );
			gl.glDrawElementsInstanced( GL_TRIANGLES, sphereNumElements, GL_UNSIGNED_INT, 0, instanceCount );
			gl.glBindVertexArray( 0 );
		}
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4f vm, final Cylinders cylinders, final int highlightIndex, final double r0, final double r1 )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );
		hotloadShader();

		final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();
		cylinderProg.getUniformMatrix4f( "pvm" ).set( pvm );
		cylinderProg.getUniformMatrix4f( "vm" ).set( vm );
		cylinderProg.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
		cylinderProg.getUniform1i( "highlight" ).set( highlightIndex );
		cylinderProg.getUniform2f( "radii" ).set( ( float ) r0, ( float ) r1 );
		cylinderProg.setUniforms( context );

		sphereProg.getUniformMatrix4f( "pvm" ).set( pvm );
		sphereProg.getUniformMatrix4f( "vm" ).set( vm );
		sphereProg.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
		sphereProg.getUniform1i( "highlight" ).set( highlightIndex );
		sphereProg.getUniform2f( "radii" ).set( ( float ) r0, ( float ) r1 );
		sphereProg.setUniforms( context );

		instanceArrays.get( cylinders ).draw( gl, context );
	}
}
