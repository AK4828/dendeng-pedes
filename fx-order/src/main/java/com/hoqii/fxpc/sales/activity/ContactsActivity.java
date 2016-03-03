package com.hoqii.fxpc.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.ContactsAdapter;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ContactsActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        init();
        initSet();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                super.onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(){

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.contacts_recycler);
    }

    private void initSet(){
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.contact);

        if (getIntent() != null){
            String businesPartnerId = getIntent().getExtras().getString("businessPartnerId", null);
            contactsAdapter = new ContactsAdapter(this, businesPartnerId);
        }else {
            contactsAdapter = new ContactsAdapter(this);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsAdapter);
    }

    public void sendContactId(){
        setResult(RESULT_OK);
        finish();
    }


}
