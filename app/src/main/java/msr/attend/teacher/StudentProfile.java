package msr.attend.teacher;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.Utils;

public class StudentProfile extends Fragment {
    private TextView studentName, studentBatch, studentDepart, studentNo, guardianNo;
    private GridView classAttendGridView;
    private List<ClassAttendModel> attendList;
    Map<String, Integer> subCodeBySem;

    public StudentProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        studentName = view.findViewById(R.id.studentName);
        studentBatch = view.findViewById(R.id.studentBatch);
        studentDepart = view.findViewById(R.id.studentDepart);
        studentNo = view.findViewById(R.id.studentNo);
        guardianNo = view.findViewById(R.id.guardianNo);
        classAttendGridView = view.findViewById(R.id.classAttendGridView);

        Bundle bundle = getArguments();
        StudentModel model = Utils.getGsonParser().fromJson(bundle.getString("student"), StudentModel.class);
        studentName.setText(model.getName());
        studentBatch.setText(model.getBatch());
        studentDepart.setText(model.getDepartment());
        studentNo.setText(model.getStudentPhone());
        guardianNo.setText(model.getGuardianPhone());

        showQRDialog(model);

        subCodeBySem = new HashMap<>();
        int[] subCodeBySemester = {R.array.first_bsc_cse, R.array.second_bsc_cse, R.array.third_bsc_cse, R.array.fourth_bsc_cse,
                R.array.fifth_bsc_cse, R.array.sixth_bsc_cse, R.array.seventh_bsc_cse, R.array.eighth_bsc_cse, R.array.ninth_bsc_cse,
                R.array.tenth_bsc_cse, R.array.eleventh_bsc_cse, R.array.twelfth_bsc_cse};

        new FirebaseDatabaseHelper().getAllAttendanceInfoByStudentId(model.getId(), attendList -> {
//            for (ClassAttendModel attendModel : attendList) {
//                Log.e(new SimpleDateFormat("dd-MM-yyyy").format(Long.valueOf(attendModel.getDate())), attendModel.getPresent());
//            }
            if (getActivity() != null) {
                this.attendList = attendList;
                classAttendGridView.setAdapter(new AttendAdapter(getContext(), attendList, subCodeBySem, subCodeBySemester));
            }

        });



        classAttendGridView.setOnItemClickListener((parent, view1, position, id) -> {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.class_attend_details);

            LinearLayout layout = dialog.findViewById(R.id.classInfoDetails);

            for (Map.Entry<String, Integer> entry : subCodeBySem.entrySet()){
                if (entry.getValue().equals(position+1)){
                    TextView subjectCode = new TextView(getContext());
                    subjectCode.setText(entry.getKey());
                    layout.addView(subjectCode);
                    for (ClassAttendModel attendModel : attendList){
                        if (entry.getKey().equals(attendModel.getSubjectCode()) && attendModel.getPresent().equals("true")){
                            TextView textView = new TextView(getContext());
                            DateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                            textView.setText("    |- "+format.format(new Date(Long.parseLong(attendModel.getDate())))+" ");
                            layout.addView(textView);
                        }
                    }
                    TextView gap = new TextView(getContext());
                    gap.setText("\n");
                    layout.addView(gap);
                }
            }

            dialog.show();
        });

    }

    class AttendAdapter extends BaseAdapter {
        Context context;
        List<ClassAttendModel> list = null;
        Map<String, Integer> subCodeBySem = null;
        int subCodeBySemesterLength = 0;
//        Map<String, Integer> subCodeBySem = new HashMap<>();
//        int[] subCodeBySemester = {R.array.first_bsc_cse, R.array.second_bsc_cse, R.array.third_bsc_cse, R.array.fourth_bsc_cse,
//                R.array.fifth_bsc_cse, R.array.sixth_bsc_cse, R.array.seventh_bsc_cse, R.array.eighth_bsc_cse, R.array.ninth_bsc_cse,
//                R.array.tenth_bsc_cse, R.array.eleventh_bsc_cse, R.array.twelfth_bsc_cse};

        public AttendAdapter(Context context, List<ClassAttendModel> list, Map<String, Integer> subCodeBySem, int[] subCodeBySemester) {
            this.context = context;
            this.list = list;
            this.subCodeBySem = subCodeBySem;
            this.subCodeBySemesterLength = subCodeBySemester.length;
            for (int i = 0; i < subCodeBySemester.length; i++) {
                String[] subCode = getResources().getStringArray(subCodeBySemester[i]);
                insertSubCodeBySem(subCodeBySem, subCode, i + 1);
            }
            Log.e("print ", subCodeBySem.toString());
        }

        @Override
        public int getCount() {
            return subCodeBySemesterLength;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView semesterTitle = new TextView(context);
            semesterTitle.setText((position+1) +" Semester");
            semesterTitle.setTextSize(20);
            semesterTitle.setGravity(Gravity.CENTER);
            layout.addView(semesterTitle);

            for (Map.Entry<String, Integer> m : subCodeBySem.entrySet()){
                if (m.getValue().equals(position+1)){
                    TextView view = new TextView(context);
                    int countClass = 0;
                    for (ClassAttendModel attendModel : list){
                        if (m.getKey().equals(attendModel.getSubjectCode()) && attendModel.getPresent().equals("true")){
                            countClass++;
                        }
                    }
                    view.setText(m.getKey()+" -> "+countClass);
                    layout.addView(view);
                }
            }

            return layout;
        }

        private void insertSubCodeBySem(Map<String, Integer> map, String[] subCode, int semesterNum) {
            for (int i = 0; i < subCode.length; i++) {
                map.put(subCode[i], semesterNum);
            }
        }
    }

    void showQRDialog(final StudentModel model){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.qr_code);

        TextView close = dialog.findViewById(R.id.close);
        final ImageView barcode = dialog.findViewById(R.id.imageView);
        Button save = dialog.findViewById(R.id.save);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(model.getId(), BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            final Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
            barcode.setImageBitmap(bitmap);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, model.getRoll()+"_"+
                            model.getBatch()+"_"+model.getDepartment(), null);

                    dialog.cancel();
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }

        dialog.show();
    }

}