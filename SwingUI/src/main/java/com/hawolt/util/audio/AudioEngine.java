package com.hawolt.util.audio;

import com.hawolt.Swiftrift;
import com.hawolt.io.Core;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created: 09/08/2023 11:37
 * Author: Twitter @hawolt
 **/

public class AudioEngine {

    private static final Line.Info PLAYBACK_DEVICE = new Line.Info(SourceDataLine.class);
    private static final AudioFormat BASE_AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            AudioSystem.NOT_SPECIFIED,
            16, 2, 4,
            AudioSystem.NOT_SPECIFIED, true
    );
    private static final DataLine.Info BASE_DATA_LINE = new DataLine.Info(Clip.class, BASE_AUDIO_FORMAT);
    private static final List<Mixer> SUPPORTED = new LinkedList<>();
    private static final Map<String, byte[]> CACHE = new HashMap<>();
    public static Mixer SELECTED_MIXER;

    private static Float gain;

    public static void install() {
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(PLAYBACK_DEVICE) && mixer.isLineSupported(BASE_DATA_LINE)) {
                SUPPORTED.add(mixer);
            }
        }
        SELECTED_MIXER = getDefaultMixer();
    }

    public static void setGain(Float gain) {
        AudioEngine.gain = gain;
    }

    public static void setMasterOutput(float volume) {
        AudioEngineMasterOutput.setMasterOutputVolume(volume);
    }

    public static Mixer getDefaultMixer() {
        if (SUPPORTED.isEmpty()) return null;
        return SUPPORTED.get(0);
    }

    private static ByteArrayOutputStream convertToWaveFormatFromMP3(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(new BufferedInputStream(stream));
        AudioFormat sourceFormat = mp3Stream.getFormat();
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(), 16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);
        AudioInputStream converted = AudioSystem.getAudioInputStream(format, mp3Stream);
        File file = File.createTempFile(UUID.randomUUID().toString(), ".swift-rift");
        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(Files.readAllBytes(file.toPath()));
        if (file.delete()) Logger.debug("[audio-engine] deleted temporary conversion file:{}", file.getName());
        return outputStream;
    }

    private static void cache(Sound sound) throws UnsupportedAudioFileException, IOException {
        try (InputStream inputStream = RunLevel.get(String.join("/", "audio", sound.filename))) {
            ByteArrayOutputStream outputStream = sound.filename.endsWith("mp3")
                    ? convertToWaveFormatFromMP3(inputStream)
                    : Core.read(inputStream);
            CACHE.put(sound.filename, outputStream.toByteArray());
        }
    }

    public static void play(Sound sound) {
        Swiftrift.service.execute(() -> {
            try {
                if (!CACHE.containsKey(sound.filename)) cache(sound);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(CACHE.get(sound.filename));
                try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(byteStream))) {
                    Clip clip = AudioSystem.getClip(SELECTED_MIXER.getMixerInfo());
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.open(audioInputStream);
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(gain);
                    clip.start();
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                Logger.error("[audio-engine] failed to play sound:{}", sound.filename);
                Logger.error(e);
            }
        });
    }
}
