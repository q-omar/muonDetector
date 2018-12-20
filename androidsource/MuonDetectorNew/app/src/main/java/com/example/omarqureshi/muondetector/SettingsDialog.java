package com.example.omarqureshi.muondetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

/**
 * This class is used to create a settings menu dialog.
 */
public class SettingsDialog extends DialogFragment {

    private Button saveBtn;
    private Button cancelBtn;
    private SettingsDialogListener listener = null;
    private SharedPreferences sharedPrefs;

    public SettingsDialog() { }

    /**
     * Used to create new instances of this class
     * @param title  title of the settings window
     * @return  new instance of SettingsDialog
     */
    public static SettingsDialog newInstance(String title) {
        SettingsDialog frag = new SettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }

    // Define listener interface
    public interface SettingsDialogListener {
        void settingsSaved();
    }

    // Setter for listener
    public void setSettingsDialogListener(SettingsDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_menu, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get timer pickers from view and set their boundaries
        final NumberPicker hourPicker = (NumberPicker) view.findViewById(R.id.hourPicker);
        final NumberPicker minutePicker = (NumberPicker) view.findViewById(R.id.minutePicker);
        final NumberPicker secondPicker = (NumberPicker) view.findViewById(R.id.secondPicker);

        setNumberPicker(hourPicker, 0, 23);
        setNumberPicker(minutePicker, 0, 59);
        setNumberPicker(secondPicker, 0, 59);

        sharedPrefs = getActivity().getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        // Set default recording time to 1 minute
        minutePicker.setValue(sharedPrefs.getInt(getString(R.string.minutes_key), 1));
        hourPicker.setValue(sharedPrefs.getInt(getString(R.string.hours_key), 0));
        secondPicker.setValue(sharedPrefs.getInt(getString(R.string.seconds_key),0));

        // set up buttons
        saveBtn = (Button) view.findViewById(R.id.saveSettingsBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {     // Save user's changed settings
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPrefs.edit();

                int minutes = minutePicker.getValue();
                int hours = hourPicker.getValue();
                int seconds = secondPicker.getValue();

                if (minutes == 0 && hours == 0 && seconds == 0) {   // Quick fix to prevent timer duration of 0
                    seconds = 1;
                }

                // Save new timer duration
                editor.putInt(getString(R.string.minutes_key),minutes);
                editor.putInt(getString(R.string.hours_key), hours);
                editor.putInt(getString(R.string.seconds_key), seconds);
                editor.apply();

                // Alert listener
                if (listener != null) {
                    listener.settingsSaved();
                }
                dismiss();      // Close window
            }
        });

        cancelBtn = (Button) view.findViewById(R.id.cancelSettingsBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();          // Close window
            }
        });

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Settings");
        getDialog().setTitle(title);

    }

    /**
     * Small function for setting up the boundaries of a number picker.
     * @param np  a NumberPicker widget
     * @param max the largest value of the NumberPicker
     * @param min the minimum value of the NumberPicker
     */
    public void setNumberPicker(NumberPicker np, int min, int max) {
        np.setMaxValue(max);
        np.setMinValue(min);
        np.setWrapSelectorWheel(false);
    }
}