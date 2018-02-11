package com.omer.clickgame;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.omer.clickgame.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    Button clickbutton, restartbutton, globalhigh, localhigh;
    TextView score, timeleft, level, necessaryclick, localhighscoretoptext, globalscorestoptext;
    CountDownTimer countDownTimer;
    Level currentLevel, firstlevel;
    Realm realm;
    Boolean isFirstPress;
    int totalScore;
    MediaPlayer mp, mp2;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myRef;
    String key;
    private InterstitialAd mInterstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        buttonPress();
        restartButtonPress();
        localHighScoresButton();
        globalHighScoresButton();
    }

    private void initialize() {
        clickbutton = findViewById(R.id.clickbutton);
        restartbutton = findViewById(R.id.restartbutton);
        score = findViewById(R.id.score);
        level = findViewById(R.id.level);
        timeleft = findViewById(R.id.timeleft);
        globalhigh = findViewById(R.id.globalhigh);
        localhigh = findViewById(R.id.localhigh);
        localhighscoretoptext = findViewById(R.id.localhighscoretoptext);
        globalscorestoptext = findViewById(R.id.globalscorestoptext);
        realm = Realm.getDefaultInstance();
        firstlevel = new Level(50, 10, 1);
        currentLevel = firstlevel;
        timeleft.setText(firstlevel.getTime() + "");
        totalScore = 0;
        mp = MediaPlayer.create(this, R.raw.lasersoundeffect);
        mp2 = MediaPlayer.create(this, R.raw.levelpassed);
        score.setText(0 + "");
        isFirstPress = true;
        level.setText("Level " + firstlevel.getLevel());
        necessaryclick = findViewById(R.id.necessaryclick);
        necessaryclick.setText("click " + firstlevel.getNecessaryClick() + "" + " times");
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("Highscores");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6860388873742865~5931804954");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void buttonPress() {
        clickbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalScore++;
                if (totalScore % 10 == 0)
                    mp.start();
                score.setText(totalScore + "");
                if (isFirstPress) {
                    startCountdown(currentLevel.getTime() * 1000);
                    isFirstPress = false;
                }
            }
        });
    }


    private void startCountdown(final int time) {
        countDownTimer = generateCountdown(time).start();
    }

    private void restartButtonPress() {
        restartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartGame();
            }
        });
    }

    private void restartGame() {
        currentLevel = firstlevel;
        clickbutton.setClickable(true);
        score.setText("" + 0);
        totalScore = 0;
        level.setText("Level " + currentLevel.getLevel());
        timeleft.setText(currentLevel.getTime() + "");
        isFirstPress = true;
        necessaryclick.setText("click " + currentLevel.getNecessaryClick() + " times");
        countDownTimer.cancel();
        //countDownTimer = generateCountdown(currentLevel.getTime() * 1000);
    }

    private CountDownTimer generateCountdown(final int time) {
        return new CountDownTimer(time, 1000) {
            int left;

            @Override
            public void onTick(long l) {
                left = Integer.parseInt(timeleft.getText().toString());
                left--;
                timeleft.setText(left + "");
            }

            @Override
            public void onFinish() {
                left--;
                timeleft.setText(left + "");
                if (currentLevel.getNecessaryClick() > totalScore) {
                    clickbutton.setClickable(false);
                    Toast.makeText(getApplicationContext(), "You score is " + totalScore, Toast.LENGTH_LONG).show();
                    addNewHighScoreToLocalDatabase(totalScore);
                    if (isConnected(getApplicationContext())) {
                        addNewHighScoreToGlobalDatabase(totalScore);
                    }
                } else {
                    mp2.start();
                    proceedToNextLevel();
                }
            }
        };
    }

    private void proceedToNextLevel() {
        currentLevel = new Level(currentLevel.getNecessaryClick() + (int) (currentLevel.getNecessaryClick() / 2), currentLevel.getTime(), currentLevel.getLevel() + 1);
        level.setText("Level " + currentLevel.getLevel());
        timeleft.setText(currentLevel.getTime() + "");
        necessaryclick.setText("click " + currentLevel.getNecessaryClick() + " times");
        isFirstPress = true;
        countDownTimer = generateCountdown(currentLevel.getTime() * 1000);
    }

    private void addNewHighScoreToLocalDatabase(final int score) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                LocalHighScore localHighScore = realm.createObject(LocalHighScore.class);
                localHighScore.setScore(score);
            }
        });
    }

    private void addNewHighScoreToGlobalDatabase(final int score) {
        key = myRef.push().getKey();
        DatabaseReference myRef2 = firebaseDatabase.getReference("Highscores/" + key);
        myRef2.setValue(new GlobalHighScore(getPhoneName(), score));
    }

    private void localHighScoresButton() {
        localhigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                openLocalHighScoresDialog();
            }
        });
    }

    private void globalHighScoresButton() {
        globalhigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                openGlobalHighScoresDialog();
            }
        });
    }

    private void openLocalHighScoresDialog() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.local_scores_dialog, null);

        final TextView first = view.findViewById(R.id.first);
        final TextView second = view.findViewById(R.id.second);
        final TextView third = view.findViewById(R.id.third);
        final TextView fourth = view.findViewById(R.id.fourth);
        final TextView fifth = view.findViewById(R.id.fifth);
        final TextView sixth = view.findViewById(R.id.sixth);
        final TextView seventh = view.findViewById(R.id.seventh);
        final TextView eight = view.findViewById(R.id.eight);
        final TextView ninth = view.findViewById(R.id.ninth);
        final TextView tenth = view.findViewById(R.id.tenth);

        final TextView localhighscoretoptext = view.findViewById(R.id.localhighscoretoptext);


        RealmResults<LocalHighScore> results = realm.where(LocalHighScore.class).sort("score", Sort.DESCENDING).findAll();
        TextView[] order = {first, second, third, fourth, fifth, sixth, seventh, eight, ninth, tenth};
        for (int i = 0; results.size() > i && i < 10; i++) {
            order[i].setText(results.get(i).getScore() + "");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (dialog.isShowing()) {
                    final int random = (int) ((Math.random() * 5) + 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (random == 1)
                                localhighscoretoptext.setTextColor(Color.parseColor("#cd84f1"));
                            if (random == 2)
                                localhighscoretoptext.setTextColor(Color.parseColor("#ff4d4d"));
                            if (random == 3)
                                localhighscoretoptext.setTextColor(Color.parseColor("#fff200"));
                            if (random == 4)
                                localhighscoretoptext.setTextColor(Color.parseColor("#18dcff"));
                            if (random == 5)
                                localhighscoretoptext.setTextColor(Color.parseColor("#32ff7e"));
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();


    }

    private void openGlobalHighScoresDialog() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.global_scores_dialog, null);

        final TextView firstscore = view.findViewById(R.id.firstscore);
        final TextView secondscore = view.findViewById(R.id.secondscore);
        final TextView thirdscore = view.findViewById(R.id.thirdscore);
        final TextView fourthscore = view.findViewById(R.id.fourthscore);
        final TextView fifthscore = view.findViewById(R.id.fifthscore);
        final TextView sixthscore = view.findViewById(R.id.sixthscore);
        final TextView seventhscore = view.findViewById(R.id.seventhscore);
        final TextView eightscore = view.findViewById(R.id.eightscore);
        final TextView ninthscore = view.findViewById(R.id.ninthscore);
        final TextView tenthscore = view.findViewById(R.id.tenthscore);

        final TextView firstname = view.findViewById(R.id.name1);
        final TextView secondname = view.findViewById(R.id.name2);
        final TextView thirdname = view.findViewById(R.id.name3);
        final TextView fourthname = view.findViewById(R.id.name4);
        final TextView fifthname = view.findViewById(R.id.name5);
        final TextView sixthname = view.findViewById(R.id.name6);
        final TextView seventhname = view.findViewById(R.id.name7);
        final TextView eightname = view.findViewById(R.id.name8);
        final TextView ninthname = view.findViewById(R.id.name9);
        final TextView tenthname = view.findViewById(R.id.name10);


        final TextView[] orderScore = {firstscore, secondscore, thirdscore, fourthscore, fifthscore, sixthscore, seventhscore, eightscore, ninthscore, tenthscore};
        final TextView[] orderName = {firstname, secondname, thirdname, fourthname, fifthname, sixthname, seventhname, eightname, ninthname, tenthname};

        DatabaseReference scoresRef = firebaseDatabase.getReference("Highscores");
        scoresRef.orderByChild("score").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 9;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if(i == -1)
                        break;
                    String name = data.child("name").getValue().toString();
                    String score = data.child("score").getValue().toString();
                    orderName[i].setText(name);
                    orderScore[i].setText(score);
                    i--;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private String getPhoneName() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        return deviceName;
    }

    private String generateID() {
        String ID = "00";
        for (int i = 0; i < 10; i++) {
            int random = (int) (Math.random() * 9 + 1);
            ID = ID + random;
        }
        return ID;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }
}
