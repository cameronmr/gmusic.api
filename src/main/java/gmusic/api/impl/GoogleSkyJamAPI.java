/*******************************************************************************
 * Copyright (c) 2012 Jens Kristian Villadsen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jens Kristian Villadsen - initial API and implementation
 ******************************************************************************/
package gmusic.api.impl;

import gmusic.api.comm.JSON;
import gmusic.api.interfaces.IGoogleHttpClient;
import gmusic.api.skyjam.interfaces.IGoogleSkyJam;
import gmusic.api.skyjam.model.AlbumArtRef;
import gmusic.api.skyjam.model.Playlists;
import gmusic.api.skyjam.model.Track;
import gmusic.api.skyjam.model.TrackFeed;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import com.google.common.base.Strings;

public class GoogleSkyJamAPI extends GoogleMusicAPI implements IGoogleSkyJam
{
	public GoogleSkyJamAPI()
	{
		super();
	}

	public GoogleSkyJamAPI(IGoogleHttpClient httpClient, File file)
	{
		super(httpClient, file);
	}

	@Override
	public Collection<Track> getAllTracks() throws IOException, URISyntaxException
	{
		final Collection<Track> chunkedCollection = new ArrayList<Track>();
		final TrackFeed chunk = JSON.Deserialize(client.dispatchGet(new URI(HTTPS_WWW_GOOGLEAPIS_COM_SJ_V1BETA1_TRACKS)), TrackFeed.class);
		chunkedCollection.addAll(chunk.getData().getItems());
		chunkedCollection.addAll(getTracks(chunk.getNextPageToken()));
		return chunkedCollection;
	}

	private final Collection<Track> getTracks(String continuationToken) throws IOException, URISyntaxException
	{
		Collection<Track> chunkedCollection = new ArrayList<Track>();

		TrackFeed chunk = JSON.Deserialize(client.dispatchPost(new URI(HTTPS_WWW_GOOGLEAPIS_COM_SJ_V1BETA1_TRACKFEED), "{\"start-token\":\"" + continuationToken + "\"}"), TrackFeed.class);
		chunkedCollection.addAll(chunk.getData().getItems());

		if(!Strings.isNullOrEmpty(chunk.getNextPageToken()))
		{
			chunkedCollection.addAll(getTracks(chunk.getNextPageToken()));
		}
		return chunkedCollection;
	}

	@Override
	public Collection<File> downloadTracks(Collection<Track> tracks) throws URISyntaxException, IOException
	{
		Collection<File> files = new ArrayList<File>();
		for(Track track : tracks)
		{
			files.add(downloadTrack(track));
		}
		return files;
	}

	@Override
	public URI getTrackURL(Track track) throws URISyntaxException, IOException
	{
		return getTuneURL(track);
	}

	@Override
	public File downloadTrack(Track track) throws URISyntaxException, IOException
	{
		File file = new File(storageDirectory.getAbsolutePath() + track.getId() + ".mp3");
		if(!file.exists())
		{
			FileUtils.copyURLToFile(getTuneURL(track).toURL(), file);
			populateFileWithTuneTags(file, track);
		}
		return file;

		// if(androidDeviceId == null)
		// {
		// throw new InvalidAttributesException("Android Device ID not specified in constructor");
		// }
		// method.setQueryString(new NameValuePair[] {
		// new NameValuePair("key", "value")
		// });
		// client.dispatchGet(new URI("https://android.clients.google.com/music/mplay?" + track.getId()));
	}

	private void populateFileWithTuneTags(File file, Track track) throws IOException
	{
		try
		{
			AudioFile f = AudioFileIO.read(file);
			Tag tag = f.getTag();
			if(tag == null)
			{
				tag = new ID3v24Tag();
			}
			tag.setField(FieldKey.ALBUM, track.getAlbum());
			tag.setField(FieldKey.ALBUM_ARTIST, track.getAlbumArtist());
			tag.setField(FieldKey.ARTIST, track.getArtist());
			tag.setField(FieldKey.COMPOSER, track.getComposer());
			tag.setField(FieldKey.DISC_NO, String.valueOf(track.getDiscNumber()));
			tag.setField(FieldKey.DISC_TOTAL, String.valueOf(track.getTotalDiscCount()));
			tag.setField(FieldKey.GENRE, track.getGenre());
			tag.setField(FieldKey.TITLE, track.getTitle());
			tag.setField(FieldKey.TRACK, String.valueOf(track.getTrackNumber()));
			tag.setField(FieldKey.TRACK_TOTAL, String.valueOf(track.getTotalTrackCount()));
			tag.setField(FieldKey.YEAR, String.valueOf(track.getYear()));

			if(track.getAlbumArtRef() != null && !track.getAlbumArtRef().isEmpty())
			{
				AlbumArtRef[] array = track.getAlbumArtRef().toArray(new AlbumArtRef[track.getAlbumArtRef().size()]);
				for(int i = 0; i < array.length; i++)
				{
					Artwork artwork = new Artwork();
					File imageFile = new File(storageDirectory + System.getProperty("path.separator") + track.getId() + ".im" + i);
					FileUtils.copyURLToFile(array[i].getUrlAsURI().toURL(), imageFile);
					artwork.setFromFile(imageFile);
					tag.addField(artwork);
				}
			}

			f.setTag(tag);
			AudioFileIO.write(f);
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public Playlists getAllSkyJamPlaylists() throws IOException, URISyntaxException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrackFeed getSkyJamPlaylist(String plID) throws IOException, URISyntaxException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
