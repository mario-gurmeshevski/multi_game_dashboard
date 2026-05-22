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
    private final int timeQuantum;
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
                                    int[] correctEndTimes, int touchSlop, int timeQuantum,
                                    Callback callback) {
        this.slotsContainer = slotsContainer;
        this.poolContainer = poolContainer;
        this.viewFactory = viewFactory;
        this.isRoundRobin = isRoundRobin;
        this.slotProcesses = slotProcesses;
        this.correctStartTimes = correctStartTimes;
        this.correctEndTimes = correctEndTimes;
        this.touchSlop = touchSlop;
        this.timeQuantum = timeQuantum;
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
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop / 2f || dy > touchSlop / 2f) {
                            isDragging = true;
                            startDrag(v, DRAG_TAG_PROCESS + poolIndex);
                            if (!isRoundRobin) {
                                v.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
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
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop || dy > touchSlop) {
                            isDragging = true;
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            startDrag(v, DRAG_TAG_PROCESS + "_slot_" + slotIndex);
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
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
                        restoreDragViewVisibility(event);
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
                    restoreDragViewVisibility(event);
                    return true;
            }
            return true;
        });
    }

    private void restoreDragViewVisibility(DragEvent event) {
        if (event.getResult()) return;
        View dragView = (View) event.getLocalState();
        if (dragView != null && dragView.getVisibility() != View.VISIBLE) {
            dragView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidPlacement(ProcessInfo process, int slotIndex) {
        int slotDuration = correctEndTimes[slotIndex] - correctStartTimes[slotIndex];
        int expected = Math.min(process.getRemainingBurstTime(), timeQuantum);
        return slotDuration == expected;
    }

    private void setSlotError(int slotIndex, boolean error) {
        ViewGroup slotView = (ViewGroup) slotsContainer.getChildAt(slotIndex);
        if (slotView == null) return;
        if (error) {
            slotView.setBackgroundResource(R.drawable.target_slot_error_bg);
        } else {
            slotView.setBackgroundResource(R.drawable.target_slot_bg);
        }
    }

    private void resetSlotBackground(int slotIndex) {
        setSlotError(slotIndex, false);
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

            boolean draggedValid = true;
            boolean existingValid = true;

            if (isRoundRobin) {
                int fromSlotDuration = correctEndTimes[fromSlot] - correctStartTimes[fromSlot];
                int toSlotDuration = correctEndTimes[slotIndex] - correctStartTimes[slotIndex];
                draggedProcess.restoreBurst(fromSlotDuration);
                draggedValid = isValidPlacement(draggedProcess, slotIndex);
                draggedProcess.reduceBurst(toSlotDuration);
                if (existingInTarget != null) {
                    existingInTarget.restoreBurst(toSlotDuration);
                    existingValid = isValidPlacement(existingInTarget, fromSlot);
                    existingInTarget.reduceBurst(fromSlotDuration);
                }
            }

            if (existingInTarget != null) {
                putProcessInSlot(fromSlot, existingInTarget);
            } else {
                clearSlot(fromSlot);
            }
            putProcessInSlot(slotIndex, draggedProcess);
            dragView.setVisibility(View.GONE);

            if (isRoundRobin) {
                setSlotError(slotIndex, !draggedValid);
                if (existingInTarget != null) {
                    setSlotError(fromSlot, !existingValid);
                }
                updatePoolVisibility(draggedProcess);
                if (existingInTarget != null) {
                    updatePoolVisibility(existingInTarget);
                }
            }
        } else if (poolIndex != null) {
            ProcessInfo existingInTarget = slotProcesses[slotIndex];
            slotProcesses[slotIndex] = draggedProcess;

            boolean draggedValid = true;

            if (isRoundRobin) {
                int slotDuration = correctEndTimes[slotIndex] - correctStartTimes[slotIndex];
                if (existingInTarget != null) {
                    int existingSlotDuration = correctEndTimes[slotIndex] - correctStartTimes[slotIndex];
                    existingInTarget.restoreBurst(existingSlotDuration);
                    updatePoolVisibility(existingInTarget);
                }
                draggedValid = isValidPlacement(draggedProcess, slotIndex);
                draggedProcess.reduceBurst(slotDuration);
            } else {
                if (existingInTarget != null) {
                    returnProcessToPool(existingInTarget);
                }
                poolContainer.removeView(dragView);
            }
            putProcessInSlot(slotIndex, draggedProcess);

            if (isRoundRobin) {
                setSlotError(slotIndex, !draggedValid);
                updatePoolVisibility(draggedProcess);
            }
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

        if (isRoundRobin) {
            int slotDuration = correctEndTimes[fromSlot] - correctStartTimes[fromSlot];
            process.restoreBurst(slotDuration);
        }

        slotProcesses[fromSlot] = null;
        clearSlot(fromSlot);

        if (!isRoundRobin) {
            returnProcessToPool(process);
        } else {
            View poolSquare = viewFactory.findPoolSquare(poolContainer, process);
            if (poolSquare == null) {
                addProcessBackToPool(process);
            } else {
                poolSquare.setVisibility(View.VISIBLE);
                viewFactory.updatePoolBurstDisplay(poolContainer, process);
            }
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
        resetSlotBackground(slotIndex);
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

    private void updatePoolVisibility(ProcessInfo process) {
        viewFactory.updatePoolBurstDisplay(poolContainer, process);
        View poolSquare = viewFactory.findPoolSquare(poolContainer, process);
        if (poolSquare != null) {
            poolSquare.setVisibility(process.getRemainingBurstTime() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void addProcessBackToPool(ProcessInfo process) {
        View square = viewFactory.createProcessSquare(process, poolContainer.getChildCount());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        int margin = (int) (4 * poolContainer.getContext().getResources().getDisplayMetrics().density);
        lp.setMargins(margin, 0, margin, 0);
        square.setLayoutParams(lp);
        attachPoolDragListener(square, poolContainer.getChildCount());
        poolContainer.addView(square);
    }
}
