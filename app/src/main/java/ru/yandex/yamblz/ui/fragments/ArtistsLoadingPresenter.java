package ru.yandex.yamblz.ui.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.yandex.yamblz.data.Artist;
import ru.yandex.yamblz.data.ArtistsApi;
import ru.yandex.yamblz.data.Genre;
import ru.yandex.yamblz.ui.presenters.Presenter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by aleien on 31.07.16.
 */

public class ArtistsLoadingPresenter extends Presenter<ContentFragment> {

    CompositeSubscription subs = new CompositeSubscription();
    private ArtistsApi artistsApi;

    ArtistsLoadingPresenter(ArtistsApi artistsApi) {
        this.artistsApi = artistsApi;
    }

    void loadArtists() {
        subs.add(artistsApi.getArtists()
                .subscribeOn(Schedulers.io())
                .map(this::extractGenres)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> view().showContent(list),
                        Throwable::printStackTrace));
    }

    private List<Genre> extractGenres(List<Artist> artistsList) {
        ConcurrentHashMap<String, List<Artist>> genres = new ConcurrentHashMap<>();
        List<Genre> genreList = new ArrayList<>();

        for (Artist artist : artistsList) {
            for (String genreName : artist.genres) {
                if (genres.containsKey(genreName)) {
                    genres.get(genreName).add(artist);
                } else {
                    if (genreName != null && !genreName.equals("")) {
                        genres.put(genreName, new ArrayList<Artist>() {{
                            add(artist);
                        }});

                    }
                }
            }
        }

        for (Map.Entry<String, List<Artist>> entry : genres.entrySet()) {
//            Timber.d("Key = %s, Value = %s", entry.getKey(), entry.getValue());
            genreList.add(new Genre(entry.getKey(), entry.getValue()));
        }

        return genreList;

    }
}