package com.example.educationgame.scheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.educationgame.R;
import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.scheduler.model.ProcessInfo;

import java.util.List;

public class ProcessSquareFactory {

    private final Context context;
    private final SchedulingAlgorithm algorithm;

    public ProcessSquareFactory(Context context, SchedulingAlgorithm algorithm) {
        this.context = context;
        this.algorithm = algorithm;
    }

    public void bindProcessSquare(View square, ProcessInfo process) {
        TextView name = square.findViewById(R.id.processName);
        TextView arrival = square.findViewById(R.id.processArrival);
        TextView burst = square.findViewById(R.id.processBurst);
        View colorBar = square.findViewById(R.id.processColorBar);
        TextView priority = square.findViewById(R.id.processPriority);

        name.setText(process.getName());
        arrival.setText(context.getString(R.string.process_arrival, process.getArrivalTime()));
        burst.setText(context.getString(R.string.process_burst, process.getBurstTime()));
        colorBar.setBackgroundColor(process.getColor());

        if (algorithm == SchedulingAlgorithm.PRIORITY) {
            priority.setVisibility(View.VISIBLE);
            priority.setText(context.getString(R.string.process_priority, process.getPriority()));
        } else {
            priority.setVisibility(View.GONE);
        }
    }

    public View createProcessSquare(ProcessInfo process, int index) {
        View square = LayoutInflater.from(context).inflate(R.layout.item_process_square, (ViewGroup) null, false);
        bindProcessSquare(square, process);
        square.setTag(R.id.tag_process_info, process);
        square.setTag(R.id.tag_pool_index, index);
        return square;
    }

    public void buildTargetSlots(LinearLayout slotsContainer, int totalSlots, boolean isRoundRobin,
                                  int[] correctStartTimes, int[] correctEndTimes) {
        slotsContainer.removeAllViews();
        for (int i = 0; i < totalSlots; i++) {
            View slotView = LayoutInflater.from(context).inflate(R.layout.item_target_slot, slotsContainer, false);
            TextView slotLabel = slotView.findViewById(R.id.slotLabel);
            if (isRoundRobin) {
                slotLabel.setText(context.getString(R.string.slot_time_range, correctStartTimes[i], correctEndTimes[i]));
            } else {
                slotLabel.setText(getOrdinal(i));
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            int margin = (int) (4 * context.getResources().getDisplayMetrics().density);
            lp.setMargins(margin, 0, margin, 0);
            slotView.setLayoutParams(lp);
            slotView.setTag(R.id.tag_slot_index, i);
            slotsContainer.addView(slotView);
        }
    }

    public void buildPoolSquares(LinearLayout poolContainer, ProcessSquareDragListener dragListener,
                                  List<ProcessInfo> processes) {
        poolContainer.removeAllViews();
        for (int i = 0; i < processes.size(); i++) {
            View square = createProcessSquare(processes.get(i), i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            int margin = (int) (4 * context.getResources().getDisplayMetrics().density);
            lp.setMargins(margin, 0, margin, 0);
            square.setLayoutParams(lp);
            dragListener.attachPoolDragListener(square, i);
            poolContainer.addView(square);
        }
    }

    public View createPlacedProcessSquare(ProcessInfo process, int slotIndex) {
        View square = LayoutInflater.from(context).inflate(R.layout.item_process_square, (ViewGroup) null, false);
        bindProcessSquare(square, process);
        square.setTag("placed_square");
        square.setTag(R.id.tag_process_info, process);
        square.setTag(R.id.tag_slot_index, slotIndex);
        return square;
    }

    private String getOrdinal(int index) {
        int n = index + 1;
        if (n == 1) return "1st";
        if (n == 2) return "2nd";
        if (n == 3) return "3rd";
        return n + "th";
    }

    public interface ProcessSquareDragListener {
        void attachPoolDragListener(View square, int index);
    }
}
