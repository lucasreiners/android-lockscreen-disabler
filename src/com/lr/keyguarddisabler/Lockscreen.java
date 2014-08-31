package com.lr.keyguarddisabler;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Lockscreen implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	XSharedPreferences prefs;
	private long lastLockTime = 0; 
	/**
	 * How long (in ms) do we wait before we start enforcing the normal keyguard lock?
	 * A value of zero means we never enforce it.  The value is stored in minutes
	 * in the preferences, but we always handle it in ms.
	 */
	private int lockScreenTimeoutToEnforce;
	private String lockScreenType = LOCK_SCREEN_TYPE_SLIDE;
	private boolean LOG = false;
	
	final private static String LOCK_SCREEN_TYPE_NONE   = "none";
    final private static String LOCK_SCREEN_TYPE_SLIDE  = "slide";
    final private static String LOCK_SCREEN_TYPE_DEVICE = "device";

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences("com.lr.keyguarddisabler");
		prefs.makeWorldReadable();
		lockScreenTimeoutToEnforce = Integer.parseInt(prefs.getString("lockscreentimeout", "1")) * 60 * 1000;
		LOG = prefs.getBoolean("logtofile", false);
		lockScreenType = prefs.getString("lockscreentype", LOCK_SCREEN_TYPE_SLIDE);
		// Make sure we start any bootup locked...
	}
	
	/**
	 * Reload our preferences from file, and update
	 * any class variables that reflect preference values.
	 */
	private void reloadPrefs()
	{
	    prefs.reload();
	    lockScreenTimeoutToEnforce = Integer.parseInt(prefs.getString("lockscreentimeout", "1")) * 60 * 1000;
        LOG = prefs.getBoolean("logtofile", false);
        lockScreenType = prefs.getString("lockscreentype", LOCK_SCREEN_TYPE_SLIDE);
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

		boolean foundPackage = false;

		// Code for Slide-To-Unlock
		// Android 4.4
		if (lpparam.packageName.contains("android.keyguard")) {
		    foundPackage = true;
		    if (LOG) XposedBridge.log("Keyguard Disabler: Loading Kitkat specific code");
			hookGetSecurityMode(lpparam, 
			        "com.android.keyguard.KeyguardSecurityModel$SecurityMode", 
			        "com.android.keyguard.KeyguardSecurityModel", 
                    "com.android.keyguard.KeyguardViewMediator", 
			        "Android 4.4");

		}

		// Android < 4.4
		else if (lpparam.packageName.contains("android.internal")) {
		    foundPackage = true;
		    if (LOG) XposedBridge.log("Keyguard Disabler: Loading Jellybean specific code");
			hookGetSecurityMode(lpparam, 
			        "com.android.internal.policy.impl.keyguard.KeyguardSecurityModel$SecurityMode", 
			        "com.android.internal.policy.impl.keyguard.KeyguardSecurityModel",  
                    "com.android.internal.policy.impl.keyguard.KeyguardViewMediator", 
			        "Android < 4.4");
		}

		// HTC
		else if (lpparam.packageName.contains("com.htc.lockscreen")) {
		    foundPackage = true;
		    if (LOG) XposedBridge.log("Keyguard Disabler: Loading HTC specific code");

			try {
			    if (LOG) XposedBridge.log("Keyguard Disabler: Loading HTC specific code - trying new Sense 5.x Code - thanks to XDA @Sirlatrom");
				hookGetSecurityMode(lpparam, 
				        "com.htc.lockscreen.HtcKeyguardSecurityModel$SecurityMode", 
				        "com.htc.lockscreen.HtcKeyguardSecurityModel", 
	                    "com.htc.lockscreen.HtcKeyguardViewMediator", 
				        "HTC Sense 5.x");
				
			} catch (ClassNotFoundError ex) {
			    if (LOG) XposedBridge.log("Keyguard Disabler: Loading HTC specific code - trying new Sense 6 Code - thanks to XDA @Sirlatrom");
				hookGetSecurityMode(lpparam, 
				        "com.htc.lockscreen.keyguard.KeyguardSecurityModel$SecurityMode", 
				        "com.htc.lockscreen.keyguard.KeyguardSecurityModel",
	                    "com.htc.lockscreen.keyguard.KeyguardViewMediator", 
						"HTC Sense 6");
			}
		}
		
		// Only modify the LockPatternUtils methods for the keyguard.  Let
		// all other entities in the system think the keyguard is still 
		// properly in place.  This prevents DeviceAdministrator apps from
		// locking things down inappropriately if they call one of these methods.
		if (foundPackage)
		{
		    // Code for complete deactivation
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		    {
		        XposedHelpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", lpparam.classLoader, "isLockScreenDisabled", new XC_MethodHook() {
		            @Override
		            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		                reloadPrefs();
		                if (lockScreenType.equals(LOCK_SCREEN_TYPE_NONE)) {
		                    param.setResult(true);
		                } else if (lockScreenType.equals(LOCK_SCREEN_TYPE_SLIDE)) {
		                    param.setResult(false);
		                } 
		                // Else, this is the 'device' type, in which case we want to let
		                // this method fall through to its normal implementation, so we
		                // won't set any result value, allowing the normal method to be called.
		            }
		        });
		    } 
		    // Supposedly this allows < 4.2 to work...not for me.  Has anybody confirmed it?
		    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
		    {
		        // Pretty sure this doesn't work  - in theory it works for
	            XposedHelpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", lpparam.classLoader, "isSecure", new XC_MethodHook() {

	                @Override
	                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	                    reloadPrefs();
	                    if (lockScreenType.equals(LOCK_SCREEN_TYPE_SLIDE) || lockScreenType.equals(LOCK_SCREEN_TYPE_NONE)) {
	                        param.setResult(false);
	                    }
	                    // Else this is a 'device' type, in which case we want to let
                        // this method fall through to its normal implementation, so we
                        // won't set any result value, allowing the normal method to be called.
	                }
	            });
		    }
		}
	}

	/**
	 * Helper method to handle various incarnations of getSecurityMode.  Generally, it will
	 * return a security mode of None if we want the keyguard disabled, otherwise it returns
	 * the normal value. 
	 * @param lpparam
	 * @param nestedClassName
	 * @param outerClassName
	 * @param mediatorClassName
	 * @param variant
	 * @throws ClassNotFoundError
	 */
	private void hookGetSecurityMode(final LoadPackageParam lpparam,
			final String nestedClassName, final String outerClassName,
			final String mediatorClassName, final String variant) throws ClassNotFoundError {	

	    final Class<?> securityModeEnum = XposedHelpers.findClass(nestedClassName, lpparam.classLoader);
	    final Object securityModePassword = XposedHelpers.getStaticObjectField(securityModeEnum, "Password");
	    final Object securityModeNone = XposedHelpers.getStaticObjectField(securityModeEnum, "None");
	    final Object securityModePattern = XposedHelpers.getStaticObjectField(securityModeEnum, "Pattern");
        final Object securityModePin = XposedHelpers.getStaticObjectField(securityModeEnum, "PIN");
        final Object securityModeBiometric = XposedHelpers.getStaticObjectField(securityModeEnum, "Biometric");
        // LG KnockOn - likely doesn't work for other knock on implementations...
        Object tmpField;
        try {
            tmpField = XposedHelpers.getStaticObjectField(securityModeEnum, "KnockOn");            
        } catch (NoSuchFieldError nsfe)
        {
            tmpField = "bad_field "+nsfe.getMessage();
        }
        final Object securityModeKnockOn = tmpField;
	    
        if (LOG) XposedBridge.log("Keyguard Disabler: SecurityEnum (" + variant + ") found and parsed");
		
		// We will know when the lockscreen is enabled or disabled, and update our lastLockTime
		// appropriately.  In short, whenever the lock screen locks, our timer starts.  The one
		// time we care about the lockscreen going away is on initial bootup.  We always want
		// the normal lock to show on initial bootup to ensure somebody can't just reboot the
		// phone to get access...
		XposedHelpers.findAndHookMethod(mediatorClassName, lpparam.classLoader, "handleHide", new XC_MethodHook() {
		    @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		        if (LOG) XposedBridge.log("Keyguard Disabler: About to unlock...");        
                if (lastLockTime == 0)
                {
                    lastLockTime = SystemClock.elapsedRealtime();
                }
		    }
		});

        XposedHelpers.findAndHookMethod(mediatorClassName, lpparam.classLoader, "handleShow", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                reloadPrefs();     
                if (lastLockTime != 0)
                {
                    // If not zero, we'll set it to now. If zero, we'll set it when we first unlock
                    // This ensures at initial bootup, we're always locked.
                    lastLockTime = SystemClock.elapsedRealtime();
                }                
                if (LOG) XposedBridge.log("Keyguard Disabler: We are locking...time to enforcement: "+((lockScreenTimeoutToEnforce - (SystemClock.elapsedRealtime() - lastLockTime)) / 1000));
            }
        });

        // We use the afterHook here to let the normal method handle all the logic of what the
        // security mode should be.  If the normal method returns that it is a password, pin
        // etc., and we want to override it (based on time), then we'll return None instead.
		XposedHelpers.findAndHookMethod(outerClassName, lpparam.classLoader, "getSecurityMode", new XC_MethodHook() {
		
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // Don't reload preferences here..it's too expensive for the frequency
                // that this method gets called.  We'll reload on lock...
                
                // If they requested 'device' - this means they want the mod basically disabled,
                // so we'll honor the device settings.
                if (lockScreenType.equals(LOCK_SCREEN_TYPE_DEVICE))
                {
                    return;
                }
                
                final long currTime = SystemClock.elapsedRealtime();
                if ((lockScreenTimeoutToEnforce > 0) && ((currTime - lastLockTime) > lockScreenTimeoutToEnforce))
                {
                    if (LOG) XposedBridge.log("Keyguard Disabler: Lock as normal...time expired: "+((currTime - lastLockTime) / 1000 / 60)+" >= "+(lockScreenTimeoutToEnforce / 1000 / 60));
                    return;
                } else
                {
                    Object secModeResult = param.getResult();
                    if (LOG) XposedBridge.log("secMode is: "+secModeResult);
                    if (securityModePassword.equals(secModeResult) ||
                            securityModePattern.equals(secModeResult) ||
                            securityModePin.equals(secModeResult) ||
                            securityModeBiometric.equals(secModeResult) ||
                            securityModeKnockOn.equals(secModeResult))
                    {
                        param.setResult(securityModeNone);
                        if (LOG) XposedBridge.log("Keyguard Disabler: Hiding the lock...time not expired: "+((currTime - lastLockTime) / 1000 / 60)+" <= "+(lockScreenTimeoutToEnforce / 1000 / 60));
                    }      
                }
            }
		});
	}
}
