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
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import msr.attend.teacher.Model.Utils;

public class IndivitualPdfGenerate extends Fragment {
    private Spinner spinSemester, spinSubjectCode;
    private Button savePdf;

    public IndivitualPdfGenerate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_indivitual_pdf_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        spinSemester = view.findViewById(R.id.spinSemester);
        spinSubjectCode = view.findViewById(R.id.spinSubjectCode);
        savePdf = view.findViewById(R.id.savePdf);

        Bundle bundle = getArguments();
        StudentModel model = Utils.getGsonParser().fromJson(bundle.getString("student"), StudentModel.class);

        getActivity().setTitle(model.getName());

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

                if (!model.getDepartment().equals(null)) {
                    switch (model.getDepartment()) {
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
        final String[] subCode = {null};
        List<ClassAttendModel> atList = new ArrayList<>();
        spinSubjectCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!spinSubjectCode.getSelectedItem().toString().equals("Select Subject")) {
                    subCode[0] = spinSubjectCode.getSelectedItem().toString();
                    Toast.makeText(getContext(), ""+spinSubjectCode.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        savePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!subCode[0].isEmpty()) {
                    new FirebaseDatabaseHelper().getAllAttendanceInfoByBatchAndSubjectCode(model.getBatch(), subCode[0], new FireMan.AttendDataShort() {
                        @Override
                        public void classAttendListener(List<ClassAttendModel> attendList) {
                            try {
                                generatePdf(attendList, model, subCode[0]);
                            } catch (DocumentException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void generatePdf(List<ClassAttendModel> attendList, StudentModel student, String subCode) throws DocumentException, FileNotFoundException {
        Set<String> sRoll = new HashSet<>();
        for (ClassAttendModel c : attendList){
            sRoll.add(c.getRoll());
        }

        SortedSet<String> stdntRoll = new TreeSet<>(sRoll);

        Map<String, List<ClassAttendModel>> map = new HashMap<>();
        for (String n : stdntRoll){
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
        PdfWriter.getInstance(document, new FileOutputStream(pdfFolder+"/"+student.getBatch()+"_"+student.getName()+"_"+student.getRoll()+"_"+subCode+"_attendance.pdf"));
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
        infoTable.addCell(subCode);
        infoTable.addCell(student.getBatch());

        document.add(infoTable);

        document.add(new Paragraph("\n"));

        PdfPTable nameRollTable = new PdfPTable(2);

        Paragraph stName = new Paragraph("Name");
        stName.setAlignment(Element.ALIGN_CENTER);
        stName.setFont(new Font(Font.FontFamily.COURIER,18,Font.BOLD));
        nameRollTable.addCell(stName);

        Paragraph stRoll = new Paragraph("Roll");
        stRoll.setAlignment(Element.ALIGN_CENTER);
        stRoll.setFont(new Font(Font.FontFamily.COURIER,18,Font.BOLD));
        nameRollTable.addCell(stRoll);

        nameRollTable.addCell(student.getName());
        nameRollTable.addCell(student.getRoll());

        document.add(nameRollTable);

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

        for (String roll : stdntRoll){
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

}