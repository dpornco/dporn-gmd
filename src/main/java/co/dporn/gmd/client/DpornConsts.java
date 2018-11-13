package co.dporn.gmd.client;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public interface DpornConsts {
	Set<String> MANDATORY_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw"));
	Set<String> MANDATORY_VIDEO_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "dporncovideo"));
	Set<String> MANDATORY_EROTICA_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "erotica"));
	Set<String> MANDATORY_PHOTO_GALLERY_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "photos"));
}
