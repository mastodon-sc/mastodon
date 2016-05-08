package net.trackmate.revised.undo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * Partially stolen from {@link ByteArrayOutputStream} and
 * {@link ByteArrayInputStream}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
class UndoDataStack
{
	public final WrappedDataOutput out;

	public final WrappedDataInput in;

	public UndoDataStack( final int capacity )
	{
		buf = new byte[ capacity ];
		out = new WrappedDataOutput( new DataOutputStream( new Out() ) );
		in = new WrappedDataInput( new DataInputStream( new In() ) );
	}

	/**
	 * Called before writing data for an {@link UndoableEdit}.
	 *
	 * @return
	 */
	public int getNextDataIndex()
	{
		return wcount;
	}

	/**
	 * Called before reading data for an {@link UndoableEdit}.
	 *
	 * @param i
	 */
	public void setDataIndex( final long i )
	{
		rcount = wcount;
		wcount = pos = ( int ) i;
	}

	/**
	 * The buffer where data is stored.
	 */
	private byte buf[];

	/**
	 * The number of valid bytes in the buffer.
	 * The write pos of the {@link OutputStream}.
	 */
	private int wcount;

	/**
	 * The number of valid bytes in the current data portion.
	 * The buffer end for the {@link InputStream}.
	 */
	private int rcount;

	/**
	 * The read pos of the {@link InputStream}.
	 */
	private int pos;

	/**
	 * Increases the capacity if necessary to ensure that it can hold at
	 * least the number of elements specified by the minimum capacity
	 * argument.
	 *
	 * @param minCapacity
	 *            the desired minimum capacity
	 * @throws OutOfMemoryError
	 *             if {@code minCapacity < 0}. This is interpreted as a
	 *             request for the unsatisfiably large capacity
	 *             {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
	 */
	private void ensureCapacity( final int minCapacity )
	{
		// overflow-conscious code
		if ( minCapacity - buf.length > 0 )
			grow( minCapacity );
	}

	/**
	 * The maximum size of array to allocate. Some VMs reserve some header
	 * words in an array. Attempts to allocate larger arrays may result in
	 * OutOfMemoryError: Requested array size exceeds VM limit
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Increases the capacity to ensure that it can hold at least the number
	 * of elements specified by the minimum capacity argument.
	 *
	 * @param minCapacity
	 *            the desired minimum capacity
	 */
	private void grow( final int minCapacity )
	{
		// overflow-conscious code
		final int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if ( newCapacity - minCapacity < 0 )
			newCapacity = minCapacity;
		if ( newCapacity - MAX_ARRAY_SIZE > 0 )
			newCapacity = hugeCapacity( minCapacity );
		buf = Arrays.copyOf( buf, newCapacity );
	}

