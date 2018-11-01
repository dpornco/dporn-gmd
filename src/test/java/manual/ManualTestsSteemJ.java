package manual;

import java.util.List;

import co.dporn.gmd.servlet.SteemJInstance;

public class ManualTestsSteemJ {

	public static void main(String[] args) {
		List<String> verified = SteemJInstance.getNsfwVerifiedList();
		for (String name: verified) {
			System.out.println("V: "+name);
		}
		System.out.println("TOTAL: "+verified.size());
		List<String> active = SteemJInstance.getActiveNsfwVerifiedList();
		for (String name: active) {
			System.out.println("A: "+name);
		}
		System.out.println("ACTIVE: "+active.size());
	}

}
