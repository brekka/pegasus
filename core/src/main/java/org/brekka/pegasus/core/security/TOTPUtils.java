package org.brekka.pegasus.core.security;

/**
 * Based on the code from the following link.
 * 
 * @see https://github.com/parkghost/TOTP-authentication-demo/blob/master/src/main/java/me/brandonc/security/totp/util/TOTPUtils.java
 */
public class TOTPUtils {

	private static final int PASS_CODE_LENGTH = 6;

	private static final int INTERVAL = 30;

	private static final int WINDOW = 10;

	private static final String CRYPTO = "HmacSHA1";

	public static boolean checkCode(byte[] secret, long code) {

		// Window is used to check codes generated in the near past.
		// You can use this value to tune how far you're willing to go.
		int window = WINDOW;
		long currentInterval = getCurrentInterval();

		for (int i = -window; i <= window; ++i) {
			long hash = TOTP.generateTOTP(secret, currentInterval + i, PASS_CODE_LENGTH, CRYPTO);

			if (hash == code) {
				return true;
			}
		}

		// The validation code is invalid.
		return false;
	}

	private static long getCurrentInterval() {
		long currentTimeSeconds = System.currentTimeMillis() / 1000;
		return currentTimeSeconds / INTERVAL;
	}

}