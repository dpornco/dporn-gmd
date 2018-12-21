package manual;

import java.util.List;
import java.util.Set;

import co.dporn.gmd.servlet.utils.steemj.SteemJInstance;

public class ManualTestsSteemJ {

	public static void main(String[] args) {
		Set<String> verified = SteemJInstance.get().getDpornVerifiedSet();
		for (String name: verified) {
			System.out.println("V: "+name);
		}
		System.out.println("TOTAL: "+verified.size());
		List<String> active = SteemJInstance.get().getActiveDpornVerifiedList();
		for (String name: active) {
			System.out.println("A: "+name);
		}
		System.out.println("ACTIVE: "+active.size());
	}

}
