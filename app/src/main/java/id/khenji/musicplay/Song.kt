package id.khenji.musicplay

import kotlin.collections.MutableMap

data class Song (
    var song: String,
    var song_details: String,
    var thumb: String,
    var stream: String
)
class KSong {
    lateinit var song: String
    lateinit var song_details: String
    lateinit var thumb: String
    lateinit var stream: String

     fun setSong(
        song: String,
        song_details: String,
        thumb: String,
        stream: String
    ){
         this.song = song
         this.song_details = song_details
         this.stream = stream
         this.thumb = thumb
    }
    fun getSong(): MutableMap<String, String> {
        return mutableMapOf("song" to this.song,
            "song_details" to this.song_details,
            "stream" to this.stream,
            "thumb" to this.thumb)
    }
}