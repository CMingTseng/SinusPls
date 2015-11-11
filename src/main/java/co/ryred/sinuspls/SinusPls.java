/*
 * http://ryred.co/
 * ace[at]ac3-servers.eu
 *
 * =================================================================
 *
 * Copyright (c) 2015, Cory Redmond
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of SinusPls nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package co.ryred.sinuspls;

import co.ryred.sinuspls.gson.NullStringToEmptyAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 09/11/2015.
 */
public class SinusPls
{

	public static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory( new NullStringToEmptyAdapterFactory() ).disableHtmlEscaping().create();
	public static boolean verbose = false;
	private static Connection connection;

	public static void main( String... args ) throws ParseException, ClassNotFoundException, IOException
	{

		File dbFile;
		File outputFolderFile;
		File inputFolderFile;

		final Options options = new Options();

		for ( OptEnum option : OptEnum.values() )
			options.addOption( option.getOption() );

		CommandLine cmd = new DefaultParser().parse( options, args );

		verbose = ( cmd.hasOption( OptEnum.VERBOSE.getLongOpt() ) || cmd.hasOption( OptEnum.VERBOSE.getShortOpt() ) );
		printVerbose( "Verbose output is being printed!" );

		// Deal with help first.
		if ( cmd.hasOption( OptEnum.HELP.getLongOpt() ) || cmd.hasOption( OptEnum.HELP.getShortOpt() ) ) {
			new HelpFormatter().printHelp( "java -jar jarfile.jar [options]", options );
			return;
		}

		// Deal with the output folder.
		if ( !cmd.hasOption( OptEnum.OUTPUT_FOLDER.getShortOpt() ) && !cmd.hasOption( OptEnum.OUTPUT_FOLDER.getLongOpt() ) ) {
			Options outOpts = new Options();
			outOpts.addOption( OptEnum.OUTPUT_FOLDER.getOption() );
			new HelpFormatter().printHelp( "The output folder is not defined.", outOpts );
			return;
		}
		else {

			String outputFolder = cmd.getOptionValue( OptEnum.OUTPUT_FOLDER.getShortOpt() );
			if ( outputFolder == null ) outputFolder = cmd.getOptionValue( OptEnum.OUTPUT_FOLDER.getLongOpt() );

			if ( outputFolder == null ) {
				System.err.println( "Output folder not provided? O.o" );
				return;
			}

			printVerbose( "Output folder\n    " + outputFolder );
			outputFolderFile = new File( outputFolder );
			if ( !outputFolderFile.exists() ) { outputFolderFile.mkdirs(); }

		}

		// Deal with the database file.
		if ( !cmd.hasOption( OptEnum.DB_FILE.getShortOpt() ) && !cmd.hasOption( OptEnum.DB_FILE.getLongOpt() ) ) {
			Options outOpts = new Options();
			outOpts.addOption( OptEnum.DB_FILE.getOption() );
			new HelpFormatter().printHelp( "The database file is not defined.", outOpts );
			return;
		}
		else {

			String databaseFile = cmd.getOptionValue( OptEnum.DB_FILE.getShortOpt() );
			if ( databaseFile == null ) databaseFile = cmd.getOptionValue( OptEnum.DB_FILE.getLongOpt() );

			if ( databaseFile == null ) {
				System.err.println( "database file not provided? O.o" );
				return;
			}

			printVerbose( "Database file\n    " + databaseFile );
			dbFile = new File( databaseFile );
			if ( !dbFile.exists() ) {
				System.err.println( "database file does not exist." );
				return;
			}

		}

		// Deal with the input folder
		if ( cmd.hasOption( OptEnum.INPUT_FOLDER.getLongOpt() ) || cmd.hasOption( OptEnum.INPUT_FOLDER.getShortOpt() ) ) {

			String inputFile = cmd.getOptionValue( OptEnum.INPUT_FOLDER.getShortOpt() );
			if ( inputFile == null ) inputFile = cmd.getOptionValue( OptEnum.INPUT_FOLDER.getLongOpt() );

			if ( inputFile == null ) inputFile = ".";

			printVerbose( "Input folder\n    " + inputFile );
			inputFolderFile = new File( inputFile );
			if ( !inputFolderFile.exists() ) {
				System.err.println( "The input folder doesn't exist." );
				return;
			}

		}
		else {
			inputFolderFile = new File( "." );
			printVerbose( "Input folder\n    " + inputFolderFile.getPath() );
			if ( !inputFolderFile.exists() ) {
				System.err.println( "The input folder doesn't exist." );
				return;
			}
		}

		// Init the database.
		Class.forName( "org.sqlite.JDBC" );
		printVerbose( "SQLite JDBC driver is in the classpath." );

		try {
			String uri = "jdbc:sqlite:" + dbFile.getPath();
			printVerbose( "Database URI:\n    " + uri );
			printVerbose( "Connecting to the SQLite DB...." );
			connection = DriverManager.getConnection( uri );
			printVerbose( "Connected." );
		} catch ( SQLException e ) {
			System.err.println( "Unable to load to the database!" );
			e.printStackTrace();
			System.err.println( "Unable to load to the database!" );
			return;
		}

		// Load errors and stuff like that.. Just dirty. Might need more doing.
		printVerbose( "Loading errored files." );
		ArrayList<String> erroredPaths = new ArrayList<>();
		File errorFile = new File( ".sinuspls_err" );
		if ( errorFile.exists() ) {

			BufferedReader br = new BufferedReader( new FileReader( errorFile ) );

			String line;
			while ( ( line = br.readLine() ) != null ) { erroredPaths.add( line ); }

			br.close();
			FileUtils.forceDelete( errorFile );

		}

		errorFile.createNewFile();
		BufferedWriter bw = new BufferedWriter( new FileWriter( errorFile ) );

		// Start looping through them all.
		printVerbose( "Starting processing!" );
		Collection<File> files = FileUtils.listFiles( inputFolderFile, new String[]{ "mp3" }, true );
		System.out.println( "Files matching *.mp3:" + files.size() );
		for ( File maybeMP3File : files ) {

			if ( cmd.hasOption( OptEnum.FIX.getShortOpt() ) || cmd.hasOption( OptEnum.FIX.getLongOpt() ) ) {
				if ( !erroredPaths.contains( maybeMP3File.getPath() ) ) { continue; }
			}

			try {
				processMP3File( maybeMP3File, outputFolderFile, connection );
			} catch ( Exception e ) {
				if ( e instanceof UnsupportedAudioFileException ) {
					System.err.println( "The audio file isn't MP3..\n" + maybeMP3File.getPath() );
					e.printStackTrace();
					System.err.println( "The audio file isn't MP3..\n" + maybeMP3File.getPath() );
				}
				else if ( e instanceof SQLException ) {
					System.err.println( "Something went wrong whilst editing the database!" );
					e.printStackTrace();
					System.err.println( "Something went wrong whilst editing the database!" );
				}
				else {
					System.err.println( "Unknown exception.. Maybe file permissions?" );
					e.printStackTrace();
					System.err.println( "Unknown exception.. Maybe file permissions?" );
				}

				bw.write( maybeMP3File.getPath() );
				bw.newLine();
				bw.flush();

			}

		}

		if ( !cmd.hasOption( OptEnum.FIX.getShortOpt() ) && !cmd.hasOption( OptEnum.FIX.getLongOpt() ) ) {
			for ( String path : erroredPaths ) {
				bw.write( path );
				bw.newLine();
			}
		}

		bw.flush();
		bw.close();

	}

