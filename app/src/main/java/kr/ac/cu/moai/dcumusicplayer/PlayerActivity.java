package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3");
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);

            SeekBar tvSeek = findViewById(R.id.tvSeek);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(mp3file);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                mp.setLooping(false);
                tvSeek.setMax(mp.getDuration());
                tvSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            mediaPlayer.seekTo(progress);
                        }
                        if(seekBar.getMax() == progress){
                            mediaPlayer.stop();
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.pause();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                        mediaPlayer.start();
                    }
                });
                thread = new Thread(() -> {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()){
                        try {
                            Thread.sleep(1000);
                            runOnUiThread(() -> {
                                int current = mediaPlayer.getCurrentPosition();
                                tvSeek.setProgress(current);
                            });
                        }catch (Exception e){}
                    }
                });
                thread.start();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        mediaPlayer.stop();
        return super.onSupportNavigateUp();
    }

}