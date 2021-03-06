package com.agasinsk.recman.audioconverter;

import android.content.Context;

import com.agasinsk.recman.models.AudioFormat;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;

import java.io.File;
import java.io.IOException;

public class AndroidAudioConverter {

    private static boolean loaded;

    private final Context context;
    private File audioFile;
    private AudioFormat format;
    private IConvertCallback callback;
    private String details;

    private AndroidAudioConverter(Context context) {
        this.context = context;
    }

    private static boolean isLoaded() {
        return loaded;
    }

    public static void load(Context context, final ILoadCallback callback) {
        try {
            FFmpeg.getInstance(context).loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess() {
                    loaded = true;
                    callback.onSuccess();
                }

                @Override
                public void onFailure() {
                    loaded = false;
                    callback.onFailure(new Exception("Failed to loaded FFmpeg lib"));
                }

                @Override
                public void onFinish() {

                }
            });
        } catch (Exception e) {
            loaded = false;
            callback.onFailure(e);
        }
    }

    public static AndroidAudioConverter with(Context context) {
        return new AndroidAudioConverter(context);
    }

    public AndroidAudioConverter setFile(File originalFile) {
        this.audioFile = originalFile;
        return this;
    }

    public AndroidAudioConverter setFormat(AudioFormat format) {
        this.format = format;
        return this;
    }

    public AndroidAudioConverter setAudioFormatDetails(String audioFormatDetails) {
        if (this.format != null && format == AudioFormat.MP3) {
            this.details = audioFormatDetails;
        }

        return this;
    }

    public AndroidAudioConverter setCallback(IConvertCallback callback) {
        this.callback = callback;
        return this;
    }

    public void convert() {
        if (!isLoaded()) {
            callback.onFailure(new Exception("FFmpeg not loaded"));
            return;
        }
        if (audioFile == null || !audioFile.exists()) {
            callback.onFailure(new IOException("File not exists"));
            return;
        }
        if (!audioFile.canRead()) {
            callback.onFailure(new IOException("Can't read the file. Missing permission?"));
            return;
        }
        final File convertedFile = getConvertedFile(audioFile, format);

        final String[] command = getCommandString(audioFile.getPath(), convertedFile.getPath());
        try {
            FFmpeg.getInstance(context).execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onStart() {
                    callback.onStart();
                }

                @Override
                public void onProgress(String message) {
                    callback.onProgress(message);
                }

                @Override
                public void onSuccess(String message) {
                    callback.onSuccess(convertedFile);
                }

                @Override
                public void onFailure(String message) {
                    callback.onFailure(new IOException(message));
                }

                @Override
                public void onFinish() {
                    callback.onFinish();
                }
            });
        } catch (Exception e) {
            convertedFile.delete();
            callback.onFailure(e);
        }
    }

    private String[] getCommandString(String sourceFile, String destinationFile) {
        if (format == AudioFormat.MP3) {
            return new String[]{"-y", "-i", sourceFile, "-b:a", details + "k", destinationFile};
        }
        return new String[]{"-y", "-i", sourceFile, destinationFile};
    }

    private static File getConvertedFile(File originalFile, AudioFormat format) {
        String[] f = originalFile.getPath().split("\\.");
        String filePath = originalFile.getPath().replace(f[f.length - 1], format.getFormat());
        return new File(filePath);
    }
}