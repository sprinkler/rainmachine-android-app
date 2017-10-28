package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.IntentUtils;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WateringHistoryPresenter extends BasePresenter<WateringHistoryView> implements
        RadioOptionsDialogFragment.Callback {

    private WateringHistoryActivity activity;
    private WateringHistoryMixer mixer;

    private final CompositeDisposable disposables;

    private WateringHistoryViewModel viewModel;

    WateringHistoryPresenter(WateringHistoryActivity activity, WateringHistoryMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WateringHistoryView view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
    }

    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        WateringHistoryInterval wateringHistoryInterval = WateringHistoryInterval.values()
                [checkedItemPosition];
        disposables.add(mixer
                .createExportFile(viewModel, wateringHistoryInterval)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new CreateExportFileSubscriber()));
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    public void onClickExport() {
        String[] items = activity.getResources().getStringArray(R.array
                .watering_history_interval);
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(0, activity.getString(R
                .string.watering_history_export), activity.getString(R.string
                .watering_history_export), items, 0);
        activity.showDialogSafely(dialog);
    }

    public void onClickRetry() {
        refresh();
    }

    public void onSelectedInterval(WateringHistoryInterval wateringHistoryInterval) {
        view.updateContent(viewModel, wateringHistoryInterval);
    }

    private void refresh() {
        view.showProgress();
        activity.showProgress();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WateringHistoryViewModel> {

        @Override
        public void onNext(WateringHistoryViewModel viewModel) {
            WateringHistoryPresenter.this.viewModel = viewModel;
            view.updateContent(viewModel, WateringHistoryInterval.WEEK);
            view.showContent();
            activity.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class CreateExportFileSubscriber extends DisposableObserver<Uri> {

        @Override
        public void onNext(Uri uri) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/*");
            intent.addFlags(IntentUtils.FLAG_NEW_DOCUMENT);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.watering_history_error_export);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
