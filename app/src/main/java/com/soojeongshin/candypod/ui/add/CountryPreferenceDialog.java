/*
 * Copyright 2018 Soojeong Shin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soojeongshin.candypod.ui.add;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soojeongshin.candypod.R;

/**
 * Creates a dialog which is the same as ListPreference where the user can choose a country.
 * Reference: @see "https://stackoverflow.com/questions/33976579/how-to-call-listpreference-dialog-only"
 */
public class CountryPreferenceDialog extends DialogFragment implements DialogInterface.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    private SharedPreferences mPrefs;
    private int mPrefIndex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEntries = getResources().getStringArray(R.array.pref_country_options);
        mEntryValues = getResources().getStringArray(R.array.pref_country_values);
        mValue = mPrefs.getString(
                getString(R.string.pref_country_key),
                getString(R.string.pref_country_default));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.title_country_dialog));
        builder.setPositiveButton(null, null);
        mPrefIndex = getValueIndex();
        builder.setSingleChoiceItems(mEntries, mPrefIndex, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mPrefIndex != which) {
            mPrefIndex = which;
            mValue = mEntryValues[mPrefIndex].toString();
            mPrefs.edit().putString(getString(R.string.pref_country_key), mValue).apply();
        }
        dialog.dismiss();
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    private int findIndexOfValue(String value) {
        int defaultValue = -1;
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return defaultValue;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_country_key))) {
            mPrefs.edit().putString(key, mValue).apply();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Register the OnSharedPreferenceChange listener
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        // Unregister the OnSharedPreferenceChange listener
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
