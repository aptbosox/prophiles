package com.itude.apt.prophiles.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.itude.apt.prophiles.R;
import com.itude.apt.prophiles.actions.ProfileActivator;
import com.itude.apt.prophiles.adapters.ProfileListAdapter;
import com.itude.apt.prophiles.fragments.TextEntryDialogFragment;
import com.itude.apt.prophiles.model.Profile;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class ProfileListActivity extends AppCompatActivity {

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilelist);

        mRealm = Realm.getDefaultInstance();

        setUpActionBar();
        setUpRecyclerView();
        setUpFloatingActionButton();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profileList);
        setSupportActionBar(toolbar);
    }

    private void setUpRecyclerView() {
        final Activity activity = this;

        RecyclerView recyclerView = (RecyclerView) findViewById(
            R.id.recyclerView_profileList_profiles
        );
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        OrderedRealmCollection<Profile> profiles = mRealm.where(Profile.class).findAll();
        ProfileListAdapter adapter = new ProfileListAdapter(activity, profiles) {

            @Override
            public void onProfileSelected(String id) {
                Profile profile = Profile.getById(id, mRealm);

                Profile.select(id, mRealm);

                new ProfileActivator(profile, activity).activate();
            }

            @Override
            public boolean onProfileLongClick(String id) {
                startProfileActionMode(id);
                return true;
            }
        };

        recyclerView.setAdapter(adapter);
    }

    private void startProfileActionMode(final String profileId) {
        startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.context_profilelist, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        editProfile(profileId);
                        mode.finish();
                        return true;
                    case R.id.action_delete:
                        deleteProfile(profileId);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void setUpFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(
            R.id.floatingActionButton_profileList_addProfile
        );
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateProfileDialog();
            }
        });
    }

    private void showCreateProfileDialog() {
        TextEntryDialogFragment fragment = TextEntryDialogFragment.newInstance(
            R.string.profileList_createProfileDialogTitle,
            new TextEntryDialogFragment.OnOkListener() {
                @Override
                public void onOk(String text) {
                    createProfile(text);
                }
            }
        );

        fragment.show(getSupportFragmentManager(), "NewProfileDialogFragment");
    }

    private void createProfile(final String name) {
        final Profile profile = new Profile();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                profile.setName(name);
                realm.copyToRealm(profile);
            }
        });

        editProfile(profile.getId());
    }

    private void editProfile(String id) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra(Profile.ID, id);
        startActivity(intent);
    }

    private void deleteProfile(final String id) {
        // TODO: 1/31/17 add onSuccess and onError
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Profile.class)
                    .equalTo(Profile.ID, id)
                    .findAll()
                    .deleteAllFromRealm();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
