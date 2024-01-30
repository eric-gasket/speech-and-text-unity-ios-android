package com.starseed.speechtotext;

import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.RecognitionListener;
import android.speech.tts.UtteranceProgressListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends UnityPlayerActivity
{
    private SpeechRecognizer speech;
    private Intent intent;

    private boolean continuousListening = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(recognitionListener);
    }
    @Override
    public void onDestroy() {
        if (speech != null) {
            speech.destroy();
        }
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try
            {
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                UnityPlayer.UnitySendMessage("SpeechToText", "onResults", text.get(0));
            }
            catch(NullPointerException e)
            {
                Log.i("SpeechToText", "NullPointerException from onActivityResult: " + e.getMessage());
            }
        }

        if (continuousListening) {
            //StartListening(false);
        }
    }

    // speech to text
    public void OnStartRecording() {
        Log.i("SpeechToText", "PROOF THINGS CHANGED");
        continuousListening = true;

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Bridge.languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Bridge.languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Bridge.languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
        //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //onLog("test");
        Log.i("SpeechToText", "OnStartRecording!");

        /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageSpeech);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, Long.valueOf(5000));
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, Long.valueOf(3000));
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, Long.valueOf(3000));
        if (!prompt.equals("")) intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        UnityPlayer.currentActivity.startActivityForResult(intent, RESULT_SPEECH);*/

        StartListening(false);
    }
    public void OnStopRecording() {
        continuousListening = false;

        Log.i("SpeechToText", "OnStopRecording!");
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                speech.stopListening();
            }
        });
        UnityPlayer.UnitySendMessage("SpeechToText", "onMessage", "CallStop");
    }

    private void StartListening(boolean resetSpeech) {
        Log.w("SpeechToText", "wrapper StartListening called");
        Log.i("SpeechToText", "PROOF THINGS CHANGED");
        if (resetSpeech) {
            if (speech != null){
                speech.destroy();
            }
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(recognitionListener);
        }

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                speech.startListening(intent);
            }
        });
        UnityPlayer.UnitySendMessage("SpeechToText", "onMessage", "CallStart, Language:" + Bridge.languageSpeech);
    }

    RecognitionListener recognitionListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
            UnityPlayer.UnitySendMessage("SpeechToText", "onReadyForSpeech", params.toString());
        }
        @Override
        public void onBeginningOfSpeech() {
            UnityPlayer.UnitySendMessage("SpeechToText", "onBeginningOfSpeech", "");
        }
        @Override
        public void onRmsChanged(float rmsdB) {
            //UnityPlayer.UnitySendMessage("SpeechToText", "onRmsChanged", "" + rmsdB);
        }
        @Override
        public void onBufferReceived(byte[] buffer) {
            UnityPlayer.UnitySendMessage("SpeechToText", "onMessage", "onBufferReceived: " + buffer.length);
        }
        @Override
        public void onEndOfSpeech() {
            UnityPlayer.UnitySendMessage("SpeechToText", "onEndOfSpeech", "");
        }
        @Override
        public void onError(int error) {
            UnityPlayer.UnitySendMessage("SpeechToText", "onError", "" + error);
            if (continuousListening){

                StartListening(false);
            }
            Log.w("SpeechToText", "onErrorCalled!");
        }
        @Override
        public void onResults(Bundle results) {
            try
            {
                ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                UnityPlayer.UnitySendMessage("SpeechToText", "onResults", text.get(0));

                if (continuousListening){
                    //StartListening(false);
                }
            }
            catch (NullPointerException e)
            {
                Log.i("SpeechToText", "NullPointerException from onResults PROOF: " + e.getMessage());

                if (true){
                    StartListening(false);
                }
            }

            UnityPlayer.UnitySendMessage("SpeechToText", "onResults", "");
        }
        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i("SpeechToText", "PROOF THINGS CHANGED");

            try
            {
                ArrayList<String> text = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (text.size() > 0)
                {
                    UnityPlayer.UnitySendMessage("SpeechToText", "onPartialResults", text.get(0));
                }
                else {
                    Log.i("SpeechToText", "onPartialResults: nothing yet");
                    Log.i("SpeechToText", "TRY RESTARTING HERE?");
                    StartListening(true);
                }

            }
            catch(NullPointerException e)
            {
                Log.i("SpeechToText", "NullPointerException from onPartialResults PROOF: " + e.getMessage());
                StartListening(true);
            }
            catch(IndexOutOfBoundsException i)
            {
                Log.i("SpeechToText", "IndexOutOfBoundsException from onPartialResults: " + i.getMessage());
                StartListening(true);
            }
        }
        @Override
        public void onEvent(int eventType, Bundle params) {

            UnityPlayer.UnitySendMessage("SpeechToText", "onMessage", "onEvent");
        }
    };

    /**
     * Convert a string based locale into a Locale Object.
     * Assumes the string has form "{language}_{country}_{variant}".
     * Examples: "en", "de_DE", "_GB", "en_US_WIN", "de__POSIX", "fr_MAC"
     *
     * @param localeString The String
     * @return the Locale
     */
    public static Locale getLocaleFromString(String localeString)
    {
        if (localeString == null)
        {
            return null;
        }
        localeString = localeString.trim();
        if (localeString.equalsIgnoreCase("default"))
        {
            return Locale.getDefault();
        }

        // Extract language
        int languageIndex = localeString.indexOf('_');
        String language;
        if (languageIndex == -1)
        {
            // No further "_" so is "{language}" only
            return new Locale(localeString, "");
        }
        else
        {
            language = localeString.substring(0, languageIndex);
        }

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1);
        String country;
        if (countryIndex == -1)
        {
            // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex+1);
            return new Locale(language, country);
        }
        else
        {
            // Assume all remaining is the variant so is "{language}_{country}_{variant}"
            country = localeString.substring(languageIndex+1, countryIndex);
            String variant = localeString.substring(countryIndex+1);
            return new Locale(language, country, variant);
        }
    }
}