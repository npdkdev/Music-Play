package id.khenji.musicplay

import retrofit2.Call;
import retrofit2.http.GET;

interface API {
    @GET("repo.json")
    fun showSongs(): Call<ArrayList<Song>?>?
}