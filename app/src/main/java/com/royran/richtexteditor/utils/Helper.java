/*
 * Copyright (C) 2015-2018 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.royran.richtexteditor.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Bidi;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Miscellaneous helper methods
 */
public abstract class Helper {

    private static float sDensity = Float.MAX_VALUE;
    private static float sDensity4Fonts = Float.MAX_VALUE;

    private static final int LEADING_MARGIN = 28;
    private static int sLeadingMarging = -1;

    /**
     * This method encodes the query part of an url
     * @param url an url (e.g. http://www.1gravity.com?query=üö)
     * @return The url with an encoded query, e.g. http://www.1gravity.com?query%3D%C3%BC%C3%B6
     */
    public static String encodeUrl(String url) {
        Uri uri = Uri.parse(url);

        try {
            Map<String, List<String>> splitQuery = splitQuery(uri);
            StringBuilder encodedQuery = new StringBuilder();
            for (String key : splitQuery.keySet()) {
                for (String value : splitQuery.get(key)) {
                    if (encodedQuery.length() > 0) {
                        encodedQuery.append("&");
                    }
                    encodedQuery.append(key + "=" + URLEncoder.encode(value, "UTF-8"));
                }
            }
            String queryString = encodedQuery != null && encodedQuery.length() > 0 ? "?" + encodedQuery : "";

            URI baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
            return baseUri + queryString;
        }
        catch (UnsupportedEncodingException ignore) {}
        catch (URISyntaxException ignore) {}

        return uri.toString();
    }

    /**
     * This method decodes an url with encoded query string
     * @param url an url with encoded query string (e.g. http://www.1gravity.com?query%C3%BC%C3%B6)
     * @return The decoded url, e.g. http://www.1gravity.com?query=üö
     */
    public static String decodeQuery(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException ignore) {}

        return url;
    }

    /**
     * Splits the query parameters into key value pairs.
     * See: http://stackoverflow.com/a/13592567/534471.
     */
    private static Map<String, List<String>> splitQuery(Uri uri) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
        String query = uri.getQuery();
        if (query == null) return query_pairs;

        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    /**
     * This method determines if the direction of a substring is right-to-left.
     * If the string is empty that determination is based on the default system language
     * Locale.getDefault().
     * The method can handle invalid substring definitions (start > end etc.), in which case the
     * method returns False.
     *
     * @return True if the text direction is right-to-left, false otherwise.
     */
    public static boolean isRTL(CharSequence s, int start, int end) {
        if (s == null || s.length() == 0) {
            // empty string --> determine the direction from the default language
            return isRTL(Locale.getDefault());
        }

        if (start == end) {
            // if no character is selected we need to expand the selection
            start = Math.max(0, --start);
            if (start == end) {
                end = Math.min(s.length(), ++end);
            }
        }

        try {
            Bidi bidi = new Bidi(s.subSequence(start, end).toString(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            return ! bidi.baseIsLeftToRight();
        }
        catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean isRTL(Locale locale) {
        int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    /**
     * Returns the screen width and height.
     *
     * @param context
     * @return
     */
    public static int[] getScreenWidthAndHeight(Context context) {
        Point outSize = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(outSize);

        int[] widthAndHeight = new int[2];
        widthAndHeight[0] = outSize.x;
        widthAndHeight[1] = outSize.y;
        return widthAndHeight;
    }

    /**
     * Gets the pixels by the given number of dp.
     *
     * @param context
     * @param dp
     * @return
     */
    public static int getPixelByDp(Context context, int dp) {
        int pixels = dp;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        pixels = (int) (displayMetrics.density * dp + 0.5);
        return pixels;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
