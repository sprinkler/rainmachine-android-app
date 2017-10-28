package com.rainmachine.presentation.screens.help;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.web.WebActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends SprinklerActivity implements HelpContract.View {

    @Inject
    HelpContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HelpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);

        presenter.init();
    }

    @Override
    public Object getModule() {
        return new HelpModule(this);
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void setup(List<AdapterItemType> items) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, null, true));
        HelpAdapter adapter = new HelpAdapter(this, presenter, items);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void startWebScreen(String url) {
        startActivity(WebActivity.getStartIntent(this, url));
    }
}
