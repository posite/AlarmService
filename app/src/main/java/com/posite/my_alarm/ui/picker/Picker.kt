package com.posite.my_alarm.ui.picker

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.posite.my_alarm.data.model.PickerState
import com.posite.my_alarm.ui.theme.MyAlarmTheme
import com.posite.my_alarm.util.fadingEdge
import com.posite.my_alarm.util.noRippleClickable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> Picker(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: PickerState<T>,
    startIndex: Int = 0,
    visibleItemsCount: Int = 3,
    textModifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    selectedTextStyle: TextStyle = LocalTextStyle.current,
    itemPadding: PaddingValues = PaddingValues(8.dp),
    fadingEdgeGradient: Brush = Brush.verticalGradient(
        0f to Color.Transparent,
        0.5f to Color.Black,
        1f to Color.Transparent
    ),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    itemTextAlignment: Alignment.Vertical = Alignment.CenterVertically,
    isInfinity: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val visibleItemsMiddle = remember { visibleItemsCount / 2 }

    val adjustedItems = if (!isInfinity) {
        listOf(null) + items + listOf(null)
    } else {
        items
    }

    val listScrollCount = if (isInfinity) {
        Int.MAX_VALUE
    } else {
        adjustedItems.size
    }

    val listScrollMiddle = remember { listScrollCount / 2 }
    val listStartIndex = remember {
        if (isInfinity) {
            listScrollMiddle - listScrollMiddle % adjustedItems.size - visibleItemsMiddle + startIndex
        } else {
            startIndex
        }
    }

    fun getItem(index: Int) = adjustedItems[index % adjustedItems.size]

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val itemHeight = with(density) {
        selectedTextStyle.fontSize.toDp() + itemPadding.calculateTopPadding() + itemPadding.calculateBottomPadding()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .mapNotNull { index -> getItem(index + visibleItemsMiddle) }
            .distinctUntilChanged()
            .collect { item -> state.selectedItem = item }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentSize()
                .height(itemHeight * 3)
                .fadingEdge(fadingEdgeGradient)
        ) {
            items(
                listScrollCount,
                key = { it },
            ) { index ->
                val fraction by remember {
                    derivedStateOf {
                        val currentItem =
                            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == index }
                        currentItem?.offset?.let { offset ->
                            val itemHeightPx = with(density) { itemHeight.toPx() }
                            val fraction =
                                (offset - itemHeightPx * visibleItemsMiddle) / itemHeightPx
                            abs(fraction.coerceIn(-1f, 1f))
                        } ?: 0f
                    }
                }

                val currentItemText by remember {
                    mutableStateOf(if (getItem(index) == null) "" else getItem(index).toString())
                }

                Log.d("Picker", "Index: $index, Text: $currentItemText")

                Text(
                    text = currentItemText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle.copy(
                        fontSize = lerp(
                            selectedTextStyle.fontSize,
                            textStyle.fontSize,
                            fraction
                        ),
                        color = lerp(
                            selectedTextStyle.color,
                            textStyle.color,
                            fraction
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(align = itemTextAlignment)
                        .wrapContentWidth()
                        .then(textModifier)
                        .noRippleClickable {
                            //Log.d("Picker", "Clicked on $index item: ${getItem(index)}")
                            if(!isInfinity && (index == 0 || index == listScrollCount - 1)) {
                                return@noRippleClickable
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = index - 1)
                            }
                        }
                )
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun PickerPreView() {
    MyAlarmTheme {
        Picker(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp),
            items = listOf("오전", " 오후"),
            state = PickerState(0),
            startIndex = 0,
            textStyle = TextStyle(fontSize = 24.sp),
            selectedTextStyle = TextStyle(fontSize = 24.sp),
            visibleItemsCount = 2,
            isInfinity = false
        )
    }
}