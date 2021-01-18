package msr.attend.teacher.Model;

public class ClassRepresentative {
    private String studentId;
    private String batch;

    public ClassRepresentative() {
    }

    public ClassRepresentative(String studentId, String batch) {
        this.studentId = studentId;
        this.batch = batch;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

}
