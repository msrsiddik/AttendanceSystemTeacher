package msr.attend.teacher.Model;

public class ClassAttendModel {
    private String stuId;
    private String subjectCode;
    private String date;
    private String present;
    private String teacherId;

    public ClassAttendModel() {
    }

    public ClassAttendModel(String stuId, String subjectCode, String date, String present, String teacherId) {
        this.stuId = stuId;
        this.subjectCode = subjectCode;
        this.date = date;
        this.present = present;
        this.teacherId = teacherId;
    }

    public String getStuId() {
        return stuId;
    }

    public void setStuId(String stuId) {
        this.stuId = stuId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "ClassAttendModel{" +
                "stuId='" + stuId + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", date='" + date + '\'' +
                ", present='" + present + '\'' +
                ", teacherId='" + teacherId + '\'' +
                '}';
    }
}
