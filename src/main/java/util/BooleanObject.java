package util;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class BooleanObject {
	private boolean value;

	public BooleanObject(boolean value) {
		this.value = value;
	}

	public void setOr(BooleanObject obj1, BooleanObject obj2) {
		if (obj1 == null) {
			this.value = obj2.value;
			return;
		}

		if (obj2 == null) {
			this.value = obj1.value;
			return;
		}

		this.value = obj1.value || obj2.value;
		return;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value + "";
	}

}
