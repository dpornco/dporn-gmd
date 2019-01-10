package co.dporn.gmd.shared;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import steem.model.Beneficiary;

public interface DpornConsts {
	Set<String> MANDATORY_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw"));
	Set<String> MANDATORY_VIDEO_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "dporncovideo"));
	Set<String> MANDATORY_EROTICA_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "erotica"));
	Set<String> MANDATORY_PHOTO_GALLERY_TAGS = new TreeSet<>(Arrays.asList("dporn", "nsfw", "photos"));
	List<Beneficiary> BENEFICIARIES_DEFAULT = Arrays.asList(//
			new Beneficiary("dporn", 200), //
			new Beneficiary("dporn.pay", 700), //
			new Beneficiary("dpornco", 600));
	List<Beneficiary> BENEFICIARIES_BLOG_ENTRIES = Arrays.asList(//
			new Beneficiary("dpornco", 100));
	String APP_ID_VERSION = "dporn/2.0";
}
