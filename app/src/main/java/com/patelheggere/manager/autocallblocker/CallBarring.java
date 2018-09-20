package com.patelheggere.manager.autocallblocker;

/**
 * Created by manager on 23/8/16.
 */

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

// Extend the class from BroadcastReceiver to listen when there is a incoming call
public class CallBarring extends BroadcastReceiver
{
    private static final String TAG = "CallBarring";
    // This String will hold the incoming phone number
    private String number;
    // Object of BlacklistDAO to query to database
    private BlacklistDAO blackListDao;

    // It holds the list of Blacklist objects fetched from Database
    public static List<Blacklist> blockList;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "call barring: ");
        // If, the received action is not a type of "Phone_State", ignore it
        if (!intent.getAction().equals("android.intent.action.PHONE_STATE"))
            return;

            // Else, try to do some action
        else
        {
            // Fetch the number of incoming call
            number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            // Check, whether this is a member of "Black listed" phone numbers stored in the database
            Log.d(TAG, "onReceive: "+number);
            if(number!=null) {
                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                    blackListDao = new BlacklistDAO(context);

                    // Fetch the list of Black listed numbers from Database using DAO object
                    blockList = blackListDao.getAllBlacklist();

                    if (!blockList.contains(new Blacklist(number))) {
                        // If yes, invoke the method
                        sendSms(number, "", false);
                        disconnectPhoneItelephony(context);

                        return;
                    }
                }
                else {
                    return;
                }
            }
        }
    }


    private void sendSms(String phonenumber,String message, boolean isBinary)
    {
        Log.d(TAG, "sendSms: "+message);
        SmsManager manager = SmsManager.getDefault();
        message = "ಸಂಪರ್ಕಿಸಲು ಈ ನಂಬರ್ ಗೆ ಕರೆ ಮಾಡಿ 9449018633";
        String message2 = "To Contact Please call on " ;
        manager.sendTextMessage(phonenumber, null, message2+" "+message, null, null);
    }

    // Method to disconnect phone automatically and programmatically
    // Keep this method as it is
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void disconnectPhoneItelephony(Context context)
    {
        ITelephony telephonyService;
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        try
        {
            Class c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(telephony);
            telephonyService.endCall();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}