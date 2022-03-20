package muni.cotemplate.module.configurations.input;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public class COTemplateConfiguration {
    @SerializedName("mask")
    public String mask;

    @SerializedName("mask_elements")
    public List<COTemplateMaskElement> elements;

    public int getimesCount() {
        return elements.stream().findFirst().orElse(new COTemplateMaskElement()).times.size();
    }

    public HashMap<Character, Integer> getCharacterCounts() {
        HashMap<Character, Integer> characterCounts = new HashMap<>();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            characterCounts.putIfAbsent(c, 0);
            characterCounts.put(c, characterCounts.get(c) + 1);
        }

        return characterCounts;
    }
}
