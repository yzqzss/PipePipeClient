package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;

import java.util.concurrent.TimeUnit;

import static org.schabi.newpipe.extractor.ServiceList.NicoNico;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public final class ServiceHelper {
    private static final StreamingService DEFAULT_FALLBACK_SERVICE = ServiceList.YouTube;

    private ServiceHelper() { }

    @DrawableRes
    public static int getIcon(final int serviceId) {
        switch (serviceId) {
            case 0:
                return R.drawable.place_holder_youtube;
            case 1:
                return R.drawable.place_holder_cloud;
            case 2:
                return R.drawable.place_holder_gadse;
            case 3:
                return R.drawable.place_holder_peertube;
            case 4:
                return R.drawable.place_holder_bandcamp;
            case 5:
            case 6:
                return R.drawable.place_holder_niconico;
            default:
                return R.drawable.place_holder_circle;
        }
    }

    public static String getTranslatedFilterString(final String filter, final Context c) {
        switch (filter) {
            case "search":
                return c.getString(R.string.search);
            case "all":
                return c.getString(R.string.all);
            case "videos":
            case "sepia_videos":
            case "music_videos":
                return c.getString(R.string.videos_string);
            case "channels":
                return c.getString(R.string.channels);
            case "playlists":
            case "music_playlists":
                return c.getString(R.string.playlists);
            case "tracks":
                return c.getString(R.string.tracks);
            case "users":
                return c.getString(R.string.users);
            case "conferences":
                return c.getString(R.string.conferences);
            case "events":
                return c.getString(R.string.events);
            case "music_songs":
                return c.getString(R.string.songs);
            case "music_albums":
                return c.getString(R.string.albums);
            case "music_artists":
                return c.getString(R.string.artists);
            case "lives":
                return c.getString(R.string.lives);
            case "animes":
                return c.getString(R.string.animes);
            case "movies_and_tv":
                return c.getString(R.string.movies_and_tv);
            case "tags_only":
                return c.getString(R.string.tags_only);
            case "sortby":
                return c.getString(R.string.sortby);
            case "sortorder":
                return c.getString(R.string.sortorder);
            case "features":
                return c.getString(R.string.features);
            case "sort_popular":
                return c.getString(R.string.sort_popular);
            case "sort_view":
                return c.getString(R.string.sort_view);
            case "sort_bookmark":
                return c.getString(R.string.sort_bookmark);
            case "sort_likes":
                return c.getString(R.string.sort_likes);
            case "sort_comments":
                return c.getString(R.string.sort_comments);
            case "sort_bullet_comments":
                return c.getString(R.string.sort_bullet_comments);
            case "sort_length":
                return c.getString(R.string.sort_length);
            case "sort_publish_time":
                return c.getString(R.string.sort_publish_time);
            case "sort_last_comment_time":
                return c.getString(R.string.sort_last_comment_time);
            case "sort_video_count":
                return c.getString(R.string.sort_video_count);
            case "sort_overall":
                return c.getString(R.string.sort_overall);
            case "sort_relevance":
                return c.getString(R.string.sort_relevance);
            case "sort_rating":
                return c.getString(R.string.sort_rating);
            case "sort_ascending":
                return c.getString(R.string.sort_ascending);
            default:
                return filter;
        }
    }

    /**
     * Get a resource string with instructions for importing subscriptions for each service.
     *
     * @param serviceId service to get the instructions for
     * @return the string resource containing the instructions or -1 if the service don't support it
     */
    @StringRes
    public static int getImportInstructions(final int serviceId) {
        switch (serviceId) {
            case 0:
                return R.string.import_youtube_instructions;
            case 1:
                return R.string.import_soundcloud_instructions;
            default:
                return -1;
        }
    }

    /**
     * For services that support importing from a channel url, return a hint that will
     * be used in the EditText that the user will type in his channel url.
     *
     * @param serviceId service to get the hint for
     * @return the hint's string resource or -1 if the service don't support it
     */
    @StringRes
    public static int getImportInstructionsHint(final int serviceId) {
        switch (serviceId) {
            case 1:
                return R.string.import_soundcloud_instructions_hint;
            default:
                return -1;
        }
    }

    public static int getSelectedServiceId(final Context context) {
        final String serviceName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.current_service_key),
                        context.getString(R.string.default_service_value));

        int serviceId;
        try {
            serviceId = NewPipe.getService(serviceName).getServiceId();
        } catch (final ExtractionException e) {
            serviceId = DEFAULT_FALLBACK_SERVICE.getServiceId();
        }

        return serviceId;
    }

    public static void setSelectedServiceId(final Context context, final int serviceId) {
        String serviceName;
        try {
            serviceName = NewPipe.getService(serviceId).getServiceInfo().getName();
        } catch (final ExtractionException e) {
            serviceName = DEFAULT_FALLBACK_SERVICE.getServiceInfo().getName();
        }

        setSelectedServicePreferences(context, serviceName);
    }

    public static void setSelectedServiceId(final Context context, final String serviceName) {
        final int serviceId = NewPipe.getIdOfService(serviceName);
        if (serviceId == -1) {
            setSelectedServicePreferences(context,
                    DEFAULT_FALLBACK_SERVICE.getServiceInfo().getName());
        } else {
            setSelectedServicePreferences(context, serviceName);
        }
    }

    private static void setSelectedServicePreferences(final Context context,
                                                      final String serviceName) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().
                putString(context.getString(R.string.current_service_key), serviceName).apply();
    }

    public static long getCacheExpirationMillis(final int serviceId) {
        if (serviceId == SoundCloud.getServiceId()) {
            return TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
        } else if (serviceId == NicoNico.getServiceId()) {
            return TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);
        } else {
            return TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        }
    }

    public static boolean isBeta(final StreamingService s) {
        switch (s.getServiceInfo().getName()) {
            case "YouTube":
            case "BiliBili":
            case "NicoNico":
                return false;
            default:
                return true;
        }
    }

    public static void initService(final Context context, final int serviceId) {
        if (serviceId == ServiceList.PeerTube.getServiceId()) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            final String json = sharedPreferences.getString(context.getString(
                    R.string.peertube_selected_instance_key), null);
            if (null == json) {
                return;
            }

            final JsonObject jsonObject;
            try {
                jsonObject = JsonParser.object().from(json);
            } catch (final JsonParserException e) {
                return;
            }
            final String name = jsonObject.getString("name");
            final String url = jsonObject.getString("url");
            final PeertubeInstance instance = new PeertubeInstance(url, name);
            ServiceList.PeerTube.setInstance(instance);
        } else if (serviceId == ServiceList.NicoNico.getServiceId()) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            final String tokens = sharedPreferences.getString(context.getString(
                    R.string.niconico_cookies_key), null);
            ServiceList.NicoNico.setTokens(tokens);
            if(sharedPreferences.getBoolean(context.getString(R.string.override_cookies_niconico_key), false)) {
                ServiceList.NicoNico.setTokens(sharedPreferences.getString(context.getString(R.string.override_cookies_niconico_value_key), null));
            }
        } else if (serviceId == ServiceList.BiliBili.getServiceId()) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            final String tokens = sharedPreferences.getString(context.getString(
                    R.string.bilibili_cookies_key), null);
            ServiceList.BiliBili.setTokens(tokens);
            if(sharedPreferences.getBoolean(context.getString(R.string.override_cookies_bilibili_key), false)) {
                ServiceList.BiliBili.setTokens(sharedPreferences.getString(context.getString(R.string.override_cookies_bilibili_value_key), null));
            }
        }
    }

    public static void initServices(final Context context) {
        for (final StreamingService s : ServiceList.all()) {
            initService(context, s.getServiceId());
        }
    }
}
