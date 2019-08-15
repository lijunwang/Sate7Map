package com.sate7.geo.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sate7.geo.map.util.SpHelper;
import com.sate7.geo.map.util.XLog;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.exit_left_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.exit_left_to_right);
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        private final String KEY_FREQUENCY = "frequency";
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            if(!getResources().getBoolean(R.bool.config_need_sure_settings)){
                XLog.d("remove last_sure");
                getPreferenceScreen().removePreference(findPreference("last_sure"));
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            EditTextPreference frequency = getPreferenceManager().findPreference(KEY_FREQUENCY);
            String info = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(KEY_FREQUENCY, "" + getResources().getInteger(R.integer.track_freq_default));
            String convert = getResources().getString(R.string.track_settings_summery,Integer.parseInt(info));
            frequency.setSummary(convert);
            frequency.setOnPreferenceChangeListener(this);
            frequency.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });


        }


        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            XLog.d("onPreferenceChange ... " + preference.getKey() + "," + newValue);
            if (KEY_FREQUENCY.equals(preference.getKey())) {
                if (TextUtils.isEmpty((CharSequence) newValue)) {
                    return false;
                } else if (Integer.parseInt(String.valueOf(newValue)) >= getResources().getInteger(R.integer.track_freq_max)) {
                    EditTextPreference editTextPreference = (EditTextPreference) preference;
                    editTextPreference.setText("" + getResources().getInteger(R.integer.track_freq_max));
                    preference.setSummary(getResources().getString(R.string.track_settings_summery, getResources().getInteger(R.integer.track_freq_max)));
                    return false;
                } else {
                    preference.setSummary(getResources().getString(R.string.track_settings_summery, Integer.parseInt((String) newValue)));
                    return true;
                }
            }
            return false;
        }
    }
}