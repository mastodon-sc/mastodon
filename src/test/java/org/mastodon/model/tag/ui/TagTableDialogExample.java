package org.mastodon.model.tag.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.model.tag.ui.TagTable;

public class TagTableDialogExample
{
	static class MyElement
	{
		private String name;

		public MyElement( final String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public void setName( final String name )
		{
			this.name = name;
		}
	}

	static class MyElements extends ArrayList< MyElement >
	{
		private static final long serialVersionUID = 1L;

		public MyElement addElement()
		{
			final MyElement element = new MyElement( "addElement" );
			add( element );
			return element;
		}
	}

	static class TagSetDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		public TagSetDialog( final Frame owner, final MyElements elements )
		{
			super( owner, "tag sets configuration", false );

			final TagTable< MyElements, MyElement > tagTable = new TagTable<>(
					elements,
					MyElements::addElement,
					MyElements::size,
					(c, e ) -> c.remove( e ),
					(c, i) -> c.get( i ),
					MyElement::setName,
					MyElement::getName );

			final JPanel tagSetPanel = new JPanel( new BorderLayout( 0, 0 ) );
			tagSetPanel.add( tagTable.getTable(), BorderLayout.CENTER );

			tagSetPanel.setPreferredSize( new Dimension( 400, 500 ) );
			getContentPane().add( tagSetPanel );
			pack();
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException
	{
		final MyElements elements = new MyElements();
		elements.add( new MyElement( "element 1" ) );
		elements.add( new MyElement( "element 2" ) );
		elements.add( new MyElement( "element 3" ) );

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final TagSetDialog frame = new TagSetDialog( null, elements );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLocationByPlatform( true );
		frame.setVisible( true );
	}
}
