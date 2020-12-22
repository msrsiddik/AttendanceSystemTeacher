package msr.attend.teacher;

import msr.attend.teacher.Model.ClassModel;

public interface FragmentInterface {
    void login();
    void gotoDashBoard();
    void gotoMyBatch();
    void gotoMyClassAttend();
    void gotoAttendanceRegister(ClassModel classModel);
    void addStudentForm();

    void gotoMyNotification();
    void gotoNoticeSet();
    void gotoAttendViewByBatch(String batch);
}
