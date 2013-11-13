package com.grootcode.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.grootcode.android.R;

/**
 * This fragment shows a login progress spinner. Upon reaching a timeout of 7 seconds (in case
 * of a poor network connection), the user can try again.
 *
 * @author ankitd Sep13
 */
public class ProgressFragment extends Fragment {
    private static final int TRY_AGAIN_DELAY_MILLIS = 7 * 1000; // 7 seconds

    private final Handler mHandler = new Handler();

    public ProgressFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_loading_progress, container, false);

        final View takingAWhilePanel = rootView.findViewById(R.id.taking_a_while_panel);
        final View tryAgainButton = rootView.findViewById(R.id.retry_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }

                takingAWhilePanel.setVisibility(View.VISIBLE);
            }
        }, TRY_AGAIN_DELAY_MILLIS);

        return rootView;
    }
}