package com.rainmachine.presentation.screens.weathersourcedetails;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.ParserFormatter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

public class WeatherSourceDetailsView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener, View.OnClickListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    private static final int FLIPPER_GENERIC_PARAMS = 0;
    private static final int FLIPPER_WUNDERGROUND_PARAMS = 1;
    private static final int FLIPPER_NETATMO_PARAMS = 2;

    @Inject
    WeatherSourceDetailsPresenter presenter;
    @Inject
    ParserFormatter parserFormatter;

    @BindView(R.id.data_source)
    TextView tvName;
    @BindView(R.id.cover_area)
    TextView tvCoverArea;
    @BindView(R.id.last_run)
    TextView tvLastRun;
    @BindView(R.id.description)
    TextView tvDescription;
    @BindView(R.id.toggle_enabled)
    SwitchCompat toggleEnabled;
    @BindView(R.id.view_wunderground_params)
    WUndergroundParamsView viewWUndergroundParams;
    @BindView(R.id.view_netatmo_params)
    NetatmoParamsView viewNetatmoParams;
    @BindView(R.id.view_params)
    LinearLayout viewParams;
    @BindView(R.id.flipper_params)
    ViewFlipper flipperParams;
    @BindView(R.id.view_buttons)
    ViewGroup viewButtons;
    @BindView(R.id.view_generic_params)
    ViewGroup viewGenericParams;

    private CompositeDisposable disposables;

    public WeatherSourceDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        disposables = new CompositeDisposable();
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
        disposables.clear();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_enabled) {
            toggleEnabled.setText(isChecked ? R.string.all_enabled : R.string.all_disabled);
            presenter.onCheckedChangedEnabled(isChecked);
        } else {
            ParamInfo paramInfo = (ParamInfo) buttonView.getTag();
            paramInfo.value = isChecked;
            presenter.onChangedBooleanParam(paramInfo.param, (Boolean) paramInfo.value);
        }
    }

    @Override
    public void onClick(View v) {
        ParamInfo paramInfo = (ParamInfo) v.getTag();
        presenter.onClick(paramInfo.param, paramInfo.value);
    }

    @OnClick(R.id.btn_retry)
    void onClickRetry() {
        presenter.onClickRetry();
    }

    @OnClick(R.id.btn_defaults)
    void onClickDefaults() {
        presenter.onClickDefaults();
    }

    public void updateContent(Parser parser) {
        tvName.setText(parser.name);
        tvCoverArea.setText(getContext().getString(R.string.all_cover_area, parserFormatter
                .coverArea(getContext(), parser)));
        tvLastRun.setText(parserFormatter.lastRun(getContext(), parser));
        tvDescription.setText(parser.description);

        toggleEnabled.setOnCheckedChangeListener(null);
        toggleEnabled.setChecked(parser.enabled);
        toggleEnabled.setOnCheckedChangeListener(this);
        toggleEnabled.setText(parser.enabled ? R.string.all_enabled : R.string.all_disabled);

        showHideButtons(parser);

        if (parser.isWUnderground()) {
            flipperParams.setDisplayedChild(FLIPPER_WUNDERGROUND_PARAMS);
            viewWUndergroundParams.updateContent(parser);
            showHideWUndergroundParams(parser);
        } else if (parser.isNetatmo()) {
            flipperParams.setDisplayedChild(FLIPPER_NETATMO_PARAMS);
            viewNetatmoParams.updateContent(parser);
            showHideNetatmoParams(parser);
        } else {
            flipperParams.setDisplayedChild(FLIPPER_GENERIC_PARAMS);
            showHideGenericParams(parser);
            if (parser.hasParams) {
                viewParams.removeAllViews();
                for (String param : parser.params.keySet()) {
                    Object value = parser.params.get(param);
                    View view;
                    if (value instanceof Boolean) {
                        view = LayoutInflater.from(getContext()).inflate(R.layout
                                .include_parser_param_boolean, viewParams, false);
                        SwitchCompat toggle = ButterKnife.findById(view, R.id.toggle);
                        toggle.setOnCheckedChangeListener(null);
                        toggle.setChecked((Boolean) value);
                        toggle.setOnCheckedChangeListener(this);
                        toggle.setTag(new ParamInfo(param, value));
                    } else {
                        view = LayoutInflater.from(getContext()).inflate(R.layout
                                .include_parser_param_text, viewParams, false);
                        TextView tvValue = ButterKnife.findById(view, R.id.value);
                        tvValue.setText(value.toString());
                        view.setTag(new ParamInfo(param, value));
                        view.setOnClickListener(this);
                    }
                    TextView tvParam = ButterKnife.findById(view, R.id.param);
                    tvParam.setText(param);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                            .LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int marginBottom = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
                    params.setMargins(0, 0, 0, marginBottom);
                    view.setLayoutParams(params);
                    viewParams.addView(view);
                }
            }
        }
    }

    public void showHideButtons(Parser parser) {
        viewButtons.setVisibility(parser.enabled && parser.hasParams ? View.VISIBLE : View.GONE);
    }

    public void showHideGenericParams(Parser parser) {
        viewGenericParams.setVisibility(parser.enabled && parser.hasParams ? View.VISIBLE : View
                .GONE);
    }

    public void showHideWUndergroundParams(Parser parser) {
        if (parser.enabled) {
            viewWUndergroundParams.setVisibility(View.VISIBLE);
        } else {
            viewWUndergroundParams.setVisibility(View.INVISIBLE);
        }
    }

    public void showHideNetatmoParams(Parser parser) {
        if (parser.enabled) {
            viewNetatmoParams.setVisibility(View.VISIBLE);
        } else {
            viewNetatmoParams.setVisibility(View.INVISIBLE);
        }
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    private static class ParamInfo {
        public String param;
        public Object value;

        private ParamInfo(String param, Object value) {
            this.param = param;
            this.value = value;
        }
    }
}
