package com.rainmachine.presentation.screens.waternow

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ViewFlipper
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.rainmachine.R
import com.rainmachine.presentation.activities.SprinklerActivity
import com.rainmachine.presentation.util.formatter.CalendarFormatter
import com.rainmachine.presentation.widgets.ItemOffsetDecoration
import javax.inject.Inject

class WaterNowView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewFlipper(context, attrs), WaterNowContract.View {

    @Inject
    lateinit var presenter: WaterNowContract.Presenter
    @Inject
    lateinit var calendarFormatter: CalendarFormatter

    @BindView(R.id.recycler)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.btn_stop_all)
    lateinit var btnStopAll: FloatingActionButton

    private lateinit var controller: WaterNowController

    init {
        if (!isInEditMode) {
            (getContext() as SprinklerActivity).inject(this)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this)
        if (!isInEditMode) {
            presenter.attachView(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            presenter.init()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!isInEditMode) {
            presenter.destroy()
        }
    }

    @OnClick(R.id.btn_stop_all)
    fun onClickStopAll() {
        presenter.onClickStopAll()
    }

    override fun render(state: WaterNowState) {
        if (state.initialize) {
            initialize()
        }
        if (state.isProgress) {
            showProgress()
        } else if (state.isContent) {
            controller.setData(state)

            val isWatering = (state.enabledZones + state.disabledZones).any { it.state != ZoneViewModel.State.IDLE }
            btnStopAll.visibility = if (isWatering) View.VISIBLE else View.GONE
            showContent()
        }
    }

    private fun initialize() {
        val offset = context.resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        recyclerView.addItemDecoration(ItemOffsetDecoration(0, offset, 0, offset))

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val viewHeight = context.resources.getDimensionPixelSize(R.dimen.height_card_with_image)
        val scrollOffset = (recyclerView.measuredHeight - viewHeight) / 2

        controller = WaterNowController(context, presenter, calendarFormatter, layoutManager,
                scrollOffset)
        recyclerView.adapter = controller.adapter
    }

    private fun showProgress() {
        displayedChild = FLIPPER_PROGRESS
    }

    private fun showContent() {
        displayedChild = FLIPPER_CONTENT
    }

    companion object {

        private val FLIPPER_CONTENT = 0
        private val FLIPPER_PROGRESS = 1
    }
}
