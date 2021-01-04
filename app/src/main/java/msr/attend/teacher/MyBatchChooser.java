package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.UserPref;

public class MyBatchChooser extends Fragment {
    private TextView chooserMessage, chooserTitle;
    private Spinner batchSpinner;
    private Button viewBtn;
    private UserPref userPref;
    private List<String> batch = new ArrayList<>();
    public MyBatchChooser() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_batch_chooser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chooserMessage = view.findViewById(R.id.chooserMessage);
        chooserTitle = view.findViewById(R.id.chooserTitle);
        batchSpinner = view.findViewById(R.id.batchSpinner);
        viewBtn = view.findViewById(R.id.viewBtn);
        userPref = new UserPref(getContext());

        FragmentInterface fragmentInterface = (FragmentInterface) getActivity();

        new FirebaseDatabaseHelper().getCourseCoordinator(userPref.getTeacherId(), models -> {
                batch.clear();
            if (models.size() > 0) {
                batch.add("Select");
                for (CoordinatorModel coordinateBatch : models) {
                    batch.add(coordinateBatch.getBatch());
                }
                ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, batch.toArray());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                batchSpinner.setAdapter(adapter);
            } else {
                chooserTitle.setVisibility(View.GONE);
                batchSpinner.setVisibility(View.GONE);
                chooserMessage.setVisibility(View.VISIBLE);
                chooserMessage.setText("You are not Coordinator");
            }
        });

        viewBtn.setOnClickListener(v -> {
            fragmentInterface.gotoMyBatch(batchSpinner.getSelectedItem().toString());
        });

    }
}