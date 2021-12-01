package net.sistr.flexibleguns.wip.gun;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class SequentialSoundData {
    private final ImmutableMap<Integer, ImmutableList<ISoundData>> sounds;

    public SequentialSoundData(Map<Integer, List<ISoundData>> sounds) {
        ImmutableMap.Builder<Integer, ImmutableList<ISoundData>> builder = ImmutableMap.builder();
        sounds.forEach((key, value) -> builder.put(key, ImmutableList.copyOf(value)));
        this.sounds = builder.build();
    }

    public ImmutableList<ISoundData> getSounds(int time) {
        ImmutableList<ISoundData> data = sounds.get(time);
        return data != null ? data : ImmutableList.of();
    }
}