	private static void processMP3File( File maybeMP3File, File outputFolder, Connection connection ) throws IOException, UnsupportedAudioFileException, SQLException
	{

		printVerbose( "- Processing " + FilenameUtils.removeExtension( maybeMP3File.getName() ) );

		AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat( maybeMP3File );
		printVerbose( "      File IS mp3." );

		if ( fileFormat instanceof TAudioFileFormat ) {
			Map<String, Object> properties = fileFormat.properties();

			UUID uuid = getUUID( maybeMP3File );
			String path = "SinusPls_" + uuid.toString().replace( "-", "" );
			long duration = TimeUnit.MICROSECONDS.toMillis( (Long) properties.get( "duration" ) );
			int bitrate = (int) properties.get( "mp3.bitrate.nominal.bps" );
			int channels = (int) properties.get( "mp3.channels" );
			int sampleRate = (int) properties.get( "mp3.length.bytes" );
			int fileSize = (int) properties.get( "mp3.length.bytes" );
			String fileName = FilenameUtils.removeExtension( maybeMP3File.getName() );
			String title = (String) properties.get( "title" );
			String artist = (String) properties.get( "author" );

			TrackMeta tm = new TrackMeta( uuid, path, duration, bitrate, channels, sampleRate, fileSize, fileName, title, artist );

			PreparedStatement stmt = connection.prepareStatement( "INSERT INTO `files`(`uuid`,`artist`,`title`,`album`,`trackinfo`,`created`,`createdby`,`type`,`playcount`,`size`) VALUES (?,?,?,?,?, datetime( 'now' ),?,?,0,0);" );
			stmt.setString( 1, uuid.toString() );
			stmt.setString( 2, tm.getArtist() );
			stmt.setString( 3, tm.getTitle() );
			stmt.setString( 4, "" );
			stmt.setString( 5, GSON.toJson( tm ) );
			stmt.setString( 6, "" );
			stmt.setString( 7, "file" );

			FileUtils.copyFile( maybeMP3File, new File( outputFolder, path ) );

			try {
				stmt.execute();
				printVerbose( "      Completed file conversion!" );
			} catch ( Exception e ) {
				FileUtils.forceDelete( new File( outputFolder, path ) );
				throw e;
			}

		}

	}

	private static UUID getUUID( File file ) throws IOException
	{
		FileInputStream fis = new FileInputStream( file );
		String md5 = DigestUtils.md5Hex( fis );
		fis.close();
		return UUID.fromString( md5.substring( 0, 8 ) + "-" + md5.substring( 8, 12 ) + "-" + md5.substring( 12, 16 ) + "-" + md5.substring( 16, 20 ) + "-" + md5.substring( 20, 32 ) );
	}

	public static void printVerbose( String string )
	{
		if ( !verbose ) return;
		for ( String output : string.split( "\\n" ) )
			System.out.println( "[D] | " + output );
	}

}
