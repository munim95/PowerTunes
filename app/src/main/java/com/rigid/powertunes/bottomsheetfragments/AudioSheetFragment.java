package com.rigid.powertunes.bottomsheetfragments;

import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.rigid.powertunes.mediaplayer.EqAudioInterface;
import com.rigid.powertunes.R;
import com.rigid.powertunes.util.FileUtil;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

public class AudioSheetFragment extends Fragment implements EqAudioInterface {
    //todo: * SAVE BAND STATES(PARTIALLY DONE) -> in onActivityCreated preset sets bands again if bands changed --needs logic
    //todo: * ADD CUSTOM PRESET FUNCTIONALITY
    //todo: * ADD FUNCTIONALITY (AUDIO EFFECTS LIKE REVERB, BASS BOOST ETC) - native only
    //todo: FINALLY- * Fix any audio glitch while changing bands and improve latency if possible

    //static tags
    private static final String TAG = AudioSheetFragment.class.getSimpleName();
    private final int PRE_AMP_MAX=1500;
    private final int PRE_AMP_DEFAULT=PRE_AMP_MAX/2;
    private final String AUDIO_FILE_PREF="com.rigid.powertunes.equalizer";

    private boolean firsttime = true;
    //static fields
    private int currAudioSessionId = -1;
    private static Equalizer equalizer;
    private static LoudnessEnhancer preAmplifier;
    private static HashMap<Short, Short> indexToLevelMap;
    private static boolean checked;
    private static float preAmpVal=-1;
    private short lastUsedPreset = -1;
    private Spinner equalizerPresetSpinner;
    private ArrayList<SeekBar> seekBars;
    private SeekBar first, second, third, fourth, fifth, preAmpBand;
    private boolean onStartUp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(getContext());
        Transition transition1 = inflater.inflateTransition(R.transition.folder_items_transition);
        transition1.setDuration(200);
        setEnterTransition(transition1);
        setExitTransition(transition1);
        if(getArguments()!=null) { //dont init ui components on startup
            onStartUp = getArguments().getBoolean("onstartup");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.audiosheet_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        readAndSetupAudioObjs();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        saveAudioObjsToFile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    //save user config (band levels, preset value, eq on/off)
    private void saveAudioObjsToFile(){
        //creates 2d data model objects for audio variables
        Object[][] modelData = new Object[4][1]; //4 attrs(rows) x 1 value(column)
        modelData[0][0]=indexToLevelMap;
        modelData[1][0] = preAmpVal;
        modelData[2][0] = checked;
        modelData[3][0] =lastUsedPreset;

        FileUtil.writeObjectToFile(getActivity(),AUDIO_FILE_PREF,modelData);
    }
    private void readAndSetupAudioObjs(){
        try {
            FileInputStream streamIn = getActivity().openFileInput(AUDIO_FILE_PREF);
            ObjectInputStream ois = new ObjectInputStream(streamIn);
            Object[][] files = (Object[][]) ois.readObject();
            ois.close();
            if(files != null) {
                lastUsedPreset=(short)files[3][0];
                for(Object[] data:files){
                    for (Object aData : data) {
                        if(aData instanceof HashMap){
                            indexToLevelMap=(HashMap<Short, Short>) aData;
                        }else if(aData instanceof Boolean){
                            checked=(boolean)aData;
                        }else if(aData instanceof Float){
                            preAmpVal=(float)aData;
                        }
                    }
                }
                for (Map.Entry<Short, Short> entry :indexToLevelMap.entrySet()) {
                    equalizer.setBandLevel(entry.getKey(), entry.getValue());
                }
                Switch eqToggle = getActivity().findViewById(R.id.equalizerToggle);
                eqToggle.setChecked(checked);
                if(checked){
                    equalizer.setEnabled(true);
                }else {
                    equalizer.setEnabled(false);
                }
                if(!onStartUp) {
                    setUpBandsAndList(eqToggle);
                    setUpEqualizerFx();
                }else{
                    setUpPreAmp();
                }
                for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                    SeekBar seekBar= seekBars.get(i);
                    seekBar.setMax(equalizer.getBandLevelRange()[1] - equalizer.getBandLevelRange()[0]);
                    seekBar.setProgress(equalizer.getBandLevel(i) - equalizer.getBandLevelRange()[0]);
                }
            }
        }catch (Exception e){
            if(!onStartUp) {
                setUpBandsAndList(getActivity().findViewById(R.id.equalizerToggle));
                setUpEqualizerFx();
            }
            e.printStackTrace();
        }
    }

    private void setUpBandsAndList(Switch eqToggle){
        eqToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG,""+isChecked);
            checked = isChecked;
            if(checked){
                equalizer.setEnabled(true);
            }else {
                equalizer.setEnabled(false);
            }
        });

        preAmpBand=getActivity().findViewById(R.id.preAmpBand);
        first=getActivity().findViewById(R.id.firstBand);
        second=getActivity().findViewById(R.id.secondBand);
        third=getActivity().findViewById(R.id.thirdBand);
        fourth=getActivity().findViewById(R.id.fourthBand);
        fifth=getActivity().findViewById(R.id.fifthBand);

