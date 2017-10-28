package com.rainmachine.presentation.screens.zonedetails;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.presentation.activities.BaseActivity;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.PresentationUtils;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;
import com.rainmachine.presentation.widgets.ItemOffsetDecoration;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ZoneDetailsAdvancedView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CHILD_STANDARD = 0;
    private static final int FLIPPER_CHILD_SLOPE = 1;
    private static final int FLIPPER_CHILD_SOIL = 2;
    private static final int FLIPPER_CHILD_SPRINKLER_HEAD = 3;
    private static final int FLIPPER_CHILD_VEGETATION = 4;
    private static final int FLIPPER_CHILD_VEGETATION_CROP_COEFFICIENT = 5;

    @Inject
    ZoneDetailsAdvancedPresenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    DecimalFormatter decimalFormatter;

    @BindView(R.id.tv_vegetation_type)
    TextView tvVegetationType;
    @BindView(R.id.img_vegetation_type)
    ImageView imgVegetationType;
    @BindView(R.id.recycler_view_vegetation_type)
    RecyclerView recyclerViewVegetationType;

    @BindView(R.id.tv_soil_type)
    TextView tvSoilType;
    @BindView(R.id.img_soil_type)
    ImageView imgSoilType;
    @BindView(R.id.recycler_view_soil_type)
    RecyclerView recyclerViewSoilType;

    @BindView(R.id.tv_sprinkler_heads)
    TextView tvSprinklerHeads;
    @BindView(R.id.img_sprinkler_heads)
    ImageView imgSprinklerHeads;
    @BindView(R.id.recycler_view_sprinkler_heads)
    RecyclerView recyclerViewSprinklerHeads;

    @BindView(R.id.tv_slope)
    TextView tvSlope;
    @BindView(R.id.img_slope)
    ImageView imgSlope;
    @BindView(R.id.recycler_view_slope)
    RecyclerView recyclerViewSlope;

    @BindView(R.id.tv_exposure)
    TextView tvExposure;
    @BindView(R.id.img_exposure)
    ImageView imgExposure;
    @BindView(R.id.recycler_view_exposure)
    RecyclerView recyclerViewExposure;

    @BindView(R.id.view_advanced_part1)
    ViewGroup viewPart1;
    @BindView(R.id.view_advanced_part2)
    ViewGroup viewPart2;
    @BindView(R.id.tv_field_capacity)
    TextView tvFieldCapacity;

    @BindView(R.id.tv_units_area)
    TextView tvUnitsArea;
    @BindView(R.id.input_area)
    EditText inputArea;
    @BindView(R.id.tv_units_flow_rate)
    TextView tvUnitsFlowRate;
    @BindView(R.id.input_flow_rate)
    EditText inputFlowRate;

    /* Custom slope views */
    @BindView(R.id.input_surface_accumulation)
    EditText inputSurfaceAccumulation;
    @BindView(R.id.tv_units_slope)
    TextView tvUnitsSlope;

    /* Custom soil views */
    @BindView(R.id.input_intake_rate)
    EditText inputSoilIntakeRate;
    @BindView(R.id.tv_units_soil_intake_rate)
    TextView tvUnitsSoilIntakeRate;
    @BindView(R.id.input_soil_field_capacity)
    EditText inputSoilFieldCapacity;

    /* Custom sprinkler head views */
    @BindView(R.id.input_precipitation_rate)
    EditText inputPrecipitationRate;
    @BindView(R.id.tv_units_sprinkler_head_rate)
    TextView tvUnitsSprinklerHeadRate;
    @BindView(R.id.input_sprinkler_head_application_efficiency)
    EditText inputApplicationEfficiency;

    /* Custom vegetation views */
    @BindView(R.id.input_allowed_depletion)
    EditText inputAllowedDepletion;
    @BindView(R.id.input_root_depth)
    EditText inputRootDepth;
    @BindView(R.id.tv_units_root_depth)
    TextView tvUnitsRootDepth;
    @BindView(R.id.input_wilting)
    EditText inputWilting;
    @BindView(R.id.toggle_tall_vegetation)
    SwitchCompat toggleTallVegetation;
    @BindView(R.id.input_single_year_value)
    EditText inputSingleYearValue;
    @BindView(R.id.check_single_year_value)
    CheckBox checkSingleYearValue;
    @BindView(R.id.check_multiple_monthly_values)
    CheckBox checkMultipleMonthlyValues;

    /* Custom vegetation crop coefficient views */
    @BindViews({R.id.input_kc_january, R.id.input_kc_february, R.id.input_kc_march, R.id
            .input_kc_april, R.id.input_kc_may, R.id.input_kc_june, R.id
            .input_kc_july, R.id.input_kc_august, R.id.input_kc_september, R.id.input_kc_october,
            R.id.input_kc_november, R.id.input_kc_december})
    List<EditText> inputsKc;

    private ZoneDetailsAdvancedAdapter vegetationAdapter;
    private ZoneDetailsAdvancedAdapter soilAdapter;
    private ZoneDetailsAdvancedAdapter sprinklerHeadsAdapter;
    private ZoneDetailsAdvancedAdapter slopeAdapter;
    private ZoneDetailsAdvancedAdapter exposureAdapter;

    public ZoneDetailsAdvancedView(Context context, AttributeSet attrs) {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_single_year_value) {
            checkSingleYearValue.setOnCheckedChangeListener(null);
            if (!isChecked) {
                checkSingleYearValue.setChecked(true);
            }
            checkSingleYearValue.setOnCheckedChangeListener(this);

            checkMultipleMonthlyValues.setOnCheckedChangeListener(null);
            checkMultipleMonthlyValues.setChecked(false);
            checkMultipleMonthlyValues.setOnCheckedChangeListener(this);

            presenter.onClickYearlyKc();
        } else if (id == R.id.check_multiple_monthly_values) {
            checkMultipleMonthlyValues.setOnCheckedChangeListener(null);
            if (!isChecked) {
                checkMultipleMonthlyValues.setChecked(true);
            }
            checkMultipleMonthlyValues.setOnCheckedChangeListener(this);

            checkSingleYearValue.setOnCheckedChangeListener(null);
            checkSingleYearValue.setChecked(false);
            checkSingleYearValue.setOnCheckedChangeListener(this);

            presenter.onClickMonthlyKc();
        }
    }

    @OnClick(R.id.view_field_capacity)
    void onClickFieldCapacity() {
        presenter.onClickFieldCapacity();
    }

    @OnClick(R.id.view_vegetation_type)
    void onClickVegetationType() {
        TagExpanded tag = (TagExpanded) tvVegetationType.getTag();
        if (tag.isExpanded) {
            collapseVegetationType();
        } else {
            tvVegetationType.setVisibility(View.INVISIBLE);
            imgVegetationType.setImageResource(R.drawable.ic_keyboard_arrow_up);
            recyclerViewVegetationType.setVisibility(View.VISIBLE);
            recyclerViewVegetationType.requestLayout();
            tag.isExpanded = true;

            presenter.onExtendedVegetationType();
        }
    }

    @OnClick(R.id.view_soil_type)
    void onClickSoilType() {
        TagExpanded tag = (TagExpanded) tvSoilType.getTag();
        if (tag.isExpanded) {
            collapseSoilType();
        } else {
            tvSoilType.setVisibility(View.INVISIBLE);
            imgSoilType.setImageResource(R.drawable.ic_keyboard_arrow_up);
            recyclerViewSoilType.setVisibility(View.VISIBLE);
            recyclerViewSoilType.requestLayout();
            tag.isExpanded = true;

            collapseVegetationType();
            collapseSlope();
            collapseSprinklerHeads();
            collapseExposure();
        }
    }

    @OnClick(R.id.view_sprinkler_heads)
    void onClickSprinklerHeads() {
        TagExpanded tag = (TagExpanded) tvSprinklerHeads.getTag();
        if (tag.isExpanded) {
            collapseSprinklerHeads();
        } else {
            tvSprinklerHeads.setVisibility(View.INVISIBLE);
            imgSprinklerHeads.setImageResource(R.drawable.ic_keyboard_arrow_up);
            recyclerViewSprinklerHeads.setVisibility(View.VISIBLE);
            recyclerViewSprinklerHeads.requestLayout();
            tag.isExpanded = true;

            collapseSoilType();
            collapseSlope();
            collapseVegetationType();
            collapseExposure();
        }
    }

    @OnClick(R.id.view_slope)
    void onClickSlope() {
        TagExpanded tag = (TagExpanded) tvSlope.getTag();
        if (tag.isExpanded) {
            collapseSlope();
        } else {
            tvSlope.setVisibility(View.INVISIBLE);
            imgSlope.setImageResource(R.drawable.ic_keyboard_arrow_up);
            recyclerViewSlope.setVisibility(View.VISIBLE);
            recyclerViewSlope.requestLayout();
            tag.isExpanded = true;

            collapseSoilType();
            collapseVegetationType();
            collapseSprinklerHeads();
            collapseExposure();
        }
    }

    @OnClick(R.id.view_exposure)
    void onClickExposure() {
        TagExpanded tag = (TagExpanded) tvExposure.getTag();
        if (tag.isExpanded) {
            collapseExposure();
        } else {
            tvExposure.setVisibility(View.INVISIBLE);
            imgExposure.setImageResource(R.drawable.ic_keyboard_arrow_up);
            recyclerViewExposure.setVisibility(View.VISIBLE);
            recyclerViewExposure.requestLayout();
            tag.isExpanded = true;

            collapseSoilType();
            collapseSlope();
            collapseSprinklerHeads();
            collapseVegetationType();
        }
    }

    public void collapseSoilType() {
        TagExpanded tag = (TagExpanded) tvSoilType.getTag();
        tvSoilType.setVisibility(View.VISIBLE);
        imgSoilType.setImageResource(R.drawable.ic_keyboard_arrow_down);
        recyclerViewSoilType.setVisibility(View.GONE);
        recyclerViewSoilType.requestLayout();
        tag.isExpanded = false;
    }

    public void collapseVegetationType() {
        TagExpanded tag = (TagExpanded) tvVegetationType.getTag();
        tvVegetationType.setVisibility(View.VISIBLE);
        imgVegetationType.setImageResource(R.drawable.ic_keyboard_arrow_down);
        recyclerViewVegetationType.setVisibility(View.GONE);
        recyclerViewVegetationType.requestLayout();
        tag.isExpanded = false;
    }

    public void collapseSprinklerHeads() {
        TagExpanded tag = (TagExpanded) tvSprinklerHeads.getTag();
        tvSprinklerHeads.setVisibility(View.VISIBLE);
        imgSprinklerHeads.setImageResource(R.drawable.ic_keyboard_arrow_down);
        recyclerViewSprinklerHeads.setVisibility(View.GONE);
        recyclerViewSprinklerHeads.requestLayout();
        tag.isExpanded = false;
    }

    public void collapseSlope() {
        TagExpanded tag = (TagExpanded) tvSlope.getTag();
        tvSlope.setVisibility(View.VISIBLE);
        imgSlope.setImageResource(R.drawable.ic_keyboard_arrow_down);
        recyclerViewSlope.setVisibility(View.GONE);
        recyclerViewSlope.requestLayout();
        tag.isExpanded = false;
    }

    public void collapseExposure() {
        TagExpanded tag = (TagExpanded) tvExposure.getTag();
        tvExposure.setVisibility(View.VISIBLE);
        imgExposure.setImageResource(R.drawable.ic_keyboard_arrow_down);
        recyclerViewExposure.setVisibility(View.GONE);
        recyclerViewExposure.requestLayout();
        tag.isExpanded = false;
    }

    public void setup(boolean showAllAdvancedSettings, boolean showExtraSoilTypes, boolean
            showOtherVegetationType) {
        tvVegetationType.setVisibility(View.VISIBLE);
        imgVegetationType.setImageResource(R.drawable.ic_keyboard_arrow_down);
        tvVegetationType.setTag(new TagExpanded(false));

        recyclerViewVegetationType.addItemDecoration(new ItemOffsetDecoration(getContext(), R
                .dimen.spacing_medium));
        recyclerViewVegetationType.setLayoutManager(new GridLayoutManager(getContext(), 3));
        vegetationAdapter = new ZoneDetailsAdvancedAdapter(getContext(), vegetationTypeItems
                (showOtherVegetationType),
                presenter);
        recyclerViewVegetationType.setAdapter(vegetationAdapter);

        if (showAllAdvancedSettings) {
            viewPart1.setVisibility(View.VISIBLE);
            viewPart2.setVisibility(View.VISIBLE);

            tvSoilType.setVisibility(View.VISIBLE);
            imgSoilType.setImageResource(R.drawable.ic_keyboard_arrow_down);
            tvSoilType.setTag(new TagExpanded(false));

            recyclerViewSoilType.addItemDecoration(new ItemOffsetDecoration(getContext(), R
                    .dimen.spacing_medium));
            recyclerViewSoilType.setLayoutManager(new GridLayoutManager(getContext(), 3));
            soilAdapter = new ZoneDetailsAdvancedAdapter(getContext(), soilTypeItems
                    (showExtraSoilTypes), presenter);
            recyclerViewSoilType.setAdapter(soilAdapter);

            tvSprinklerHeads.setVisibility(View.VISIBLE);
            imgSprinklerHeads.setImageResource(R.drawable.ic_keyboard_arrow_down);
            tvSprinklerHeads.setTag(new TagExpanded(false));

            recyclerViewSprinklerHeads.addItemDecoration(new ItemOffsetDecoration(getContext(), R
                    .dimen.spacing_medium));
            recyclerViewSprinklerHeads.setLayoutManager(new GridLayoutManager(getContext(), 3));
            sprinklerHeadsAdapter = new ZoneDetailsAdvancedAdapter(getContext(),
                    sprinklerHeadsItems(), presenter);
            recyclerViewSprinklerHeads.setAdapter(sprinklerHeadsAdapter);

            tvSlope.setVisibility(View.VISIBLE);
            imgSlope.setImageResource(R.drawable.ic_keyboard_arrow_down);
            tvSlope.setTag(new TagExpanded(false));

            recyclerViewSlope.addItemDecoration(new ItemOffsetDecoration(getContext(), R
                    .dimen.spacing_medium));
            recyclerViewSlope.setLayoutManager(new GridLayoutManager(getContext(), 3));
            slopeAdapter = new ZoneDetailsAdvancedAdapter(getContext(), slopeItems(),
                    presenter);
            recyclerViewSlope.setAdapter(slopeAdapter);

            tvExposure.setVisibility(View.VISIBLE);
            imgExposure.setImageResource(R.drawable.ic_keyboard_arrow_down);
            tvExposure.setTag(new TagExpanded(false));

            recyclerViewExposure.addItemDecoration(new ItemOffsetDecoration(getContext(), R
                    .dimen.spacing_medium));
            recyclerViewExposure.setLayoutManager(new GridLayoutManager(getContext(), 3));
            exposureAdapter = new ZoneDetailsAdvancedAdapter(getContext(), exposureItems(),
                    presenter);
            recyclerViewExposure.setAdapter(exposureAdapter);
        } else {
            viewPart1.setVisibility(View.GONE);
            viewPart2.setVisibility(View.GONE);
        }
    }

    public void render(ZoneProperties zoneProperties, boolean showAllAdvancedSettings, boolean
            isUnitsMetric, boolean showOtherVegetationType) {
        ZoneDetailsAdvancedItem item;
        if (zoneProperties.vegetationType != ZoneProperties.VegetationType.NOT_SET) {
            item = new ItemVegetationType(zoneProperties.vegetationType);
        } else {
            // default value
            item = new ItemVegetationType(ZoneProperties.VegetationType.LAWN);
        }
        vegetationAdapter.setSelectedItem(item);
        updateVegetationType(zoneProperties.vegetationType, showOtherVegetationType);
        if (showAllAdvancedSettings) {
            if (zoneProperties.soilType != ZoneProperties.SoilType.NOT_SET) {
                item = new ItemSoilType(zoneProperties.soilType);
            } else {
                // default value
                item = new ItemSoilType(ZoneProperties.SoilType.CLAY_LOAM);
            }
            soilAdapter.setSelectedItem(item);
            updateSoilType(zoneProperties.soilType);

            if (zoneProperties.sprinklerHeads != ZoneProperties.SprinklerHeads.NOT_SET) {
                item = new ItemSprinklerHeads(zoneProperties.sprinklerHeads);
            } else {
                // default value
                item = new ItemSprinklerHeads(ZoneProperties.SprinklerHeads.POPUP_SPRAY);
            }
            sprinklerHeadsAdapter.setSelectedItem(item);
            updateSprinklerHeads(zoneProperties.sprinklerHeads);

            if (zoneProperties.slope != ZoneProperties.Slope.NOT_SET) {
                item = new ItemSlope(zoneProperties.slope);
            } else {
                // default value
                item = new ItemSlope(ZoneProperties.Slope.FLAT);
            }
            slopeAdapter.setSelectedItem(item);
            updateSlope(zoneProperties.slope);

            if (zoneProperties.exposure != ZoneProperties.Exposure.NOT_SET) {
                item = new ItemExposure(zoneProperties.exposure);
            } else {
                // default value
                item = new ItemExposure(ZoneProperties.Exposure.FULL_SUN);
            }
            exposureAdapter.setSelectedItem(item);
            updateExposure(zoneProperties.exposure);

            updateSimulationFields(zoneProperties, isUnitsMetric);

            tvUnitsArea.setText(isUnitsMetric ? R.string.zone_details_area_units_square_metre : R
                    .string.zone_details_area_units_square_foot);
            inputArea.setText(decimalFormatter.lengthUnitsDecimals(zoneProperties.area
                    (isUnitsMetric), isUnitsMetric));
            tvUnitsFlowRate.setText(isUnitsMetric ? R.string
                    .zone_details_flow_rate_units_cubic_metre_hour : R.string
                    .zone_details_flow_rate_units_gallons_minute);
            inputFlowRate.setText(decimalFormatter.lengthUnitsDecimals(zoneProperties.flowRate
                    (isUnitsMetric), isUnitsMetric));
        }
    }

    public void updateVegetationType(ZoneProperties.VegetationType vegetationType, boolean
            showOtherVegetationType) {
        tvVegetationType.setText(vegetationTypeText(vegetationType, showOtherVegetationType));
    }

    public void updateSoilType(ZoneProperties.SoilType soilType) {
        tvSoilType.setText(soilTypeText(soilType));
    }

    public void updateSprinklerHeads(ZoneProperties.SprinklerHeads sprinklerHeads) {
        tvSprinklerHeads.setText(sprinklerHeadsText(sprinklerHeads));
    }

    public void updateSlope(ZoneProperties.Slope slope) {
        tvSlope.setText(slopeText(slope));
    }

    public void updateExposure(ZoneProperties.Exposure exposure) {
        tvExposure.setText(exposureText(exposure));
    }

    public void updateFieldCapacity(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        float fieldCapacity = (zoneProperties.savingsPercentage / 100.0f) * zoneProperties
                .currentFieldCapacity(isUnitsMetric);
        if (fieldCapacity >= 0) {
            String sFieldCapacity = decimalFormatter.lengthUnitsDecimals(fieldCapacity,
                    isUnitsMetric) + "" + " " + (isUnitsMetric ? getContext().getString(R.string
                    .all_mm) : getContext()
                    .getString(R.string.all_inch));
            tvFieldCapacity.setText(sFieldCapacity);
        } else {
            tvFieldCapacity.setText(R.string.zone_details_advanced_invalid_values);
        }
    }

    public void updateSimulationFields(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        updateFieldCapacity(zoneProperties, isUnitsMetric);
    }

    public void showFieldCapacityDialog(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        BaseActivity activity = (BaseActivity) getContext();
        DialogFragment dialog = FieldCapacityDialogFragment.newInstance(zoneProperties
                        .savingsPercentage, zoneProperties.currentFieldCapacity(isUnitsMetric),
                isUnitsMetric);
        activity.showDialogSafely(dialog);
    }

    public void showCustomSlopeView(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        String sAccumulation = decimalFormatter.lengthUnitsDecimals(zoneProperties
                .allowedSurfaceAcc(isUnitsMetric), isUnitsMetric);
        inputSurfaceAccumulation.setText(sAccumulation);
        inputSurfaceAccumulation.setSelection(0, inputSurfaceAccumulation.length());
        tvUnitsSlope.setText(isUnitsMetric ? R.string.all_mm : R.string.all_inch);
        setDisplayedChild(FLIPPER_CHILD_SLOPE);
        PresentationUtils.showSoftKeyboard(inputSurfaceAccumulation);
    }

    public void showCustomSoilView(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        inputSoilIntakeRate.setText(decimalFormatter.lengthUnitsDecimals(zoneProperties
                .soilIntakeRate(isUnitsMetric), isUnitsMetric));
        inputSoilIntakeRate.setSelection(0, inputSoilIntakeRate.length());
        tvUnitsSoilIntakeRate.setText(isUnitsMetric ? R.string.all_mm_h : R.string.all_inch_h);
        inputSoilFieldCapacity.setText(decimalFormatter.limitedDecimals(zoneProperties
                .fieldCapacity, 3));
        inputSoilFieldCapacity.setSelection(0, inputSoilFieldCapacity.length());
        setDisplayedChild(FLIPPER_CHILD_SOIL);
        PresentationUtils.showSoftKeyboard(inputSoilIntakeRate);
    }

    public void showCustomSprinklerHeadView(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        inputPrecipitationRate.setText(decimalFormatter.lengthUnitsDecimals(zoneProperties
                .precipitationRate(isUnitsMetric), isUnitsMetric));
        inputPrecipitationRate.setSelection(0, inputPrecipitationRate.length());
        tvUnitsSprinklerHeadRate.setText(isUnitsMetric ? R.string.all_mm_h : R.string.all_inch_h);
        inputApplicationEfficiency.setText(decimalFormatter.limitedDecimals(zoneProperties
                .appEfficiency, 3));
        inputApplicationEfficiency.setSelection(0, inputApplicationEfficiency.length());
        setDisplayedChild(FLIPPER_CHILD_SPRINKLER_HEAD);
        PresentationUtils.showSoftKeyboard(inputPrecipitationRate);
    }

    public void showCustomVegetationView(ZoneProperties zoneProperties, boolean isUnitsMetric) {
        inputAllowedDepletion.setText(decimalFormatter.limitedDecimals(zoneProperties
                .maxAllowedDepletion, 3));
        inputRootDepth.setText(decimalFormatter.lengthUnitsDecimals(zoneProperties.rootDepth
                (isUnitsMetric), isUnitsMetric));
        tvUnitsRootDepth.setText(isUnitsMetric ? R.string.all_mm : R.string.all_inch);
        inputWilting.setText(decimalFormatter.limitedDecimals(zoneProperties.permWilting,
                3));
        toggleTallVegetation.setChecked(zoneProperties.isTallPlant);
        if (zoneProperties.useYearlyKc) {
            inputSingleYearValue.setText(decimalFormatter.limitedDecimals(zoneProperties
                    .etCoefficient, 3));
            checkSingleYearValue.setChecked(true);
            checkMultipleMonthlyValues.setChecked(false);
        } else {
            checkSingleYearValue.setChecked(false);
            checkMultipleMonthlyValues.setChecked(true);
        }
        checkSingleYearValue.setOnCheckedChangeListener(this);
        checkMultipleMonthlyValues.setOnCheckedChangeListener(this);
        showVegetationView();
        PresentationUtils.showSoftKeyboard(inputAllowedDepletion);
    }

    public void showCustomVegetationCropCoefficientView(ZoneProperties zoneProperties) {
        for (int i = 0; i < zoneProperties.detailedMonthsKc.size(); i++) {
            inputsKc.get(i).setText(decimalFormatter.limitedDecimals(zoneProperties
                    .detailedMonthsKc.get(i), 3));
        }
        setDisplayedChild(FLIPPER_CHILD_VEGETATION_CROP_COEFFICIENT);
        PresentationUtils.showSoftKeyboard(inputsKc.get(0));
    }

    public void showStandardView() {
        setDisplayedChild(FLIPPER_CHILD_STANDARD);
    }

    public void showVegetationView() {
        setDisplayedChild(FLIPPER_CHILD_VEGETATION);
    }

    public boolean isShowingStandardView() {
        return getDisplayedChild() == FLIPPER_CHILD_STANDARD;
    }

    public void doCustomBackLogic() {
        if (getDisplayedChild() == FLIPPER_CHILD_SLOPE) {
            try {
                float value = decimalFormatter.toFloat(inputSurfaceAccumulation.getText()
                        .toString());
                presenter.onSaveExitCustomSlopeView(value);
                PresentationUtils.hideSoftKeyboard(inputSurfaceAccumulation);
            } catch (ParseException pe) {
                inputSurfaceAccumulation.setError(getContext().getString(R.string
                        .all_error_invalid));
            }
        } else if (getDisplayedChild() == FLIPPER_CHILD_SOIL) {
            float intakeRate = Float.NaN;
            float soilFieldCapacity = Float.NaN;
            try {
                intakeRate = decimalFormatter.toFloat(inputSoilIntakeRate.getText().toString());
            } catch (ParseException pe) {
                inputSoilIntakeRate.setError(getContext().getString(R.string.all_error_invalid));
            }
            try {
                soilFieldCapacity = decimalFormatter.toFloat(inputSoilFieldCapacity.getText()
                        .toString());
            } catch (ParseException pe) {
                inputSoilFieldCapacity.setError(getContext().getString(R.string.all_error_invalid));
            }
            if (!Float.isNaN(intakeRate) && !Float.isNaN(soilFieldCapacity)) {
                presenter.onSaveExitCustomSoilView(intakeRate, soilFieldCapacity);
                PresentationUtils.hideSoftKeyboard(this);
            }
        } else if (getDisplayedChild() == FLIPPER_CHILD_SPRINKLER_HEAD) {
            float precipitationRate = Float.NaN;
            float applicationEfficiency = Float.NaN;
            try {
                precipitationRate = decimalFormatter.toFloat(inputPrecipitationRate.getText()
                        .toString());
            } catch (ParseException pe) {
                inputPrecipitationRate.setError(getContext().getString(R.string.all_error_invalid));
            }
            try {
                applicationEfficiency = decimalFormatter.toFloat(inputApplicationEfficiency
                        .getText().toString());
            } catch (ParseException pe) {
                inputApplicationEfficiency.setError(getContext().getString(R.string
                        .all_error_invalid));
            }
            if (!Float.isNaN(precipitationRate) && !Float.isNaN(applicationEfficiency)) {
                presenter.onSaveExitCustomSprinklerHeadView(precipitationRate,
                        applicationEfficiency);
                PresentationUtils.hideSoftKeyboard(this);
            }
        } else if (getDisplayedChild() == FLIPPER_CHILD_VEGETATION) {
            boolean isAllValid = true;
            float allowedDepletion = Float.NaN;
            float rootDepth = Float.NaN;
            float wilting = Float.NaN;
            float singleYearValue = Float.NaN;
            try {
                allowedDepletion = decimalFormatter.toFloat(inputAllowedDepletion.getText()
                        .toString());
            } catch (ParseException pe) {
                inputAllowedDepletion.setError(getContext().getString(R.string.all_error_invalid));
                isAllValid = false;
            }
            try {
                rootDepth = decimalFormatter.toFloat(inputRootDepth.getText().toString());
            } catch (ParseException pe) {
                inputRootDepth.setError(getContext().getString(R.string
                        .all_error_invalid));
                isAllValid = false;
            }
            try {
                wilting = decimalFormatter.toFloat(inputWilting.getText().toString());
            } catch (ParseException pe) {
                inputWilting.setError(getContext().getString(R.string.all_error_invalid));
                isAllValid = false;
            }
            if (checkSingleYearValue.isChecked()) {
                try {
                    singleYearValue = decimalFormatter.toFloat(inputSingleYearValue.getText()
                            .toString());
                } catch (ParseException pe) {
                    inputSingleYearValue.setError(getContext().getString(R.string
                            .all_error_invalid));
                    isAllValid = false;
                }
            }
            boolean isTallPlant = toggleTallVegetation.isChecked();

            if (isAllValid) {
                presenter.onSaveExitCustomVegetationView(allowedDepletion, rootDepth, wilting,
                        isTallPlant, singleYearValue, checkSingleYearValue.isChecked());
                PresentationUtils.hideSoftKeyboard(this);
            }
        } else if (getDisplayedChild() == FLIPPER_CHILD_VEGETATION_CROP_COEFFICIENT) {
            boolean isAllValid = true;
            List<Float> values = new ArrayList<>(inputsKc.size());
            for (EditText input : inputsKc) {
                try {
                    float value = decimalFormatter.toFloat(input.getText().toString());
                    values.add(value);
                } catch (ParseException pe) {
                    input.setError(getContext().getString(R.string.all_error_invalid));
                    isAllValid = false;
                }
            }
            if (isAllValid) {
                presenter.onSaveExitCustomVegetationCropCoefficientView(values);
            }
        } else {
            if (presenter.showAllAdvancedZoneSettings()) {
                float area = Float.NaN;
                float flowRate = Float.NaN;
                try {
                    area = decimalFormatter.toFloat(inputArea.getText().toString());
                } catch (ParseException pe) {
                    inputArea.setError(getContext().getString(R.string.all_error_invalid));
                }
                try {
                    flowRate = decimalFormatter.toFloat(inputFlowRate.getText().toString());
                } catch (ParseException pe) {
                    inputFlowRate.setError(getContext().getString(R.string.all_error_invalid));
                }
                if (!Float.isNaN(area) && !Float.isNaN(flowRate)) {
                    presenter.onSaveExitOptional(area, flowRate);
                    PresentationUtils.hideSoftKeyboard(this);
                }
            } else {
                presenter.onExitAdvanced();
                PresentationUtils.hideSoftKeyboard(this);
            }
        }
    }

    private List<ZoneDetailsAdvancedItem> vegetationTypeItems(boolean showOtherVegetationType) {
        List<ZoneDetailsAdvancedItem> items = new ArrayList<>();
        ZoneProperties.VegetationType type = ZoneProperties.VegetationType.LAWN;
        ZoneDetailsAdvancedItem item = new ItemVegetationType(type, vegetationTypeText(type,
                showOtherVegetationType), R.drawable.ic_vegetationtype_lawn);
        items.add(item);
        type = ZoneProperties.VegetationType.FRUIT_TREES;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_fruit_trees);
        items.add(item);
        type = ZoneProperties.VegetationType.FLOWERS;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_flowers);
        items.add(item);
        type = ZoneProperties.VegetationType.VEGETABLES;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_vegetables);
        items.add(item);
        type = ZoneProperties.VegetationType.CITRUS;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_citrus);
        items.add(item);
        type = ZoneProperties.VegetationType.TREES_BUSHES;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_trees_bushes);
        items.add(item);
        type = ZoneProperties.VegetationType.XERISCAPE;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_vegetationtype_xeriscape);
        items.add(item);
        type = ZoneProperties.VegetationType.CUSTOM;
        item = new ItemVegetationType(type, vegetationTypeText(type, showOtherVegetationType), R
                .drawable.ic_custom);
        items.add(item);
        return items;
    }

    private String vegetationTypeText(ZoneProperties.VegetationType vegetationType, boolean
            showOtherVegetationType) {
        switch (vegetationType) {
            case LAWN:
                return getResources().getString(R.string.zone_details_lawn);
            case FRUIT_TREES:
                return getResources().getString(R.string.zone_details_fruit_trees);
            case FLOWERS:
                return getResources().getString(R.string.zone_details_flowers);
            case VEGETABLES:
                return getResources().getString(R.string.zone_details_vegetables);
            case CITRUS:
                return getResources().getString(R.string.zone_details_citrus);
            case TREES_BUSHES:
                return getResources().getString(R.string.zone_details_trees_bushes);
            case XERISCAPE:
                return getResources().getString(R.string.zone_details_xeriscape);
            case CUSTOM:
                return getResources().getString(showOtherVegetationType ? R.string.all_other : R
                        .string.all_custom);
            default:
                return getResources().getString(R.string.zone_details_lawn);
        }
    }

    private List<ZoneDetailsAdvancedItem> soilTypeItems(boolean showExtraSoilTypes) {
        List<ZoneDetailsAdvancedItem> items = new ArrayList<>();
        ZoneProperties.SoilType soilType = ZoneProperties.SoilType.CLAY_LOAM;
        ZoneDetailsAdvancedItem item = new ItemSoilType(soilType, soilTypeText(soilType), R
                .drawable.ic_soiltype_clay_loam);
        items.add(item);
        soilType = ZoneProperties.SoilType.SILTY_CLAY;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable
                .ic_soiltype_silty_clay);
        items.add(item);
        soilType = ZoneProperties.SoilType.CLAY;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable.ic_soiltype_clay);
        items.add(item);
        soilType = ZoneProperties.SoilType.LOAM;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable.ic_soiltype_loam);
        items.add(item);
        soilType = ZoneProperties.SoilType.SANDY_LOAM;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable
                .ic_soiltype_sandy_loam);
        items.add(item);
        soilType = ZoneProperties.SoilType.LOAMY_SAND;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable
                .ic_soiltype_loamy_sand);
        items.add(item);
        soilType = ZoneProperties.SoilType.SAND;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable.ic_soiltype_sand);
        items.add(item);
        if (showExtraSoilTypes) {
            soilType = ZoneProperties.SoilType.SANDY_CLAY;
            item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable
                    .ic_soiltype_sandy_clay);
            items.add(item);
            soilType = ZoneProperties.SoilType.SILT_LOAM;
            item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable
                    .ic_soiltype_silt_loam);
            items.add(item);
            soilType = ZoneProperties.SoilType.SILT;
            item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable.ic_soiltype_silt);
            items.add(item);
        }
        soilType = ZoneProperties.SoilType.CUSTOM;
        item = new ItemSoilType(soilType, soilTypeText(soilType), R.drawable.ic_custom);
        items.add(item);
        return items;
    }

    private String soilTypeText(ZoneProperties.SoilType soilType) {
        switch (soilType) {
            case CLAY_LOAM:
                return getResources().getString(R.string.zone_details_soil_clay_loam);
            case SILTY_CLAY:
                return getResources().getString(R.string.zone_details_soil_silty_clay);
            case CLAY:
                return getResources().getString(R.string.zone_details_soil_clay);
            case LOAM:
                return getResources().getString(R.string.zone_details_soil_loam);
            case SANDY_LOAM:
                return getResources().getString(R.string.zone_details_soil_sandy_loam);
            case LOAMY_SAND:
                return getResources().getString(R.string.zone_details_soil_loamy_sand);
            case SAND:
                return getResources().getString(R.string.zone_details_soil_sand);
            case CUSTOM:
                return getResources().getString(R.string.all_custom);
            case SANDY_CLAY:
                return getResources().getString(R.string.zone_details_soil_sandy_clay);
            case SILT_LOAM:
                return getResources().getString(R.string.zone_details_soil_silt_loam);
            case SILT:
                return getResources().getString(R.string.zone_details_soil_silt);
            default:
                return getResources().getString(R.string.zone_details_soil_clay_loam);
        }
    }

    private List<ZoneDetailsAdvancedItem> sprinklerHeadsItems() {
        List<ZoneDetailsAdvancedItem> items = new ArrayList<>();
        ZoneProperties.SprinklerHeads sprinklerHeads = ZoneProperties.SprinklerHeads.POPUP_SPRAY;
        ZoneDetailsAdvancedItem item = new ItemSprinklerHeads(sprinklerHeads,
                sprinklerHeadsText(sprinklerHeads), R.drawable.ic_sprinkler_head_type_popup_spray);
        items.add(item);
        sprinklerHeads = ZoneProperties.SprinklerHeads.ROTORS;
        item = new ItemSprinklerHeads(sprinklerHeads, sprinklerHeadsText(sprinklerHeads), R
                .drawable.ic_sprinkler_head_type_rotors);
        items.add(item);
        sprinklerHeads = ZoneProperties.SprinklerHeads.SURFACE_DRIP;
        item = new ItemSprinklerHeads(sprinklerHeads, sprinklerHeadsText(sprinklerHeads), R
                .drawable.ic_sprinkler_head_type_surface_drip);
        items.add(item);
        sprinklerHeads = ZoneProperties.SprinklerHeads.BUBBLERS;
        item = new ItemSprinklerHeads(sprinklerHeads, sprinklerHeadsText(sprinklerHeads), R.drawable
                .ic_sprinkler_head_type_bubblers);
        items.add(item);
        sprinklerHeads = ZoneProperties.SprinklerHeads.CUSTOM;
        item = new ItemSprinklerHeads(sprinklerHeads, sprinklerHeadsText(sprinklerHeads), R
                .drawable.ic_custom);
        items.add(item);
        return items;
    }

    private String sprinklerHeadsText(ZoneProperties.SprinklerHeads sprinklerHeads) {
        switch (sprinklerHeads) {
            case POPUP_SPRAY:
                return getResources().getString(R.string.zone_details_head_popup_spray);
            case ROTORS:
                return getResources().getString(R.string.zone_details_head_rotors);
            case SURFACE_DRIP:
                return getResources().getString(R.string.zone_details_head_surface_drip);
            case BUBBLERS:
                return getResources().getString(R.string.zone_details_head_bubblers_drip);
            case CUSTOM:
                return getResources().getString(R.string.all_custom);
            default:
                return getResources().getString(R.string.zone_details_head_popup_spray);
        }
    }

    private List<ZoneDetailsAdvancedItem> slopeItems() {
        List<ZoneDetailsAdvancedItem> items = new ArrayList<>();
        ZoneProperties.Slope slope = ZoneProperties.Slope.FLAT;
        ZoneDetailsAdvancedItem item = new ItemSlope(slope, slopeText(slope), R.drawable
                .ic_slopetype_flat);
        items.add(item);
        slope = ZoneProperties.Slope.MODERATE;
        item = new ItemSlope(slope, slopeText(slope), R.drawable.ic_slopetype_moderate);
        items.add(item);
        slope = ZoneProperties.Slope.HIGH;
        item = new ItemSlope(slope, slopeText(slope), R.drawable.ic_slopetype_high);
        items.add(item);
        slope = ZoneProperties.Slope.VERY_HIGH;
        item = new ItemSlope(slope, slopeText(slope), R.drawable.ic_slopetype_very_high);
        items.add(item);
        slope = ZoneProperties.Slope.CUSTOM;
        item = new ItemSlope(slope, slopeText(slope), R.drawable.ic_custom);
        items.add(item);
        return items;
    }

    private String slopeText(ZoneProperties.Slope slope) {
        switch (slope) {
            case FLAT:
                return getResources().getString(R.string.zone_details_slope_flat);
            case MODERATE:
                return getResources().getString(R.string.zone_details_slope_moderate);
            case HIGH:
                return getResources().getString(R.string.zone_details_slope_high);
            case VERY_HIGH:
                return getResources().getString(R.string.zone_details_slope_very_high);
            case CUSTOM:
                return getResources().getString(R.string.all_custom);
            default:
                return getResources().getString(R.string.zone_details_slope_flat);
        }
    }

    private List<ZoneDetailsAdvancedItem> exposureItems() {
        List<ZoneDetailsAdvancedItem> items = new ArrayList<>();
        ZoneProperties.Exposure exposure = ZoneProperties.Exposure.FULL_SUN;
        ZoneDetailsAdvancedItem item = new ItemExposure(exposure, exposureText(exposure), R
                .drawable.ic_sunexposure_full_sun);
        items.add(item);
        exposure = ZoneProperties.Exposure.PARTIAL_SHADE;
        item = new ItemExposure(exposure, exposureText(exposure), R.drawable
                .ic_sunexposure_partial_exposure);
        items.add(item);
        exposure = ZoneProperties.Exposure.FULL_SHADE;
        item = new ItemExposure(exposure, exposureText(exposure), R.drawable
                .ic_sunexposure_full_shade);
        items.add(item);
        return items;
    }

    private String exposureText(ZoneProperties.Exposure exposure) {
        switch (exposure) {
            case FULL_SUN:
                return getResources().getString(R.string.zone_details_exposure_full_sun);
            case PARTIAL_SHADE:
                return getResources().getString(R.string.zone_details_exposure_partial_shade);
            case FULL_SHADE:
                return getResources().getString(R.string.zone_details_exposure_full_shade);
            default:
                return getResources().getString(R.string.zone_details_exposure_full_sun);
        }
    }

    private static class TagExpanded {
        public boolean isExpanded;

        public TagExpanded(boolean isExpanded) {
            this.isExpanded = isExpanded;
        }
    }
}
