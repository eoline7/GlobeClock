package com.example.globeclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String _title;
    String _zone;
    boolean _second;
    int _fontsize;
    String[] _zones;
    String _key_title;
    String _key_zone;
    String _key_second;
    String _key_fontsize;

    protected static final int FLASH_ZONE_TIME = 1;
    Timer mTimer;
    TimerTask mTask;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences cfg = getPreferences(Activity.MODE_PRIVATE);
        _key_title = getString(R.string.cfg_title);
        _key_zone = getString(R.string.cfg_zone);
        _key_second = getString(R.string.cfg_second);
        _key_fontsize = getString(R.string.cfg_fontsize);
        _title = cfg.getString(_key_title, getString(R.string.init_title));
        _zone = cfg.getString(_key_zone, getString(R.string.init_zone));
        _second = cfg.getBoolean(_key_second, false);
        _fontsize = cfg.getInt(_key_fontsize, 48);
        _zones = TimeZone.getAvailableIDs();

        showTitle();
        showZone();
        showSecSwitch();
        showFontsize();

        TextView tv_title = findViewById(R.id.aTitle);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "点击事件", Toast.LENGTH_SHORT).show();

                final EditText et = new EditText(v.getContext());
                et.setText(_title);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.label_title).setView(et);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _title = et.getText().toString().trim();
                        showTitle();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
                builder.create().show();
            }
        });

        TextView tv_zone = findViewById(R.id.aZone);
        tv_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = 0;
                for(; _zones.length > idx; ++idx) {
                    if(_zones[idx].equals(_zone)) break;
                }
                if(idx>=_zones.length) idx=0;

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.label_zone);
                builder.setSingleChoiceItems(_zones, idx, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        _zone = _zones[which];
                    }
                });
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showZone();
                    }
                });
                builder.create().show();
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==FLASH_ZONE_TIME) {
                    flashZonetime();
                }
            }
        };
        mTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(FLASH_ZONE_TIME);
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, 0, 1000);

        Switch sw_second = findViewById(R.id.aSecond);
        sw_second.setChecked(_second);
        sw_second.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _second = isChecked;
                showSecSwitch();
            }
        });

        SeekBar sb_fontsize = findViewById(R.id.aFontSize);
        sb_fontsize.setProgress(scale_val());
        sb_fontsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    switch(progress){
                        case 0:
                            _fontsize = 32;
                            break;
                        case 1:
                            _fontsize = 48;
                            break;
                        default:
                            _fontsize = 64;
                            break;
                    }
                    showFontsize();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    void showTitle() {
        TextView tv = findViewById(R.id.aTitle);
        String str = String.format("<h2>%s</h2>%s", getString(R.string.label_title), _title);
        tv.setText(Html.fromHtml(str,0));
    }
    void showZone() {
        TextView tv = findViewById(R.id.aZone);
        String str = String.format("<h2>%s</h2>%s", getString(R.string.label_zone), _zone);
        tv.setText(Html.fromHtml(str,0));
    }
    void flashZonetime() {
        TextView tv = findViewById(R.id.aZoneTime);
        Calendar ca = Calendar.getInstance();
        TimeZone cur_zone = TimeZone.getTimeZone(_zone);
        ca.setTimeZone(cur_zone);
        int hh = ca.get(Calendar.HOUR_OF_DAY);
        int mm = ca.get(Calendar.MINUTE);
        int ss = ca.get(Calendar.SECOND);
        if (_second) {
            tv.setText(String.format("%1$02d:%2$02d:%3$02d", hh, mm, ss));
        } else {
            tv.setText(String.format("%1$02d:%2$02d", hh, mm));
        }
    }
    void showSecSwitch() {
        TextView tv = findViewById(R.id.aSecText);
        String onoff = getString(_second ? R.string.label_secOn : R.string.label_secOff);
        String str = String.format("<h2>%s</h2>%s", getString(R.string.label_second), onoff);
        tv.setText(Html.fromHtml(str,0));
    }
    void showFontsize() {
        TextView tv = findViewById(R.id.aFontText);
        String str = String.format("<h2>%s</h2>%s", getString(R.string.label_font), scale_name());
        tv.setText(Html.fromHtml(str,0));
    }
    String scale_name() {
        if(_fontsize>48) return getString(R.string.label_bigsize);
        if(_fontsize<48) return getString(R.string.label_smosize);
        return getString(R.string.label_midsize);
    }
    int scale_val() {
        if(_fontsize>48) return 2;
        if(_fontsize<48) return 0;
        return 1;
    }

    @Override
    protected void onStop() {
        mTimer.cancel();
        SharedPreferences setinfo = getPreferences(Activity.MODE_PRIVATE);
        setinfo.edit()
                .putString(_key_title, _title)
                .putString(_key_zone, _zone)
                .putBoolean(_key_second, _second)
                .putInt(_key_fontsize, _fontsize)
                .commit();
        super.onStop();
    }
}
