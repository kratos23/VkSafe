package com.pavelkrylov.vsafe.feautures.order_details

import com.pavelkrylov.vsafe.logic.OrderStatus

data class UIState(val orderId: Long, val orderInfo: OrderInfo?)

data class OrderInfo(val orderStatus: OrderStatus, val orderList: List<IOrderDetailsItem>)