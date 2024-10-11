package com.example.myapplication.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class MusicPlayerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_SONG = 1;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 2;

    private ImageButton playButton, prevButton, rewindButton, fastForwardButton, nextButton;
    private SeekBar seekBar, seekBarVolume;
    private TextView textView, songTitle;
    private ImageView albumCover;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private ArrayList<String> songList;
    private ArrayList<String> songUrlList;
    private int currentSongIndex;
    private boolean flag = true;
    private PopupWindow popupWindow;
    private HashSet<String> songSet; // 用于避免重复

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // 初始化UI控件
        albumCover = findViewById(R.id.albumCover);
        songTitle = findViewById(R.id.songTitle);
        playButton = findViewById(R.id.playButton);
        prevButton = findViewById(R.id.prevButton);
        rewindButton = findViewById(R.id.rewindButton);
        fastForwardButton = findViewById(R.id.fastForwardButton);
        nextButton = findViewById(R.id.nextButton);
        seekBar = findViewById(R.id.seekBar);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        textView = findViewById(R.id.textView);
        Button showSongsButton = findViewById(R.id.showSongsButton);
        //Button addSongButton = findViewById(R.id.addSongButton);

        // 初始化MediaPlayer和数据
        mediaPlayer = new MediaPlayer();
        songList = new ArrayList<>();
        songUrlList = new ArrayList<>();
        songSet = new HashSet<>(); // 用于避免重复
        currentSongIndex = 0;

        // 设置音量控制
        seekBarVolume.setMax(100);
        seekBarVolume.setProgress(50);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaPlayer.setVolume(progress / 100f, progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 设置播放/暂停按钮
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songUrlList.isEmpty()) {
                    Toast.makeText(MusicPlayerActivity.this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    songTitle.setText(songList.get(currentSongIndex));
                }
                isPlaying = !isPlaying;
            }
        });

        // 设置前进和后退按钮
        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    int newPosition = mediaPlayer.getCurrentPosition() + 10000;
                    mediaPlayer.seekTo(Math.min(newPosition, mediaPlayer.getDuration()));
                }
            }
        });
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    int newPosition = mediaPlayer.getCurrentPosition() - 10000;
                    mediaPlayer.seekTo(Math.max(newPosition, 0));
                }
            }
        });

        // 设置进度条
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(progress * mediaPlayer.getDuration() / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 请求存储权限
        checkAndRequestStoragePermissions();

        // 显示悬浮窗口
        showSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });

        // 设置上一曲和下一曲按钮
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSong();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });

        // 自动播放下一首
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });

        // 设置添加歌曲按钮
//        //addSongButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openFilePicker();
//            }
//        });

        // 加载应用内嵌音乐
        loadEmbeddedSongs();

        // 开始后台任务更新进度条和文本
        new MyTask().execute();
    }

    private void checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_PERMISSION_STORAGE);
            } else {
                loadLocalSongs();
            }
        } else {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION_STORAGE);
            } else {
                loadLocalSongs();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadLocalSongs();
            } else {
                Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadLocalSongs() {
        // 从特定目录加载歌曲
        File musicDir = new File(Environment.getExternalStorageDirectory(), "Music/MP3");
        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        if (!songSet.contains(file.getAbsolutePath())) {
                            songList.add(file.getName());
                            songUrlList.add(file.getAbsolutePath());
                            songSet.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        // 从本地存储读取歌曲列表
        scanDeviceForMp3Files();

        // 更新适配器以显示歌曲列表
        if (songList.isEmpty()) {
            Toast.makeText(this, "没有找到本地音乐文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEmbeddedSongs() {
        // 添加应用内嵌音乐文件
        String[] embeddedSongs = {"song1.mp3", "song2.mp3", "song3.mp3"}; // 替换为你的音乐文件名
        String[] songNames = {"小兔子乖乖", "新年好", "别看我只是一只羊"}; // 替换为你想显示的歌曲名称

        for (int i = 0; i < embeddedSongs.length; i++) {
            int resId = getResources().getIdentifier(embeddedSongs[i].replace(".mp3", ""), "raw", getPackageName());
            if (resId != 0) {
                songList.add(songNames[i]); // 使用自定义歌曲名称
                songUrlList.add("android.resource://" + getPackageName() + "/" + resId);
                songSet.add("android.resource://" + getPackageName() + "/" + resId);
            }
        }
    }

    private void scanDeviceForMp3Files() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if (path.endsWith(".mp3")) {
                    if (!songSet.contains(path)) {
                        songList.add(new File(path).getName());
                        songUrlList.add(path);
                        songSet.add(path);
                    }
                }
            }
            cursor.close();
        }
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Audio.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlaying) {
            mediaPlayer.start();
        }
    }

    private void playSelectedSong() {
        if (songUrlList.isEmpty()) {
            Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer.reset();
            if (songUrlList.get(currentSongIndex).startsWith("android.resource://")) {
                mediaPlayer.setDataSource(this, Uri.parse(songUrlList.get(currentSongIndex)));
            } else {
                mediaPlayer.setDataSource(songUrlList.get(currentSongIndex));
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            isPlaying = true;
            songTitle.setText(songList.get(currentSongIndex)); // 更新歌曲标题
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void playCurrentSong() {
        if (songUrlList.isEmpty()) {
            Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        playSelectedSong();
    }

    private void nextSong() {
        if (songUrlList.isEmpty()) {
            Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSongIndex = (currentSongIndex + 1) % songUrlList.size();
        playSelectedSong();
    }

    private void prevSong() {
        if (songUrlList.isEmpty()) {
            Toast.makeText(this, "没有可播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSongIndex = (currentSongIndex - 1 + songUrlList.size()) % songUrlList.size();
        playSelectedSong();
    }

    private class MyTask extends AsyncTask<Object, Integer, Object> {
        @Override
        protected Object doInBackground(Object... objects) {
            while (flag) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(1);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mediaPlayer == null || !mediaPlayer.isPlaying()) return;
            int duration = mediaPlayer.getDuration();
            int current = mediaPlayer.getCurrentPosition();
            seekBar.setProgress((int) ((current / (float) duration) * 100));

            String str = "  总时长：" + (duration / 1000 / 60) + "分" + (duration / 1000 % 60) + "秒";
            str += "  当前进度：" + (current / 1000 / 60) + "分" + (current / 1000 % 60) + "秒";
            textView.setText(str);
        }
    }

    private void showPopupWindow() {
        // 创建并显示悬浮窗口
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_song_list, null);
        popupWindow = new PopupWindow(popupView,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                true);

        ListView popupSongListView = popupView.findViewById(R.id.popupSongListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        popupSongListView.setAdapter(adapter);

        popupSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSongIndex = position;
                playSelectedSong();
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(findViewById(R.id.showSongsButton),
                android.view.Gravity.CENTER, 0, 0);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "选择音频文件"), REQUEST_CODE_PICK_SONG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_SONG && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = getPathFromUri(uri);
                if (filePath != null && !songSet.contains(filePath)) {
                    String fileName = new File(filePath).getName();
                    songList.add(fileName);
                    songUrlList.add(filePath);
                    songSet.add(filePath);
                    Toast.makeText(this, "歌曲已添加: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadLocalSongs();
                } else {
                    Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
