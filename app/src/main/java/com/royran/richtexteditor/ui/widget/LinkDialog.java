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

package com.royran.richtexteditor.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.royran.richtexteditor.R;
import com.royran.richtexteditor.utils.Helper;
import com.royran.richtexteditor.utils.validator.EmailValidator;
import com.royran.richtexteditor.utils.validator.UrlValidator;

import java.util.Locale;

/**
 * A DialogFragment to add, modify or remove links from Spanned text.
 */
public class LinkDialog {
    private static final UrlValidator sUrlValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_ALL_SCHEMES);
    private static final EmailValidator sEmailValidator = EmailValidator.getInstance(false);

    public interface LinkDialogListener {
        void onInsert(String newAddress, String linkText);

        void onRemove();
    }

    public static void show(final Context context, String address, String linkText, LinkDialogListener listener) {
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.insert_link_dialog, null);

        // link address
        String tmp = "http://";
        if (address != null && ! address.isEmpty()) {
            try {
                Uri uri = Uri.parse( Helper.decodeQuery(address) );
                // if we have an email address remove the mailto: part for editing purposes
                tmp = startsWithMailto(address) ? uri.getSchemeSpecificPart() : uri.toString();
            } catch (Exception ignore) {}
        }
        final String url = tmp;
        final TextView addressView = view.findViewById(R.id.linkURL);
        if (url != null) {
            addressView.setText(url);
        }

        final TextView textView = view.findViewById(R.id.linkText);
        if (linkText != null) {
            textView.setText(linkText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.rte_create_a_link)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // OK button
                    validate(context, dialog, addressView, textView, listener);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                });

        if (address != null) {
            builder.setNeutralButton(R.string.rte_remove_action, (dialog, which) -> {
                // Remove button
                if (listener != null) {
                    listener.onRemove();
                }
            });
        }
        builder.show();
    }

    private static void validate(final Context context, DialogInterface dialog, TextView addressView, TextView textView, LinkDialogListener listener) {
        // retrieve link address and do some cleanup
        final String address = addressView.getText().toString().trim();

        boolean isEmail = sEmailValidator.isValid(address);
        boolean isUrl = sUrlValidator.isValid(address);
        if (requiredFieldValid(addressView) && (isUrl || isEmail)) {
            // valid url or email address

            // encode address
            String newAddress = Helper.encodeUrl(address);

            // add mailto: for email addresses
            if (isEmail && !startsWithMailto(newAddress)) {
                newAddress = "mailto:" + newAddress;
            }

            // use the original address text as link text if the user didn't enter anything
            String linkText = textView.getText().toString();
            if (linkText.length() == 0) {
                linkText = address;
            }
            if (listener != null) {
                listener.onInsert(newAddress, linkText);
            }
            try { dialog.dismiss(); } catch (Exception ignore) {}
        } else {
            // invalid address (neither a url nor an email address
            String errorMessage = context.getString(R.string.rte_invalid_link, address);
            Toast.makeText(addressView.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean startsWithMailto(String address) {
        return address != null && address.toLowerCase(Locale.getDefault()).startsWith("mailto:");
    }

    private static boolean requiredFieldValid(TextView view) {
        return view.getText() != null && view.getText().length() > 0;
    }
}