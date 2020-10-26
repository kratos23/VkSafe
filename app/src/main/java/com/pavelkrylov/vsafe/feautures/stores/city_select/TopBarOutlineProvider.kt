package com.pavelkrylov.vsafe.feautures.stores.city_select

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import com.pavelkrylov.vsafe.base.dpToPx

class TopBarOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRect(0, 18.dpToPx, view.width, view.height)
    }

}