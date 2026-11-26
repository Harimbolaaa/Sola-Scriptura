package com.harimbola.solascriptura.feature.truthmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.LocalStudyColors
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.domain.model.TruthNodeState
import kotlin.math.roundToInt

@Composable
fun TruthTreeScreen(
    onOpenStudyDetail: (String) -> Unit,
    viewModel: TruthTreeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val semantic = LocalStudyColors.current
    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dims.screenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.truth_tree_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.truth_tree_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(semantic.completedNodeColor, stringResource(R.string.truth_tree_legend_completed))
            LegendItem(semantic.inProgressNodeColor, stringResource(R.string.truth_tree_legend_in_progress))
            LegendItem(colorScheme.outline, stringResource(R.string.truth_tree_legend_unstarted))
        }

        // Interactive Canvas box
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colorScheme.surfaceContainerLow,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val nodes = state.graph.nodes
                val edges = state.graph.edges
                val selectedId = state.selectedNode?.studyId
                val nodePositions = remember(nodes) {
                    nodes.associate { node ->
                        node.studyId to Offset(
                            x = 180f + node.laneIndex * 300f,
                            y = 140f + node.depth * 220f
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("truth_tree_canvas")
                        .pointerInput(nodes) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.45f, 2.2f)
                                panOffset += pan
                            }
                        }
                        .pointerInput(nodes, scale, panOffset) {
                            detectTapGestures { tap ->
                                val boxW = 240f * scale
                                val boxH = 92f * scale
                                val hit = nodes.firstOrNull { node ->
                                    val center = (nodePositions[node.studyId] ?: Offset.Zero) * scale + panOffset
                                    tap.x in (center.x - boxW / 2f)..(center.x + boxW / 2f) &&
                                        tap.y in (center.y - boxH / 2f)..(center.y + boxH / 2f)
                                }
                                if (hit != null) {
                                    viewModel.selectNode(hit.studyId)
                                }
                            }
                        }
                ) {
                    val boxW = 240f * scale
                    val boxH = 92f * scale
                    edges.forEach { edge ->
                        val from = nodePositions[edge.fromStudyId]?.let { it * scale + panOffset }
                        val to = nodePositions[edge.toStudyId]?.let { it * scale + panOffset }
                        if (from != null && to != null) {
                            drawLine(
                                color = colorScheme.outlineVariant,
                                start = from,
                                end = to,
                                strokeWidth = 3f * scale
                            )
                        }
                    }

                    nodes.forEach { node ->
                        val center = (nodePositions[node.studyId] ?: Offset.Zero) * scale + panOffset
                        val nodeColor = when (node.state) {
                            TruthNodeState.COMPLETED -> semantic.completedNodeColor
                            TruthNodeState.IN_PROGRESS -> semantic.inProgressNodeColor
                            TruthNodeState.UNSTARTED -> colorScheme.outline
                        }
                        val topLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f)
                        val isSelected = node.studyId == selectedId

                        drawRoundRect(
                            color = colorScheme.surfaceContainerHigh,
                            topLeft = topLeft,
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(24f * scale, 24f * scale)
                        )
                        drawRoundRect(
                            color = if (isSelected) colorScheme.primary else nodeColor,
                            topLeft = topLeft,
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(24f * scale, 24f * scale),
                            style = Stroke(width = (if (isSelected) 5f else 2.5f) * scale)
                        )
                        drawMeasuredNodeLabel(
                            textMeasurer = textMeasurer,
                            text = node.title,
                            style = TextStyle(
                                color = colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            topLeft = Offset(topLeft.x + 14f * scale, topLeft.y + 18f * scale),
                            maxWidthPx = (boxW - 28f * scale).roundToInt().coerceAtLeast(16),
                            maxHeightPx = (boxH - 28f * scale).roundToInt().coerceAtLeast(16)
                        )
                    }
                }

                // Zoom controls overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
                    ) {
                        Row {
                            IconButton(
                                onClick = { scale = (scale + 0.2f).coerceAtMost(2.2f) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("truth_tree_zoom_in")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Zoom avant")
                            }
                            IconButton(
                                onClick = { scale = (scale - 0.2f).coerceAtLeast(0.45f) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("truth_tree_zoom_out")
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Zoom arrière")
                            }
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    panOffset = Offset.Zero
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("truth_tree_reset_view")
                            ) {
                                Icon(Icons.Rounded.CenterFocusStrong, contentDescription = "Recentrer")
                            }
                        }
                    }
                }
            }
        }

        // Selected study detail card at bottom
        state.selectedStudy?.let { study ->
            SolaSurfaceCard(
                modifier = Modifier
                    .widthIn(max = dims.maxContentWidth)
                    .align(Alignment.CenterHorizontally)
                    .testTag("truth_tree_selected_card")
            ) {
                Text(
                    text = study.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = study.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenStudyDetail(study.studyId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("truth_tree_open_study_button")
                ) {
                    Text(stringResource(R.string.truth_tree_open_study))
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMeasuredNodeLabel(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    topLeft: Offset,
    maxWidthPx: Int,
    maxHeightPx: Int
) {
    val result = textMeasurer.measure(
        text = text,
        style = style,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        constraints = Constraints(maxWidth = maxWidthPx, maxHeight = maxHeightPx)
    )
    drawText(result, topLeft = topLeft)
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
