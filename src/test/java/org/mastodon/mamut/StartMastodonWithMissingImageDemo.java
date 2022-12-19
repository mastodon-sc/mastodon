/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

/**
 * Demonstrate how Mastodon can still be started if the image data is missing.
 *
 * @author Matthias Arzt
 */
public class StartMastodonWithMissingImageDemo extends JFrame
{
	private StartMastodonWithMissingImageDemo() {
		super("Start Mastodon With Missing Image Demo");
		setLayout( new MigLayout("fill, wrap") );
		add(new JLabel("This is how Mastodon starts:"));
		addButton( "... without dataset XML", "/org/mastodon/mamut/examples/tiny-no-image/tiny-project-no-dataset-xml.mastodon" );
		addButton( "... from backup dataset XML", "/org/mastodon/mamut/examples/tiny-no-image/tiny-project-backup-dataset-xml.mastodon" );
		addButton( "... without HDF5 file", "/org/mastodon/mamut/examples/tiny-missing-hdf5/tiny-project.mastodon" );
		addButton( "... with unknown host", "/org/mastodon/mamut/examples/tiny-unknown-url/remote-dataset.mastodon" );
	}

	private void addButton( String title, String resourceName )
	{
		JButton button = new JButton( title );
		button.addActionListener( ignore -> openProjectFromResources( resourceName ) );
		add( button, "grow" );
	}

	private void openProjectFromResources( String resourceName ) {
		String file = StartMastodonWithMissingImageDemo.class.getResource( resourceName ).getPath();
		try
		{
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			final WindowManager windowManager = new WindowManager( new Context() );
			windowManager.getProjectManager().open( new MamutProjectIO().load( file ), true );
			final MainWindow win = new MainWindow( windowManager );
			win.setVisible( true );

		}
		catch ( Throwable e )
		{
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		JFrame frame = new StartMastodonWithMissingImageDemo();
		frame.pack();
		frame.setVisible( true );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
	}
}
