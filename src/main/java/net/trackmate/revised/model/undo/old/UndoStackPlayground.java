package net.trackmate.revised.model.undo.old;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class UndoStackPlayground
{

	public static class MyByteArrayStackStream extends OutputStream
	{

		/**
		 * The buffer where data is stored.
		 */
		protected byte buf[];

		/**
		 * The number of valid bytes in the buffer.
		 */
		protected int count;

		/**
		 * Creates a new byte array output stream, with the specified buffer
		 * capacity, in bytes.
		 *
		 * @param capacity
		 *            the initial capacity.
		 */
		public MyByteArrayStackStream( final int capacity )
		{
			buf = new byte[ capacity ];
		}

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

		/**
		 * Writes the specified byte to this byte array output stream.
		 *
		 * @param b
		 *            the byte to be written.
		 */
		@Override
		public void write( final int b )
		{
			ensureCapacity( count + 1 );
			buf[ count ] = ( byte ) b;
			count += 1;
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
			ensureCapacity( count + len );
			System.arraycopy( b, off, buf, count, len );
			count += len;
		}

		/**
		 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods
		 * in this class can be called after the stream has been closed without
		 * generating an <tt>IOException</tt>.
		 */
		@Override
		public void close() throws IOException
		{}
	}


	public static class MyByteArrayInputStream extends InputStream
	{
		private final MyByteArrayStackStream stack;

		protected int pos;

		/**
		 * Creates a <code>ByteArrayInputStream</code> so that it uses
		 * <code>buf</code> as its buffer array. The buffer array is not copied.
		 * The initial value of <code>pos</code> is <code>0</code> and the
		 * initial value of <code>count</code> is the length of <code>buf</code>
		 * .
		 *
		 * @param buf
		 *            the input buffer.
		 */
		public MyByteArrayInputStream( final MyByteArrayStackStream stack )
		{
			this.stack = stack;
			this.pos = 0;
		}

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
		public synchronized int read()
		{
			return ( pos < stack.count ) ? ( stack.buf[ pos++ ] & 0xff ) : -1;
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
		public synchronized int read( final byte b[], final int off, int len )
		{
			if ( b == null )
			{
				throw new NullPointerException();
			}
			else if ( off < 0 || len < 0 || len > b.length - off ) { throw new IndexOutOfBoundsException(); }

			if ( pos >= stack.count ) { return -1; }

			final int avail = stack.count - pos;
			if ( len > avail )
			{
				len = avail;
			}
			if ( len <= 0 ) { return 0; }
			System.arraycopy( stack.buf, pos, b, off, len );
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
			long k = stack.count - pos;
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
			return stack.count - pos;
		}

		/**
		 * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in
		 * this class can be called after the stream has been closed without
		 * generating an <tt>IOException</tt>.
		 */
		@Override
		public void close() throws IOException
		{}

		public void setPos( final int pos )
		{
			this.pos = pos;
		}
	}

	public static void main( final String[] args ) throws IOException, ClassNotFoundException
	{
		final MyByteArrayStackStream bos = new MyByteArrayStackStream( 1024 * 1024 * 32 );
		final MyByteArrayInputStream bis = new MyByteArrayInputStream( bos );

		System.out.println( bos.count );
		final DataOutputStream dos = new DataOutputStream( bos );
		System.out.println( bos.count );
		final DataInputStream dis = new DataInputStream( bis );

		dos.writeInt( 42424242 );
		dos.flush();
		System.out.println( bos.count );

		dos.writeBoolean( true );
		dos.flush();
		System.out.println( bos.count );

		final byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		dos.write( bytes );
		dos.flush();
		System.out.println( bos.count );

//		System.out.println( dis.readInt() );
//		final byte[] ibytes = new byte[ 10 ];
//		dis.readFully( ibytes );
//		for ( final byte b : ibytes )
//			System.out.print( b + " " );
//		System.out.println();
//
//		final int x = bos.count;
//		bos.count = 0;
//		dos.writeInt( 11111111 );
//		dos.write( 42 );
//		bos.count = x;
//
//		bis.setPos( 0 );
//
//		System.out.println( dis.readInt() );
//		dis.readFully( ibytes );
//		for ( final byte b : ibytes )
//			System.out.print( b + " " );
//		System.out.println();
	}
}
