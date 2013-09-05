package com.example.fragment_assignment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MainFragmentActivity extends FragmentActivity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        
        //Add fragment 1 without XML
        FragmentManager fragmentManager = getSupportFragmentManager();	//"support" because support libraries
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        Fragment1 fragment1 = new Fragment1();
        fragmentTransaction.add(R.id.main_viewgroup, fragment1, Constants.FRAGMENT1_NAME);
        fragmentTransaction.commit();
    }

}
