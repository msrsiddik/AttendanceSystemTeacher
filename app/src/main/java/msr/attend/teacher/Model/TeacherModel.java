package msr.attend.teacher.Model;

public class TeacherModel {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String gender;
    private String password;

    public TeacherModel() {
    }

    public TeacherModel(String id, String name, String phone, String email, String department, String gender, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.gender = gender;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
