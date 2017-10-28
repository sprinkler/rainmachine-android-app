package com.rainmachine.presentation.screens.about;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.BuildConfig;
import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutView extends ViewFlipper implements AboutContract.View {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    AboutContract.Presenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.flipper)
    ViewFlipper flipper;
    @BindView(R.id.view_diagnostics)
    ViewGroup viewDiagnostics;
    @BindView(R.id.progress_text)
    TextView progressText;

    private AboutAdapter adapter;

    public AboutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnClick(R.id.btn_retry)
    public void onClickRetry() {
        presenter.onClickRetry();
    }

    @OnClick(R.id.btn_send_diagnostics)
    public void onClickSendDiagnostics() {
        presenter.onClickSendDiagnostics();
    }

    @Override
    public void setup(boolean showDiagnostics) {
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new AboutAdapter(getContext(), presenter, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        toggleDiagnosticsBar(showDiagnostics);
    }

    @Override
    public void updateContent(AboutViewModel viewModel) {
        List<AdapterItemAbout> items = new ArrayList<>();
        AdapterItemAbout item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_rain_machine_version);
        item.description = "v" + viewModel.update.currentVersion;
        if (viewModel.update.update && !Strings.isBlank(viewModel.update.newVersion)) {
            item.description = getContext().getString(R.string.about_version_update, viewModel
                    .update
                    .currentVersion, viewModel.update.newVersion);
        }
        item.isUpdatable = viewModel.update.update;
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_app_version);
        item.description = "v" + BuildConfig.VERSION_NAME;
        items.add(item);
        if (viewModel.showRemoteAccessStatus) {
            item = new AdapterItemAbout();
            item.name = getContext().getString(R.string.about_remote_access_status);
            item.description = viewModel.remoteAccessStatus;
            items.add(item);
        }
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_hardware_version);
        item.description = "v" + viewModel.versions.hardwareVersion;
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_api_version);
        item.description = "v" + viewModel.versions.apiVersion;
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_ip_address);
        item.description = Strings.valueOrDefault(viewModel.wifiSettings.ipAddress, getContext()
                .getString(R.string.about_none_lower_case));
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_netmask);
        item.description = Strings.valueOrDefault(viewModel.wifiSettings.netmaskAddress,
                getContext()
                .getString(R.string.about_none_lower_case));
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_gateway);
        item.description = Strings.valueOrDefault(viewModel.gatewayAddress, getContext()
                .getString(R.string.about_none_lower_case));
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_mac_address);
        item.description = Strings.valueOrDefault(viewModel.wifiSettings.macAddress, getContext()
                .getString(R.string.about_none_lower_case));
        item.isClickableForSupport = true;
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_ssid_name);
        item.description = Strings.valueOrDefault(viewModel.wifiSettings.ssid, getContext()
                .getString(R
                .string.about_none_lower_case));
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_mem_usage);
        item.description = getContext().getString(R.string.about_kb, viewModel.memUsage);
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_cpu_usage);
        item.description = "" + viewModel.cpuUsage + " " + getContext().getString(R.string
                .all_percent);
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_uptime);
        item.description = Strings.valueOrDefault(viewModel.uptime, getContext().getString
                (R.string.about_none_lower_case));
        items.add(item);
        adapter.setItems(items);
        flipper.setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void updateContent3(AboutViewModel viewModel) {
        List<AdapterItemAbout> items = new ArrayList<>();
        AdapterItemAbout item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_rain_machine_version);
        item.description = viewModel.update.currentVersion;
        if (viewModel.update.update && !Strings.isBlank(viewModel.update.newVersion)) {
            item.description = item.description + " (new: " + viewModel.update.newVersion + ")";
        }
        item.isUpdatable = viewModel.update.update;
        items.add(item);
        item = new AdapterItemAbout();
        item.name = getContext().getString(R.string.about_app_version);
        item.description = BuildConfig.VERSION_NAME;
        items.add(item);
        adapter.setItems(items);
        flipper.setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showContent() {
        flipper.setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showProgress(String progressText) {
        this.progressText.setText(progressText);
        flipper.setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showError() {
        flipper.setDisplayedChild(FLIPPER_ERROR);
    }

    private void toggleDiagnosticsBar(boolean showDiagnostics) {
        viewDiagnostics.setVisibility(showDiagnostics ? View.VISIBLE : View.GONE);
    }
}
