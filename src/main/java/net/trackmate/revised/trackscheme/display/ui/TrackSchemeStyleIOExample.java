package net.trackmate.revised.trackscheme.display.ui;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import net.trackmate.io.yaml.WorkaroundConstructor;
import net.trackmate.io.yaml.WorkaroundRepresenter;
import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.ConstructBasicStroke;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.ConstructColor;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.ConstructFont;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.ConstructStyle;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.RepresentBasicStroke;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.RepresentColor;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.RepresentFont;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStyleIO.RepresentStyle;

public class TrackSchemeStyleIOExample
{
	public static void main( final String[] args )
	{
		final TrackSchemeStyle style = TrackSchemeStyle.modernStyle();

		final Yaml yaml = createYaml();
		final String dump = yaml.dump( style );
		System.out.println( dump );

		final TrackSchemeStyle load = yaml.loadAs( dump, TrackSchemeStyle.class );

		final String dump2 = yaml.dump( load );
		System.out.println( dump2 );
	}

	static class TrackSchemeStyleRepresenter extends WorkaroundRepresenter
	{
		public TrackSchemeStyleRepresenter()
		{
			putRepresent( new RepresentColor( this ) );
			putRepresent( new RepresentBasicStroke( this ) );
			putRepresent( new RepresentFont( this ) );
			putRepresent( new RepresentStyle( this ) );
		}
	}

	static class TrackschemeStyleConstructor extends WorkaroundConstructor
	{
		public TrackschemeStyleConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructColor( this ) );
			putConstruct( new ConstructBasicStroke( this ) );
			putConstruct( new ConstructFont( this ) );
			putConstruct( new ConstructStyle( this ) );
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new TrackSchemeStyleRepresenter();
		final Constructor constructor = new TrackschemeStyleConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}
}
