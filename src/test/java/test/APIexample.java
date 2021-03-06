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
package test;

import gmusic.api.impl.GoogleSkyJamAPI;
import gmusic.api.model.Playlist;
import gmusic.api.model.Playlists;
import gmusic.api.model.Song;
import gmusic.api.skyjam.model.Track;

import java.util.Calendar;
import java.util.Collection;

public class APIexample
{
	public static void main(String args[])
	{
		String password = "*";
		String username = "jenskristianvilladsen@gmail.com";
		System.out.println(Calendar.getInstance().getTime());
//		IGoogleMusicAPI api = new GoogleMusicAPI(new HttpUrlConnector(), new File("."));
//		IGoogleMusicAPI api = new GoogleSkyJamAPI();
		GoogleSkyJamAPI api = new GoogleSkyJamAPI();

		try
		{
			api.login(username, password);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		
		try
		{
			api.login(username, password);
			// QueryResponse response = api.search("Jane");
			// api.downloadSongs(response.getResults().getSongs());
			Playlists playlists = api.getAllPlaylists();
			if(playlists.getMagicPlaylists() != null)
			{
				for(Playlist list : playlists.getMagicPlaylists())
				{
					System.out.println("--- " + list.getTitle() + " " + list.getPlaylistId() + " ---");
					for(Song song : list.getPlaylist())
					{
						System.out.println(song.getName() + " " + song.getArtist());
					}
				}
			}
			
			Collection<Track> tracks = api.getAllTracks();
			
			for(Track t : tracks)
			{
				System.out.println(t);
				if(t.getAlbumArtRef()!= null && !t.getAlbumArtRef().isEmpty())
				{
					api.downloadTrack(t);
				}
			}

			for(Playlist list : playlists.getPlaylists())
			{
				System.out.println("--- " + list.getTitle() + " " + list.getPlaylistId() + " ---");
				for(Song song : list.getPlaylist())
				{
					System.out.println(song.getName() + " " + song.getArtist());
					if(song.getAlbumArtUrl() != null)
					{
						api.downloadSong(song);
					}
				}
			}
			Collection<Song> songs = api.getAllSongs();
			api.downloadSong(songs.iterator().next());
			// api.downloadSongs(songs);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println(Calendar.getInstance().getTime());
	}
}
