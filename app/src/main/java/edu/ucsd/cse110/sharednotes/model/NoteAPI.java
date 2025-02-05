package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @WorkerThread
    public String getNote(Note note) {
        // URLs cannot contain spaces, so we replace them with %20.
//        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + note.title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("GET", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AnyThread
    public void getNoteAsync(Note note) {
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> getNote(note));
    }


    @WorkerThread
    public String putNote(Note note) {
        // URLs cannot contain spaces, so we replace them with %20.
        //String encodedMsg = msg.replace(" ", "%20");
        String json = notePutJson(note);

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + note.title)
                .method("PUT", RequestBody.create(JSON,json))
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("PUT", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String notePutJson(Note note){
        return "{\r\n'" +
                "content: " + note.content +"\",\r\n"+
                "version: " + note.version +
                "}";
    }


    @AnyThread
    public void putUserAsync(Note note) {
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> putNote(note));
    }


    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
}