	private static int hugeCapacity( final int minCapacity )
	{
		if ( minCapacity < 0 ) // overflow
			throw new OutOfMemoryError();
		return ( minCapacity > MAX_ARRAY_SIZE ) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	private class Out extends OutputStream
	{
		/**
		 * Writes the specified byte to this byte array output stream.
		 *
		 * @param b
		 *            the byte to be written.
		 */
		@Override
		public void write( final int b )
		{
			ensureCapacity( wcount + 1 );
			buf[ wcount ] = ( byte ) b;
			wcount += 1;
		}

		/**
		 * Writes <code>len</code> bytes from the specified byte array starting
		 * at offset <code>off</code> to this byte array output stream.
		 *
		 * @param b
		 *            the data.
		 * @param off
		 *            the start offset in the data.
		 * @param len
		 *            the number of bytes to write.
		 */
		@Override
		public void write( final byte b[], final int off, final int len )
		{
			if ( ( off < 0 ) || ( off > b.length ) || ( len < 0 ) ||
					( ( off + len ) - b.length > 0 ) ) { throw new IndexOutOfBoundsException(); }
			ensureCapacity( wcount + len );
			System.arraycopy( b, off, buf, wcount, len );
			wcount += len;
		}

		/**
		 * Closing has no effect.
		 */
		@Override
		public void close()
		{}
	}

	private class In extends InputStream
	{
		/**
		 * Reads the next byte of data from this input stream. The value byte is
		 * returned as an <code>int</code> in the range <code>0</code> to
		 * <code>255</code>. If no byte is available because the end of the
		 * stream has been reached, the value <code>-1</code> is returned.
		 * <p>
		 * This <code>read</code> method cannot block.
		 *
		 * @return the next byte of data, or <code>-1</code> if the end of the
		 *         stream has been reached.
		 */
		@Override
		public int read()
		{
			return ( pos < rcount ) ? ( buf[ pos++ ] & 0xff ) : -1;
		}

		/**
		 * Reads up to <code>len</code> bytes of data into an array of bytes
		 * from this input stream. If <code>pos</code> equals <code>count</code>
		 * , then <code>-1</code> is returned to indicate end of file.
		 * Otherwise, the number <code>k</code> of bytes read is equal to the
		 * smaller of <code>len</code> and <code>count-pos</code>. If
		 * <code>k</code> is positive, then bytes <code>buf[pos]</code> through
		 * <code>buf[pos+k-1]</code> are copied into <code>b[off]</code> through
		 * <code>b[off+k-1]</code> in the manner performed by
		 * <code>System.arraycopy</code>. The value <code>k</code> is added into
		 * <code>pos</code> and <code>k</code> is returned.
		 * <p>
		 * This <code>read</code> method cannot block.
		 *
		 * @param b
		 *            the buffer into which the data is read.
		 * @param off
		 *            the start offset in the destination array <code>b</code>
		 * @param len
		 *            the maximum number of bytes read.
		 * @return the total number of bytes read into the buffer, or
		 *         <code>-1</code> if there is no more data because the end of
		 *         the stream has been reached.
		 * @exception NullPointerException
		 *                If <code>b</code> is <code>null</code>.
		 * @exception IndexOutOfBoundsException
		 *                If <code>off</code> is negative, <code>len</code> is
		 *                negative, or <code>len</code> is greater than
		 *                <code>b.length - off</code>
		 */
		@Override
		public int read( final byte b[], final int off, int len )
		{
			if ( b == null )
			{
				throw new NullPointerException();
			}
			else if ( off < 0 || len < 0 || len > b.length - off ) { throw new IndexOutOfBoundsException(); }

			if ( pos >= rcount ) { return -1; }

			final int avail = rcount - pos;
			if ( len > avail )
			{
				len = avail;
			}
			if ( len <= 0 ) { return 0; }
			System.arraycopy( buf, pos, b, off, len );
			pos += len;
			return len;
		}

		/**
		 * Skips <code>n</code> bytes of input from this input stream. Fewer
		 * bytes might be skipped if the end of the input stream is reached. The
		 * actual number <code>k</code> of bytes to be skipped is equal to the
		 * smaller of <code>n</code> and <code>count-pos</code>. The value
		 * <code>k</code> is added into <code>pos</code> and <code>k</code> is
		 * returned.
		 *
		 * @param n
		 *            the number of bytes to be skipped.
		 * @return the actual number of bytes skipped.
		 */
		@Override
		public synchronized long skip( final long n )
		{
			long k = rcount - pos;
			if ( n < k )
			{
				k = n < 0 ? 0 : n;
			}

			pos += k;
			return k;
		}

		/**
		 * Returns the number of remaining bytes that can be read (or skipped
		 * over) from this input stream.
		 * <p>
		 * The value returned is <code>count&nbsp;- pos</code>, which is the
		 * number of bytes remaining to be read from the input buffer.
		 *
		 * @return the number of remaining bytes that can be read (or skipped
		 *         over) from this input stream without blocking.
		 */
		@Override
		public synchronized int available()
		{
			return rcount - pos;
		}

		/**
		 * Closing has no effect.
		 */
		@Override
		public void close()
		{}
	}

	/**
	 * Wrap all {@link DataOutput} methods and catch IOExceptions.
	 */
	public static class WrappedDataOutput
	{
		private final DataOutput dataOutput;

		WrappedDataOutput( final DataOutput dataOutput )
		{
			this.dataOutput = dataOutput;
		}

		public void write( final int b )
		{
			try
			{
				dataOutput.write( b );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void write( final byte[] b )
		{
			try
			{
				dataOutput.write( b );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void write( final byte[] b, final int off, final int len )
		{
			try
			{
				dataOutput.write( b, off, len );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeBoolean( final boolean v )
		{
			try
			{
				dataOutput.writeBoolean( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeByte( final int v )
		{
			try
			{
				dataOutput.writeByte( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeShort( final int v )
		{
			try
			{
				dataOutput.writeShort( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeChar( final int v )
		{
			try
			{
				dataOutput.writeChar( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeInt( final int v )
		{
			try
			{
				dataOutput.writeInt( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeLong( final long v )
		{
			try
			{
				dataOutput.writeLong( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeFloat( final float v )
		{
			try
			{
				dataOutput.writeFloat( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeDouble( final double v )
		{
			try
			{
				dataOutput.writeDouble( v );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeBytes( final String s )
		{
			try
			{
				dataOutput.writeBytes( s );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeChars( final String s )
		{
			try
			{
				dataOutput.writeChars( s );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void writeUTF( final String s )
		{
			try
			{
				dataOutput.writeUTF( s );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}
	}

	/**
	 * Wrap all {@link DataInput} methods and catch IOExceptions.
	 */
	public static class WrappedDataInput
	{
		private final DataInput dataInput;

		WrappedDataInput( final DataInput dataInput )
		{
			this.dataInput = dataInput;
		}

		public void readFully( final byte[] b )
		{
			try
			{
				dataInput.readFully( b );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public void readFully( final byte[] b, final int off, final int len )
		{
			try
			{
				dataInput.readFully( b, off, len );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
		}

		public int skipBytes( final int n )
		{
			try
			{
				return dataInput.skipBytes( n );
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public boolean readBoolean()
		{
			try
			{
				return dataInput.readBoolean();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return false;
		}

		public byte readByte()
		{
			try
			{
				return dataInput.readByte();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public int readUnsignedByte()
		{
			try
			{
				return dataInput.readUnsignedByte();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public short readShort()
		{
			try
			{
				return dataInput.readShort();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public int readUnsignedShort()
		{
			try
			{
				return dataInput.readUnsignedShort();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public char readChar()
		{
			try
			{
				return dataInput.readChar();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public int readInt()
		{
			try
			{
				return dataInput.readInt();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public long readLong()
		{
			try
			{
				return dataInput.readLong();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public float readFloat()
		{
			try
			{
				return dataInput.readFloat();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public double readDouble()
		{
			try
			{
				return dataInput.readDouble();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return 0;
		}

		public String readLine()
		{
			try
			{
				return dataInput.readLine();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return null;
		}

		public String readUTF()
		{
			try
			{
				return dataInput.readUTF();
			}
			catch ( final IOException e )
			{
				// should never happen, but better print it to be safe...
				e.printStackTrace();
			}
			return null;
		}
	}
}
