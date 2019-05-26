package com.crewly.crew

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.models.Rank
import com.crewly.utils.*
import com.crewly.views.EnterExitRightView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.rank_selection_view.view.*

/**
 * Created by Derek on 24/06/2018
 */
class RankSelectionView @JvmOverloads constructor(
  context: Context,
  attributes: AttributeSet? = null,
  defStyle: Int = 0
):
  ConstraintLayout(context, attributes, defStyle), EnterExitRightView {

  companion object {
    private val pilotRanks = arrayOf(Rank.CAPTAIN, Rank.FIRST_OFFICER, Rank.PU_SEP, Rank.PU_LC,
      Rank.PU, Rank.JU_PU, Rank.JU, Rank.JU_NEW)

    private val crewRanks = arrayOf(Rank.FIRST_OFFICER, Rank.PU_SEP, Rank.PU_LC,
      Rank.PU, Rank.JU_PU, Rank.JU, Rank.JU_NEW)
  }

  override val view: View = this
  private val disposables = CompositeDisposable()

  private var selectedRank: RankView? = null
  var rankSelectedAction: ((rank: Rank) -> Unit)? = null

  init {
    setBackgroundColor(context.getColorCompat(R.color.white))
    isClickable = true
    inflate(R.layout.rank_selection_view, attachToRoot = true)
    setUpPadding()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    observeCloseImage()
    observeRanks()
  }

  override fun onDetachedFromWindow() {
    disposables.clear()
    super.onDetachedFromWindow()
  }

  fun displayRanks(isPilot: Boolean, selectedRank: Rank? = null) {
    val ranks = if (isPilot) pilotRanks else crewRanks
    val rankViews = arrayOf(rank_one, rank_two, rank_three, rank_four, rank_five, rank_six, rank_seven, rank_eight)

    rankViews.forEachIndexed { index, rankView ->
      if (index < ranks.size) {
        rankView.rank = ranks[index]
      } else rankView.visibility = View.INVISIBLE
    }

    selectedRank?.let {
      val selectedRankPos = if (isPilot) pilotRanks.indexOf(it) else crewRanks.indexOf(it)
      if (selectedRankPos != -1) {
        rankViews[selectedRankPos].isSelected = true
        this.selectedRank = rankViews[selectedRankPos]
      }
    }
  }

  private fun setUpPadding() {
    val padding = context.resources.getDimensionPixelOffset(R.dimen.rank_selection_view_padding)
    smartPadding(leftPadding = padding, rightPadding = padding)
  }

  private fun observeCloseImage() {
    disposables + image_close
      .throttleClicks()
      .subscribe {
        selectedRank?.let { rankSelectedAction?.invoke(it.rank) }
        hideView()
      }
  }

  private fun observeRanks() {
    disposables + rank_one.throttleClicks().mapAsView(rank_one)
      .mergeWith(rank_two.throttleClicks().mapAsView(rank_two))
      .mergeWith(rank_three.throttleClicks().mapAsView(rank_three))
      .mergeWith(rank_four.throttleClicks().mapAsView(rank_four))
      .mergeWith(rank_five.throttleClicks().mapAsView(rank_five))
      .mergeWith(rank_six.throttleClicks().mapAsView(rank_six))
      .mergeWith(rank_seven.throttleClicks().mapAsView(rank_seven))
      .mergeWith(rank_eight.throttleClicks().mapAsView(rank_eight))
      .subscribe { selectedView -> handleRankSelection(selectedView) }
  }

  private fun handleRankSelection(selectedRank: View) {
    this.selectedRank?.isSelected = false
    selectedRank.isSelected = true
    this.selectedRank = selectedRank as RankView
  }
}