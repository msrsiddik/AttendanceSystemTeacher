package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class DashBoard extends Fragment {
    private ImageButton myBatch;
    private FragmentInterface fragmentInterface;

    public DashBoard() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dash_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        myBatch = view.findViewById(R.id.myBatch);

        fragmentInterface = (FragmentInterface) getActivity();
        myBatch.setOnClickListener(v -> {
            fragmentInterface.gotoMyBatch();
        });
    }
}