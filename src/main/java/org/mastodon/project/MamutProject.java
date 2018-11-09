package org.mastodon.project;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MamutProject
{
	/**
	 * The project folder or {@code .mastodon} file.
	 */
	private File projectRoot;

	/**
	 * The BDV xml file.
	 */
	private File datasetXmlFile;

	static final String PROJECT_FILE_NAME = "project.xml";
	static final String RAW_MODEL_FILE_NAME = "model.raw";
	static final String RAW_TAGS_FILE_NAME = "tags.raw";

	public MamutProject( final String projectRoot )
	{
		this( new File( projectRoot ), null );
	}

	public MamutProject( final File projectRoot )
	{
		this( projectRoot, null );
	}

	public MamutProject(
			final File projectRoot,
			final File datasetXmlFile )
	{
		this.projectRoot = projectRoot;
		this.datasetXmlFile = datasetXmlFile;
	}

	/**
	 * Get the project folder or zip file.
	 *
	 * @return the project folder or zip file.
	 */
	public File getProjectRoot()
	{
		return projectRoot;
	}

	public void setProjectRoot( final File projectRoot )
	{
		this.projectRoot = projectRoot;
	}

	public File getDatasetXmlFile()
	{
		return datasetXmlFile;
	}

	public void setDatasetXmlFile( final File datasetXmlFile )
	{
		this.datasetXmlFile = datasetXmlFile;
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n"
				+ " - projectRoot: " + getProjectRoot() + "\n"
				+ " - dataset: " + getDatasetXmlFile();
	}

	public ProjectReader openForReading() throws IOException
	{
		return projectRoot.isDirectory()
				? new ReadFromDirectory()
				: new ReadFromZip();
	}

	public ProjectWriter openForWriting() throws IOException
	{
		return projectRoot.isDirectory()
				? new WriteToDirectory()
				: new WriteToZip();
	}

	public interface ProjectReader extends Closeable
	{
		InputStream getProjectXmlInputStream() throws IOException;

		InputStream getRawModelInputStream() throws IOException;

		InputStream getRawTagsInputStream() throws IOException;
	}

	public interface ProjectWriter extends Closeable
	{
		OutputStream getProjectXmlOutputStream() throws IOException;

		OutputStream getRawModelOutputStream() throws IOException;

		OutputStream getRawTagsOutputStream() throws IOException;
	}

	private class ReadFromDirectory implements ProjectReader
	{
		@Override
		public InputStream getProjectXmlInputStream() throws FileNotFoundException
		{
			return new FileInputStream( new File( projectRoot, PROJECT_FILE_NAME ) );
		}

		@Override
		public InputStream getRawModelInputStream() throws FileNotFoundException
		{
			return new FileInputStream( new File( projectRoot, RAW_MODEL_FILE_NAME ) );
		}

		@Override
		public InputStream getRawTagsInputStream() throws FileNotFoundException
		{
			return new FileInputStream( new File( projectRoot, RAW_TAGS_FILE_NAME ) );
		}

		@Override
		public void close()
		{
		}
	}

	private class ReadFromZip implements ProjectReader
	{
		private final ReadZip zip;

		ReadFromZip() throws IOException
		{
			zip = new ReadZip( projectRoot );
		}

		@Override
		public InputStream getProjectXmlInputStream() throws IOException
		{
			return zip.getInputStream( PROJECT_FILE_NAME );
		}

		@Override
		public InputStream getRawModelInputStream() throws IOException
		{
			return zip.getInputStream( RAW_MODEL_FILE_NAME );
		}

		@Override
		public InputStream getRawTagsInputStream() throws IOException
		{
			return zip.getInputStream( RAW_TAGS_FILE_NAME );
		}

		@Override
		public void close() throws IOException
		{
			zip.close();
		}
	}

	private class WriteToDirectory implements ProjectWriter
	{
		@Override
		public OutputStream getProjectXmlOutputStream() throws FileNotFoundException
		{
			return new FileOutputStream( new File( projectRoot, PROJECT_FILE_NAME ) );
		}

		@Override
		public OutputStream getRawModelOutputStream() throws FileNotFoundException
		{
			return new FileOutputStream( new File( projectRoot, RAW_MODEL_FILE_NAME ) );
		}

		@Override
		public OutputStream getRawTagsOutputStream() throws FileNotFoundException
		{
			return new FileOutputStream( new File( projectRoot, RAW_TAGS_FILE_NAME ) );
		}

		@Override
		public void close() throws IOException
		{
		}
	}

	private class WriteToZip implements ProjectWriter
	{
		private final WriteZip zip;

		WriteToZip() throws IOException
		{
			zip = new WriteZip( projectRoot );
		}

		@Override
		public OutputStream getProjectXmlOutputStream() throws IOException
		{
			return zip.getOutputStream( PROJECT_FILE_NAME );
		}

		@Override
		public OutputStream getRawModelOutputStream() throws IOException
		{
			return zip.getOutputStream( RAW_MODEL_FILE_NAME );
		}

		@Override
		public OutputStream getRawTagsOutputStream() throws IOException
		{
			return zip.getOutputStream( RAW_TAGS_FILE_NAME );
		}

		@Override
		public void close() throws IOException
		{
			zip.close();
		}
	}
}
