package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public HashMap<Artist, List<Album>> getArtistAlbumMap() {
        return artistAlbumMap;
    }

    public HashMap<Album, List<Song>> getAlbumSongMap() {
        return albumSongMap;
    }

    public HashMap<Playlist, List<Song>> getPlaylistSongMap() {
        return playlistSongMap;
    }

    public HashMap<Playlist, List<User>> getPlaylistListenerMap() {
        return playlistListenerMap;
    }

    public HashMap<User, Playlist> getCreatorPlaylistMap() {
        return creatorPlaylistMap;
    }

    public HashMap<User, List<Playlist>> getUserPlaylistMap() {
        return userPlaylistMap;
    }

    public HashMap<Song, List<User>> getSongLikeMap() {
        return songLikeMap;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        getUsers().add(user);
        getUserPlaylistMap().put(user,new ArrayList<>());
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        getArtists().add(artist);
        getArtistAlbumMap().put(artist, new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findOrCreateArtist(artistName);
        Album album = new Album(title);
        getAlbums().add(album);
        getArtistAlbumMap().get(artist).add(album);
        getAlbumSongMap().put(album,new ArrayList<>());
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = findAlbum(albumName);
        if(album == null){
            throw new Exception("Album does not exist");
        }
        Song newSong = new Song(title,length);
        getSongs().add(newSong);
        List<Song> albumSongs = getAlbumSongMap().getOrDefault(album, new ArrayList<>());
        albumSongs.add(newSong);
        getAlbumSongMap().put(album,albumSongs);
        getSongLikeMap().put(newSong,new ArrayList<>());
        return newSong;
    }

    private Artist findOrCreateArtist(String artistName) {
        for(Artist artist : getArtists()){
            if(artist.getName().equalsIgnoreCase(artistName)){
                return artist;
            }
        }
        return createArtist(artistName);
    }

    private Album findAlbum(String albumName) {
        for(Album album : getAlbums()){
            if(album.getTitle().equalsIgnoreCase(albumName)){
                return album;
            }
        }
        return null;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUserByMobile(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }
        List<Song> songsToAdd = new ArrayList<>();
        for(Song song : getSongs()){
            if(song.getLength() == length){
                songsToAdd.add(song);
            }
        }
        if(songsToAdd.isEmpty()){
            throw new Exception("No songs with the given length found");
        }

        Playlist playlist = new Playlist(title);
        getPlaylistSongMap().put(playlist,songsToAdd);
        getCreatorPlaylistMap().put(user,playlist);
        getUserPlaylistMap().get(user).add(playlist);

        return playlist;
    }

    private User findUserByMobile(String mobile) {
        for(User user : getUsers()){
            if(user.getMobile().equals(mobile)){
                return user;
            }
        }
        return null;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUserByMobile(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }
        List<Song> songsToAdd = new ArrayList<>();
        for(String songTitle : songTitles){
            Song song = findSongByTitle(songTitle);
            if(song != null){
                songsToAdd.add(song);
            }
        }

        if(songsToAdd.isEmpty()){
            throw new Exception("No songs with the given titles found");
        }

        Playlist playlist = new Playlist(title);
        getPlaylistSongMap().put(playlist,songsToAdd);
        getCreatorPlaylistMap().put(user,playlist);
        getUserPlaylistMap().get(user).add(playlist);

        return playlist;
    }

    private Song findSongByTitle(String songTitle) {
        for(Song song : getSongs()){
            if(song.getTitle().equalsIgnoreCase(songTitle)){
                return song;
            }
        }
        return null;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUserByMobile(mobile);

        if(user == null){
            throw new Exception("User does not exist");
        }

        for(Playlist playlist : getPlaylists()){
            if(playlist.getTitle().equalsIgnoreCase(playlistTitle)){
                List<User> listeners = getPlaylistListenerMap().getOrDefault(playlist,new ArrayList<>());
                if(!listeners.contains(user)){
                    listeners.add(user);
                    getPlaylistListenerMap().put(playlist,listeners);
                }
                return playlist;
            }
        }
        throw new Exception("Playlist does not exist");
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUserByMobile(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }

        Song song = findSongByTitle(songTitle);
        if(song == null){
            throw new Exception("Song does not exist");
        }

        List<User> likedByUsers = getSongLikeMap().get(song);
        if(!likedByUsers.contains(user)){
            likedByUsers.add(user);
            getSongLikeMap().put(song,likedByUsers);

            Artist artist = findArtistByName(song.getArtistName());
            if(artist != null){
                artist.setLikes((artist.getLikes() + 1));
            }
            song.setLikes(song.getLikes() + 1);
        }
        return song;
    }

    private Artist findArtistByName(String name) {
        for (Artist artist : artists) {
            if (artist.getName().equalsIgnoreCase(name)) {
                return artist;
            }
        }
        return null;
    }
    public String mostPopularArtist() {
        Artist mostPopularArtist = null;
        int maxLikes = 0;

        for (Artist artist : artists) {
            if (artist.getLikes() > maxLikes) {
                maxLikes = artist.getLikes();
                mostPopularArtist = artist;
            }
        }

        if (mostPopularArtist != null) {
            return mostPopularArtist.getName();
        } else {
            return "No artists found";
        }
    }

    public String mostPopularSong() {
        Song mostPopularSong = null;
        int maxLikes = 0;

        for (Song song : songs) {
            if (song.getLikes() > maxLikes) {
                maxLikes = song.getLikes();
                mostPopularSong = song;
            }
        }

        if (mostPopularSong != null) {
            return mostPopularSong.getTitle();
        } else {
            return "No songs found";
        }
    }
}