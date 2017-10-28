package com.rainmachine.presentation.screens.cloudaccounts;

import android.os.Parcelable;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.domain.model.CheckCloudAccountStatus;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.parceler.Parcels;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class CloudAccountsPresenter extends BasePresenter<CloudAccountsView> implements
        ActionMessageParcelableDialogFragment.Callback, InfoMessageDialogFragment.Callback {

    private CloudAccountsActivity activity;
    private CloudAccountsMixer mixer;
    private CompositeDisposable disposables;

    CloudAccountsPresenter(CloudAccountsActivity activity, CloudAccountsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(CloudAccountsView view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessageParcelablePositiveClick(int dialogId, Parcelable parcelable) {
        CloudInfo cloudInfo = Parcels.unwrap(parcelable);
        disposables.add(mixer.removeCloudInfo(cloudInfo)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void onDialogActionMessageParcelableNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageParcelableCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        // Do nothing
    }

    public void onClickDeleteCloudAccount(CloudInfo cloudInfo) {
        DialogFragment dialog = ActionMessageParcelableDialogFragment.newInstance(0, null,
                activity.getString(R.string
                        .cloud_accounts_are_you_sure_delete_cloud), activity.getString(R
                        .string.all_yes),
                activity.getString(R.string.all_no), Parcels.wrap(cloudInfo));
        activity.showDialogSafely(dialog);
    }

    public void onClickEdiCloudAccount(CloudInfo cloudInfo) {
        DialogFragment dialog = CloudAccountsDialogFragment.newInstance(activity.getString(R.string
                .cloud_accounts_account), cloudInfo);
        activity.showDialogSafely(dialog);
    }

    public void onClickAddAccount() {
        DialogFragment dialog = CloudAccountsDialogFragment.newInstance(activity.getString(R.string
                .cloud_accounts_account));
        activity.showDialogSafely(dialog);
    }

    public void onCreateCloudAccount(String email, String password) {
        view.showProgress();
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.email = email;
        cloudInfo.password = password;
        disposables.add(mixer.createCloudInfo(cloudInfo)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new CreateCloudInfoSubscriber()));
    }

    public void onUpdateCloudAccount(Long _id, String email, String password) {
        view.showProgress();
        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo._id = _id;
        cloudInfo.email = email;
        cloudInfo.password = password;
        disposables.add(mixer.updateCloudInfo(cloudInfo)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new UpdateCloudInfoSubscriber()));
    }

    private void refresh() {
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private void handleCreateUpdateCloudInfo(CheckCloudAccountViewModel viewModel) {
        if (viewModel.status == CheckCloudAccountStatus.SUCCESS) {
            refresh();
        } else {
            String message = "";
            if (viewModel.status == CheckCloudAccountStatus
                    .ERROR_TYPE_ALREADY_EXISTING) {
                message = activity.getString(R.string.cloud_accounts_duplicate_email);
            } else if (viewModel.status == CheckCloudAccountStatus
                    .ERROR_TYPE_INVALID_PASSWORD) {
                message = activity.getString(R.string.cloud_accounts_invalid_password);
            } else if (viewModel.status == CheckCloudAccountStatus
                    .ERROR_TYPE_NOT_CONNECTED_INTERNET) {
                message = activity.getString(R.string.cloud_accounts_not_connected, viewModel
                        .cloudInfo.email);
                refresh();
            } else if (viewModel.status == CheckCloudAccountStatus.ERROR_TYPE_NO_DEVICE) {
                message = activity.getString(R.string.cloud_accounts_no_devices, viewModel
                        .cloudInfo.email);
                refresh();
            } else if (viewModel.status == CheckCloudAccountStatus
                    .ERROR_TYPE_SERVER_PROBLEM) {
                message = activity.getString(R.string.cloud_accounts_server_problem);
            }
            DialogFragment dialog = InfoMessageDialogFragment.newInstance(0, activity.getString(R
                    .string.cloud_accounts_error), message, activity.getString(R.string.all_ok));
            activity.showDialogSafely(dialog);
        }
        view.showContent();
    }

    private final class RefreshSubscriber extends DisposableObserver<CloudAccountsViewModel> {

        @Override
        public void onNext(CloudAccountsViewModel viewModel) {
            view.render(viewModel);
            view.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class CreateCloudInfoSubscriber extends
            DisposableObserver<CheckCloudAccountViewModel> {

        @Override
        public void onNext(CheckCloudAccountViewModel viewModel) {
            if (viewModel.status == CheckCloudAccountStatus.SUCCESS || viewModel.status ==
                    CheckCloudAccountStatus.ERROR_TYPE_NOT_CONNECTED_INTERNET || viewModel.status ==
                    CheckCloudAccountStatus.ERROR_TYPE_NO_DEVICE) {
                Toasts.show(activity.getString(R.string.cloud_accounts_added));
            }
            handleCreateUpdateCloudInfo(viewModel);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class UpdateCloudInfoSubscriber extends
            DisposableObserver<CheckCloudAccountViewModel> {

        @Override
        public void onNext(CheckCloudAccountViewModel viewModel) {
            if (viewModel.status == CheckCloudAccountStatus.SUCCESS || viewModel.status ==
                    CheckCloudAccountStatus.ERROR_TYPE_NOT_CONNECTED_INTERNET || viewModel.status ==
                    CheckCloudAccountStatus.ERROR_TYPE_NO_DEVICE) {
                Toasts.show(activity.getString(R.string.cloud_accounts_updated));
            }
            handleCreateUpdateCloudInfo(viewModel);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
