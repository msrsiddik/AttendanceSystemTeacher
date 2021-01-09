package msr.attend.teacher;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import msr.attend.teacher.Model.StudentModel;
import msr.attend.teacher.Model.UserPref;
import msr.attend.teacher.Model.Utils;

public class MyBatch extends Fragment {
    private UserPref userPref;
    private FloatingActionButton addStudentBtn;
    private FragmentInterface fragmentInterface;
    private Button allQrSaveBtn;
    private ListView studentListView;
    private List<StudentModel> studentModelList = null;

    public MyBatch() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_batch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        allQrSaveBtn = view.findViewById(R.id.allQrSaveBtn);
        studentListView = view.findViewById(R.id.myBatchStudentList);
        userPref = new UserPref(getContext());
        addStudentBtn = view.findViewById(R.id.addStudentBtn);

        getActivity().setTitle("My Batch");

        Bundle bundle = getArguments();
        String batch = bundle.getString("batch");
        loadStudentFromDb(batch);

        fragmentInterface = (FragmentInterface) getActivity();
        addStudentBtn.setOnClickListener(v -> fragmentInterface.addStudentForm());
        registerForContextMenu(studentListView);

        allQrSaveBtn.setOnClickListener(v -> {

            File pdfFolder = new File(Environment.getExternalStorageState(), "QRCode");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }

            Document document = new Document(PageSize.A4);
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(pdfFolder+"/"+batch+".pdf"));
                document.open();
                PdfPTable pdfPTable = new PdfPTable(2);
                for (StudentModel model : studentModelList){
                    BitMatrix bitMatrix=multiFormatWriter.encode(model.getId(), BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
                    final Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    pdfPTable.addCell(Image.getInstance(stream.toByteArray()));
                    pdfPTable.addCell(new Paragraph("Name : "+model.getName() +
                            "\n Roll : "+model.getRoll()+"\n Batch : "+model.getBatch()+"\n Depart : "+model.getDepartment()));
                }
                document.add(pdfPTable);
                document.close();
                Toast.makeText(getContext(), "Save to Document Folder", Toast.LENGTH_SHORT).show();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void loadStudentFromDb(String batch) {
        new FirebaseDatabaseHelper().getMyBatchStudent(batch, list -> {
            if (getActivity()!=null) {
                this.studentModelList = list;
                studentListView.setAdapter(new MyStudentAdapter(getContext(), list));
                studentListView.setOnItemClickListener((parent, view, position, id) -> {
                    Bundle bundle = new Bundle();
                    StudentModel model = list.get(position);
                    String s = Utils.getGsonParser().toJson(model);
                    bundle.putString("student", s);
                    StudentProfile profile = new StudentProfile();
                    profile.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.fragContainer, profile).addToBackStack(null).commit();
                });
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.myBatchStudentList){
            MenuInflater inflater = new MenuInflater(getContext());
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        CharSequence title = item.getTitle();
        AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if ("Edit".equals(title)) {
            StudentModel studentModel = studentModelList.get(menuinfo.position);
            AddStudent editStudent = new AddStudent();
            Bundle bundle = new Bundle();
            bundle.putString("id", studentModel.getId());
            bundle.putString("name", studentModel.getName());
            bundle.putString("depart", studentModel.getDepartment());
            bundle.putString("studentRoll", studentModel.getRoll());
            bundle.putString("studentId", studentModel.getStudentId());
            bundle.putString("batch", studentModel.getBatch());
            bundle.putString("stPhone", studentModel.getStudentPhone());
            bundle.putString("grPhone", studentModel.getGuardianPhone());
            editStudent.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragContainer, editStudent).addToBackStack(null).commit();

        } else {
            return false;
        }

        return true;
    }

    class MyStudentAdapter extends ArrayAdapter<StudentModel> {
        Context context;
        List<StudentModel> list = null;
        public MyStudentAdapter(@NonNull Context context, @NonNull List<StudentModel> objects) {
            super(context, R.layout.my_batch_student_row, objects);
            this.context = context;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.my_batch_student_row, parent,false);

            TextView name = view.findViewById(R.id.studentName);
            name.setText(list.get(position).getName());
            TextView roll = view.findViewById(R.id.studentRoll);
            roll.setText(list.get(position).getRoll());
            return view;
        }
    }
}