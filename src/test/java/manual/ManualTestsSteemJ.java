package manual;

import java.util.List;

import co.dporn.gmd.servlet.utils.SteemJInstance;

public class ManualTestsSteemJ {

	public static void main(String[] args) {
		List<String> verified = SteemJInstance.get().getNsfwVerifiedList();
		for (String name: verified) {
			System.out.println("V: "+name);
		}
		System.out.println("TOTAL: "+verified.size());
		List<String> active = SteemJInstance.get().getActiveNsfwVerifiedList();
		for (String name: active) {
			System.out.println("A: "+name);
		}
		System.out.println("ACTIVE: "+active.size());
	}

}
