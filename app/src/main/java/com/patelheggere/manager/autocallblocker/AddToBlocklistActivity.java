package com.patelheggere.manager.autocallblocker;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddToBlocklistActivity extends Activity implements OnClickListener {

    // Declaration all on screen components
    private EditText country_code_et, phone_et;
    private Button reset_btn, submit_btn;

    // Declaration of BlacklistDAO to interact with SQlite database
    private com.patelheggere.manager.autocallblocker.BlacklistDAO blackListDao;

    public static int CONTACT_REQUEST = 99;
    private Button fromContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.patelheggere.manager.autocallblocker.R.layout.activity_add_to_blocklist);

        // Initialization of the DAO object.
        blackListDao = new com.patelheggere.manager.autocallblocker.BlacklistDAO(this);

        fromContacts = findViewById(com.patelheggere.manager.autocallblocker.R.id.btn_from_contacts);
        fromContacts.setOnClickListener(this);

        country_code_et = (EditText) findViewById(com.patelheggere.manager.autocallblocker.R.id.country_code_et);
        phone_et = (EditText) findViewById(com.patelheggere.manager.autocallblocker.R.id.phone_et);
        country_code_et.setText("91");
        reset_btn = (Button) findViewById(com.patelheggere.manager.autocallblocker.R.id.reset_btn);
        submit_btn = (Button) findViewById(com.patelheggere.manager.autocallblocker.R.id.submit_btn);

        reset_btn.setOnClickListener(this);
        submit_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v == submit_btn)
        {
            if(country_code_et.getText().toString().trim().length() == 0 &&
                    phone_et.getText().toString().trim().length() == 13)
            {
                // Once click on "Submit", it's first creates the Blacklist object
                final com.patelheggere.manager.autocallblocker.Blacklist phone = new com.patelheggere.manager.autocallblocker.Blacklist();

                // Then, set all the values from user input
                phone.phoneNumber =  phone_et.getText().toString();
                // Insert the object to the database
                blackListDao.create(phone);

                // Show the success message to user
                showDialog();
            }
            // All input fields are mandatory, so made a check
            else if(country_code_et.getText().toString().trim().length() > 0 &&
                    phone_et.getText().toString().trim().length() == 10)
            {
                // Once click on "Submit", it's first creates the Blacklist object
                final com.patelheggere.manager.autocallblocker.Blacklist phone = new com.patelheggere.manager.autocallblocker.Blacklist();

                // Then, set all the values from user input
                phone.phoneNumber = "+" + country_code_et.getText().toString() + phone_et.getText().toString();

                // Insert the object to the database
                blackListDao.create(phone);

                // Show the success message to user
                showDialog();
            }
            // Show a dialog with appropriate message in case input fields are blank
            else
            {
                showMessageDialog("All fields are mandatory and correct 10 digit !!");
            }
        }
        else if(v == reset_btn)
        {
            reset();
        }

        if(v==fromContacts)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, CONTACT_REQUEST);
        }
    }

    // Clear the entered text
    private void reset()
    {
        //country_code_et.setText("");
        phone_et.setText("");
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case 99:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while(numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                num = num.replaceAll("-","");
                                Log.d("TZG", "onActivityResult: "+num+"\n length:"+num.length());
                                if (num.length() == 13) {
                                    phone_et.setText(num);
                                    country_code_et.setText("");
                                    country_code_et.setEnabled(false);
                                    return;
                                }
                                else if (num.length() == 10) {
                                    phone_et.setText(num);
                                    country_code_et.setText("+91");
                                    country_code_et.setEnabled(true);
                                    return;
                                }
                                else if (num.length()==11 && num.charAt(0)=='0')
                                {
                                    phone_et.setText(num.substring(1, num.length()));
                                    country_code_et.setText("91");
                                    country_code_et.setEnabled(false);
                                    return;
                                }
                            }
                            //Toast.makeText(MainActivity.this, "Number="+num, Toast.LENGTH_LONG).show();

                        }
                    }
                    break;
                }
        }
    }

    private void showDialog()
    {
        // After submission, Dialog opens up with "Success" message. So, build the AlartBox first
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set the appropriate message into it.
        alertDialogBuilder.setMessage("Phone Number added to receiving list successfully !!");

        // Add a positive button and it's action. In our case action would be, just hide the dialog box ,
        // and erase the user inputs.
        alertDialogBuilder.setPositiveButton("Add More",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        reset();
                    }
                });

        // Add a negative button and it's action. In our case, close the current screen
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        // Now, create the Dialog and show it.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showMessageDialog(final String message)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
