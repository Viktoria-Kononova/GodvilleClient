package com.example.kononova.godville;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kononova.godville.Getter.APIGetter;
import com.example.kononova.godville.Getter.FakeGetter;
import com.example.kononova.godville.Getter.IGetter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ProgressFragment extends Fragment implements View.OnClickListener {

    private TextView heroTextView;
    private TextView clanTextView;
    private TextView healthTextView;
    private TextView petTextView;
    private TextView diaryTextView;

    private String contentText = null;
    private EditText godNameEditText;
    private IGetter getter;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        getter = new APIGetter();

        heroTextView = (TextView)view.findViewById(R.id.textViewHero);
        clanTextView = (TextView)view.findViewById(R.id.textViewClan);
        healthTextView = (TextView)view.findViewById(R.id.textViewHealth);
        petTextView = (TextView)view.findViewById(R.id.textViewPet);
        diaryTextView = (TextView)view.findViewById(R.id.textViewDiary);

        godNameEditText =(EditText)view.findViewById(R.id.editTextGodName);
        Button confirmButton = (Button)view.findViewById(R.id.buttonConfirm);
        confirmButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        String godName = godNameEditText.getText().toString();
        Utils.hideKeyboard(getContext(), view);
        new ProgressTask(godName).execute();
    }


    class ProgressTask extends AsyncTask<String, Void, String>{
        String godvillePath = null;

        @Override
        protected String doInBackground(String... path){
            String content;
            try{
                content = getter.getContent(godvillePath);
            }
            catch (Exception e){
                e.printStackTrace();
                content = null;
            }
            return content;
        }

        ProgressTask (String godName){
            try {
                godvillePath = "http://godville.net/gods/api/" + URLEncoder.encode(godName, "UTF-8") + ".json";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... items){
        }

        @Override
        protected void onPostExecute(String content){
            contentText = content;
            if (content == null){
                Toast.makeText(getActivity(), getResources().getString(R.string.check), Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getActivity(),getResources().getString(R.string.data_loaded) , Toast.LENGTH_SHORT).show();
            JsonObject heroJsonObject = null;
            try {
                heroJsonObject = new JsonParser().parse(content).getAsJsonObject();
                String heroName = heroJsonObject.get("name").getAsString();
                String heroCharacter = heroJsonObject.get("alignment").getAsString();
                String heroMotto = heroJsonObject.get("motto").getAsString();
                String heroClan = heroJsonObject.get("clan").getAsString();
                String heroClanPosition = heroJsonObject.get("clan_position").getAsString();
                String heroLevel = heroJsonObject.get("level").getAsString();
                JsonElement health = heroJsonObject.get("health");
                String heroHealth = "?";
                if (health!=null) {
                    heroHealth =health.getAsString();
                }
                String heroMaxHealth = heroJsonObject.get("max_health").getAsString();
                JsonElement diary = heroJsonObject.get("diary_last");
                String heroDiary = "?";
                if (diary!=null) {
                    heroDiary = diary.getAsString();
                }

                JsonObject petJsonObject = heroJsonObject.get("pet").getAsJsonObject();
                String heroPet = petJsonObject.get("pet_name").getAsString();
                JsonElement level = petJsonObject.get("pet_level");
                String heroPetLevel = getResources().getString(R.string.pet_dead);
                if (level!=null && !level.getAsString().isEmpty()) {
                    heroPetLevel = level.getAsString() + " " + getResources().getString(R.string.level_line);
                }

                heroTextView.setText(heroName + "\n" + heroLevel + " " + getResources().getString(R.string.level_line) + "\n" + heroCharacter + "\n" + heroMotto);
                clanTextView.setText(heroClan + "\n" + "(" + heroClanPosition + ")");
                healthTextView.setText(heroHealth + "/" + heroMaxHealth + " " + getResources().getString(R.string.health_points));
                petTextView.setText(heroPet + "\n" + heroPetLevel);
                diaryTextView.setText(heroDiary);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
