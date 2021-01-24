package msr.attend.teacher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import msr.attend.teacher.Model.ClassAttendModel;
import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;

public class MyBatchAttendaceDateByDate extends Fragment {
    private Button saveAttendancePdf;
    private Switch sortSwitch;
    private TextView viewByDate, viewByName;
    private ExpandableListView expanListView, expanListView2;
    private String batch;
    private String subCode;
    private FirebaseDatabaseHelper firebaseDatabaseHelper;
    private UserPref userPref;

    public MyBatchAttendaceDateByDate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_batch_attendace_date_by_date, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        saveAttendancePdf = view.findViewById(R.id.saveAttendancePdf);
        sortSwitch = view.findViewById(R.id.sortSwitch);
        viewByDate = view.findViewById(R.id.viewByDate);
        viewByName = view.findViewById(R.id.viewByName);
        expanListView = view.findViewById(R.id.expanListView);
        expanListView2 = view.findViewById(R.id.expanListView2);

        userPref = new UserPref(getContext());

        Bundle bundle = getArguments();
        batch = bundle.getString("batch");
        subCode = bundle.getString("subCode");

        firebaseDatabaseHelper = new FirebaseDatabaseHelper();

        firebaseDatabaseHelper.getAllAttendanceInfoByBatchAndSubjectCode(batch,subCode,attendList -> {

            saveAttendancePdf.setOnClickListener(v -> {
                try {
                    if (attendList.size() > 0) {
                        generatePdf(attendList);
                    } else {
                        Toast.makeText(getContext(), "Class Not Found, Save PDF failed!", Toast.LENGTH_SHORT).show();
                    }
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });

            if (getActivity() != null){

                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                List<String> date = new ArrayList<>();
                for (ClassAttendModel classAttend : attendList){
                    date.add(format.format(new Date(Long.parseLong(classAttend.getDate()))));
                }

                List<String> parent = new ArrayList<>(new HashSet<>(date));
                HashMap<String, List<String>> child = new HashMap<>();
                for (String p : parent){
                    List<String> list = new ArrayList<>();
                    for (ClassAttendModel attendModel : attendList){
                        if (format.format(new Date(Long.parseLong(attendModel.getDate()))).equals(p)){
                            list.add(attendModel.getStuId());
                        }
                    }
                    child.put(p,list);
                }

                firebaseDatabaseHelper.getMyBatchStudent(userPref.getDepartment(), batch, list -> {
                    expanListView.setAdapter(new ExpanListAdapter(getContext(), child,parent,list));
                });

                //------------------------+-+-+-----------------------
                HashSet<String> parent2 = new HashSet<>();
                for (ClassAttendModel attendModel : attendList) {
                    parent2.add(attendModel.getStuId());
                }

                HashMap<String, List<String>> child2 = new HashMap<>();
                for (String id : parent2) {
                    List<String> list = new ArrayList<>();
                    for (ClassAttendModel attendModel : attendList) {
                        if (attendModel.getPresent().equals("true") && attendModel.getStuId().equals(id)){
                            list.add(format.format(new Date(Long.parseLong(attendModel.getDate()))));
                        }
                    }
                    child2.put(id,list);
                }

                firebaseDatabaseHelper.getMyBatchStudent(userPref.getDepartment(), batch,list -> {
                    expanListView2.setAdapter(new ExpanListAdapter2(getContext(), child2, new ArrayList<>(parent2),list));
                });

            }
        });

        viewByName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        sortSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                viewByDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                viewByName.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
                expanListView.setVisibility(View.GONE);
                expanListView2.setVisibility(View.VISIBLE);
            } else {
                viewByName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                viewByDate.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
                expanListView2.setVisibility(View.GONE);
                expanListView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void generatePdf(List<ClassAttendModel> attendList) throws DocumentException, FileNotFoundException {
        Set<String> sRoll = new HashSet<>();
        for (ClassAttendModel c : attendList){
            sRoll.add(c.getRoll());
        }

        SortedSet<String> studentRoll = new TreeSet<>(sRoll);

        Map<String, List<ClassAttendModel>> map = new HashMap<>();
        for (String n : studentRoll){
            List<ClassAttendModel> list = new ArrayList<>();
            for (ClassAttendModel c : attendList){
                if (c.getRoll().equals(n)){
                    list.add(c);
                }
            }
            map.put(n,list);
        }

        final int[] a = {0};
        List<String> dates = new ArrayList<>();
        for (Map.Entry<String, List<ClassAttendModel>> entry : map.entrySet()) {
            String s = entry.getKey();
            List<ClassAttendModel> classAttendModels = entry.getValue();
            if (a[0] < classAttendModels.size()) {
                a[0] = classAttendModels.size();
                dates.clear();
                for (ClassAttendModel classAttendModel : classAttendModels) {
                    dates.add(classAttendModel.getDate());
                }
            }
        }

        File pdfFolder = new File(getContext().getExternalFilesDir(null), "Report");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
        }

        Document document = new Document(PageSize.A4.rotate(),0,0,40,0);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFolder+"/"+this.batch+"-"+this.subCode+"_attendance.pdf"));
        document.open();

        Paragraph universityName = new Paragraph(new Phrase(10,"Dhaka International University",
                new Font(Font.FontFamily.HELVETICA,25)));
        universityName.setFont(new Font(Font.FontFamily.TIMES_ROMAN,25,Font.BOLD));
        universityName.setKeepTogether(true);
        universityName.setAlignment(Element.ALIGN_CENTER);
        document.add(universityName);

        document.add(new Paragraph("\n"));

        Paragraph departName = new Paragraph(new Phrase(10,"Department of Computer Science and Engineering",
                new Font(Font.FontFamily.TIMES_ROMAN,16)));
        departName.setAlignment(Element.ALIGN_CENTER);
        document.add(departName);

        document.add(new Paragraph("\n"));

        PdfPTable infoTable = new PdfPTable(3);

        Paragraph courseTitle = new Paragraph("Course Title");
        courseTitle.setAlignment(Element.ALIGN_CENTER);
        courseTitle.setFont(new Font(Font.FontFamily.COURIER,18,Font.BOLD));
        infoTable.addCell(courseTitle);

        Paragraph courseCode = new Paragraph("Course Code");
        courseTitle.setAlignment(Element.ALIGN_CENTER);
        courseTitle.setFont(new Font(Font.FontFamily.COURIER,18,Font.BOLD));
        infoTable.addCell(courseCode);

        Paragraph batch = new Paragraph("Batch");
        courseTitle.setAlignment(Element.ALIGN_CENTER);
        courseTitle.setFont(new Font(Font.FontFamily.COURIER,18,Font.BOLD));
        infoTable.addCell(batch);

        infoTable.addCell("Subject Name Null");
        infoTable.addCell(this.subCode);
        infoTable.addCell(this.batch);

        document.add(infoTable);

        document.add(new Paragraph("\n"));

        Font f = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD, GrayColor.GRAYBLACK);
        PdfPCell headerTable = new PdfPCell(new Phrase("Attendance Report",f));
        headerTable.setBackgroundColor(GrayColor.LIGHT_GRAY);
        headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTable.setColspan(dates.size()+4);

        PdfPTable pdfPTable = new PdfPTable(dates.size()+4);
        pdfPTable.setWidthPercentage(97);
        pdfPTable.addCell(headerTable);
        pdfPTable.addCell("Date\nRoll");
        for (String d : dates) {
            PdfPCell cell = new PdfPCell(new Phrase(new SimpleDateFormat("dd.MM.yy").format(new Date(Long.parseLong(d)))));
            cell.setRotation(90);
            pdfPTable.addCell(cell);
        }

        PdfPCell present = new PdfPCell(new Phrase("Present"));
        present.setRotation(90);
        pdfPTable.addCell(present);
        PdfPCell absent = new PdfPCell(new Phrase("Absent"));
        absent.setRotation(90);
        pdfPTable.addCell(absent);
        PdfPCell total = new PdfPCell(new Phrase("Percent"));
        total.setRotation(90);
        pdfPTable.addCell(total);

        for (String roll : studentRoll){
            pdfPTable.addCell(roll);
            final int[] _p = {0};
            int _a = 0;
            for (int i = 0 ; i < dates.size(); i++){
                int finalI = i;
                final boolean[] print = {false};
                for (ClassAttendModel classAttendModel : attendList) {
                    if (classAttendModel.getDate().equals(dates.get(finalI)) && classAttendModel.getRoll().equals(roll) && classAttendModel.getPresent().equals("true")) {
                        pdfPTable.addCell("P");
                        ++_p[0];
                        print[0] = true;
                    }
                }
                if (!print[0]){
                    pdfPTable.addCell("A");
                    ++_a;
                }
            }
            pdfPTable.addCell(_p[0]+"");
            pdfPTable.addCell(_a+"");
            pdfPTable.addCell((_p[0]*100)/(_p[0]+_a)+"%");
        }

        document.add(pdfPTable);
        document.close();

        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();

    }

    class ExpanListAdapter extends BaseExpandableListAdapter {
        Context context;
        HashMap<String, List<String>> child;
        List<String> parent;
        List<StudentModel> students;

        public ExpanListAdapter(Context context, HashMap<String, List<String>> child, List<String> parent, List<StudentModel> students) {
            this.context = context;
            this.child = child;
            this.parent = parent;
            this.students = students;
        }

        @Override
        public int getGroupCount() {
            return parent.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return child.get(parent.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return parent.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return child.get(parent.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String title = (String) getGroup(groupPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_header_row,null);
            }
            TextView txt = convertView.findViewById(R.id.header);
            txt.setTypeface(null, Typeface.BOLD);
            txt.setText(title);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = (String) getChild(groupPosition,childPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_child_row,null);
            }
            TextView textView = convertView.findViewById(R.id.child);
            for (StudentModel student : students){
                if (student.getId().equals(child)) {
                    textView.setText(student.getRoll()+", "+student.getName());
                    textView.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        String s = Utils.getGsonParser().toJson(student);
                        bundle.putString("student", s);
                        bundle.putString("subCode", subCode);
                        bundle.putString("batch", batch);
                        bundle.putInt("totalClass",this.parent.size());
                        MyStudent myStudent = new MyStudent();
                        myStudent.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.fragContainer, myStudent).addToBackStack(null).commit();
                    });
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    class ExpanListAdapter2 extends BaseExpandableListAdapter {
        Context context;
        HashMap<String, List<String>> child;
        List<String> parent;
        List<StudentModel> students;
        int heightClass = 0;

        public ExpanListAdapter2(Context context, HashMap<String, List<String>> child, List<String> parent, List<StudentModel> students) {
            this.context = context;
            this.child = child;
            this.parent = parent;
            this.students = students;
            for (Map.Entry<String, List<String>> entry : child.entrySet()) {
                if (heightClass < entry.getValue().size()){
                    heightClass = entry.getValue().size();
                }
            }
        }

        @Override
        public int getGroupCount() {
            return parent.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return child.get(parent.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return parent.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return child.get(parent.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String title = (String) getGroup(groupPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_header_row,null);
            }
            TextView txt = convertView.findViewById(R.id.header);
            TextView classCountV = convertView.findViewById(R.id.classCountV);
            txt.setTypeface(null, Typeface.BOLD);
            for (StudentModel student : students) {
                if (student.getId().equals(title)){
                    txt.setText(student.getName());
                }
            }

            for (Map.Entry<String, List<String>> hashMap : child.entrySet()){
                if (hashMap.getKey().equals(title)){
                    classCountV.setText(hashMap.getValue().size()+" of "+heightClass);
                }
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = (String) getChild(groupPosition,childPosition);
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ex_child_row,null);
            }
            TextView textView = convertView.findViewById(R.id.child);
            textView.setText(child);
//            for (StudentModel student : students){
//                if (student.getId().equals(child)) {
//                    textView.setText(student.getRoll()+", "+student.getName());
//                    textView.setOnClickListener(v -> {
//                        Bundle bundle = new Bundle();
//                        String s = Utils.getGsonParser().toJson(student);
//                        bundle.putString("student", s);
//                        bundle.putString("subCode", subCode);
//                        bundle.putString("batch", batch);
//                        bundle.putInt("totalClass",this.parent.size());
//                        MyStudent myStudent = new MyStudent();
//                        myStudent.setArguments(bundle);
//                        getFragmentManager().beginTransaction().replace(R.id.fragContainer, myStudent).addToBackStack(null).commit();
//                    });
//                }
//            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


}