package com.example.bluetooth.le;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;


public class LoginActivity extends AppCompatActivity implements ValueEventListener, DatabaseReference.CompletionListener {

    private EditText et_customer_mail;
    private EditText et_customer_psw;

    private String user_id;
    private String customer_mail;

    private DatabaseReference reference;
    private FirebaseAuth auth;

    private User user;
    private CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = new User("","");

        // reference to the database's customers root node
        reference = FirebaseDatabase.getInstance().getReference();

        // get an instance of the authentication handler
        auth = FirebaseAuth.getInstance();

        HealthApplication.setDatabase(reference,auth);

        // try to authenticate user if an auth session is present
        if (auth.getCurrentUser() != null){
            final String email = auth.getCurrentUser().getEmail();

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            RetrieveUserInfoAndGoToMainActivity(dataSnapshot, email);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        // if no session found, display the activity
        else{

            setContentView(R.layout.activity_login);

            et_customer_mail = findViewById(R.id.cust_email);
            et_customer_psw = findViewById(R.id.rest_password);
            Button btn_login = findViewById(R.id.login_btn);
            Button btn_newcustomer = findViewById(R.id.newcust_btn);
            checkbox = findViewById(R.id.checkBox);

            // create a listener to handle clicks on login button
            LoginButtonListener lbtn_listener = new LoginButtonListener(this);
            btn_login.setOnClickListener(lbtn_listener);

            // create a listener to handle clicks on new customer button
            NewCustButtonListener nrbtn_listener = new NewCustButtonListener(this);
            btn_newcustomer.setOnClickListener(nrbtn_listener);

            SharedPreferences pref = getApplicationContext().getSharedPreferences("login_mail", 0); // 0 - for private mode
            if(pref.getString("email_login", null) != null) {
                et_customer_mail.setText(pref.getString("email_login", null));
                checkbox.setChecked(true);
                et_customer_psw.requestFocus();
            }

        }

    }

    @Override
    // ----------------------------------------
    // ---- Handles the customer information retrieval after the click on Login
    // ----------------------------------------
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        RetrieveUserInfoAndGoToMainActivity(dataSnapshot, et_customer_mail.getText().toString());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}

    @Override
    // ----------------------------------------
    // ---- Handles the opening of the MainActivity when the new customer is pushed in the DB
    // ----------------------------------------
    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

        user.setID(user_id);
        user.setEmail(customer_mail);

        // tell the application to use this customer id nad mail
        ((HealthApplication)this.getApplication()).setUser(user);

        // Open the customer profile activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    /**
     * Retrieve from FireBase db info from a user, given its email address. This info are used to populate
     * the Customer object that represent this user. After that, moves the user in the Restaurant List Activity
     * @param customersSnapshot A FireBase DB snapshot on Customers node
     * @param email The user's mail we are trying to auth
     */
    private void RetrieveUserInfoAndGoToMainActivity(DataSnapshot customersSnapshot, String email){

        // for each customer
        for (DataSnapshot ds : customersSnapshot.getChildren()){

            String mail = (String)ds.child("email").getValue();

            // if the current element has the same email as the one written by the user (or last session)
            if (email.equals(mail)){

                // set the rest id
                user_id = ds.getKey();
                user.setID(user_id);
                user.setEmail(email);
                user.setMac_address((String)ds.child("mac_address").getValue());
                user.setCurrent_steps((String)ds.child("current_steps").getValue());
                user.setCurrent_heart_rate((String)ds.child("current_heart_rate").getValue());
                //user.setHeart_rate((String)ds.child("heart_rate").getValue());
                //user.setCurrent_steps((String)ds.child("current_steps").getValue());
                // tell the application to use this customer id and mail
                ((HealthApplication)this.getApplication()).setUser(user);

                    // Open the reservation list activity
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
            }
        }
    }


    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    // ---- This class is a listener that reacts to click to the login button
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    public class LoginButtonListener implements View.OnClickListener{

        private final LoginActivity login_activity;

        // class constructor
        public LoginButtonListener(LoginActivity l){
            login_activity = l;
        }

        @Override
        public void onClick(View v) {

            final String mail = et_customer_mail.getText().toString();
            final String psw = et_customer_psw.getText().toString();

            // do nothing if the user didn't insert the customer mail or password
            if (mail.trim().length() == 0 || psw.trim().length() == 0)
                return;

            // try to login the user to firebase auth
            auth.signInWithEmailAndPassword(mail, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        // called when the operation is complete
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // if the user is successfully logged
                            if (task.isSuccessful()) {

                                // store this customer mail
                                customer_mail = mail;


                                SharedPreferences pref = getApplicationContext().getSharedPreferences("login_mail", 0); // 0 - for private mode
                                SharedPreferences.Editor editor = pref.edit();
                                if(checkbox.isChecked() && checkbox != null) {
                                    editor.putString("email_login", customer_mail);
                                }else{
                                    editor.putString("email_login", null);
                                }
                                editor.apply();

                                // look in firebase db for a matching customer.
                                // The class that handles the information retrieval is the Login Activity
                                // which implements the interface ValueEventListener.
                                // addListenerForSingleValueEvent will ensure that we unsubscribe
                                // immediately from the value event listener.
                                reference.addListenerForSingleValueEvent(login_activity);

                            }
                            else{

                                Toast.makeText(LoginActivity.this, R.string.login_failed,
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
            });
        }
    }

    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    // ---- This class is a listener that reacts to click to the new user button
    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    public class NewCustButtonListener implements View.OnClickListener{

        private final LoginActivity login_activity;

        // class constructor
        public NewCustButtonListener(LoginActivity l){
            login_activity = l;
        }

        @Override
        public void onClick(View v) {

            final String mail = et_customer_mail.getText().toString();
            final String psw = et_customer_psw.getText().toString();

            // do nothing if the user didn't insert the customer mail or password
            if (mail.trim().length() == 0 || psw.trim().length() == 0)
                return;

            // try to create a new user in firebase auth with the chosen password
            auth.createUserWithEmailAndPassword(mail, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        // called when the operation is complete
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            // if the new user is successfully created
                            if (task.isSuccessful()){

                                /// add a new child in the customers hierarchy with a random id
                                DatabaseReference new_user = reference.push();

                                // store this customer id and mail
                                user_id = new_user.getKey();
                                customer_mail = mail;

                                // initialize this new customer as an empty one in FireBase db.
                                // Specify also what to do when the insertion is complete through a CompletionListener
                                // which in this case is the Login Activity.
                                new_user.setValue(new User(user_id, customer_mail),
                                                  login_activity);
                            }

                            else{

                                Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
            });

        }
    }
}
