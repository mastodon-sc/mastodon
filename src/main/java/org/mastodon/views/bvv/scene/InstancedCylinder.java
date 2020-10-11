package org.mastodon.views.bvv.scene;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
 * <p>
 * {@ode InstancedCylinder} sets up vertex buffer and element buffer objects to to draw a unit cylinder.
 * Then {@link #draw} is called with an {@code InstanceArray} to draw a set of cylindrical frustums as instances of the transformed unit cylinder.
 * <p>
 * TODO
 * {@code InstanceArray}s can be created using {@link #createInstanceArray()} and filled with data using {@code updateShapes()} and {@code updateColors()}.
 * {@code InstanceArray} has a modCount field that is used (externally) to keep track of whether the ellipsoid data is up to date.
 */
public class InstancedCylinder
{
	private final int subdivisions;

	private final Shader prog;

	private int cylinderVbo;
	private int cylinderEbo;
	private int cylinderNumElements;
	private InstanceArray instanceArray;

	public InstancedCylinder( final int subdivisions )
	{
		this.subdivisions = subdivisions;
		final Segment ex1vp = new SegmentTemplate( InstancedCylinder.class, "instancedcylinder.vp" ).instantiate();
		final Segment ex1fp = new SegmentTemplate( InstancedCylinder.class, "instancedcylinder.fp" ).instantiate();
		prog = new DefaultShader( ex1vp.getCode(), ex1fp.getCode() );
	}

	private boolean initialized;

	private void init( GL3 gl )
	{
		initialized = true;

		final UnitCylinder cylinder = new UnitCylinder( subdivisions );
		final float[] vertices = cylinder.getVertices();
		final int[] indices = cylinder.getIndices();
		cylinderNumElements = cylinder.getNumElements();

		final int[] tmp = new int[ 2 ];
		gl.glGenBuffers( 2, tmp, 0 );
		cylinderVbo = tmp[ 0 ];
		cylinderEbo = tmp[ 1 ];

		gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
		gl.glBufferData( GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		instanceArray = new InstanceArray();
	}

	public InstanceArray createInstanceArray()
	{
		return new InstanceArray();
	}

	public class InstanceArray implements HasModCount
	{
		@Override
		public int getModCount()
		{
			return modCount;
		}

		@Override
		public void setModCount( final int modCount )
		{
			this.modCount = modCount;
		}

		private int modCount = -1;

		private int vao;

		private boolean initialized;

		private void init( GL3 gl )
		{
			initialized = true;

			if ( !InstancedCylinder.this.initialized )
				InstancedCylinder.this.init( gl );

			final int[] tmp = new int[ 2 ];
			gl.glGenVertexArrays( 1, tmp, 0 );
			vao = tmp[ 0 ];

			gl.glBindVertexArray( vao );
			gl.glBindBuffer( GL_ARRAY_BUFFER, cylinderVbo );
			gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
			gl.glEnableVertexAttribArray( 0 );
			gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, cylinderEbo );
			gl.glBindVertexArray( 0 );
		}

		private void draw( GL3 gl )
		{
			if ( !initialized )
				init( gl );

			gl.glBindVertexArray( vao );
			gl.glDrawElements( GL_TRIANGLES, cylinderNumElements, GL_UNSIGNED_INT, 0 );
			gl.glBindVertexArray( 0 );
		}
	}

	public void draw( GL3 gl, Matrix4fc pvm, Matrix4f vm )
	{
		if ( !initialized )
			init( gl );

		JoglGpuContext context = JoglGpuContext.get( gl );

		final Matrix4f itvm = vm.invert( new Matrix4f() ).transpose();
		prog.getUniformMatrix4f( "pvm" ).set( pvm );
		prog.getUniformMatrix4f( "vm" ).set( vm );
		prog.getUniformMatrix3f( "itvm" ).set( itvm.get3x3( new Matrix3f() ) );
		prog.setUniforms( context );
		prog.use( context );

		instanceArray.draw( gl );
	}
}
