package com.example.educationgame.scheduler;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.educationgame.R;
import com.example.educationgame.data.scheduler.model.ProcessInfo;

public class SchedulerDragController implements ProcessSquareFactory.ProcessSquareDragListener {

    private static final String DRAG_TAG_PROCESS = "process_";

    private final LinearLayout slotsContainer;
    private final LinearLayout poolContainer;
    private final ProcessSquareFactory viewFactory;
    private final boolean isRoundRobin;
    private final int[] correctStartTimes;
    private final int[] correctEndTimes;
    private final int touchSlop;
    private final Callback callback;

    private final ProcessInfo[] slotProcesses;
    private float dragStartX, dragStartY;
    private boolean isDragging = false;

    public interface Callback {
        void onSlotDropCompleted();
        void onReturnToPool();
        boolean isCompleted();
    }

    public SchedulerDragController(LinearLayout slotsContainer, LinearLayout poolContainer,
                                    ProcessSquareFactory viewFactory, boolean isRoundRobin,
                                    ProcessInfo[] slotProcesses, int[] correctStartTimes,
                                    int[] correctEndTimes, int touchSlop, Callback callback) {
        this.slotsContainer = slotsContainer;
        this.poolContainer = poolContainer;
        this.viewFactory = viewFactory;
        this.isRoundRobin = isRoundRobin;
        this.slotProcesses = slotProcesses;
        this.correctStartTimes = correctStartTimes;
        this.correctEndTimes = correctEndTimes;
        this.touchSlop = touchSlop;
        this.callback = callback;
    }

    public void attachPoolDragListener(View square, int poolIndex) {
        square.setOnTouchListener((v, event) -> {
            if (callback.isCompleted()) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = event.getRawX();
                    dragStartY = event.getRawY();
                    isDragging = false;
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop / 2f || dy > touchSlop / 2f) {
                            isDragging = true;
                            v.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                            startDrag(v, DRAG_TAG_PROCESS + poolIndex);
                            if (!isRoundRobin) {
                                v.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isDragging) {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    }
                    isDragging = false;
                    return false;
            }
            return false;
        });
    }

    private void attachSlotDragListener(View square, int slotIndex) {
        square.setOnTouchListener((v, event) -> {
            if (callback.isCompleted()) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = event.getRawX();
                    dragStartY = event.getRawY();
                    isDragging = false;
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop / 2f || dy > touchSlop / 2f) {
                            isDragging = true;
                            v.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                            startDrag(v, DRAG_TAG_PROCESS + "_slot_" + slotIndex);
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isDragging) {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    }
                    isDragging = false;
                    return false;
            }
            return false;
        });
    }

    private void startDrag(View v, String dragData) {
        ClipData clip = ClipData.newPlainText(dragData, dragData);
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
        v.startDragAndDrop(clip, shadow, v, 0);
    }

    public void setupDropZones(HorizontalScrollView poolScrollView) {
        for (int i = 0; i < slotsContainer.getChildCount(); i++) {
            View slotView = slotsContainer.getChildAt(i);
            int slotIndex = i;
            slotView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (!callback.isCompleted()) {
                            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start();
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DROP:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        if (callback.isCompleted()) return true;
                        handleDropOnSlot(slotIndex, event);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        if (!isRoundRobin) {
                            restoreDragViewVisibility(event);
                        }
                        return true;
                }
                return true;
            });
        }

        poolScrollView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    if (callback.isCompleted()) return true;
                    handleDropOnPool(event);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!isRoundRobin) {
                        restoreDragViewVisibility(event);
                    }
                    return true;
            }
            return true;
        });
    }

    private void restoreDragViewVisibility(DragEvent event) {
        View dragView = (View) event.getLocalState();
        if (dragView != null && dragView.getVisibility() != View.VISIBLE) {
            dragView.setVisibility(View.VISIBLE);
        }
    }

    private void handleDropOnSlot(int slotIndex, DragEvent event) {
        View dragView = (View) event.getLocalState();
        if (dragView == null) return;

        ProcessInfo draggedProcess = (ProcessInfo) dragView.getTag(R.id.tag_process_info);
        if (draggedProcess == null) return;

        Integer poolIndex = (Integer) dragView.getTag(R.id.tag_pool_index);
        Integer fromSlot = (Integer) dragView.getTag(R.id.tag_slot_index);

        if (fromSlot != null) {
            ProcessInfo existingInTarget = slotProcesses[slotIndex];
            slotProcesses[slotIndex] = draggedProcess;
            slotProcesses[fromSlot] = existingInTarget;

            if (existingInTarget != null) {
                putProcessInSlot(fromSlot, existingInTarget);
            } else {
                clearSlot(fromSlot);
            }
            putProcessInSlot(slotIndex, draggedProcess);
            dragView.setVisibility(View.GONE);
        } else if (poolIndex != null) {
            ProcessInfo existingInTarget = slotProcesses[slotIndex];
            slotProcesses[slotIndex] = draggedProcess;

            if (!isRoundRobin) {
                if (existingInTarget != null) {
                    returnProcessToPool(existingInTarget);
                }
                poolContainer.removeView(dragView);
            }
            putProcessInSlot(slotIndex, draggedProcess);
        }

        callback.onSlotDropCompleted();
    }

    private void handleDropOnPool(DragEvent event) {
        View dragView = (View) event.getLocalState();
        if (dragView == null) return;

        Integer fromSlot = (Integer) dragView.getTag(R.id.tag_slot_index);
        if (fromSlot == null) {
            if (!isRoundRobin) {
                dragView.setVisibility(View.VISIBLE);
            }
            return;
        }

        ProcessInfo process = slotProcesses[fromSlot];
        if (process == null) {
            if (!isRoundRobin) {
                dragView.setVisibility(View.VISIBLE);
            }
            return;
        }

        slotProcesses[fromSlot] = null;
        clearSlot(fromSlot);

        if (!isRoundRobin) {
            returnProcessToPool(process);
        }

        callback.onReturnToPool();
    }

    public void putProcessInSlot(int slotIndex, ProcessInfo process) {
        ViewGroup slotView = (ViewGroup) slotsContainer.getChildAt(slotIndex);
        if (slotView == null) return;

        TextView slotLabel = slotView.findViewById(R.id.slotLabel);
        slotLabel.setVisibility(View.GONE);

        View existingSquare = slotView.findViewWithTag("placed_square");
        if (existingSquare != null) {
            slotView.removeView(existingSquare);
        }

        View square = viewFactory.createPlacedProcessSquare(process, slotIndex);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        square.setLayoutParams(lp);
        attachSlotDragListener(square, slotIndex);

        slotView.addView(square);
    }

    public void clearSlot(int slotIndex) {
        ViewGroup slotView = (ViewGroup) slotsContainer.getChildAt(slotIndex);
        if (slotView == null) return;

        View placedSquare = slotView.findViewWithTag("placed_square");
        if (placedSquare != null) {
            slotView.removeView(placedSquare);
        }
        TextView slotLabel = slotView.findViewById(R.id.slotLabel);
        slotLabel.setVisibility(View.VISIBLE);
    }

    public void returnProcessToPool(ProcessInfo process) {
        View square = viewFactory.createProcessSquare(process, poolContainer.getChildCount());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        int margin = (int) (4 * poolContainer.getContext().getResources().getDisplayMetrics().density);
        lp.setMargins(margin, 0, margin, 0);
        square.setLayoutParams(lp);
        attachPoolDragListener(square, poolContainer.getChildCount());
        poolContainer.addView(square);
    }
}
