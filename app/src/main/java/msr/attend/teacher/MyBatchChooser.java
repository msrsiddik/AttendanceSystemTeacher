package msr.attend.teacher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import msr.attend.teacher.Model.CoordinatorModel;
import msr.attend.teacher.Model.UserPref;

public class MyBatchChooser extends Fragment {
    private TextView chooserMessage, chooserTitle;
    private Spinner batchSpinner;
    private Button viewBtn, resultBtn;
    private UserPref userPref;
    private List<String> batch = new ArrayList<>();
    private FragmentInterface fragmentInterface;

    private Spinner spinBatch, spinSemester, spinSubjectCode;

    private FirebaseDatabaseHelper firebaseDatabaseHelper;

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
        spinBatch = view.findViewById(R.id.spinBatch);
        spinSemester = view.findViewById(R.id.spinSemester);
        spinSubjectCode = view.findViewById(R.id.spinSubjectCode);
        resultBtn = view.findViewById(R.id.resultBtn);

        userPref = new UserPref(getContext());

        fragmentInterface = (FragmentInterface) getActivity();

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        firebaseDatabaseHelper.getCourseCoordinator(userPref.getTeacherId(), models -> {
            batch.clear();
            if (models.size() > 0) {
                batch.add("Select");
                for (CoordinatorModel coordinateBatch : models) {
                    batch.add(coordinateBatch.getBatch());
                }
                if (getActivity() != null) {

                    ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, batch.toArray());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    batchSpinner.setAdapter(adapter);
                    if (!userPref.isSuperUser()) {
                        spinBatch.setAdapter(adapter);
                    }
                }
            } else {
                chooserTitle.setVisibility(View.GONE);
                batchSpinner.setVisibility(View.GONE);
                viewBtn.setVisibility(view.GONE);
                if (!userPref.isSuperUser()) {
                    chooserMessage.setVisibility(View.VISIBLE);
                    chooserMessage.setText("You are not Coordinator");
                }
            }
        });

        if (userPref.isSuperUser()) {
            firebaseDatabaseHelper.getAllRunningBatch(userPref.getDepartment(), batchs -> {
                List<String> list = new ArrayList<>();
                for (String s : batchs) {
                    list.add(s);
                }
                list.add(0,"Select");
                ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, list);
                spinBatch.setAdapter(adapter);
            });
        }

        viewBtn.setOnClickListener(v -> {
            String selectBatch = batchSpinner.getSelectedItem().toString();
            if (!selectBatch.equals("Select")) {
                userPref.setMyBatch(selectBatch);
                fragmentInterface.gotoMyBatch(selectBatch);
            } else {
                Toast.makeText(getContext(), "Please Select Batch", Toast.LENGTH_SHORT).show();
            }
        });

        attendanceViewWorker();

        resultBtn.setOnClickListener(v -> {
            String selectBatch = spinBatch.getSelectedItem().toString();
            String selectSubject = spinSubjectCode.getSelectedItem().toString();
            if (!selectBatch.equals("Select") && !selectSubject.equals("Select")) {
                fragmentInterface.gotoMyBatchAttendanceDateByDate(selectBatch, selectSubject);
            } else {
                Toast.makeText(getContext(), "All Select Item Required", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void attendanceViewWorker() {

        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(getContext(), R.array.semester, android.R.layout.simple_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSemester.setAdapter(semesterAdapter);

        int[] subCodeBySemesterCSE = {R.array.subCodeFirstItem, R.array.first_bsc_cse, R.array.second_bsc_cse, R.array.third_bsc_cse, R.array.fourth_bsc_cse,
                R.array.fifth_bsc_cse, R.array.sixth_bsc_cse, R.array.seventh_bsc_cse, R.array.eighth_bsc_cse, R.array.ninth_bsc_cse,
                R.array.tenth_bsc_cse, R.array.eleventh_bsc_cse, R.array.twelfth_bsc_cse};

        int[] subCodeBySemesterEEE = {R.array.subCodeFirstItem, R.array.first_bsc_eee, R.array.second_bsc_eee, R.array.third_bsc_eee, R.array.fourth_bsc_eee,
                R.array.fifth_bsc_eee, R.array.sixth_bsc_eee, R.array.seventh_bsc_eee, R.array.eighth_bsc_eee, R.array.ninth_bsc_eee,
                R.array.tenth_bsc_eee, R.array.eleventh_bsc_eee, R.array.twelfth_bsc_eee};

        int[] subCodeBySemesterEETE = {R.array.subCodeFirstItem, R.array.first_bsc_eete, R.array.second_bsc_eete, R.array.third_bsc_eete, R.array.fourth_bsc_eete,
                R.array.fifth_bsc_eete, R.array.sixth_bsc_eete, R.array.seventh_bsc_eete, R.array.eighth_bsc_eete, R.array.ninth_bsc_eete,
                R.array.tenth_bsc_eete, R.array.eleventh_bsc_eete, R.array.twelfth_bsc_eete};

        int[] subCodeBySemesterEnglish = {R.array.subCodeFirstItem, R.array.first_hons_english, R.array.second_hons_english, R.array.third_hons_english, R.array.fourth_hons_english,
                R.array.fifth_hons_english, R.array.sixth_hons_english, R.array.seventh_hons_english, R.array.eighth_hons_english, R.array.ninth_hons_english,
                R.array.tenth_hons_english, R.array.eleventh_hons_english, R.array.twelfth_hons_english};

        int[] subCodeBySemesterLaw = {R.array.subCodeFirstItem, R.array.first_hons_llb, R.array.second_hons_llb, R.array.third_hons_llb, R.array.fourth_hons_llb,
                R.array.fifth_hons_llb, R.array.sixth_hons_llb, R.array.seventh_hons_llb, R.array.eighth_hons_llb, R.array.ninth_hons_llb,
                R.array.tenth_hons_llb, R.array.eleventh_hons_llb, R.array.twelfth_hons_llb};

        int[] subCodeBySemesterSociology = {R.array.subCodeFirstItem, R.array.first_bss_hons_sociology, R.array.second_bss_hons_sociology, R.array.third_bss_hons_sociology, R.array.fourth_bss_hons_sociology,
                R.array.fifth_bss_hons_sociology, R.array.sixth_bss_hons_sociology, R.array.seventh_bss_hons_sociology, R.array.eighth_bss_hons_sociology, R.array.ninth_bss_hons_sociology,
                R.array.tenth_bss_hons_sociology, R.array.eleventh_bss_hons_sociology, R.array.twelfth_bss_hons_sociology};

        spinSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<CharSequence> subCodeAdapterCSE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterCSE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEEE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEEE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEETE = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEETE[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterEnglish = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterEnglish[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterLaw = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterLaw[position], android.R.layout.simple_spinner_item);
                ArrayAdapter<CharSequence> subCodeAdapterSociology = ArrayAdapter.createFromResource(getContext(), subCodeBySemesterSociology[position], android.R.layout.simple_spinner_item);

                subCodeAdapterCSE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEEE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEETE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterEnglish.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterLaw.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCodeAdapterSociology.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Log.e("position", position + "");

                if (!userPref.getDepartment().equals(null)) {
                    switch (userPref.getDepartment()) {
                        case "CSE":
                            spinSubjectCode.setAdapter(subCodeAdapterCSE);
                            break;
                        case "EEE":
                            spinSubjectCode.setAdapter(subCodeAdapterEEE);
                            break;
                        case "EETE":
                            spinSubjectCode.setAdapter(subCodeAdapterEETE);
                            break;
                        case "English":
                            spinSubjectCode.setAdapter(subCodeAdapterEnglish);
                            break;
                        case "Law":
                            spinSubjectCode.setAdapter(subCodeAdapterLaw);
                            break;
                        case "Sociology":
                            spinSubjectCode.setAdapter(subCodeAdapterSociology);
                            break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}