//        bassSeekbar=getActivity().findViewById(R.id.seekbarbass);

        addSeekToArray();
    }
    private void addSeekToArray(){
        seekBars= new ArrayList<>();
        seekBars.add(first);
        seekBars.add(second);
        seekBars.add(third);
        seekBars.add(fourth);
        seekBars.add(fifth);
    }

    //set up pre amplifier
    private void setUpPreAmp(){
        preAmplifier.setEnabled(true);
        preAmplifier.setTargetGain(preAmpVal!=-1?(int)preAmpVal:PRE_AMP_DEFAULT);
        preAmpVal=preAmplifier.getTargetGain();
        if(!onStartUp) {
            preAmpBand.setMax(PRE_AMP_MAX);
            preAmpBand.setProgress(preAmpVal != -1 ? (int) preAmpVal : PRE_AMP_DEFAULT);
            preAmpBand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        preAmplifier.setTargetGain(progress);
                    }
                    preAmpVal = preAmplifier.getTargetGain();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    equalizerPresetSpinner.setSelection(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }
    //every time activity loads this is called
    private void setUpEqualizerFx(){
        equalizerPresetSpinner = getActivity().findViewById(R.id.presetSpinner);
        equalizeSound(equalizerPresetSpinner);
        setUpPreAmp();
        if(indexToLevelMap==null)
            indexToLevelMap=new HashMap<>();

        short numberFrequencyBands = equalizer.getNumberOfBands();

        final short lowerEqualizerBandLevel = equalizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = equalizer.getBandLevelRange()[1];


        for (short i = 0; i < numberFrequencyBands; i++) {
            SeekBar seekBar= seekBars.get(i);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            seekBar.setProgress(equalizer.getBandLevel(i));
//            Log.d(TAG,"band "+ equalizer.getBandLevel(i));

//            change progress as its changed by moving the sliders
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    short currLevel = (short) (progress + lowerEqualizerBandLevel);

                    if(seekBar == seekBars.get(0)){
                        equalizer.setBandLevel((short) 0,
                                currLevel);
                        indexToLevelMap.put((short)0, currLevel);
                    }else if(seekBar == seekBars.get(1)){
                        equalizer.setBandLevel((short)1,
                                currLevel);
                        indexToLevelMap.put((short)1, currLevel);
                    }else if(seekBar == seekBars.get(2)){
                        equalizer.setBandLevel((short)2,
                                currLevel);
                        indexToLevelMap.put((short)2, currLevel);
                    }else if(seekBar == seekBars.get(3)){
                        equalizer.setBandLevel((short)3,
                                currLevel);
                        indexToLevelMap.put((short)3, currLevel);
                    }else if(seekBar == seekBars.get(4)){
                        equalizer.setBandLevel((short)4,
                                currLevel);
                        indexToLevelMap.put((short)4, currLevel);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    //not used
                    equalizerPresetSpinner.setSelection(0);
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    //not used
                }
            });
            //        show the spinner
        }
    }
    private void equalizeSound(Spinner equalizerPresetSpinner) {
//        set up the spinner
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter
                = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        get list of the device's equalizer presets
        for (short i=0; i <=equalizer.getNumberOfPresets(); i++) {
            if(i==0)
            equalizerPresetNames.add("None");
            else if(i>0)
            equalizerPresetNames.add(equalizer.getPresetName((short) (i-1)));
        }
        equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        if(lastUsedPreset!=-1){
            equalizerPresetSpinner.setSelection(lastUsedPreset);
        }
//        handle the spinner item selections
        equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                if(!equalizer.getPresetName((short)(position-1)).equals("")) {
                    preAmplifier.setTargetGain(PRE_AMP_DEFAULT);
                    preAmpBand.setMax(PRE_AMP_MAX);
                    preAmpBand.setProgress(PRE_AMP_DEFAULT);
                }
                if(position>0) {
                    lastUsedPreset = (short) (position);
                    //first list item selected by default and sets the preset accordingly
                    equalizer.usePreset((short)(position-1));

//                set seekBar indicators according to selected preset
                    for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
                        SeekBar seekBar = seekBars.get(i);
//                    get current gain setting for this equalizer band
//                    set the progress indicator of this seekBar to indicate the current gain value
                        seekBar.setMax(equalizer.getBandLevelRange()[1] - equalizer.getBandLevelRange()[0]);
                        seekBar.setProgress(equalizer.getBandLevel(i) - equalizer.getBandLevelRange()[0]);
                    }
                }else if(position==0){
                    //none selected
                    lastUsedPreset=(short)-1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                not used
            }
        });
    }


    @Override
    public void onLoad(int sessionId) {
        setEqualizer(sessionId);
    }

//new eq called every time a new song is played so restore last settings here
    private void setEqualizer(int audioSessionId) {
        if (currAudioSessionId != audioSessionId) {
            currAudioSessionId = audioSessionId;
            if(equalizer!=null) {
                equalizer.release();
            }
            if(preAmplifier!=null) {
                preAmplifier.release();
            }
            equalizer=new Equalizer(0,audioSessionId);
            preAmplifier=new LoudnessEnhancer(audioSessionId);
            preAmplifier.setEnabled(true);
            if (checked) {
                equalizer.setEnabled(true);
            } else {
                equalizer.setEnabled(false);
            }

            if(preAmpVal!=-1){
                preAmplifier.setTargetGain((int)preAmpVal);
            }
            if(indexToLevelMap!=null){
                for (Map.Entry<Short, Short> entry :indexToLevelMap.entrySet()) {
                    equalizer.setBandLevel(entry.getKey(), entry.getValue());
                    Log.d(TAG,entry.getKey()+" "+ entry.getValue());
                }

            }
        }
    }

}
