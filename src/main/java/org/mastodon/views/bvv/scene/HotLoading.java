package org.mastodon.views.bvv.scene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.scijava.listeners.Listeners;

// TODO: move to BVV
public class HotLoading
{
	static class FilePair
	{
		private final File target;

		private final File resource;

		private long lastModified;

		public final Listeners.List< Runnable > listeners = new Listeners.List<>();

		FilePair( final String targetName )
		{
			target = new File( targetName );
			resource = new File( targetName.replaceAll( "target/classes", "src/main/resources" ) );
			lastModified = resource.lastModified();
		}

		void checkModified() throws IOException
		{
			final long mod = resource.lastModified();
			if ( mod != lastModified )
			{
				lastModified = mod;
				Files.copy( resource.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING );
				listeners.list.forEach( Runnable::run );
			}
		}
	}

	// key is targetName with which FilePair was constructed
	private static final Map< String, FilePair > files = new HashMap<>();

	private static void add( final String f, final Runnable runnable )
	{
		files.computeIfAbsent( f, FilePair::new ).listeners.add( runnable );
	}

	private static void checkModified()
	{
		try
		{
			for ( final FilePair file : files.values() )
				file.checkModified();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}

	public static class ShaderHotLoader
	{
		private final AtomicBoolean modified;

		private final Runnable setModified;

		public ShaderHotLoader( final String... filesToWatch )
		{
			this( false, filesToWatch );
		}

		public ShaderHotLoader( final boolean initiallyModified, final String... filesToWatch )
		{
			modified = new AtomicBoolean( initiallyModified );
			setModified = () -> modified.set( true );
			for ( final String f : filesToWatch )
				HotLoading.add( f, setModified );
		}

		public ShaderHotLoader watch( final Class< ? > resourceContext, final String resourceName )
		{
			watch( resourceContext.getResource( resourceName ).getFile() );
			return this;
		}

		public ShaderHotLoader watch( final String filename )
		{
			HotLoading.add( filename, setModified );
			return this;
		}

		public boolean isModified()
		{
			HotLoading.checkModified();
			return modified.getAndSet( false );
		}
	}
}
